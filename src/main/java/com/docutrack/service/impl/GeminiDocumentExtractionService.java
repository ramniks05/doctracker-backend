package com.docutrack.service.impl;

import com.docutrack.config.GeminiProperties;
import com.docutrack.dto.document.DocumentExtractResponseDto;
import com.docutrack.exception.BadRequestException;
import com.docutrack.service.DocumentExtractionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class GeminiDocumentExtractionService implements DocumentExtractionService {

  private static final Logger log = LoggerFactory.getLogger(GeminiDocumentExtractionService.class);

  private final GeminiProperties props;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final HttpClient httpClient = HttpClient.newHttpClient();

  @Override
  public DocumentExtractResponseDto extract(Long userId, MultipartFile file) {
    if (props.apiKey() == null || props.apiKey().isBlank()) {
      throw new BadRequestException("Gemini API key not configured on server");
    }
    if (file == null || file.isEmpty()) {
      throw new BadRequestException("file is required");
    }

    try {
      String mimeType = file.getContentType() == null ? "image/jpeg" : file.getContentType();
      String base64 = Base64.getEncoder().encodeToString(file.getBytes());

      String prompt = """
          Extract document fields from this purchase/warranty document image.
          Return ONLY valid JSON (no markdown) with keys:
          name, brandName, categoryId (number or null),
          purchaseDate (YYYY-MM-DD or null),
          warrantyMonths (integer or null),
          expiryDate (YYYY-MM-DD or null),
          notes (string or null),
          ocrRawText (string or null).
          If unsure, set null.
          """;

      String body = """
          {
            "contents": [{
              "role": "user",
              "parts": [
                { "text": %s },
                { "inlineData": { "mimeType": %s, "data": %s } }
              ]
            }],
            "generationConfig": {
              "temperature": 0.2,
              "maxOutputTokens": 1024,
              "responseMimeType": "application/json"
            }
          }
          """;

      String jsonBody = body.formatted(
          objectMapper.writeValueAsString(prompt),
          objectMapper.writeValueAsString(mimeType),
          objectMapper.writeValueAsString(base64)
      );

      String model = props.model() == null || props.model().isBlank() ? "gemini-2.5-flash-lite" : props.model().trim();
      String url = "https://generativelanguage.googleapis.com/v1beta/models/"
          + URLEncoder.encode(model, StandardCharsets.UTF_8)
          + ":generateContent?key="
          + URLEncoder.encode(props.apiKey(), StandardCharsets.UTF_8);

      HttpRequest req = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
          .build();

      HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
      if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
        log.error("Gemini error status={} body={}", resp.statusCode(), resp.body());
        throw new BadRequestException(buildGeminiErrorMessage(resp.statusCode(), resp.body()));
      }

      JsonNode root = objectMapper.readTree(resp.body());
      String jsonText = extractCandidateText(root);
      if (jsonText == null || jsonText.isBlank()) {
        throw new BadRequestException("Could not parse extracted JSON from Gemini response");
      }

      JsonNode extracted = objectMapper.readTree(jsonText);

      return DocumentExtractResponseDto.builder()
          .message("Extracted successfully")
          .name(text(extracted, "name"))
          .brandName(text(extracted, "brandName"))
          .categoryId(longOrNull(extracted, "categoryId"))
          .purchaseDate(dateOrNull(extracted, "purchaseDate"))
          .warrantyMonths(intOrNull(extracted, "warrantyMonths"))
          .expiryDate(dateOrNull(extracted, "expiryDate"))
          .notes(text(extracted, "notes"))
          .ocrRawText(text(extracted, "ocrRawText"))
          .extractedJson(extracted.toString())
          .build();
    } catch (BadRequestException e) {
      throw e;
    } catch (Exception e) {
      log.error("Gemini extraction failed for userId={}", userId, e);
      throw new BadRequestException("Extraction failed: " + sanitize(e.getMessage()));
    }
  }

  private String buildGeminiErrorMessage(int statusCode, String rawBody) {
    String detail = null;
    try {
      JsonNode body = objectMapper.readTree(rawBody);
      JsonNode error = body.get("error");
      if (error != null && error.get("message") != null && !error.get("message").asText().isBlank()) {
        detail = error.get("message").asText();
      }
    } catch (Exception ignored) {
      // Fallback to raw response if JSON parsing fails.
    }

    String suffix = detail != null ? detail : sanitize(rawBody);
    if (suffix == null || suffix.isBlank()) {
      return "Extraction failed (Gemini HTTP " + statusCode + ")";
    }
    return "Extraction failed (Gemini HTTP " + statusCode + "): " + suffix;
  }

  private String sanitize(String input) {
    if (input == null) return null;
    String oneLine = input.replaceAll("\\s+", " ").trim();
    int maxLen = 500;
    if (oneLine.length() <= maxLen) {
      return oneLine;
    }
    return oneLine.substring(0, maxLen) + "...";
  }

  private String extractCandidateText(JsonNode root) {
    JsonNode candidates = root.get("candidates");
    if (candidates == null || !candidates.isArray() || candidates.isEmpty()) return null;
    JsonNode content = candidates.get(0).get("content");
    if (content == null) return null;
    JsonNode parts = content.get("parts");
    if (parts == null || !parts.isArray() || parts.isEmpty()) return null;
    JsonNode text = parts.get(0).get("text");
    return text == null ? null : text.asText();
  }

  private String text(JsonNode node, String key) {
    JsonNode v = node.get(key);
    if (v == null || v.isNull()) return null;
    String s = v.asText();
    return s.isBlank() ? null : s;
  }

  private Integer intOrNull(JsonNode node, String key) {
    JsonNode v = node.get(key);
    if (v == null || v.isNull()) return null;
    if (v.isInt()) return v.asInt();
    String s = v.asText();
    if (s == null || s.isBlank()) return null;
    return Integer.valueOf(s);
  }

  private Long longOrNull(JsonNode node, String key) {
    JsonNode v = node.get(key);
    if (v == null || v.isNull()) return null;
    if (v.isLong() || v.isInt()) return v.asLong();
    String s = v.asText();
    if (s == null || s.isBlank()) return null;
    return Long.valueOf(s);
  }

  private LocalDate dateOrNull(JsonNode node, String key) {
    String s = text(node, key);
    if (s == null) return null;
    return LocalDate.parse(s);
  }
}

