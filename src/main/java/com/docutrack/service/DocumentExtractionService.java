package com.docutrack.service;

import com.docutrack.dto.document.DocumentExtractResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentExtractionService {
  DocumentExtractResponseDto extract(Long userId, MultipartFile file);
}

