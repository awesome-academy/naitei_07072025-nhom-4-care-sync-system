package com.example.backend.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(name = "DoctorScheduleCreateRequest")
public record DoctorScheduleCreateRequest(@NotNull @FutureOrPresent LocalDateTime startTime,

        @NotNull @Future LocalDateTime endTime,

        @Min(5) Integer intervalMinutes) {
}
