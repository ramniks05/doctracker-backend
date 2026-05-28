package com.docutrack.config;

import com.docutrack.entity.AppPlatform;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.version")
public record AppVersionProperties(
    PlatformVersionConfig android,
    PlatformVersionConfig ios,
    PlatformVersionConfig web
) {

  public PlatformVersionConfig forPlatform(AppPlatform platform) {
    return switch (platform) {
      case ANDROID -> android;
      case IOS -> ios;
      case WEB -> web;
    };
  }
}
