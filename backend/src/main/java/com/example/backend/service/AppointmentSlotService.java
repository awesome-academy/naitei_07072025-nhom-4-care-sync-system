package com.example.backend.service;

import com.example.backend.dto.AppointmentSlotDto;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentSlotService {
    List<AppointmentSlotDto> getAvailableSlots(Long doctorId, LocalDate date);
}