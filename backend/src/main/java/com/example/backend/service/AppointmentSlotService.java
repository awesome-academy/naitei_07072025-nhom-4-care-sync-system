package com.example.backend.service;

import com.example.backend.dto.DoctorSlotsDto;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentSlotService {
    List<DoctorSlotsDto> getAvailableSlotsByServices(LocalDate date, List<Long> serviceIds);
}
