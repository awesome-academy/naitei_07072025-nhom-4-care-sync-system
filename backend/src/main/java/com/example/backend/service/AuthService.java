package com.example.backend.service;

import com.example.backend.dto.JwtAuthResponseDto;
import com.example.backend.dto.LoginRequestDto;
import com.example.backend.entity.User;
import com.example.backend.exception.BusinessException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import com.example.backend.exception.UnauthorizedException;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token không hợp lệ."));
        if (user.getTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException("TOKEN_EXPIRED", "Token xác thực hết hạn.");
        }

        user.setActive(true);
        user.setVerificationToken(null);
        user.setTokenExpiryDate(null);
        userRepository.save(user);
    }


    public JwtAuthResponseDto login(LoginRequestDto loginDto) {
        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password."));

        // 2. So sánh mật khẩu đã mã hóa
        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password.");
        }

        String token = jwtTokenProvider.generateToken(user.getEmail());

        return new JwtAuthResponseDto(token);
    }
}
