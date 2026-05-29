package com.docutrack.service.impl;

import com.docutrack.config.UploadProperties;
import com.docutrack.exception.BadRequestException;
import com.docutrack.service.FileStorageService;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class LocalFileStorageService implements FileStorageService {

  private static final Logger log = LoggerFactory.getLogger(LocalFileStorageService.class);
  private static final String FILES_PREFIX = "/files/";

  private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

  private final UploadProperties props;

  @Override
  public String store(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new BadRequestException("File is required");
    }
    String contentType = file.getContentType();
    if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
      throw new BadRequestException("Only JPEG, PNG, or WEBP images are allowed");
    }

    String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
    String ext = extensionFrom(original);
    String filename = System.currentTimeMillis()
        + "-"
        + java.util.UUID.randomUUID().toString().substring(0, 8)
        + (ext.isBlank() ? "" : ("." + ext));

    try {
      Path uploadDir = uploadDirectory();
      Files.createDirectories(uploadDir);

      Path destination = uploadDir.resolve(filename).normalize();
      ensureUnderUploadDir(uploadDir, destination);
      Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
      log.info("Stored file originalName='{}' as '{}'", original, destination);
    } catch (IOException e) {
      throw new BadRequestException("Could not store file");
    }

    return FILES_PREFIX + filename;
  }

  @Override
  public String normalizeStoredPath(String pathOrUrl) {
    if (pathOrUrl == null || pathOrUrl.isBlank()) {
      return null;
    }
    String trimmed = pathOrUrl.trim();

    if (trimmed.startsWith(FILES_PREFIX)) {
      return sanitizeFilesPath(trimmed);
    }
    if (trimmed.startsWith("files/")) {
      return sanitizeFilesPath("/" + trimmed);
    }

    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
      try {
        URI uri = URI.create(trimmed);
        String path = uri.getPath();
        if (path != null && path.startsWith(FILES_PREFIX)) {
          return sanitizeFilesPath(path);
        }
      } catch (IllegalArgumentException ignored) {
        return null;
      }
      return null;
    }

    return null;
  }

  @Override
  public void deleteLocalFile(String storedPath) {
    String normalized = normalizeStoredPath(storedPath);
    if (normalized == null) {
      return;
    }

    String filename = normalized.substring(FILES_PREFIX.length());
    if (filename.isBlank() || filename.contains("..") || filename.contains("/")) {
      log.warn("Refusing to delete invalid stored path: {}", storedPath);
      return;
    }

    try {
      Path uploadDir = uploadDirectory();
      Path target = uploadDir.resolve(filename).normalize();
      ensureUnderUploadDir(uploadDir, target);
      if (Files.deleteIfExists(target)) {
        log.info("Deleted local file {}", target);
      }
    } catch (IOException e) {
      log.warn("Failed to delete local file for path={}", storedPath, e);
    }
  }

  private Path uploadDirectory() {
    return Path.of(props.dir()).toAbsolutePath().normalize();
  }

  private void ensureUnderUploadDir(Path uploadDir, Path target) throws IOException {
    if (!target.startsWith(uploadDir)) {
      throw new IOException("Path escapes upload directory");
    }
  }

  private String sanitizeFilesPath(String path) {
    if (!path.startsWith(FILES_PREFIX)) {
      return null;
    }
    String filename = path.substring(FILES_PREFIX.length());
    if (filename.isBlank() || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
      return null;
    }
    return FILES_PREFIX + filename;
  }

  private String extensionFrom(String name) {
    int dot = name.lastIndexOf('.');
    if (dot < 0 || dot == name.length() - 1) return "";
    return name.substring(dot + 1).toLowerCase();
  }
}
