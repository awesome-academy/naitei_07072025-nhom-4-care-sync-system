package com.example.backend.util;

public class AppConstants {

    // API Response Messages
    public static final String SUCCESS_MESSAGE = "Operation successful.";
    public static final String ERROR_MESSAGE = "Operation failed.";
    public static final String RESOURCE_NOT_FOUND = "Resource not found.";
    public static final String UNAUTHORIZED_ACCESS = "Unauthorized access.";
    public static final String VALIDATION_ERROR = "Validation failed.";
    public static final String USERNAME_ALREADY_EXISTS = "Username already exists.";
    public static final String EMAIL_ALREADY_EXISTS = "Email already exists.";
    public static final String INVALID_CREDENTIALS = "Invalid username or password.";
    public static final String APPOINTMENT_NOT_AVAILABLE = "Selected time slot is not available.";
    public static final String APPOINTMENT_CANCEL_FAILED = "Appointment cannot be cancelled.";
    public static final String APPOINTMENT_RESCHEDULE_FAILED = "Appointment cannot be rescheduled.";

    // Pagination and Sorting
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final String DEFAULT_SORT_BY = "createdAt";
    public static final String DEFAULT_SORT_DIRECTION = "desc";

    // User Roles
    public static final String ROLE_PATIENT = "PATIENT";
    public static final String ROLE_DOCTOR = "DOCTOR";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_RECEPTIONIST = "RECEPTIONIST";

    // JWT
    public static final String JWT_SECRET_KEY = "${jwt.secret}";
    public static final long JWT_EXPIRATION_MS = 86400000; // 24 hours
    public static final long JWT_REFRESH_EXPIRATION_MS = 604800000; // 7 days

    // Email
    public static final String EMAIL_SUBJECT_APPOINTMENT_CONFIRMATION = "Xác nhận lịch hẹn khám bệnh";
    public static final String EMAIL_SUBJECT_APPOINTMENT_REMINDER = "Nhắc nhở lịch hẹn khám bệnh";
    public static final String EMAIL_SUBJECT_PASSWORD_RESET = "Yêu cầu đặt lại mật khẩu";

    // File Upload
    public static final String UPLOAD_DIR = "uploads/";
    public static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB

    // Other
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    private AppConstants() {
        // restrict instantiation
    }
}


