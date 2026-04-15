package com.docutrack.service;

import com.docutrack.dto.auth.SendOtpRequestDto;
import com.docutrack.dto.auth.SendOtpResponseDto;
import com.docutrack.dto.auth.UpdateProfileRequestDto;
import com.docutrack.dto.auth.VerifyOtpRequestDto;
import com.docutrack.dto.auth.VerifyOtpResponseDto;

public interface AuthService {
  SendOtpResponseDto sendOtp(SendOtpRequestDto request);

  SendOtpResponseDto resendOtp(SendOtpRequestDto request);

  VerifyOtpResponseDto verifyOtp(VerifyOtpRequestDto request);

  void updateProfile(Long userId, UpdateProfileRequestDto request);
}

