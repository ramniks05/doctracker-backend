package com.docutrack.service.impl;

import com.docutrack.dto.admin.AdminUserDetailDto;
import com.docutrack.dto.admin.AdminUserDocumentDto;
import com.docutrack.dto.admin.AdminUserPageDto;
import com.docutrack.dto.admin.AdminUserSummaryDto;
import com.docutrack.dto.admin.CountByKeyDto;
import com.docutrack.entity.DocumentEntity;
import com.docutrack.entity.DocumentStatus;
import com.docutrack.entity.UserEntity;
import com.docutrack.exception.NotFoundException;
import com.docutrack.repository.DocumentRepository;
import com.docutrack.repository.UserRepository;
import com.docutrack.repository.projection.KeyCountProjection;
import com.docutrack.repository.projection.UserDocumentCountProjection;
import com.docutrack.service.AdminUserService;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserServiceImpl implements AdminUserService {

  private final UserRepository userRepository;
  private final DocumentRepository documentRepository;

  @Override
  public AdminUserPageDto listUsers(String query, int page, int size, String sort) {
    int safePage = Math.max(page, 0);
    int safeSize = Math.min(Math.max(size, 1), 100);
    Pageable pageable = PageRequest.of(safePage, safeSize, parseSort(sort));

    String q = query == null ? "" : query.trim();
    Page<UserEntity> result = userRepository.search(q, pageable);

    List<Long> userIds = result.getContent().stream().map(UserEntity::getId).toList();
    Map<Long, UserDocumentCountProjection> countsByUser = userIds.isEmpty()
        ? Map.of()
        : documentRepository.countByUserIds(userIds).stream()
            .collect(Collectors.toMap(UserDocumentCountProjection::getUserId, Function.identity()));

    List<AdminUserSummaryDto> content = result.getContent().stream()
        .map(user -> toSummary(user, countsByUser.get(user.getId())))
        .toList();

    return AdminUserPageDto.builder()
        .content(content)
        .page(result.getNumber())
        .size(result.getSize())
        .totalElements(result.getTotalElements())
        .totalPages(result.getTotalPages())
        .first(result.isFirst())
        .last(result.isLast())
        .build();
  }

  @Override
  public AdminUserDetailDto getUserDetail(Long userId) {
    UserEntity user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found: " + userId));

    List<KeyCountProjection> byCategory = documentRepository.countByCategoryForUser(userId);
    List<DocumentEntity> recent = documentRepository.findTop10ByUser_IdOrderByCreatedAtDesc(userId);

    return AdminUserDetailDto.builder()
        .id(user.getId())
        .mobileNumber(user.getMobileNumber())
        .name(user.getName())
        .email(user.getEmail())
        .verified(Boolean.TRUE.equals(user.getIsVerified()))
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .totalDocuments(documentRepository.countByUser_Id(userId))
        .activeDocuments(documentRepository.countByUser_IdAndStatus(userId, DocumentStatus.ACTIVE))
        .expiredDocuments(documentRepository.countByUser_IdAndStatus(userId, DocumentStatus.EXPIRED))
        .expiringSoonDocuments(documentRepository.countByUser_IdAndStatus(userId, DocumentStatus.EXPIRING_SOON))
        .documentsWithImage(documentRepository.countWithImageForUser(userId))
        .documentsWithOcr(documentRepository.countWithOcrForUser(userId))
        .documentsByCategory(byCategory.stream()
            .map(r -> CountByKeyDto.builder()
                .key(r.getKey())
                .count(r.getCount() != null ? r.getCount() : 0L)
                .build())
            .toList())
        .recentDocuments(recent.stream()
            .map(d -> AdminUserDocumentDto.builder()
                .id(d.getId())
                .name(d.getName())
                .categoryName(d.getCategory().getName())
                .status(d.getStatus())
                .expiryDate(d.getExpiryDate())
                .createdAt(d.getCreatedAt())
                .build())
            .toList())
        .build();
  }

  private AdminUserSummaryDto toSummary(UserEntity user, UserDocumentCountProjection counts) {
    long total = counts != null && counts.getTotal() != null ? counts.getTotal() : 0L;
    long active = counts != null && counts.getActive() != null ? counts.getActive() : 0L;
    long expired = counts != null && counts.getExpired() != null ? counts.getExpired() : 0L;
    long expiringSoon = counts != null && counts.getExpiringSoon() != null ? counts.getExpiringSoon() : 0L;

    return AdminUserSummaryDto.builder()
        .id(user.getId())
        .mobileNumber(user.getMobileNumber())
        .name(user.getName())
        .email(user.getEmail())
        .verified(Boolean.TRUE.equals(user.getIsVerified()))
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .documentCount(total)
        .activeDocuments(active)
        .expiredDocuments(expired)
        .expiringSoonDocuments(expiringSoon)
        .build();
  }

  private Sort parseSort(String sort) {
    if (sort == null || sort.isBlank()) {
      return Sort.by(Sort.Direction.DESC, "createdAt");
    }
    String[] parts = sort.split(",", 2);
    String field = parts[0].trim();
    Sort.Direction direction = parts.length > 1 && parts[1].trim().equalsIgnoreCase("asc")
        ? Sort.Direction.ASC
        : Sort.Direction.DESC;
    return Sort.by(direction, field);
  }
}
