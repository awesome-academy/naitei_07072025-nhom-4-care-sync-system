package com.example.backend.dto;

import java.time.LocalDateTime;

public record ReviewResponse(Long id, Long appointmentId, Integer overallRating,
        Integer expertiseRating, Integer communicationRating, Integer punctualityRating,
        Integer careRating, Boolean isRecommended, String comment, Boolean isAnonymous,
        LocalDateTime createdAt) {
}
