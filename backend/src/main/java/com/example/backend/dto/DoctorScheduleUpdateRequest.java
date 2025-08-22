package com.example.backend.dto;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(name = "DoctorScheduleUpdateRequest")
public record DoctorScheduleUpdateRequest(@NotNull @FutureOrPresent LocalDateTime startTime,

        @NotNull @Future LocalDateTime endTime) {
}
