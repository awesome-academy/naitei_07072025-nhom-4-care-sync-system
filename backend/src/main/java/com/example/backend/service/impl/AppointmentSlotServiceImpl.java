package com.example.backend.service.impl;

import com.example.backend.constant.enums.AppointmentSlotStatus;
import com.example.backend.dto.AppointmentSlotDto;
import com.example.backend.entity.AppointmentSlot;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.AppointmentSlotRepository;
import com.example.backend.repository.DoctorRepository;
import com.example.backend.service.AppointmentSlotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentSlotServiceImpl implements AppointmentSlotService {

    private final AppointmentSlotRepository slotRepository;
    private final DoctorRepository doctorRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentSlotDto> getAvailableSlots(Long doctorId, LocalDate date) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor", "id", doctorId);
        }

        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime nextDayStart = date.plusDays(1).atStartOfDay();

        List<AppointmentSlot> slots =
                slotRepository.findByDoctorIdAndStartTimeBetweenAndStatusAndAppointmentIsNull(
                        doctorId, dayStart, nextDayStart, AppointmentSlotStatus.AVAILABLE
                );

        return slots.stream()
                .map(s -> new AppointmentSlotDto(
                        s.getId(),
                        s.getStartTime(),
                        s.getEndTime(),
                        s.getStatus())
                )
                .toList();
    }
}