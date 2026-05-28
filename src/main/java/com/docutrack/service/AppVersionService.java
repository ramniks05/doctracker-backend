package com.docutrack.service;

import com.docutrack.dto.app.AppConfigResponseDto;
import com.docutrack.dto.app.AppUpgradeRequiredResponse;
import com.docutrack.entity.AppPlatform;
import com.docutrack.entity.AppVersionConfigEntity;
import java.util.Optional;

public interface AppVersionService {

  AppConfigResponseDto getConfig(AppPlatform platform, Integer clientBuild);

  Optional<AppVersionConfigEntity> findConfig(AppPlatform platform);

  boolean isUpdateRequired(AppVersionConfigEntity config, int clientBuild);

  AppUpgradeRequiredResponse buildUpgradeRequiredResponse(AppVersionConfigEntity config);
}
