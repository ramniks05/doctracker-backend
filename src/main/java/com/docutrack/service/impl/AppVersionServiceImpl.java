package com.docutrack.service.impl;

import com.docutrack.config.AppVersionProperties;
import com.docutrack.config.PlatformVersionConfig;
import com.docutrack.dto.app.AppConfigResponseDto;
import com.docutrack.dto.app.AppUpgradeRequiredResponse;
import com.docutrack.entity.AppPlatform;
import com.docutrack.service.AppVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppVersionServiceImpl implements AppVersionService {

  private static final String UPGRADE_MESSAGE =
      "App update required. Please upgrade to the latest version.";

  private final AppVersionProperties versionProperties;

  @Override
  public AppConfigResponseDto getConfig(AppPlatform platform, Integer clientBuild) {
    PlatformVersionConfig config = getConfigForPlatform(platform);
    return toConfigResponse(platform, config, clientBuild);
  }

  @Override
  public PlatformVersionConfig getConfigForPlatform(AppPlatform platform) {
    return versionProperties.forPlatform(platform);
  }

  @Override
  public boolean isUpdateRequired(PlatformVersionConfig config, int clientBuild) {
    return clientBuild < config.minBuild();
  }

  @Override
  public AppUpgradeRequiredResponse buildUpgradeRequiredResponse(PlatformVersionConfig config) {
    return AppUpgradeRequiredResponse.builder()
        .statusCode(426)
        .message(UPGRADE_MESSAGE)
        .minBuild(config.minBuild())
        .latestBuild(config.latestBuild())
        .forceUpdate(true)
        .storeUrl(config.storeUrl())
        .build();
  }

  private AppConfigResponseDto toConfigResponse(
      AppPlatform platform, PlatformVersionConfig config, Integer clientBuild) {
    boolean updateRequired = clientBuild != null && isUpdateRequired(config, clientBuild);
    boolean forceUpdate = updateRequired && config.forceUpdate();
    boolean softUpdate = clientBuild != null
        && clientBuild < config.latestBuild()
        && !forceUpdate
        && config.softUpdate();

    return AppConfigResponseDto.builder()
        .platform(platform.getValue())
        .latestVersion(config.latestVersion())
        .latestBuild(config.latestBuild())
        .minVersion(config.minVersion())
        .minBuild(config.minBuild())
        .forceUpdate(forceUpdate)
        .softUpdate(softUpdate)
        .updateRequired(updateRequired)
        .title(config.title())
        .message(config.message())
        .storeUrl(config.storeUrl())
        .releaseNotes(config.releaseNotes())
        .build();
  }
}
