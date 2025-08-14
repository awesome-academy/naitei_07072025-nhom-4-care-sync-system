package com.example.backend.service;

import com.example.backend.dto.DoctorDto;

import java.util.List;

public interface DoctorService {
    List<DoctorDto> getAllDoctors();
    List<DoctorDto> getAllActiveDoctors();
    List<DoctorDto> getDoctorsBySpecialty(Long specialtyId);
}
