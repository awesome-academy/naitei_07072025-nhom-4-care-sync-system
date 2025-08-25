package com.example.backend.service;

import com.example.backend.dto.ScheduleSlotDto;
import com.example.backend.dto.DoctorScheduleCreateRequest;
import com.example.backend.dto.DoctorScheduleUpdateRequest;

import com.example.backend.dto.ScheduleTemplateDto;
import com.example.backend.dto.ScheduleTemplateUpsertRequest;
import java.util.List;

public interface DoctorScheduleService {
    List<ScheduleSlotDto> createSlots(Long doctorId, DoctorScheduleCreateRequest request);
    ScheduleSlotDto updateSlot(Long doctorId, Long slotId, DoctorScheduleUpdateRequest request);

    List<ScheduleTemplateDto> listMyTemplates();

    ScheduleTemplateDto createTemplate(ScheduleTemplateUpsertRequest req);

    ScheduleTemplateDto updateTemplate(Long templateId, ScheduleTemplateUpsertRequest req);

    void deleteTemplate(Long templateId);
}
