package com.example.backend.controller;

import com.example.backend.constant.ApiConstants;
import com.example.backend.constant.MessageConstants;
import com.example.backend.constant.PagingConstants;
import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.PageResponse;
import com.example.backend.dto.SpecialtyDto;
import com.example.backend.dto.SpecialtySearchRequest;
import com.example.backend.service.SpecialtyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.SPECIALTIES_ENDPOINT)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Specialty", description = "Specialty management APIs")
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    @GetMapping("/search")
    @Operation(summary = "Search specialties with filtering and pagination", description = "Search specialties by name, filter by location and status, with pagination support")
    public ApiResponse<PageResponse<SpecialtyDto>> searchSpecialties(
            @Parameter(description = "Search query for specialty name (case-insensitive)") @RequestParam(required = false) String q,

            @Parameter(description = "Filter by doctor location") @RequestParam(required = false) String location,

            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer page,

            @Parameter(description = "Page size") @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer size,

            @Parameter(description = "Sort by field") @RequestParam(defaultValue = PagingConstants.DEFAULT_SORT_BY) String sortBy,

            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = PagingConstants.DEFAULT_SORT_DIRECTION) String sortDirection) {
        log.info("Received specialty search request: q={}, location={}, page={}, size={}", q,
                location, page, size);

        SpecialtySearchRequest request = new SpecialtySearchRequest();
        request.setQ(q);
        request.setLocation(location);
        request.setPage(page);
        request.setSize(size);
        request.setSortBy(sortBy);
        request.setSortDirection(sortDirection);

        PageResponse<SpecialtyDto> result = specialtyService.searchSpecialties(request);

        return ApiResponse.success(result, MessageConstants.SPECIALTY_SEARCH_SUCCESS);
    }
}
