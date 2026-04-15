package com.docutrack.dto.auth;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SendOtpResponseDto {
  String message;
  String otp;
}

