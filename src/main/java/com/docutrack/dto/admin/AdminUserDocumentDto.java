package com.docutrack.dto.admin;

import com.docutrack.entity.DocumentStatus;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AdminUserDocumentDto {
  Long id;
  String name;
  String categoryName;
  DocumentStatus status;
  LocalDate expiryDate;
  Instant createdAt;
}
