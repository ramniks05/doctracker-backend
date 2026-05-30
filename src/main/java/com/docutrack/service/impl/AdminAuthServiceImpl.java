package com.docutrack.service.impl;

import com.docutrack.config.AdminProperties;
import com.docutrack.dto.admin.AdminLoginRequestDto;
import com.docutrack.dto.admin.AdminLoginResponseDto;
import com.docutrack.exception.UnauthorizedException;
import com.docutrack.service.AdminAuthService;
import com.docutrack.util.JwtService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

  private final AdminProperties adminProperties;
  private final JwtService jwtService;

  @Override
  public AdminLoginResponseDto login(AdminLoginRequestDto request) {
    if (!secureEquals(adminProperties.getUsername(), request.getUsername())
        || !secureEquals(adminProperties.getPassword(), request.getPassword())) {
      throw new UnauthorizedException("Invalid admin credentials");
    }

    Duration ttl = adminProperties.getAccessTokenTtl() != null
        ? adminProperties.getAccessTokenTtl()
        : Duration.ofHours(8);

    String token = jwtService.generateAdminAccessToken(request.getUsername(), ttl);

    return AdminLoginResponseDto.builder()
        .username(request.getUsername())
        .accessToken(token)
        .role(JwtService.ROLE_ADMIN)
        .build();
  }

  private boolean secureEquals(String expected, String actual) {
    if (expected == null || actual == null) {
      return expected == actual;
    }
    return MessageDigest.isEqual(
        expected.getBytes(StandardCharsets.UTF_8),
        actual.getBytes(StandardCharsets.UTF_8));
  }
}
