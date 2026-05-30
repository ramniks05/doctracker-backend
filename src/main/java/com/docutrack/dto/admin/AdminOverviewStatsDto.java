package com.docutrack.dto.admin;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AdminOverviewStatsDto {
  long totalUsers;
  long verifiedUsers;
  long unverifiedUsers;
  long usersWithName;
  long usersWithEmail;
  long usersWithProfileComplete;
  long newUsersToday;
  long newUsersLast7Days;
  long newUsersLast30Days;

  long totalDocuments;
  long activeDocuments;
  long expiredDocuments;
  long expiringSoonDocuments;
  long documentsWithImage;
  long documentsWithSecondImage;
  long documentsWithOcr;
  long documentsWithWarranty;
  long documentsExpiringNext7Days;
  long documentsExpiringNext30Days;
  double averageDocumentsPerUser;

  long totalCategories;
  List<CountByKeyDto> documentsByCategory;

  long totalOtpRequests;
  long otpRequestsLast24Hours;
  long otpRequestsLast7Days;

  long totalNotificationsSent;
  List<CountByKeyDto> notificationsByReminderType;
  List<CountByKeyDto> notificationsByChannel;
}
