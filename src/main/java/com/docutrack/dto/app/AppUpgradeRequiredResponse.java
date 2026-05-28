package com.docutrack.dto.app;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AppUpgradeRequiredResponse {
  int statusCode;
  String message;
  int minBuild;
  int latestBuild;
  boolean forceUpdate;
  String storeUrl;
}
