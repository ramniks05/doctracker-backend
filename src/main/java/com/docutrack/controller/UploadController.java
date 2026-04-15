package com.docutrack.controller;

import com.docutrack.dto.upload.FileUploadResponseDto;
import com.docutrack.service.FileStorageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

  private final FileStorageService fileStorageService;

  public UploadController(FileStorageService fileStorageService) {
    this.fileStorageService = fileStorageService;
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<FileUploadResponseDto> upload(@RequestParam("file") MultipartFile file) {
    String url = fileStorageService.store(file);
    return ResponseEntity.ok(FileUploadResponseDto.builder()
        .fileName(file.getOriginalFilename())
        .url(url)
        .build());
  }
}

