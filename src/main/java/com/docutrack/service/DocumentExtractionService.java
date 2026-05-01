package com.docutrack.service;

import com.docutrack.dto.document.DocumentExtractResponseDto;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentExtractionService {
  /**
   * Extract fields from 1..2 images. Use all images provided.
   */
  DocumentExtractResponseDto extract(Long userId, List<MultipartFile> files);

  /**
   * Backward-compatible single-file extraction.
   */
  default DocumentExtractResponseDto extract(Long userId, MultipartFile file) {
    return extract(userId, List.of(file));
  }
}

