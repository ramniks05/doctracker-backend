package com.docutrack.dto.admin;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AdminUserSummaryDto {
  Long id;
  String mobileNumber;
  String name;
  String email;
  boolean verified;
  Instant createdAt;
  Instant updatedAt;
  long documentCount;
  long activeDocuments;
  long expiredDocuments;
  long expiringSoonDocuments;
}
