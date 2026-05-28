package com.docutrack.controller;

import com.docutrack.dto.app.AppConfigResponseDto;
import com.docutrack.entity.AppPlatform;
import com.docutrack.service.AppVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app")
@RequiredArgsConstructor
public class AppConfigController {

  private final AppVersionService appVersionService;

  @GetMapping("/config")
  public ResponseEntity<AppConfigResponseDto> getConfig(
      @RequestParam(required = false) String platform,
      @RequestParam(required = false) String version,
      @RequestParam(required = false) Integer build) {

    AppPlatform resolved = AppPlatform.fromValue(platform);
    AppConfigResponseDto body = appVersionService.getConfig(resolved, build);

    return ResponseEntity.ok()
        .cacheControl(CacheControl.noCache())
        .body(body);
  }
}
