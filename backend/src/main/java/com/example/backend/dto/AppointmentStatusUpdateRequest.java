package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record AppointmentStatusUpdateRequest(
        @NotBlank(message = "Trạng thái không được để trống") String status) {
}
