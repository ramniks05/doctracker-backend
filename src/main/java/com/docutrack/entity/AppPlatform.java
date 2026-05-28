package com.docutrack.entity;

import com.docutrack.exception.BadRequestException;
import java.util.Locale;

public enum AppPlatform {
  ANDROID("android"),
  IOS("ios"),
  WEB("web");

  private final String value;

  AppPlatform(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static AppPlatform fromValue(String raw) {
    if (raw == null || raw.isBlank()) {
      return ANDROID;
    }
    String normalized = raw.trim().toLowerCase(Locale.ROOT);
    for (AppPlatform platform : values()) {
      if (platform.value.equals(normalized)) {
        return platform;
      }
    }
    throw new BadRequestException("Invalid platform. Use android, ios, or web.");
  }

  public static AppPlatform fromValueOrNull(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    return fromValue(raw);
  }
}
