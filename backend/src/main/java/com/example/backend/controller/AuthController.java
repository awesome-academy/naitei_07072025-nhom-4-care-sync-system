package com.example.backend.controller;

import com.example.backend.constant.ApiConstants;
import com.example.backend.dto.JwtAuthResponseDto;
import com.example.backend.dto.LoginRequestDto;
import com.example.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.AUTH_ENDPOINT)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(
                "<h1>Xác thực tài khoản thành công!</h1><p>Bạn có thể đóng cửa sổ này và đăng nhập vào ứng dụng.</p>");
    }

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponseDto> login(@Valid @RequestBody LoginRequestDto loginDto) {
        JwtAuthResponseDto response = authService.login(loginDto);
        return ResponseEntity.ok(response);
    }
}
