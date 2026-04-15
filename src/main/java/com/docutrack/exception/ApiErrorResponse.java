package com.docutrack.exception;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ApiErrorResponse {
  Instant timestamp;
  int status;
  String error;
  String message;
  String path;
  List<FieldViolation> violations;

  @Value
  @Builder
  public static class FieldViolation {
    String field;
    String message;
  }
}

