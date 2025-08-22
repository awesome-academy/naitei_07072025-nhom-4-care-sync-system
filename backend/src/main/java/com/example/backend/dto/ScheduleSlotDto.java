package com.example.backend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ScheduleSlotDto", description = "Slot được tạo/cập nhật bởi API lịch làm việc của bác sĩ")
public record ScheduleSlotDto(

        @Schema(description = "ID của slot", example = "1") Long id,

        @Schema(description = "Thời gian bắt đầu (ISO-8601)", example = "2025-08-25T09:00:00") @JsonProperty("start") @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startTime,

        @Schema(description = "Thời gian kết thúc (ISO-8601)", example = "2025-08-25T09:30:00") @JsonProperty("end") @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime endTime) {
}
