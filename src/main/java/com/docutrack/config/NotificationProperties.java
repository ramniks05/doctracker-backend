package com.docutrack.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.notifications")
public record NotificationProperties(
    String expiryCron,
    WhatsApp whatsApp
) {
  public record WhatsApp(
      boolean enabled,
      String apiBaseUrl,
      String accessToken,
      String phoneNumberId,
      String businessNumber
  ) {}
}
