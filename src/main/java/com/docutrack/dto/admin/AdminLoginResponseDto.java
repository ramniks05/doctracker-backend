package com.docutrack.dto.admin;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AdminLoginResponseDto {
  String username;
  String accessToken;
  String role;
}
