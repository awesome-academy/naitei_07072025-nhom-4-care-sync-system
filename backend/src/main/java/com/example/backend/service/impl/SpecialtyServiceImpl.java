package com.example.backend.service.impl;

import com.example.backend.dto.PageResponse;
import com.example.backend.dto.SpecialtyDto;
import com.example.backend.dto.SpecialtySearchRequest;
import com.example.backend.entity.Specialty;
import com.example.backend.mapper.SpecialtyMapper;
import com.example.backend.repository.SpecialtyRepository;
import com.example.backend.service.SpecialtyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SpecialtyServiceImpl implements SpecialtyService {

    private final SpecialtyRepository specialtyRepository;
    private final SpecialtyMapper specialtyMapper;

    @Override
    public PageResponse<SpecialtyDto> searchSpecialties(SpecialtySearchRequest request) {
        log.info("Searching specialties with request: {}", request);

        // Tạo Pageable với sorting
        Sort sort = Sort.by(
                Sort.Direction.fromString(
                        request.getSortDirection() != null ? request.getSortDirection() : "desc"),
                request.getSortBy() != null ? request.getSortBy() : "createdAt");

        Pageable pageable = PageRequest.of(request.getPage() != null ? request.getPage() : 0,
                request.getSize() != null ? request.getSize() : 10, sort);

        // Thực hiện tìm kiếm (bỏ isActive vì entity chưa có)
        Page<Specialty> specialtyPage = specialtyRepository.searchSpecialties(request.getQ(),
                request.getLocation(), pageable);

        // Convert to DTOs với thông tin bổ sung
        List<SpecialtyDto> specialtyDtos = specialtyPage.getContent().stream()
                .map(this::enrichSpecialtyDto).collect(Collectors.toList());

        // Tạo PageResponse
        PageResponse<SpecialtyDto> response = PageResponse.<SpecialtyDto>builder()
                .content(specialtyDtos)
                .pageable(PageResponse.PageableInfo.builder().page(specialtyPage.getNumber())
                        .size(specialtyPage.getSize())
                        .totalElements(specialtyPage.getTotalElements())
                        .totalPages(specialtyPage.getTotalPages()).first(specialtyPage.isFirst())
                        .last(specialtyPage.isLast()).hasNext(specialtyPage.hasNext())
                        .hasPrevious(specialtyPage.hasPrevious()).build())
                .build();

        log.info("Found {} specialties", specialtyPage.getTotalElements());
        return response;
    }

    /**
     * Enrich specialty DTO với thông tin bổ sung
     * 
     * @param specialty
     *            Entity chuyên khoa
     * @return DTO với thông tin đầy đủ
     */
    private SpecialtyDto enrichSpecialtyDto(Specialty specialty) {
        SpecialtyDto dto = specialtyMapper.toDto(specialty);

        // Thêm số lượng bác sĩ
        Long doctorCount = specialtyRepository.countDoctorsBySpecialty(specialty.getId());
        dto.setDoctorCount(doctorCount);

        // Thêm danh sách địa điểm có sẵn
        List<String> locations = specialtyRepository
                .findAvailableLocationsBySpecialty(specialty.getId());
        dto.setAvailableLocations(locations);

        return dto;
    }
}
