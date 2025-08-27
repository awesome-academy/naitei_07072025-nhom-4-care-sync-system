package com.example.backend.controller.doctor;

import com.example.backend.constant.ApiConstants;
import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.DoctorTimeOffRequestDto;
import com.example.backend.service.DoctorTimeOffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.DOCTOR_TIME_OFF_ENDPOINT)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Doctor Time Off", description = "Doctor time off management APIs")
public class DoctorTimeOffController {

    private final DoctorTimeOffService doctorTimeOffService;

    @PostMapping
    @Operation(summary = "Create doctor time off", description = "Create a new time off request for a doctor")
    public ApiResponse<String> createTimeOff(@Valid @RequestBody DoctorTimeOffRequestDto request) {
        log.info("Received time off creation request for doctor: {}", request.doctorId());
        return doctorTimeOffService.createTimeOff(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update doctor time off", description = "Update an existing time off for a doctor")
    public ApiResponse<String> updateTimeOff(@PathVariable Long id,
            @Valid @RequestBody DoctorTimeOffRequestDto request) {
        log.info("Received time off update request for id: {}", id);
        return doctorTimeOffService.updateTimeOff(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete doctor time off", description = "Delete an existing time off for a doctor")
    public ApiResponse<String> deleteTimeOff(@PathVariable Long id) {
        log.info("Received time off deletion request for id: {}", id);
        return doctorTimeOffService.deleteTimeOff(id);
    }
}
