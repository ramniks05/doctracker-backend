package com.docutrack.service.impl;

import com.docutrack.config.NotificationProperties;
import com.docutrack.service.WhatsAppNotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class MetaWhatsAppNotificationService implements WhatsAppNotificationService {

  private static final Logger log = LoggerFactory.getLogger(MetaWhatsAppNotificationService.class);

  private final NotificationProperties notificationProperties;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final HttpClient httpClient = HttpClient.newHttpClient();

  @Override
  public boolean sendText(String toMobileNumber, String messageText) {
    NotificationProperties.WhatsApp props = notificationProperties.whatsApp();
    if (props == null || !props.enabled()) {
      log.info("WhatsApp notifications disabled; skipping recipient={}", toMobileNumber);
      return false;
    }
    if (isBlank(props.accessToken()) || isBlank(props.phoneNumberId())) {
      log.warn("WhatsApp config missing token or phoneNumberId; skipping recipient={}", toMobileNumber);
      return false;
    }

    try {
      String normalizedTo = normalizeNumber(toMobileNumber);
      String endpoint = normalizeBaseUrl(props.apiBaseUrl()) + "/" + props.phoneNumberId().trim() + "/messages";

      Map<String, Object> payload = new LinkedHashMap<>();
      payload.put("messaging_product", "whatsapp");
      payload.put("to", normalizedTo);
      payload.put("type", "text");
      payload.put("text", Map.of("preview_url", false, "body", messageText));

      HttpRequest req = HttpRequest.newBuilder()
          .uri(URI.create(endpoint))
          .header("Content-Type", "application/json")
          .header("Authorization", "Bearer " + props.accessToken().trim())
          .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
          .build();

      HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
      if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
        log.warn("WhatsApp send failed status={} body={}", resp.statusCode(), resp.body());
        return false;
      }
      log.info("WhatsApp reminder sent to {}", normalizedTo);
      return true;
    } catch (Exception e) {
      log.error("WhatsApp send failed for recipient={}", toMobileNumber, e);
      return false;
    }
  }

  private String normalizeBaseUrl(String apiBaseUrl) {
    String base = isBlank(apiBaseUrl) ? "https://graph.facebook.com/v21.0" : apiBaseUrl.trim();
    return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
  }

  private String normalizeNumber(String mobileNumber) {
    return mobileNumber == null ? "" : mobileNumber.replaceAll("[^0-9]", "");
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
