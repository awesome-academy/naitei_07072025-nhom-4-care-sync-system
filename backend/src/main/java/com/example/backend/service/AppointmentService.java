package com.example.backend.service;

import com.example.backend.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AppointmentService {
    AppointmentCreateResponse create(AppointmentCreateRequest request);
    AppointmentCreateResponse confirm(Long appointmentId);
    AppointmentCreateResponse reject(Long appointmentId, AppointmentRejectRequest request);
    Page<AppointmentSummaryResponse> getMyAppointments(AppointmentFilterRequest filters,
            Pageable pageable);
    PageResponse<AppointmentSummaryDto> listDoctorAppointments(AppointmentListRequest request);
    AppointmentCancelResponse cancelByPatient(Long appointmentId, Boolean confirmPolicy);
    AppointmentDetailResponse updateStatusByDoctor(Long appointmentId,
            UpdateAppointmentStatusRequest request);
    AppointmentDetailResponse getAppointmentDetails(Long id);
}
