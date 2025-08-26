package com.example.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record AppointmentDetailResponse(Long id, String status, LocalDateTime appointmentTime,
        LocalDateTime endTime, String notes, BigDecimal totalPrice, DoctorInfo doctor,
        PatientInfo patient, List<ServiceItem> services, List<String> availableActions,
        List<Object> history) {
    @Builder
    public record DoctorInfo(Long id, String fullName, String specialtyName) {
    }

    @Builder
    public record PatientInfo(Long id, String fullName, String phoneNumber) {
    }

    @Builder
    public record ServiceItem(Long id, String name, BigDecimal price) {
    }
}
