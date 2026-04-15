package com.docutrack.dto.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

@Data
public class DocumentCreateMultipartRequestDto {

  @NotNull
  private MultipartFile file;

  @NotBlank
  private String name;

  private String brandName;

  @NotNull
  private Long categoryId;

  @NotNull
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate purchaseDate;

  @NotNull
  private Integer warrantyMonths;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate expiryDate;

  private String notes;

  private String ocrRawText;
}

