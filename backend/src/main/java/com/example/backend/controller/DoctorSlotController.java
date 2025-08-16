package com.example.backend.controller;

import com.example.backend.constant.ApiConstants;
import com.example.backend.constant.DateTimeConstants;
import com.example.backend.constant.MessageConstants;
import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.AppointmentSlotDto;
import com.example.backend.service.AppointmentSlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(ApiConstants.DOCTORS_ENDPOINT)
@Tag(name = "Doctors", description = "Doctor-related APIs")
@RequiredArgsConstructor
public class DoctorSlotController {

    private final AppointmentSlotService appointmentSlotService;

    @GetMapping("/{doctorId}/slots")
    @Operation(summary = "Get available appointment slots by date",
            description = "Return the list of AVAILABLE slots of the doctor on the specified date")
    public ApiResponse<List<AppointmentSlotDto>> getAvailableSlots(
            @PathVariable Long doctorId,
            @RequestParam("date")
            @DateTimeFormat(pattern = DateTimeConstants.DATE_FORMAT) LocalDate date
    ) {
        List<AppointmentSlotDto> data = appointmentSlotService.getAvailableSlots(doctorId, date);
        return ApiResponse.success(data, MessageConstants.SUCCESS_MESSAGE);
    }
}
