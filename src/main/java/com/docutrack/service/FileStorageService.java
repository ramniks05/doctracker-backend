package com.docutrack.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
  /**
   * Stores the file and returns its stored path (e.g. {@code /files/name.jpg}).
   */
  String store(MultipartFile file);

  /**
   * Normalizes a DB value or full public URL to {@code /files/...}, or {@code null} if not app-managed storage.
   */
  String normalizeStoredPath(String pathOrUrl);

  /**
   * Deletes a file under the upload directory. No-op for external URLs or missing files.
   */
  void deleteLocalFile(String storedPath);
}

