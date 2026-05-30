package com.docutrack.service;

import com.docutrack.dto.admin.AdminDocumentStatsDto;
import com.docutrack.dto.admin.AdminGrowthStatsDto;
import com.docutrack.dto.admin.AdminOverviewStatsDto;

public interface AdminStatsService {
  AdminOverviewStatsDto getOverview();

  AdminDocumentStatsDto getDocumentStats();

  AdminGrowthStatsDto getGrowthStats(int days);
}
