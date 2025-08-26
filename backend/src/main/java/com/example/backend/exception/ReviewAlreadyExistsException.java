package com.example.backend.exception;

public class ReviewAlreadyExistsException extends BusinessException {

    public ReviewAlreadyExistsException(String message) {
        super("error.review.already.exists", message);
    }

    public ReviewAlreadyExistsException() {
        super("error.review.already.exists", "Review already exists for this appointment");
    }
}
