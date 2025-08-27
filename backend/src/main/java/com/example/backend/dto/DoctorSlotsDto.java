package com.example.backend.dto;

import java.util.List;

public record DoctorSlotsDto(Long doctorId, String name, List<AppointmentSlotDto> slots) {
    public DoctorSlotsDto {
        slots = (slots == null) ? List.of() : List.copyOf(slots);
    }
}
