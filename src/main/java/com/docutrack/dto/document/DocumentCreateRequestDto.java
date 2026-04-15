package com.docutrack.dto.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class DocumentCreateRequestDto {

  @NotBlank
  private String name;

  @NotNull
  private Long categoryId;

  private String brandName;
  private LocalDate purchaseDate;
  private LocalDate expiryDate;
  private String notes;
  private String imageUrl;
}

