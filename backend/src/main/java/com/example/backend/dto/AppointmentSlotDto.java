package com.example.backend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AppointmentSlotDto", description = "Khung giờ trống của bác sĩ trong ngày")
public class AppointmentSlotDto {

    @Schema(description = "ID của slot", example = "1")
    private Long id;

    @Schema(description = "Thời gian bắt đầu (ISO-8601)", example = "2025-08-20T16:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonIgnore
    private LocalDateTime startTime;

    @Schema(description = "Thời gian kết thúc (ISO-8601)", example = "2025-08-20T17:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonIgnore
    private LocalDateTime endTime;

    public AppointmentSlotDto(Long id, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getId() {
        return id;
    }

    @JsonProperty("start")
    public LocalDateTime getStart() {
        return startTime;
    }

    @JsonProperty("end")
    public LocalDateTime getEnd() {
        return endTime;
    }
}
