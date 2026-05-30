package com.docutrack.controller;

import com.docutrack.dto.admin.AdminLoginRequestDto;
import com.docutrack.dto.admin.AdminLoginResponseDto;
import com.docutrack.service.AdminAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
@Tag(name = "Admin Auth")
public class AdminAuthController {

  private final AdminAuthService adminAuthService;

  @PostMapping("/login")
  @Operation(summary = "Admin login with username and password")
  public ResponseEntity<AdminLoginResponseDto> login(@Valid @RequestBody AdminLoginRequestDto request) {
    return ResponseEntity.ok(adminAuthService.login(request));
  }
}
