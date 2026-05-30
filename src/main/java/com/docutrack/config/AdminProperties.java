package com.docutrack.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.admin")
@Getter
@Setter
public class AdminProperties {

  public static final String DEFAULT_USERNAME = "admin";
  public static final String DEFAULT_PASSWORD = "ExpiryX@Admin2026";

  private String username = DEFAULT_USERNAME;
  private String password = DEFAULT_PASSWORD;
  private Duration accessTokenTtl = Duration.ofHours(8);
}
