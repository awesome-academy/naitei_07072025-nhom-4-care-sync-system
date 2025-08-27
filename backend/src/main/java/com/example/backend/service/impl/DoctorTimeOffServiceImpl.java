package com.example.backend.service.impl;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.DoctorTimeOffRequestDto;
import com.example.backend.entity.Doctor;
import com.example.backend.entity.DoctorTimeOff;
import com.example.backend.repository.DoctorRepository;
import com.example.backend.repository.DoctorTimeOffRepository;
import com.example.backend.service.DoctorTimeOffService;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.exception.BusinessException;
import com.example.backend.util.TimeOffValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DoctorTimeOffServiceImpl implements DoctorTimeOffService {

    private final DoctorTimeOffRepository doctorTimeOffRepository;
    private final DoctorRepository doctorRepository;
    private final TimeOffValidator timeOffValidator;
    private final MessageSource messageSource;

    @Override
    public ApiResponse<String> createTimeOff(DoctorTimeOffRequestDto request) {
        log.info("Creating time off for doctor: {}", request.doctorId());

        // Parse và validate datetime
        LocalDateTime startDateTime = timeOffValidator.parseDateTime(request.startDatetime());
        LocalDateTime endDateTime = timeOffValidator.parseDateTime(request.endDatetime());

        // Validation business logic
        timeOffValidator.validateTimeOffRequest(startDateTime, endDateTime);

        // Kiểm tra doctor có tồn tại không
        Doctor doctor = doctorRepository.findById(request.doctorId()).orElseThrow(
                () -> new ResourceNotFoundException("error.user.not.found", request.doctorId()));

        // Kiểm tra xung đột thời gian
        if (doctorTimeOffRepository.existsOverlappingTimeOff(request.doctorId(), startDateTime,
                endDateTime)) {
            throw new BusinessException("error.time.off.conflict");
        }

        // Tạo DoctorTimeOff
        DoctorTimeOff timeOff = new DoctorTimeOff();
        timeOff.setDoctor(doctor);
        timeOff.setStartDatetime(startDateTime);
        timeOff.setEndDatetime(endDateTime);
        timeOff.setReason(request.reason());

        doctorTimeOffRepository.save(timeOff);

        log.info("Time off created successfully for doctor: {}", request.doctorId());
        String message = messageSource.getMessage("success.doctor.time.off.created", null,
                LocaleContextHolder.getLocale());
        return ApiResponse.success(null, message);
    }

    @Override
    public ApiResponse<String> updateTimeOff(Long id, DoctorTimeOffRequestDto request) {
        log.info("Updating time off for id: {}", id);

        DoctorTimeOff timeOff = doctorTimeOffRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("error.doctor.time.off.not.found", id));

        LocalDateTime startDateTime = timeOffValidator.parseDateTime(request.startDatetime());
        LocalDateTime endDateTime = timeOffValidator.parseDateTime(request.endDatetime());

        timeOffValidator.validateTimeOffRequest(startDateTime, endDateTime);

        Doctor doctor = doctorRepository.findById(request.doctorId()).orElseThrow(
                () -> new ResourceNotFoundException("error.user.not.found", request.doctorId()));

        if (doctorTimeOffRepository.existsOverlappingTimeOffExcludingId(request.doctorId(),
                startDateTime, endDateTime, id)) {
            throw new BusinessException("error.time.off.conflict");
        }

        timeOff.setDoctor(doctor);
        timeOff.setStartDatetime(startDateTime);
        timeOff.setEndDatetime(endDateTime);
        timeOff.setReason(request.reason());

        doctorTimeOffRepository.save(timeOff);

        log.info("Time off updated successfully for id: {}", id);
        String message = messageSource.getMessage("success.operation", null,
                LocaleContextHolder.getLocale());
        return ApiResponse.success(null, message);
    }

    @Override
    public ApiResponse<String> deleteTimeOff(Long id) {
        log.info("Deleting time off for id: {}", id);

        DoctorTimeOff timeOff = doctorTimeOffRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("error.doctor.time.off.not.found", id));

        if (timeOff.getStartDatetime().isBefore(LocalDateTime.now())) {
            String errorMessage = messageSource.getMessage("error.timeoff.already.passed", null, // Không
                                                                                                 // có
                                                                                                 // tham
                                                                                                 // số
                    LocaleContextHolder.getLocale());
            throw new BusinessException(errorMessage);
        }

        doctorTimeOffRepository.delete(timeOff);

        log.info("Time off deleted successfully for id: {}", id);
        String message = messageSource.getMessage("success.operation", null,
                LocaleContextHolder.getLocale());
        return ApiResponse.success(null, message);
    }
}
