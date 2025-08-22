package com.example.backend.controller;

import com.example.backend.constant.ApiConstants;
import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.ScheduleSlotDto;
import com.example.backend.dto.DoctorScheduleCreateRequest;
import com.example.backend.dto.DoctorScheduleUpdateRequest;
import com.example.backend.service.DoctorScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.DOCTORS_ENDPOINT)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Doctor Schedule", description = "Manage doctor's working schedules (appointment slots)")
public class DoctorScheduleController {

    private final DoctorScheduleService scheduleService;
    private final MessageSource messageSource;

    @PostMapping("/{doctorId}/schedules")
    @Operation(summary = "Create working schedule", description = "Create one or many appointment slots for a doctor. "
            + "If intervalMinutes is provided, the time range will be split into multiple slots.")
    public ApiResponse<List<ScheduleSlotDto>> createSchedule(
            @PathVariable("doctorId") Long doctorId,
            @Valid @RequestBody DoctorScheduleCreateRequest request) {

        log.info("Create schedule doctorId={}, req={}", doctorId, request);
        List<ScheduleSlotDto> created = scheduleService.createSlots(doctorId, request);
        String message = messageSource.getMessage("success.schedule.created", null,
                LocaleContextHolder.getLocale());
        return ApiResponse.success(created, message);
    }

    @PutMapping("/{doctorId}/schedules/{slotId}")
    @Operation(summary = "Update a working slot", description = "Update start/end time of a slot. Only allows when the slot is AVAILABLE.")
    public ApiResponse<ScheduleSlotDto> updateSlot(@PathVariable("doctorId") Long doctorId,
            @PathVariable("slotId") Long slotId,
            @Valid @RequestBody DoctorScheduleUpdateRequest request) {

        log.info("Update schedule doctorId={}, slotId={}, req={}", doctorId, slotId, request);
        ScheduleSlotDto dto = scheduleService.updateSlot(doctorId, slotId, request);
        String message = messageSource.getMessage("success.schedule.updated", null,
                LocaleContextHolder.getLocale());
        return ApiResponse.success(dto, message);
    }
}
