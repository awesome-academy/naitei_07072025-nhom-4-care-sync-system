package com.example.backend.controller;

import com.example.backend.constant.ApiConstants;
import com.example.backend.dto.DoctorSlotsDto;
import com.example.backend.service.AppointmentSlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(ApiConstants.DOCTORS_ENDPOINT)
@Tag(name = "Doctors", description = "Doctor-related APIs")
@RequiredArgsConstructor
@Validated
public class DoctorSlotController {

    private final AppointmentSlotService appointmentSlotService;
    private final MessageSource messageSource;

    @GetMapping(ApiConstants.AVAILABLE_SLOTS)
    @Operation(summary = "Get available appointment slots by date", description = "Return the list of AVAILABLE slots of the doctor on the specified date")
    public ResponseEntity<List<DoctorSlotsDto>> getAvailableSlotsByServices(
            @RequestParam @NotNull(message = "{validation.date.required") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,

            @RequestParam("services") @NotEmpty(message = "{validation.services.required}") List<Long> serviceIds) {
        var result = appointmentSlotService.getAvailableSlotsByServices(date, serviceIds);
        return ResponseEntity.ok(result);
    }
}
