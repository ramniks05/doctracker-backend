package com.docutrack.controller;

import com.docutrack.dto.admin.AdminDocumentStatsDto;
import com.docutrack.dto.admin.AdminGrowthStatsDto;
import com.docutrack.dto.admin.AdminOverviewStatsDto;
import com.docutrack.service.AdminStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin Stats")
public class AdminStatsController {

  private final AdminStatsService adminStatsService;

  @GetMapping("/overview")
  @Operation(summary = "Dashboard overview: users, documents, OTP, notifications")
  public ResponseEntity<AdminOverviewStatsDto> overview() {
    return ResponseEntity.ok(adminStatsService.getOverview());
  }

  @GetMapping("/documents")
  @Operation(summary = "Detailed document analytics")
  public ResponseEntity<AdminDocumentStatsDto> documents() {
    return ResponseEntity.ok(adminStatsService.getDocumentStats());
  }

  @GetMapping("/growth")
  @Operation(summary = "Daily growth series for users, documents, OTPs, notifications")
  public ResponseEntity<AdminGrowthStatsDto> growth(
      @RequestParam(defaultValue = "30") int days) {
    return ResponseEntity.ok(adminStatsService.getGrowthStats(days));
  }
}
