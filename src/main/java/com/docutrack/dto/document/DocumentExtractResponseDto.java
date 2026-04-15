package com.docutrack.dto.document;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DocumentExtractResponseDto {
  String message;

  // Suggested fields (frontend should allow user correction)
  String name;
  String brandName;
  Long categoryId;
  LocalDate purchaseDate;
  Integer warrantyMonths;
  LocalDate expiryDate;
  String notes;

  // For debugging / future use
  String ocrRawText;
  String extractedJson;
}

