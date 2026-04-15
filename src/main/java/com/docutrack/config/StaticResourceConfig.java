package com.docutrack.config;

import java.nio.file.Path;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

  private final UploadProperties uploadProperties;

  public StaticResourceConfig(UploadProperties uploadProperties) {
    this.uploadProperties = uploadProperties;
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    Path uploadDir = Path.of(uploadProperties.dir()).toAbsolutePath().normalize();
    String location = uploadDir.toUri().toString();
    registry.addResourceHandler("/files/**")
        .addResourceLocations(location);
  }
}

