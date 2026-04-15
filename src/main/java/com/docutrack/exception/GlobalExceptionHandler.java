package com.docutrack.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleNotFound(NotFoundException ex, HttpServletRequest req) {
    return build(HttpStatus.NOT_FOUND, ex.getMessage(), req.getRequestURI(), null);
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ApiErrorResponse> handleBadRequest(BadRequestException ex, HttpServletRequest req) {
    log.warn("BadRequest path={} message={}", req.getRequestURI(), ex.getMessage());
    return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req.getRequestURI(), null);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ApiErrorResponse> handleUnauthorized(UnauthorizedException ex, HttpServletRequest req) {
    return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), req.getRequestURI(), null);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    List<ApiErrorResponse.FieldViolation> violations =
        ex.getBindingResult().getAllErrors().stream()
            .filter(err -> err instanceof FieldError)
            .map(err -> (FieldError) err)
            .map(fe -> ApiErrorResponse.FieldViolation.builder()
                .field(fe.getField())
                .message(fe.getDefaultMessage())
                .build())
            .collect(Collectors.toList());

    log.warn("Validation failed path={} violations={}", req.getRequestURI(), violations);
    return build(HttpStatus.BAD_REQUEST, "Validation failed", req.getRequestURI(), violations);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleNoResource(NoResourceFoundException ex, HttpServletRequest req) {
    return build(HttpStatus.NOT_FOUND, "Not found", req.getRequestURI(), null);
  }

  @ExceptionHandler(ErrorResponseException.class)
  public ResponseEntity<ApiErrorResponse> handleSpringErrorResponse(ErrorResponseException ex, HttpServletRequest req) {
    HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
    if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;
    String msg = ex.getBody() != null && ex.getBody().getDetail() != null ? ex.getBody().getDetail() : ex.getMessage();
    if (status == HttpStatus.BAD_REQUEST) {
      log.warn("Spring bad request path={} message={}", req.getRequestURI(), msg);
    }
    return build(status, msg, req.getRequestURI(), null);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
    log.error("Unhandled exception for path={}", req.getRequestURI(), ex);
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", req.getRequestURI(), null);
  }

  private ResponseEntity<ApiErrorResponse> build(
      HttpStatus status, String message, String path, List<ApiErrorResponse.FieldViolation> violations) {
    ApiErrorResponse body = ApiErrorResponse.builder()
        .timestamp(Instant.now())
        .status(status.value())
        .error(status.getReasonPhrase())
        .message(message)
        .path(path)
        .violations(violations)
        .build();
    return ResponseEntity.status(status).body(body);
  }
}

