package com.docutrack.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
  /**
   * Stores the file and returns its public URL (served by this app).
   */
  String store(MultipartFile file);
}

