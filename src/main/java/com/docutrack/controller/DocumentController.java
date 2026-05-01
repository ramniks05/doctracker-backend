package com.docutrack.controller;

import com.docutrack.dto.document.DocumentCreateMultipartRequestDto;
import com.docutrack.dto.document.DocumentResponseDto;
import com.docutrack.dto.document.DocumentUpdateRequestDto;
import com.docutrack.dto.document.DocumentExtractResponseDto;
import com.docutrack.entity.DocumentStatus;
import com.docutrack.security.UserPrincipal;
import com.docutrack.service.DocumentService;
import com.docutrack.service.DocumentExtractionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
@SecurityRequirement(name = "bearerAuth")
public class DocumentController {

  private final DocumentService documentService;
  private final DocumentExtractionService documentExtractionService;

  public DocumentController(DocumentService documentService, DocumentExtractionService documentExtractionService) {
    this.documentService = documentService;
    this.documentExtractionService = documentExtractionService;
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<DocumentResponseDto> create(
      @AuthenticationPrincipal UserPrincipal principal,
      @Valid @org.springframework.web.bind.annotation.ModelAttribute DocumentCreateMultipartRequestDto request) {
    return ResponseEntity.ok(documentService.createWithImage(principal.getUserId(), request));
  }

  /**
   * Uploads an image and returns best-effort extracted fields.
   * Flutter can show these fields for user correction before calling POST /api/documents to save.
   */
  @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<DocumentExtractResponseDto> extract(
      @AuthenticationPrincipal UserPrincipal principal,
      @RequestParam(value = "files", required = false) MultipartFile[] files,
      @RequestParam(value = "file", required = false) MultipartFile file) {

    java.util.ArrayList<MultipartFile> all = new java.util.ArrayList<>();
    if (files != null) {
      for (MultipartFile f : files) {
        if (f != null && !f.isEmpty()) all.add(f);
      }
    }
    if (all.isEmpty() && file != null && !file.isEmpty()) {
      all.add(file);
    }

    return ResponseEntity.ok(documentExtractionService.extract(principal.getUserId(), all));
  }

  @GetMapping
  public ResponseEntity<Page<DocumentResponseDto>> getAll(
      @AuthenticationPrincipal UserPrincipal principal,
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) DocumentStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "expiryDate") String sortBy,
      @RequestParam(defaultValue = "asc") String sortDir
  ) {
    Sort sort = "desc".equalsIgnoreCase(sortDir)
        ? Sort.by(sortBy).descending()
        : Sort.by(sortBy).ascending();
    Pageable pageable = PageRequest.of(page, size, sort);
    return ResponseEntity.ok(documentService.getAll(principal.getUserId(), categoryId, status, pageable));
  }

  @GetMapping("/{id}")
  public ResponseEntity<DocumentResponseDto> getById(
      @PathVariable Long id,
      @AuthenticationPrincipal UserPrincipal principal) {
    return ResponseEntity.ok(documentService.getById(id, principal.getUserId()));
  }

  @PutMapping("/{id}")
  public ResponseEntity<DocumentResponseDto> update(
      @PathVariable Long id,
      @AuthenticationPrincipal UserPrincipal principal,
      @Valid @RequestBody DocumentUpdateRequestDto request) {
    return ResponseEntity.ok(documentService.update(id, principal.getUserId(), request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
    documentService.delete(id, principal.getUserId());
    return ResponseEntity.noContent().build();
  }
}

