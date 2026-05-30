package com.docutrack.dto.admin;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AdminUserDetailDto {
  Long id;
  String mobileNumber;
  String name;
  String email;
  boolean verified;
  Instant createdAt;
  Instant updatedAt;
  long totalDocuments;
  long activeDocuments;
  long expiredDocuments;
  long expiringSoonDocuments;
  long documentsWithImage;
  long documentsWithOcr;
  List<CountByKeyDto> documentsByCategory;
  List<AdminUserDocumentDto> recentDocuments;
}
