package com.example.backend.service.impl;

import com.example.backend.dto.ScheduleTemplateDto;
import com.example.backend.dto.ScheduleTemplateUpsertRequest;
import com.example.backend.constant.enums.AppointmentSlotStatus;
import com.example.backend.dto.DoctorScheduleCreateRequest;
import com.example.backend.dto.DoctorScheduleUpdateRequest;
import com.example.backend.dto.ScheduleSlotDto;
import com.example.backend.entity.AppointmentSlot;
import com.example.backend.entity.Doctor;
import com.example.backend.entity.DoctorWorkingHours;
import com.example.backend.exception.BusinessException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.mapper.DoctorScheduleMapper;
import com.example.backend.repository.AppointmentSlotRepository;
import com.example.backend.repository.DoctorRepository;
import com.example.backend.repository.DoctorWorkingHoursRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.DoctorScheduleService;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DoctorScheduleServiceImpl implements DoctorScheduleService {

    private final DoctorRepository doctorRepository;
    private final DoctorWorkingHoursRepository workingHoursRepository;
    private final UserRepository userRepository;
    private final AppointmentSlotRepository slotRepository;

    public DoctorScheduleServiceImpl(DoctorRepository doctorRepository,
            DoctorWorkingHoursRepository workingHoursRepository, UserRepository userRepository) {
        this.doctorRepository = doctorRepository;
        this.workingHoursRepository = workingHoursRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<ScheduleTemplateDto> listMyTemplates() {
        Doctor doctor = getCurrentDoctorOrThrow();
        return workingHoursRepository.findByDoctorIdOrderByDayOfWeekAscStartTimeAsc(doctor.getId())
                .stream().map(DoctorScheduleMapper::toDto).toList();
    }
    @PersistenceContext
    private EntityManager em;

    @Override
    public List<ScheduleSlotDto> createSlots(Long doctorId, DoctorScheduleCreateRequest req) {
        log.info("Create schedule doctorId={}, start={}, end={}, interval={}", doctorId,
                req.startTime(), req.endTime(), req.intervalMinutes());

        validateWindow(req.startTime(), req.endTime());

        // Tìm bác sĩ
        Doctor doctor = doctorRepository.findById(doctorId).orElseThrow(
                () -> new ResourceNotFoundException("error.doctor.not.found", doctorId));

        // Tính interval (phút). Null => tạo 1 slot duy nhất bằng cả khoảng.
        int interval = (req.intervalMinutes() == null)
                ? Math.toIntExact(Duration.between(req.startTime(), req.endTime()).toMinutes())
                : req.intervalMinutes();
    @Transactional
    public ScheduleTemplateDto createTemplate(ScheduleTemplateUpsertRequest req) {
        validateTime(req.startTime(), req.endTime());

        if (interval <= 0) {
            throw new BusinessException("error.request.invalid",
                    "intervalMinutes must be positive");
        }
        Doctor doctor = getCurrentDoctorOrThrow();

        List<AppointmentSlot> toSave = new ArrayList<>();
        LocalDateTime cur = req.startTime();
        boolean overlap = workingHoursRepository.existsOverlap(doctor.getId(),
                req.dayOfWeek().getValue(), req.startTime(), req.endTime());
        if (overlap)
            throw new BusinessException("error.workinghour.overlap");

        // Chia slot liên tiếp [cur, next), slot cuối có thể ngắn hơn nếu không chia
        // hết.
        while (cur.isBefore(req.endTime())) {
            LocalDateTime next = cur.plusMinutes(interval);
            if (next.isAfter(req.endTime()))
                next = req.endTime();
        DoctorWorkingHours e = new DoctorWorkingHours();
        e.setDoctor(doctor);
        e.setDayOfWeek(req.dayOfWeek().getValue());
        e.setStartTime(req.startTime());
        e.setEndTime(req.endTime());

            // Kiểm tra overlap với slot khác của cùng bác sĩ
            if (existsOverlap(doctorId, cur, next)) {
                throw new BusinessException("error.appointment.slot.unavailable",
                        "Overlaps with existing slot");
            }
        DoctorWorkingHours saved = workingHoursRepository.save(e);
        // TODO: rebuild AVAILABLE slots (nếu bạn muốn)

            AppointmentSlot s = new AppointmentSlot();
            s.setDoctor(doctor);
            s.setStartTime(cur);
            s.setEndTime(next);
            s.setStatus(AppointmentSlotStatus.AVAILABLE);
            toSave.add(s);

            cur = next;
        }

        List<AppointmentSlot> saved = slotRepository.saveAll(toSave);
        return saved.stream().map(this::toDto).toList();
        return DoctorScheduleMapper.toDto(saved);
    }

    @Override
    public ScheduleSlotDto updateSlot(Long doctorId, Long slotId, DoctorScheduleUpdateRequest req) {
        log.info("Update schedule doctorId={}, slotId={}, start={}, end={}", doctorId, slotId,
                req.startTime(), req.endTime());
    @Transactional
    public ScheduleTemplateDto updateTemplate(Long templateId, ScheduleTemplateUpsertRequest req) {
        validateTime(req.startTime(), req.endTime());

        Doctor doctor = getCurrentDoctorOrThrow();

        DoctorWorkingHours e = workingHoursRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("workinghour.not_found"));

        if (!e.getDoctor().getId().equals(doctor.getId()))
            throw new AccessDeniedException("Not owner of this template");

        boolean overlap = workingHoursRepository.existsOverlapExcludingId(doctor.getId(),
                req.dayOfWeek().getValue(), req.startTime(), req.endTime(), e.getId());
        if (overlap)
            throw new BusinessException("error.workinghour.overlap");

        boolean shrink = isShrinking(e.getStartTime(), e.getEndTime(), req.startTime(),
                req.endTime());

        e.setDayOfWeek(req.dayOfWeek().getValue());
        e.setStartTime(req.startTime());
        e.setEndTime(req.endTime());

        DoctorWorkingHours saved = workingHoursRepository.save(e);

        if (shrink) {
            // TODO: đóng/xoá slot AVAILABLE bị ảnh hưởng; nếu đụng BOOKED → throw
            // BusinessException("error.workinghour.affects.booked")
        }

        return DoctorScheduleMapper.toDto(saved);
    }

        validateWindow(req.startTime(), req.endTime());
    @Override
    @Transactional
    public void deleteTemplate(Long templateId) {
        Doctor doctor = getCurrentDoctorOrThrow();

        // Lấy slot thuộc về đúng doctor
        AppointmentSlot slot = slotRepository.findByIdAndDoctorId(slotId, doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("error.slot.not.found", slotId));
        DoctorWorkingHours e = workingHoursRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("workinghour.not_found"));

        // Chỉ cho sửa khi slot còn AVAILABLE
        if (slot.getStatus() != AppointmentSlotStatus.AVAILABLE) {
            throw new BusinessException("error.appointment.slot.unavailable",
                    "Cannot edit a non-AVAILABLE slot");
        }
        if (!e.getDoctor().getId().equals(doctor.getId()))
            throw new AccessDeniedException("Not owner of this template");

        // TODO: kiểm tra slot BOOKED tương lai; nếu có thì chặn hoặc set inactive
        workingHoursRepository.delete(e);
    }

        // Nếu thời gian không thay đổi, bỏ qua kiểm tra overlap
        boolean unchanged = req.startTime().equals(slot.getStartTime())
                && req.endTime().equals(slot.getEndTime());

        if (!unchanged
                && existsOverlapExcludingSelf(doctorId, slotId, req.startTime(), req.endTime())) {
            throw new BusinessException("error.appointment.slot.unavailable",
                    "Updated time overlaps with other slots");
        }
    /* ===== helpers ===== */

        slot.setStartTime(req.startTime());
        slot.setEndTime(req.endTime());
        AppointmentSlot saved = slotRepository.save(slot);
        return toDto(saved);
    }
    private Doctor getCurrentDoctorOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null)
            throw new AccessDeniedException("Unauthenticated");

    // ===================== helpers =====================
        // Giả định username trong Authentication là email/username của User (theo cấu
        // trúc dự án của bạn).
        var user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("User not found"));

    private void validateWindow(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new BusinessException("error.request.invalid", "startTime/endTime is null");
        }
        if (!end.isAfter(start)) {
            throw new BusinessException("error.request.invalid", "endTime must be after startTime");
        }
        return doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AccessDeniedException("Current user is not a doctor"));
    }

    /** Kiểm tra có slot nào (cùng doctor) giao nhau [start, end) hay không */
    private boolean existsOverlap(Long doctorId, LocalDateTime start, LocalDateTime end) {
        return slotRepository.existsOverlap(doctorId, start, end);
    private void validateTime(LocalTime start, LocalTime end) {
        if (start == null || end == null || !start.isBefore(end))
            throw new BusinessException("error.workinghour.invalid");
    }

    /**
     * Kiểm tra overlap nhưng loại trừ chính slot đang update. Dùng JPQL trực tiếp
     * để không cần thay đổi repository.
     */
    private boolean existsOverlapExcludingSelf(Long doctorId, Long slotId, LocalDateTime start,
            LocalDateTime end) {
        String jpql = """
                select count(s) from AppointmentSlot s
                where s.doctor.id = :doctorId
                  and s.id <> :slotId
                  and s.startTime < :end
                  and s.endTime   > :start
                """;
        TypedQuery<Long> q = em.createQuery(jpql, Long.class).setParameter("doctorId", doctorId)
                .setParameter("slotId", slotId).setParameter("start", start)
                .setParameter("end", end);
        Long cnt = q.getSingleResult();
        return cnt != null && cnt > 0;
    }

    private ScheduleSlotDto toDto(AppointmentSlot s) {
        return new ScheduleSlotDto(s.getId(), s.getStartTime(), s.getEndTime());
    private boolean isShrinking(LocalTime oldStart, LocalTime oldEnd, LocalTime newStart,
            LocalTime newEnd) {
        return newStart.isAfter(oldStart) || newEnd.isBefore(oldEnd);
    }
}
