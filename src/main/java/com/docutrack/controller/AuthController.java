package com.docutrack.controller;

import com.docutrack.dto.auth.SendOtpRequestDto;
import com.docutrack.dto.auth.SendOtpResponseDto;
import com.docutrack.dto.auth.UpdateProfileRequestDto;
import com.docutrack.dto.auth.VerifyOtpRequestDto;
import com.docutrack.dto.auth.VerifyOtpResponseDto;
import com.docutrack.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/send-otp")
  public ResponseEntity<SendOtpResponseDto> sendOtp(@Valid @RequestBody SendOtpRequestDto request) {
    return ResponseEntity.ok(authService.sendOtp(request));
  }

  @PostMapping("/resend-otp")
  public ResponseEntity<SendOtpResponseDto> resendOtp(@Valid @RequestBody SendOtpRequestDto request) {
    return ResponseEntity.ok(authService.resendOtp(request));
  }

  @PostMapping("/verify-otp")
  public ResponseEntity<VerifyOtpResponseDto> verifyOtp(@Valid @RequestBody VerifyOtpRequestDto request) {
    return ResponseEntity.ok(authService.verifyOtp(request));
  }

  @PutMapping("/profile/{userId}")
  public ResponseEntity<String> updateProfile(
      @PathVariable Long userId,
      @Valid @RequestBody UpdateProfileRequestDto request) {
    authService.updateProfile(userId, request);
    return ResponseEntity.ok("Profile updated successfully");
  }
}

