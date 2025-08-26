package com.example.backend.service.impl;

import com.example.backend.constant.enums.AppointmentStatus;
import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.entity.ids.AppointmentServiceId;
import com.example.backend.event.AppointmentConfirmedEvent;
import com.example.backend.event.AppointmentCreatedEvent;
import com.example.backend.event.AppointmentRejectedEvent;
import com.example.backend.event.DoctorNewAppointmentRequestEvent;
import com.example.backend.exception.BusinessException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.exception.UnauthorizedException;
import com.example.backend.mapper.AppointmentMapper;
import com.example.backend.repository.*;
import com.example.backend.service.AppointmentService;
import com.example.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentSlotRepository appointmentSlotRepository;
    private final AppointmentServiceRepository appointmentServiceRepository;
    private final ServiceRepository serviceRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final AppointmentMapper appointmentMapper;
    private final MessageSource messageSource;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public AppointmentCreateResponse create(AppointmentCreateRequest request) {
        log.info("Creating appointment for patient {} with slot {}", request.patientId(),
                request.slotId());

        AppointmentSlot slot = appointmentSlotRepository.findById(request.slotId()).orElseThrow(
                () -> new ResourceNotFoundException("error.slot.not.found", request.slotId()));

        if (slot.getStatus() != com.example.backend.constant.enums.AppointmentSlotStatus.AVAILABLE) {
            throw new BusinessException("error.appointment.slot.unavailable");
        }
        if (slot.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("error.appointment.slot.unavailable");
        }

        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("error.patient.not.found",
                        request.patientId()));

        List<com.example.backend.entity.Service> services = serviceRepository
                .findAllById(request.serviceIds());
        if (services.size() != request.serviceIds().size()) {
            throw new ResourceNotFoundException("error.service.not.found");
        }

        Long slotSpecialtyId = slot.getDoctor().getSpecialty() != null
                ? slot.getDoctor().getSpecialty().getId().longValue()
                : null;
        boolean allMatch = services.stream().allMatch(svc -> svc.getSpecialty() != null
                && Objects.equals(svc.getSpecialty().getId().longValue(), slotSpecialtyId));
        if (!allMatch) {
            throw new BusinessException("error.service.specialty.mismatch");
        }

        Appointment appointmentToSave = new Appointment();
        appointmentToSave.setPatient(patient);
        appointmentToSave.setNotes(request.notes());
        appointmentToSave.setStatus(com.example.backend.constant.enums.AppointmentStatus.PENDING);
        final Appointment savedAppt = appointmentRepository.save(appointmentToSave);

        int updated = appointmentSlotRepository.reserveSlot(slot.getId(), savedAppt.getId());
        if (updated == 0) {
            throw new BusinessException("error.appointment.slot.unavailable");
        }

        List<com.example.backend.entity.AppointmentService> appointmentServices = services.stream()
                .map(svc -> {
                    var as = new com.example.backend.entity.AppointmentService();
                    as.setId(new AppointmentServiceId(savedAppt.getId(), svc.getId()));
                    as.setAppointment(savedAppt);
                    as.setService(svc);
                    as.setPriceAtBooking(svc.getPrice());
                    return as;
                }).toList();
        appointmentServiceRepository.saveAll(appointmentServices);

        BigDecimal total = appointmentServices.stream()
                .map(com.example.backend.entity.AppointmentService::getPriceAtBooking)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        publishAppointmentCreatedEvents(savedAppt, patient, slot, services, total, request.notes());

        return AppointmentMapper.buildFromServices(savedAppt, patient, slot, services, total,
                request.notes());
    }

    @Override
    @Transactional
    public AppointmentCreateResponse confirm(Long appointmentId) {
        log.info("Confirm appointment: {}", appointmentId);
        var appt = appointmentRepository.findById(appointmentId).orElseThrow(
                () -> new ResourceNotFoundException("error.appointment.not.found", appointmentId));

        if (appt.getStatus() != AppointmentStatus.PENDING) {
            throw new BusinessException("error.appointment.invalid.state", appt.getStatus().name());
        }

        var slot = appointmentSlotRepository.findByAppointmentId(appointmentId);
        if (slot == null) {
            throw new BusinessException("error.appointment.slot.missing");
        }
        if (slot.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("error.appointment.cannot.confirm.after.start");
        }

        appt.setStatus(AppointmentStatus.CONFIRMED);
        var saved = appointmentRepository.save(appt);

        publishAppointmentConfirmedEvent(saved, slot);

        var apptServices = appointmentServiceRepository.findByAppointmentId(saved.getId());
        return AppointmentMapper.buildFromAppointmentServices(saved, slot, apptServices);
    }

    @Override
    @Transactional
    public AppointmentCreateResponse reject(Long appointmentId, AppointmentRejectRequest request) {
        log.info("Reject appointment: {}, reason: {}", appointmentId, request.reason());
        var appt = appointmentRepository.findById(appointmentId).orElseThrow(
                () -> new ResourceNotFoundException("error.appointment.not.found", appointmentId));

        if (appt.getStatus() != AppointmentStatus.PENDING
                && appt.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BusinessException("error.appointment.invalid.state", appt.getStatus().name());
        }

        var slot = appointmentSlotRepository.findByAppointmentId(appointmentId);
        if (slot == null) {
            throw new BusinessException("error.appointment.slot.missing");
        }

        if (slot.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("error.appointment.cannot.reject.after.start");
        }

        appt.setStatus(AppointmentStatus.REJECTED);
        var saved = appointmentRepository.save(appt);

        appointmentSlotRepository.freeSlotByAppointmentId(saved.getId());

        publishAppointmentRejectedEvent(saved, slot, request.reason());

        var apptServices = appointmentServiceRepository.findByAppointmentId(saved.getId());
        return AppointmentMapper.buildFromAppointmentServices(saved, slot, apptServices);
    }

    @Override
    public Page<AppointmentSummaryResponse> getMyAppointments(AppointmentFilterRequest filters,
            Pageable pageable) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail).orElseThrow(() -> {
            String message = messageSource.getMessage("error.unauthorized", null, new Locale("vi"));
            return new RuntimeException(message);
        });

        Specification<Appointment> spec = isPatient(currentUser);

        if (StringUtils.hasText(filters.getStatus())) {
            spec = spec.and(hasStatus(filters.getStatus()));
        }
        if (filters.getStartDate() != null) {
            spec = spec.and(isAfter(filters.getStartDate()));
        }
        if (filters.getEndDate() != null) {
            spec = spec.and(isBefore(filters.getEndDate()));
        }
        if (filters.getDoctorId() != null) {
            spec = spec.and(hasDoctor(filters.getDoctorId()));
        }

        Page<Appointment> appointments = appointmentRepository.findAll(spec, pageable);

        return appointments.map(appointmentMapper::toAppointmentSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AppointmentSummaryDto> listDoctorAppointments(
            AppointmentListRequest request) {
        String email = SecurityUtils.getCurrentUserEmailOrThrow();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("error.user.not.found"));
        if (currentUser.getDoctor() == null) {
            throw new AccessDeniedException("error.access.denied");
        }
        Long doctorId = currentUser.getDoctor().getId();

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<Appointment> page = appointmentRepository.findByDoctorWithFilters(doctorId,
                request.getStatus(), request.getStartFrom(), request.getStartTo(), pageable);

        List<AppointmentSummaryDto> summaries = page.getContent().stream().map(a -> {
            AppointmentSlot s = a.getAppointmentSlot();
            Patient p = a.getPatient();
            User patientUser = p != null ? p.getUser() : null;
            return new AppointmentSummaryDto(a.getId(), s != null ? s.getStartTime() : null,
                    s != null ? s.getEndTime() : null,
                    a.getStatus() != null ? a.getStatus().name() : null,
                    p != null ? p.getId() : null,
                    patientUser != null ? patientUser.getFullName() : null);
        }).toList();

        Page<AppointmentSummaryDto> dtoPage = new PageImpl<>(summaries, pageable,
                page.getTotalElements());
        return PageResponse.of(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentDetailResponse getAppointmentDetails(Long id) {
        log.info("Fetching details for appointment id: {}", id);

        Appointment appointment = appointmentRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("error.appointment.not.found", id));

        return buildAppointmentDetailResponse(appointment);
    }

    @Override
    @Transactional
    public AppointmentCancelResponse cancelByPatient(Long appointmentId, Boolean confirmPolicy) {
        if (confirmPolicy == null || !confirmPolicy) {
            throw new BusinessException("error.policy.not.confirmed");
        }

        String email = SecurityUtils.getCurrentUserEmailOrThrow();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("error.user.not.found"));
        if (currentUser.getPatient() == null || currentUser.getPatient().getId() == null) {
            throw new AccessDeniedException("error.access.denied");
        }
        Long currentUserId = currentUser.getPatient().getId();

        var appt = appointmentRepository.findByIdWithSlotForUpdate(appointmentId).orElseThrow(
                () -> new ResourceNotFoundException("error.appointment.not.found", appointmentId));

        if (appt.getPatient() == null || appt.getPatient().getId() == null
                || !Objects.equals(appt.getPatient().getId(), currentUserId)) {
            throw new AccessDeniedException("error.access.denied");
        }

        String oldStatus = appt.getStatus() != null ? appt.getStatus().name() : null;

        if (appt.getStatus() == null) {
            throw new BusinessException("error.appointment.status.invalid");
        }
        switch (appt.getStatus()) {
            case PENDING :
                break;
            case CONFIRMED :
                throw new BusinessException("error.appointment.cancel.doctor.confirmed");
            default :
                throw new BusinessException("error.appointment.status.invalid",
                        appt.getStatus().name());
        }

        var slot = appt.getAppointmentSlot();
        if (slot == null) {
            throw new BusinessException("error.appointment.slot.missing");
        }
        if (slot.getStartTime() != null && !LocalDateTime.now().isBefore(slot.getStartTime())) {
            throw new BusinessException("error.appointment.cannot.cancel.started");
        }

        appt.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appt);
        boolean slotReleased = appointmentSlotRepository.freeSlotByAppointmentId(appointmentId) > 0;

        String policyMsg = messageSource.getMessage("policy.appointment.cancel", null,
                LocaleContextHolder.getLocale());

        return new AppointmentCancelResponse(appt.getId(), oldStatus, appt.getStatus().name(),
                slotReleased, policyMsg, false);
    }

    @Override
    @Transactional
    public AppointmentDetailResponse updateStatusByDoctor(Long appointmentId,
            UpdateAppointmentStatusRequest request) {
        log.info("Doctor updating status for appointment {}: to {}", appointmentId,
                request.newStatus());

        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow(
                () -> new ResourceNotFoundException("error.appointment.not.found", appointmentId));

        String currentUserEmail = SecurityUtils.getCurrentUserEmailOrThrow();
        Long doctorUserIdOfAppointment = appointment.getAppointmentSlot().getDoctor().getUser()
                .getId();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UnauthorizedException("error.user.not.found"));

        if (!currentUser.getId().equals(doctorUserIdOfAppointment)) {
            throw new AccessDeniedException("error.access.denied");
        }

        validateStatusTransition(appointment.getStatus(), request.newStatus());

        appointment.setStatus(request.newStatus());
        appointmentRepository.save(appointment);

        log.info("Successfully updated status for appointment {}", appointmentId);

        return buildAppointmentDetailResponse(appointment);
    }

    private void validateStatusTransition(AppointmentStatus current, AppointmentStatus next) {
        if (current != AppointmentStatus.CONFIRMED) {
            throw new BusinessException("error.appointment.invalid.state.for.update");
        }

        Set<AppointmentStatus> allowedNextStates = Set.of(AppointmentStatus.COMPLETED,
                AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW);

        if (!allowedNextStates.contains(next)) {
            throw new BusinessException("error.appointment.invalid.next.state");
        }
    }

    private AppointmentDetailResponse buildAppointmentDetailResponse(Appointment appointment) {
        AppointmentSlot slot = appointment.getAppointmentSlot();
        Doctor doctor = slot.getDoctor();
        User doctorUser = doctor.getUser();
        Specialty specialty = doctor.getSpecialty();
        Patient patient = appointment.getPatient();
        User patientUser = patient.getUser();
        List<com.example.backend.entity.AppointmentService> apptServices = appointmentServiceRepository
                .findByAppointmentId(appointment.getId());

        BigDecimal totalPrice = apptServices.stream()
                .map(com.example.backend.entity.AppointmentService::getPriceAtBooking)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<String> availableActions = new ArrayList<>();
        if (appointment.getStatus() == AppointmentStatus.PENDING
                || appointment.getStatus() == AppointmentStatus.CONFIRMED) {
            availableActions.add("CANCEL");
        }

        return AppointmentDetailResponse.builder().id(appointment.getId())
                .status(appointment.getStatus().name()).appointmentTime(slot.getStartTime())
                .endTime(slot.getEndTime()).notes(appointment.getNotes()).totalPrice(totalPrice)
                .doctor(AppointmentDetailResponse.DoctorInfo.builder().id(doctor.getId())
                        .fullName(doctorUser.getFullName()).specialtyName(specialty.getName())
                        .build())
                .patient(AppointmentDetailResponse.PatientInfo.builder().id(patient.getId())
                        .fullName(patientUser.getFullName())
                        .phoneNumber(patientUser.getPhoneNumber()).build())
                .services(apptServices.stream()
                        .map(as -> AppointmentDetailResponse.ServiceItem.builder()
                                .id(as.getService().getId()).name(as.getService().getName())
                                .price(as.getPriceAtBooking()).build())
                        .toList())
                .availableActions(availableActions).history(List.of()).build();
    }

    private Specification<Appointment> isPatient(User user) {
        return (root, query, cb) -> cb.equal(root.get("patient").get("user"), user);
    }

    private Specification<Appointment> hasStatus(String status) {
        return (root, query, cb) -> cb.equal(root.get("status"),
                AppointmentStatus.valueOf(status.trim().toUpperCase()));
    }

    private Specification<Appointment> isAfter(LocalDate startDate) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(
                root.get("appointmentSlot").get("startTime").as(LocalDate.class), startDate);
    }

    private Specification<Appointment> isBefore(LocalDate endDate) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(
                root.get("appointmentSlot").get("startTime").as(LocalDate.class), endDate);
    }

    private Specification<Appointment> hasDoctor(Long doctorId) {
        return (root, query, cb) -> cb.equal(root.get("appointmentSlot").get("doctor").get("id"),
                doctorId);
    }

    private void publishAppointmentCreatedEvents(Appointment appointment, Patient patient,
            AppointmentSlot slot, List<com.example.backend.entity.Service> services,
            BigDecimal totalPrice, String notes) {

        User patientUser = patient.getUser();
        Doctor doctor = slot.getDoctor();
        User doctorUser = doctor.getUser();

        AppointmentCreatedEvent patientEvent = new AppointmentCreatedEvent(appointment.getId(),
                patient.getId(), patientUser.getEmail(), patientUser.getFullName(),
                slot.getStartTime(), slot.getEndTime(), doctor.getId(), doctorUser.getFullName(),
                doctor.getSpecialty() != null ? doctor.getSpecialty().getName() : null, totalPrice);
        eventPublisher.publishEvent(patientEvent);

        List<String> serviceNames = services.stream()
                .map(com.example.backend.entity.Service::getName).collect(Collectors.toList());

        DoctorNewAppointmentRequestEvent doctorEvent = new DoctorNewAppointmentRequestEvent(
                appointment.getId(), doctor.getId(), doctorUser.getEmail(),
                doctorUser.getFullName(),
                doctor.getSpecialty() != null ? doctor.getSpecialty().getName() : null,
                patient.getId(), patientUser.getFullName(), patientUser.getEmail(),
                patientUser.getPhoneNumber(), serviceNames, totalPrice, notes,
                appointment.getCreatedAt(), slot.getStartTime(), slot.getEndTime());
        eventPublisher.publishEvent(doctorEvent);

        log.info("Published appointment created events for appointment: {}", appointment.getId());
    }

    private void publishAppointmentConfirmedEvent(Appointment appointment, AppointmentSlot slot) {
        User patientUser = appointment.getPatient().getUser();
        Doctor doctor = slot.getDoctor();
        User doctorUser = doctor.getUser();

        AppointmentConfirmedEvent event = new AppointmentConfirmedEvent(appointment.getId(),
                appointment.getPatient().getId(), patientUser.getEmail(), patientUser.getFullName(),
                slot.getStartTime(), slot.getEndTime(), doctor.getId(), doctorUser.getFullName());
        eventPublisher.publishEvent(event);

        log.info("Published appointment confirmed event for appointment: {}", appointment.getId());
    }

    private void publishAppointmentRejectedEvent(Appointment appointment, AppointmentSlot slot,
            String reason) {
        User patientUser = appointment.getPatient().getUser();
        Doctor doctor = slot.getDoctor();
        User doctorUser = doctor.getUser();

        AppointmentRejectedEvent event = new AppointmentRejectedEvent(appointment.getId(),
                appointment.getPatient().getId(), patientUser.getEmail(), patientUser.getFullName(),
                slot.getStartTime(), slot.getEndTime(), doctor.getId(), doctorUser.getFullName(),
                reason);
        eventPublisher.publishEvent(event);

        log.info("Published appointment rejected event for appointment: {}", appointment.getId());
    }
}
