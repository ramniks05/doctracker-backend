package com.docutrack.service;

import com.docutrack.dto.admin.AdminUserDetailDto;
import com.docutrack.dto.admin.AdminUserPageDto;

public interface AdminUserService {
  AdminUserPageDto listUsers(String query, int page, int size, String sort);

  AdminUserDetailDto getUserDetail(Long userId);
}
