package com.docutrack.config;

import jakarta.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UploadPathLogger {

  private static final Logger log = LoggerFactory.getLogger(UploadPathLogger.class);

  private final UploadProperties uploadProperties;

  @Value("${app.public-base-url:}")
  private String publicBaseUrl;

  public UploadPathLogger(UploadProperties uploadProperties) {
    this.uploadProperties = uploadProperties;
  }

  @PostConstruct
  void logResolvedPaths() {
    Path uploadDir = Path.of(uploadProperties.dir()).toAbsolutePath().normalize();
    boolean exists = Files.exists(uploadDir);
    boolean writable = exists && Files.isWritable(uploadDir);
    long fileCount = 0;
    if (exists) {
      try (var stream = Files.list(uploadDir)) {
        fileCount = stream.count();
      } catch (Exception e) {
        log.warn("Could not list upload directory {}", uploadDir, e);
      }
    }
    log.info(
        "Upload storage: dir={} exists={} writable={} fileCount={} publicBaseUrl={} servedAt=/files/**",
        uploadDir,
        exists,
        writable,
        fileCount,
        publicBaseUrl.isBlank() ? "(relative URLs only)" : publicBaseUrl);
  }
}
