package com.example.backend.dto;

import lombok.Data;

@Data
public class SpecialtySearchRequest {
    private String q; // Query search theo tên chuyên khoa
    private String location; // Lọc theo địa điểm của bác sĩ
    private Integer page; // Trang hiện tại
    private Integer size; // Số lượng item per page
    private String sortBy; // Sắp xếp theo trường
    private String sortDirection; // Hướng sắp xếp (asc/desc)
}
