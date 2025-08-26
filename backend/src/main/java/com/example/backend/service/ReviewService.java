package com.example.backend.service;

import com.example.backend.dto.ReviewCreateRequest;
import com.example.backend.dto.ReviewResponse;

public interface ReviewService {

    /**
     * Create a new review for a completed appointment
     * 
     * @param request
     *            Review creation request
     * @return Created review response
     */
    ReviewResponse createReview(ReviewCreateRequest request);
}
