package com.docutrack.service.impl;

import com.docutrack.dto.admin.AdminDocumentStatsDto;
import com.docutrack.dto.admin.AdminGrowthStatsDto;
import com.docutrack.dto.admin.AdminOverviewStatsDto;
import com.docutrack.dto.admin.CountByKeyDto;
import com.docutrack.dto.admin.DailyCountDto;
import com.docutrack.entity.DocumentStatus;
import com.docutrack.repository.CategoryRepository;
import com.docutrack.repository.DocumentRepository;
import com.docutrack.repository.NotificationLogRepository;
import com.docutrack.repository.OtpRepository;
import com.docutrack.repository.UserRepository;
import com.docutrack.repository.projection.DailyCountProjection;
import com.docutrack.repository.projection.KeyCountProjection;
import com.docutrack.service.AdminStatsService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStatsServiceImpl implements AdminStatsService {

  private final UserRepository userRepository;
  private final DocumentRepository documentRepository;
  private final CategoryRepository categoryRepository;
  private final OtpRepository otpRepository;
  private final NotificationLogRepository notificationLogRepository;
  private final Clock clock;

  @Override
  public AdminOverviewStatsDto getOverview() {
    Instant now = Instant.now(clock);
    LocalDate today = LocalDate.now(clock);

    long totalUsers = userRepository.count();
    long totalDocuments = documentRepository.count();

    return AdminOverviewStatsDto.builder()
        .totalUsers(totalUsers)
        .verifiedUsers(userRepository.countByIsVerifiedTrue())
        .unverifiedUsers(totalUsers - userRepository.countByIsVerifiedTrue())
        .usersWithName(userRepository.countWithName())
        .usersWithEmail(userRepository.countWithEmail())
        .usersWithProfileComplete(userRepository.countWithCompleteProfile())
        .newUsersToday(userRepository.countByCreatedAtAfter(startOfDay(now)))
        .newUsersLast7Days(userRepository.countByCreatedAtAfter(now.minus(7, ChronoUnit.DAYS)))
        .newUsersLast30Days(userRepository.countByCreatedAtAfter(now.minus(30, ChronoUnit.DAYS)))
        .totalDocuments(totalDocuments)
        .activeDocuments(documentRepository.countByStatus(DocumentStatus.ACTIVE))
        .expiredDocuments(documentRepository.countByStatus(DocumentStatus.EXPIRED))
        .expiringSoonDocuments(documentRepository.countByStatus(DocumentStatus.EXPIRING_SOON))
        .documentsWithImage(documentRepository.countByImageUrlIsNotNull())
        .documentsWithSecondImage(documentRepository.countWithSecondImage())
        .documentsWithOcr(documentRepository.countWithOcr())
        .documentsWithWarranty(documentRepository.countByWarrantyMonthsIsNotNull())
        .documentsExpiringNext7Days(documentRepository.countExpiringBetween(today, today.plusDays(7)))
        .documentsExpiringNext30Days(documentRepository.countExpiringBetween(today, today.plusDays(30)))
        .averageDocumentsPerUser(totalUsers == 0 ? 0.0 : (double) totalDocuments / totalUsers)
        .totalCategories(categoryRepository.count())
        .documentsByCategory(mapKeyCounts(documentRepository.countGroupByCategoryName()))
        .totalOtpRequests(otpRepository.count())
        .otpRequestsLast24Hours(otpRepository.countByCreatedAtAfter(now.minus(1, ChronoUnit.DAYS)))
        .otpRequestsLast7Days(otpRepository.countByCreatedAtAfter(now.minus(7, ChronoUnit.DAYS)))
        .totalNotificationsSent(notificationLogRepository.count())
        .notificationsByReminderType(mapEnumCounts(notificationLogRepository.countGroupByReminderType()))
        .notificationsByChannel(mapEnumCounts(notificationLogRepository.countGroupByChannel()))
        .build();
  }

  @Override
  public AdminDocumentStatsDto getDocumentStats() {
    Instant now = Instant.now(clock);
    LocalDate today = LocalDate.now(clock);
    Instant since30 = now.minus(30, ChronoUnit.DAYS);

    return AdminDocumentStatsDto.builder()
        .totalDocuments(documentRepository.count())
        .activeDocuments(documentRepository.countByStatus(DocumentStatus.ACTIVE))
        .expiredDocuments(documentRepository.countByStatus(DocumentStatus.EXPIRED))
        .expiringSoonDocuments(documentRepository.countByStatus(DocumentStatus.EXPIRING_SOON))
        .withPurchaseDate(documentRepository.countByPurchaseDateIsNotNull())
        .withExpiryDate(documentRepository.countByExpiryDateIsNotNull())
        .withBrandName(documentRepository.countWithBrandName())
        .withNotes(documentRepository.countWithNotes())
        .withImage(documentRepository.countByImageUrlIsNotNull())
        .withSecondImage(documentRepository.countWithSecondImage())
        .withOcr(documentRepository.countWithOcr())
        .withWarrantyMonths(documentRepository.countByWarrantyMonthsIsNotNull())
        .expiringNext7Days(documentRepository.countExpiringBetween(today, today.plusDays(7)))
        .expiringNext30Days(documentRepository.countExpiringBetween(today, today.plusDays(30)))
        .expiredLast30Days(documentRepository.countExpiredSince(today.minusDays(30)))
        .createdLast24Hours(documentRepository.countByCreatedAtAfter(now.minus(1, ChronoUnit.DAYS)))
        .createdLast7Days(documentRepository.countByCreatedAtAfter(now.minus(7, ChronoUnit.DAYS)))
        .createdLast30Days(documentRepository.countByCreatedAtAfter(since30))
        .byCategory(mapKeyCounts(documentRepository.countGroupByCategoryName()))
        .byStatus(mapStatusCounts(documentRepository.countGroupByStatus()))
        .createdPerDayLast30Days(mapDailyCounts(documentRepository.countCreatedPerDaySince(since30)))
        .build();
  }

  @Override
  public AdminGrowthStatsDto getGrowthStats(int days) {
    int windowDays = Math.min(Math.max(days, 1), 365);
    Instant since = Instant.now(clock).minus(windowDays, ChronoUnit.DAYS);

    return AdminGrowthStatsDto.builder()
        .userSignupsPerDay(mapDailyCounts(userRepository.countSignupsPerDaySince(since)))
        .documentsCreatedPerDay(mapDailyCounts(documentRepository.countCreatedPerDaySince(since)))
        .otpRequestsPerDay(mapDailyCounts(otpRepository.countPerDaySince(since)))
        .notificationsSentPerDay(mapDailyCounts(notificationLogRepository.countPerDaySince(since)))
        .build();
  }

  private Instant startOfDay(Instant now) {
    LocalDate day = LocalDate.ofInstant(now, clock.getZone());
    return day.atStartOfDay(clock.getZone()).toInstant();
  }

  private List<CountByKeyDto> mapKeyCounts(List<KeyCountProjection> rows) {
    return rows.stream()
        .map(r -> CountByKeyDto.builder()
            .key(r.getKey())
            .count(r.getCount() != null ? r.getCount() : 0L)
            .build())
        .toList();
  }

  private List<CountByKeyDto> mapStatusCounts(List<Object[]> rows) {
    return rows.stream()
        .map(r -> CountByKeyDto.builder()
            .key(String.valueOf(r[0]))
            .count(r[1] != null ? ((Number) r[1]).longValue() : 0L)
            .build())
        .toList();
  }

  private List<CountByKeyDto> mapEnumCounts(List<Object[]> rows) {
    return rows.stream()
        .map(r -> CountByKeyDto.builder()
            .key(String.valueOf(r[0]))
            .count(r[1] != null ? ((Number) r[1]).longValue() : 0L)
            .build())
        .toList();
  }

  private List<DailyCountDto> mapDailyCounts(List<DailyCountProjection> rows) {
    return rows.stream()
        .map(r -> DailyCountDto.builder()
            .date(r.getDay())
            .count(r.getCount() != null ? r.getCount() : 0L)
            .build())
        .toList();
  }
}
