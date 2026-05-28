package com.docutrack.security;

import com.docutrack.dto.app.AppUpgradeRequiredResponse;
import com.docutrack.entity.AppPlatform;
import com.docutrack.config.PlatformVersionConfig;
import com.docutrack.service.AppVersionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class AppVersionFilter extends OncePerRequestFilter {

  public static final String HEADER_PLATFORM = "X-App-Platform";
  public static final String HEADER_VERSION = "X-App-Version";
  public static final String HEADER_BUILD = "X-App-Build";

  private final AppVersionService appVersionService;
  private final ObjectMapper objectMapper;

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    if (HttpMethod.OPTIONS.matches(request.getMethod())) {
      return true;
    }

    String path = request.getRequestURI();
    if (path.startsWith("/api/app/")) {
      return true;
    }
    if (path.equals("/api/health")) {
      return true;
    }
    if (HttpMethod.POST.matches(request.getMethod()) && path.equals("/api/auth/send-otp")) {
      return true;
    }
    if (path.startsWith("/swagger-ui/")
        || path.startsWith("/v3/api-docs")
        || path.startsWith("/files/")) {
      return true;
    }
    return false;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    Integer clientBuild = parseBuildHeader(request.getHeader(HEADER_BUILD));
    if (clientBuild == null) {
      filterChain.doFilter(request, response);
      return;
    }

    AppPlatform platform = resolvePlatform(request.getHeader(HEADER_PLATFORM));
    PlatformVersionConfig config = appVersionService.getConfigForPlatform(platform);
    if (appVersionService.isUpdateRequired(config, clientBuild)) {
      writeUpgradeResponse(response, appVersionService.buildUpgradeRequiredResponse(config));
      return;
    }

    filterChain.doFilter(request, response);
  }

  private AppPlatform resolvePlatform(String rawPlatform) {
    if (rawPlatform == null || rawPlatform.isBlank()) {
      return AppPlatform.ANDROID;
    }
    try {
      return AppPlatform.fromValue(rawPlatform);
    } catch (Exception e) {
      return AppPlatform.ANDROID;
    }
  }

  private Integer parseBuildHeader(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    try {
      return Integer.parseInt(raw.trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private void writeUpgradeResponse(HttpServletResponse response, AppUpgradeRequiredResponse body)
      throws IOException {
    response.setStatus(426);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getOutputStream(), body);
  }
}
