package com.docutrack.service;

import com.docutrack.dto.document.DocumentCreateRequestDto;
import com.docutrack.dto.document.DocumentCreateMultipartRequestDto;
import com.docutrack.dto.document.DocumentResponseDto;
import com.docutrack.dto.document.DocumentUpdateRequestDto;
import com.docutrack.entity.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DocumentService {
  DocumentResponseDto create(Long userId, DocumentCreateRequestDto request);

  DocumentResponseDto createWithImage(Long userId, DocumentCreateMultipartRequestDto request);

  Page<DocumentResponseDto> getAll(Long userId, Long categoryId, DocumentStatus status, Pageable pageable);

  DocumentResponseDto getById(Long id, Long userId);

  DocumentResponseDto update(Long id, Long userId, DocumentUpdateRequestDto request);

  void delete(Long id, Long userId);
}

