package com.docutrack.service.impl;

import com.docutrack.config.UploadProperties;
import com.docutrack.exception.BadRequestException;
import com.docutrack.service.FileStorageService;
import java.io.IOException;
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
      Path uploadDir = Path.of(props.dir()).toAbsolutePath().normalize();
      Files.createDirectories(uploadDir);

      Path destination = uploadDir.resolve(filename).normalize();
      Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
      log.info("Stored file originalName='{}' as '{}'", original, destination);
    } catch (IOException e) {
      throw new BadRequestException("Could not store file");
    }

    return "/files/" + filename;
  }

  private String extensionFrom(String name) {
    int dot = name.lastIndexOf('.');
    if (dot < 0 || dot == name.length() - 1) return "";
    return name.substring(dot + 1).toLowerCase();
  }
}

