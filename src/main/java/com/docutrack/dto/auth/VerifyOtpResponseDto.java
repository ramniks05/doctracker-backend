package com.docutrack.dto.auth;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class VerifyOtpResponseDto {
  Long id;
  String mobileNumber;
  String name;
  String accessToken;
}

