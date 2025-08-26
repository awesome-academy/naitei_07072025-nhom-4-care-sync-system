package com.example.backend.dto;

import jakarta.validation.constraints.*;

public record ReviewCreateRequest(
        @NotNull(message = "Appointment ID is required") Long appointmentId,

        @NotNull(message = "Overall rating is required") @Min(value = 1, message = "Overall rating must be between 1 and 5") @Max(value = 5, message = "Overall rating must be between 1 and 5") Integer overallRating,

        @NotNull(message = "Expertise rating is required") @Min(value = 1, message = "Expertise rating must be between 1 and 5") @Max(value = 5, message = "Expertise rating must be between 1 and 5") Integer expertiseRating,

        @NotNull(message = "Communication rating is required") @Min(value = 1, message = "Communication rating must be between 1 and 5") @Max(value = 5, message = "Communication rating must be between 1 and 5") Integer communicationRating,

        @NotNull(message = "Punctuality rating is required") @Min(value = 1, message = "Punctuality rating must be between 1 and 5") @Max(value = 5, message = "Punctuality rating must be between 1 and 5") Integer punctualityRating,

        @NotNull(message = "Care rating is required") @Min(value = 1, message = "Care rating must be between 1 and 5") @Max(value = 5, message = "Care rating must be between 1 and 5") Integer careRating,

        @NotNull(message = "Recommendation is required") Boolean isRecommended,

        @Size(min = 10, max = 1000, message = "Comment must be between 10 and 1000 characters") String comment,

        @NotNull(message = "Anonymous flag is required") Boolean isAnonymous) {
}
