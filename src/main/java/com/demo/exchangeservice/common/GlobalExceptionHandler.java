package com.demo.exchangeservice.common;

import com.demo.exchangeservice.exchange.service.ExchangeException;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tools.jackson.databind.exc.InvalidFormatException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ProblemDetail handleUnexpected(Exception exception) {
    log.error("Unexpected error", exception);
    return problem(
        HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred");
  }

  @ExceptionHandler
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ProblemDetail handleExchange(ExchangeException exception) {
    log.warn(exception.getMessage());
    return problem(HttpStatus.BAD_REQUEST, "Bad Request", exception.getMessage());
  }

  @ExceptionHandler
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ProblemDetail handleIllegalArgument(IllegalArgumentException exception) {
    log.warn(exception.getMessage());
    return problem(HttpStatus.BAD_REQUEST, "Bad Request", exception.getMessage());
  }

  @ExceptionHandler
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
    Map<String, String> errors =
        exception.getBindingResult().getFieldErrors().stream()
            .collect(
                Collectors.toMap(
                    FieldError::getField,
                    error ->
                        StringUtils.hasText(error.getDefaultMessage())
                            ? error.getDefaultMessage()
                            : "is invalid",
                    (first, _) -> first,
                    TreeMap::new));
    log.warn("Request validation failed: {}", errors);
    ProblemDetail problemDetail =
        problem(
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            "Request validation failed: " + String.join(", ", errors.keySet()));
    problemDetail.setProperty("fields", errors);
    return problemDetail;
  }

  @ExceptionHandler
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ProblemDetail handleMessageNotReadable(HttpMessageNotReadableException exception) {
    Throwable cause = exception.getMostSpecificCause();
    String detail =
        cause instanceof InvalidFormatException invalidFormat
                && invalidFormat.getValue() instanceof String value
                && invalidFormat.getTargetType() instanceof Class<?> targetType
                && targetType.isEnum()
            ? "Unsupported %s: %s. Supported values: %s"
                .formatted(
                    targetType.getSimpleName(),
                    value,
                    String.join(
                        ", ",
                        Arrays.stream(targetType.getEnumConstants())
                            .map(Object::toString)
                            .toList()))
            : detailOrDefault(cause);
    log.warn(detail);
    return problem(HttpStatus.BAD_REQUEST, "Bad Request", detail);
  }

  private static String detailOrDefault(Throwable cause) {
    return cause.getMessage() == null ? "Malformed request body" : cause.getMessage();
  }

  @ExceptionHandler
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
    log.warn("Invalid value for parameter: {}", exception.getName());
    return problem(
        HttpStatus.BAD_REQUEST,
        "Bad Request",
        "Invalid value for parameter: " + exception.getName());
  }

  private static ProblemDetail problem(HttpStatus status, String title, String detail) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
    problemDetail.setTitle(title);
    return problemDetail;
  }
}
