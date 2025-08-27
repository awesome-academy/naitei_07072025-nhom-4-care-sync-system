package com.example.backend.service.impl;

import com.example.backend.dto.AppointmentSlotDto;
import com.example.backend.dto.DoctorSlotsDto;
import com.example.backend.entity.Doctor;
import com.example.backend.constant.enums.AppointmentSlotStatus;
import com.example.backend.repository.AppointmentSlotRepository;
import com.example.backend.repository.DoctorRepository;
import com.example.backend.repository.ServiceRepository;
import com.example.backend.service.AppointmentSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentSlotServiceImpl implements AppointmentSlotService {

    private final AppointmentSlotRepository appointmentSlotRepository;
    private final DoctorRepository doctorRepository;
    private final ServiceRepository serviceRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DoctorSlotsDto> getAvailableSlotsByServices(LocalDate date, List<Long> serviceIds) {
        // 0) Validate input
        if (date == null || serviceIds == null || serviceIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 1) Map serviceIds -> specialtyIds (distinct) về Long để khớp repository
        Set<Long> specialtyIds = serviceRepository.findAllById(serviceIds).stream()
                .map(s -> s.getSpecialty()) // Service -> Specialty
                .filter(Objects::nonNull).map(sp -> sp.getId()).map(Number::longValue)
                .collect(Collectors.toSet());

        if (specialtyIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 2) Lấy bác sĩ theo specialty
        List<Doctor> doctors = doctorRepository.findBySpecialtyIdIn(specialtyIds);

        if (doctors.isEmpty())
            return Collections.emptyList();

        // 3) Khoảng thời gian của 1 ngày
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        // 4) Cho từng bác sĩ -> lấy slot AVAILABLE
        List<DoctorSlotsDto> result = new ArrayList<>();
        for (Doctor d : doctors) {
            var slots = appointmentSlotRepository.findByDoctorIdAndStartTimeBetweenAndStatus(
                    d.getId(), startOfDay, endOfDay, AppointmentSlotStatus.AVAILABLE);

            if (slots.isEmpty())
                continue;

            var slotDtos = slots.stream()
                    .map(s -> new AppointmentSlotDto(s.getId(), s.getStartTime(), s.getEndTime()))
                    .toList();

            result.add(new DoctorSlotsDto(d.getId(), d.getUser().getFullName(), slotDtos));
        }
        return result;
    }
}
