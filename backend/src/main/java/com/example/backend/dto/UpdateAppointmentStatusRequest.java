package com.example.backend.dto;

import com.example.backend.constant.enums.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateAppointmentStatusRequest(
        @NotNull(message = "New status cannot be null") AppointmentStatus newStatus) {
}
