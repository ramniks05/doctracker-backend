package com.docutrack.dto.admin;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AdminDocumentStatsDto {
  long totalDocuments;
  long activeDocuments;
  long expiredDocuments;
  long expiringSoonDocuments;
  long withPurchaseDate;
  long withExpiryDate;
  long withBrandName;
  long withNotes;
  long withImage;
  long withSecondImage;
  long withOcr;
  long withWarrantyMonths;
  long expiringNext7Days;
  long expiringNext30Days;
  long expiredLast30Days;
  long createdLast24Hours;
  long createdLast7Days;
  long createdLast30Days;
  List<CountByKeyDto> byCategory;
  List<CountByKeyDto> byStatus;
  List<DailyCountDto> createdPerDayLast30Days;
}
