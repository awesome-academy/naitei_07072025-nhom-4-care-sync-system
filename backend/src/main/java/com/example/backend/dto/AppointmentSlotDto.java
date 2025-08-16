package com.example.backend.dto;

import com.example.backend.constant.enums.AppointmentSlotStatus;
import java.time.LocalDateTime;

public record AppointmentSlotDto(
        Long id,
        LocalDateTime startTime,
        LocalDateTime endTime,
        AppointmentSlotStatus status
) {}