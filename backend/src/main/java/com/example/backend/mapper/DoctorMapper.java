package com.example.backend.mapper;

import com.example.backend.dto.DoctorDto;
import com.example.backend.entity.Doctor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DoctorMapper {
    public DoctorDto toDto(Doctor doctor){
        if (doctor == null){
            return null;
        }
        return DoctorDto.builder()
                .id(doctor.getId())
                .fullName(doctor.getUser().getFullName())
                .email(doctor.getUser().getEmail())
                .phone(doctor.getUser().getPhoneNumber())
                .title(doctor.getTitle())
                .specialtyName(doctor.getSpecialty().getName())
                .experienceYears(doctor.getExperienceYears())
                .bio(doctor.getBio())
                .consultationFee(doctor.getConsultationFee())
                .build();
    }

    public List<DoctorDto> toDtoList(List<Doctor> doctors){
        if (doctors==null){
            return null;
        }

        return doctors.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

}
