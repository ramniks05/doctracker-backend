package com.docutrack.dto.app;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AppConfigResponseDto {
  String platform;
  String latestVersion;
  int latestBuild;
  String minVersion;
  int minBuild;
  boolean forceUpdate;
  boolean softUpdate;
  boolean updateRequired;
  String title;
  String message;
  String storeUrl;
  String releaseNotes;
}
