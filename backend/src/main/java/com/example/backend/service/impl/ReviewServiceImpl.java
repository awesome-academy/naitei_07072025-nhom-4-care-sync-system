package com.example.backend.service.impl;

import com.example.backend.constant.enums.AppointmentStatus;
import com.example.backend.dto.ReviewCreateRequest;
import com.example.backend.dto.ReviewResponse;
import com.example.backend.entity.Appointment;
import com.example.backend.entity.Patient;
import com.example.backend.entity.Review;
import com.example.backend.entity.User;
import com.example.backend.exception.BusinessException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.exception.ReviewAlreadyExistsException;
import com.example.backend.mapper.ReviewMapper;
import com.example.backend.repository.AppointmentRepository;
import com.example.backend.repository.PatientRepository;
import com.example.backend.repository.ReviewRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.ReviewService;
import com.example.backend.util.SecurityUtils;
import com.example.backend.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;
    private final MessageSource messageSource;

    @Override
    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request) {
        log.info("Creating review for appointment: {}", request.appointmentId());

        // Get current authenticated user
        String email = SecurityUtils.getCurrentUserEmailOrThrow();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("error.user.not.found"));

        if (currentUser.getPatient() == null) {
            throw new BusinessException(messageSource.getMessage("error.patient.not.found", null,
                    LocaleContextHolder.getLocale()));
        }

        Patient patient = currentUser.getPatient();

        // Find appointment and validate
        Appointment appointment = appointmentRepository.findById(request.appointmentId())
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage(
                        "error.appointment.not.found", new Object[]{request.appointmentId()},
                        LocaleContextHolder.getLocale())));

        // Validate appointment belongs to current patient
        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new BusinessException(messageSource.getMessage(
                    "error.review.own.appointment.only", null, LocaleContextHolder.getLocale()));
        }

        // Validate appointment status is COMPLETED
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new BusinessException(
                    messageSource.getMessage("error.review.completed.appointment.only", null,
                            LocaleContextHolder.getLocale()));
        }

        // Check if review already exists for this appointment
        if (reviewRepository.existsByAppointmentId(request.appointmentId())) {
            throw new ReviewAlreadyExistsException();
        }

        // Validate comment length if provided
        if (request.comment() != null && request.comment().trim().length() < 10) {
            throw new BusinessException(messageSource.getMessage("error.review.comment.too.short",
                    null, LocaleContextHolder.getLocale()));
        }

        // Create review entity
        Review review = reviewMapper.toEntity(request);
        review.setAppointment(appointment);

        // Save review
        Review savedReview = reviewRepository.save(review);

        log.info("Review created successfully with ID: {}", savedReview.getId());

        return reviewMapper.toResponse(savedReview);
    }
}
