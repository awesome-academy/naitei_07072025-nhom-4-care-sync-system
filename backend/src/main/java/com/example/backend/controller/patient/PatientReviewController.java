package com.example.backend.controller.patient;

import com.example.backend.constant.ApiConstants;
import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.ReviewCreateRequest;
import com.example.backend.dto.ReviewResponse;
import com.example.backend.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.PATIENT_REVIEWS_ENDPOINT)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Patient Reviews", description = "Patient review management APIs")
public class PatientReviewController {

    private final ReviewService reviewService;
    private final MessageSource messageSource;

    @PostMapping
    @Operation(summary = "Create a review for a completed appointment")
    public ApiResponse<ReviewResponse> createReview(
            @Valid @RequestBody ReviewCreateRequest request) {

        log.info("Request to create review for appointment: {}", request.appointmentId());

        ReviewResponse response = reviewService.createReview(request);

        String message = messageSource.getMessage("success.review.created", null,
                LocaleContextHolder.getLocale());

        return ApiResponse.success(response, message);
    }
}
