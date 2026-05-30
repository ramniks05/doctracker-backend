package com.docutrack.controller;

import com.docutrack.dto.admin.AdminUserDetailDto;
import com.docutrack.dto.admin.AdminUserPageDto;
import com.docutrack.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin Users")
public class AdminUserController {

  private final AdminUserService adminUserService;

  @GetMapping
  @Operation(summary = "Paginated user list with document counts and search")
  public ResponseEntity<AdminUserPageDto> listUsers(
      @RequestParam(required = false) String q,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "createdAt,desc") String sort) {
    return ResponseEntity.ok(adminUserService.listUsers(q, page, size, sort));
  }

  @GetMapping("/{userId}")
  @Operation(summary = "User profile with document breakdown and recent documents")
  public ResponseEntity<AdminUserDetailDto> getUser(@PathVariable Long userId) {
    return ResponseEntity.ok(adminUserService.getUserDetail(userId));
  }
}
