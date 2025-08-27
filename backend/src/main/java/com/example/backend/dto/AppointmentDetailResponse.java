package com.example.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppointmentDetailResponse {

    private Long id;
    private String status;
    private LocalDateTime appointmentTime;
    private LocalDateTime endTime;
    private String notes;
    private BigDecimal totalPrice;

    private DoctorInfo doctor;
    private PatientInfo patient;
    private List<ServiceItem> services;
    private List<String> availableActions; // Ví dụ: ["CANCEL", "RESCHEDULE"]
    private List<HistoryLog> history;

    @Data
    @Builder
    public static class DoctorInfo {
        private Long id;
        private String fullName;
        private String specialtyName;
    }

    @Data
    @Builder
    public static class PatientInfo {
        private Long id;
        private String fullName;
        private String phoneNumber;
    }

    @Data
    @Builder
    public static class ServiceItem {
        private Long id;
        private String name;
        private BigDecimal price;
    }

    @Data
    @Builder
    public static class HistoryLog {
        private LocalDateTime timestamp;
        private String action;
        private String details;
    }
}
