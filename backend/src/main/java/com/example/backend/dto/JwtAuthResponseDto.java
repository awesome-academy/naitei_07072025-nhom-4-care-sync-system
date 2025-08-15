package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtAuthResponseDto {
    private String accessToken;
    private String tokenType = "Bearer";

    public JwtAuthResponseDto(String accessToken) {
        this.accessToken = accessToken;
        // tokenType đã có giá trị mặc định là "Bearer"
    }
}
