package com.docutrack.service;

import com.docutrack.dto.admin.AdminLoginRequestDto;
import com.docutrack.dto.admin.AdminLoginResponseDto;

public interface AdminAuthService {
  AdminLoginResponseDto login(AdminLoginRequestDto request);
}
