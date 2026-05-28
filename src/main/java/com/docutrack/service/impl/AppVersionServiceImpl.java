package com.docutrack.service.impl;

import com.docutrack.dto.app.AppConfigResponseDto;
import com.docutrack.dto.app.AppUpgradeRequiredResponse;
import com.docutrack.entity.AppPlatform;
import com.docutrack.entity.AppVersionConfigEntity;
import com.docutrack.exception.NotFoundException;
import com.docutrack.repository.AppVersionConfigRepository;
import com.docutrack.service.AppVersionService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppVersionServiceImpl implements AppVersionService {

  private static final String UPGRADE_MESSAGE =
      "App update required. Please upgrade to the latest version.";

  private final AppVersionConfigRepository configRepository;

  @Override
  public AppConfigResponseDto getConfig(AppPlatform platform, Integer clientBuild) {
    AppVersionConfigEntity config = loadConfig(platform);
    return toConfigResponse(config, clientBuild);
  }

  @Override
  public Optional<AppVersionConfigEntity> findConfig(AppPlatform platform) {
    return configRepository.findByPlatform(platform);
  }

  @Override
  public boolean isUpdateRequired(AppVersionConfigEntity config, int clientBuild) {
    return clientBuild < config.getMinBuild();
  }

  @Override
  public AppUpgradeRequiredResponse buildUpgradeRequiredResponse(AppVersionConfigEntity config) {
    return AppUpgradeRequiredResponse.builder()
        .statusCode(426)
        .message(UPGRADE_MESSAGE)
        .minBuild(config.getMinBuild())
        .latestBuild(config.getLatestBuild())
        .forceUpdate(true)
        .storeUrl(config.getStoreUrl())
        .build();
  }

  private AppVersionConfigEntity loadConfig(AppPlatform platform) {
    return configRepository.findByPlatform(platform)
        .orElseThrow(() -> new NotFoundException("App version config not found for platform: " + platform.getValue()));
  }

  private AppConfigResponseDto toConfigResponse(AppVersionConfigEntity config, Integer clientBuild) {
    boolean updateRequired = clientBuild != null && isUpdateRequired(config, clientBuild);
    boolean forceUpdate = updateRequired && config.isForceUpdate();
    boolean softUpdate = clientBuild != null
        && clientBuild < config.getLatestBuild()
        && !forceUpdate
        && config.isSoftUpdate();

    return AppConfigResponseDto.builder()
        .platform(config.getPlatform().getValue())
        .latestVersion(config.getLatestVersion())
        .latestBuild(config.getLatestBuild())
        .minVersion(config.getMinVersion())
        .minBuild(config.getMinBuild())
        .forceUpdate(forceUpdate)
        .softUpdate(softUpdate)
        .updateRequired(updateRequired)
        .title(config.getTitle())
        .message(config.getMessage())
        .storeUrl(config.getStoreUrl())
        .releaseNotes(config.getReleaseNotes())
        .build();
  }
}
