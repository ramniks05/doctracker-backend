package com.docutrack.dto.admin;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AdminGrowthStatsDto {
  List<DailyCountDto> userSignupsPerDay;
  List<DailyCountDto> documentsCreatedPerDay;
  List<DailyCountDto> otpRequestsPerDay;
  List<DailyCountDto> notificationsSentPerDay;
}
