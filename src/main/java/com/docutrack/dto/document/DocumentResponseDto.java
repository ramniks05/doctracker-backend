package com.docutrack.dto.document;

import com.docutrack.entity.DocumentStatus;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DocumentResponseDto {
  Long id;
  Long userId;
  String name;
  Long categoryId;
  String categoryName;
  String brandName;
  LocalDate purchaseDate;
  Integer warrantyMonths;
  LocalDate expiryDate;
  String notes;
  String ocrRawText;
  String imageUrl;
  DocumentStatus status;
  Instant createdAt;
  Instant updatedAt;
}

