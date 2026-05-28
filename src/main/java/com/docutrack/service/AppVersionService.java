package com.docutrack.service;

import com.docutrack.config.PlatformVersionConfig;
import com.docutrack.dto.app.AppConfigResponseDto;
import com.docutrack.dto.app.AppUpgradeRequiredResponse;
import com.docutrack.entity.AppPlatform;

public interface AppVersionService {

  AppConfigResponseDto getConfig(AppPlatform platform, Integer clientBuild);

  PlatformVersionConfig getConfigForPlatform(AppPlatform platform);

  boolean isUpdateRequired(PlatformVersionConfig config, int clientBuild);

  AppUpgradeRequiredResponse buildUpgradeRequiredResponse(PlatformVersionConfig config);
}
