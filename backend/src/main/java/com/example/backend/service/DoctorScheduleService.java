package com.example.backend.service;

import com.example.backend.dto.ScheduleSlotDto;
import com.example.backend.dto.DoctorScheduleCreateRequest;
import com.example.backend.dto.DoctorScheduleUpdateRequest;

import java.util.List;

public interface DoctorScheduleService {
    List<ScheduleSlotDto> createSlots(Long doctorId, DoctorScheduleCreateRequest request);
    ScheduleSlotDto updateSlot(Long doctorId, Long slotId, DoctorScheduleUpdateRequest request);
}
