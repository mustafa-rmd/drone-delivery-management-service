package com.acme.services.dronedelivery.controllers.exception;

import com.acme.services.dronedelivery.exception.BusinessRuleViolationException;
import com.acme.services.dronedelivery.exception.ResourceNotFoundException;
import com.acme.services.dronedelivery.exception.UnauthorizedAccessException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class ExceptionControllerAdvice extends ResponseEntityExceptionHandler {

  private static final String PROBLEM_BASE_URL = "https://acme.com/problems";

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            (error) -> {
              if (error instanceof FieldError) {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                errors.put(fieldName, errorMessage);
              } else {
                // For class-level constraints (e.g., @ValidCoordinates)
                String errorMessage = error.getDefaultMessage();
                errors.put("message", errorMessage);
              }
            });

    ProblemDetail pd =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request content.");
    pd.setType(URI.create("about:blank"));
    pd.setTitle("Bad Request");
    pd.setProperty("errors", errors);

    return ResponseEntity.badRequest().body(pd);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ProblemDetail handleResourceNotFoundException(
      ResourceNotFoundException ex, HttpServletRequest request) {
    log.warn("Resource not found: {}", ex.getMessage());
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    pd.setType(URI.create(PROBLEM_BASE_URL + "/resource-not-found"));
    pd.setTitle("Resource Not Found");
    pd.setInstance(URI.create(request.getRequestURI()));
    return pd;
  }

  @ExceptionHandler(UnauthorizedAccessException.class)
  public ProblemDetail handleUnauthorizedAccessException(
      UnauthorizedAccessException ex, HttpServletRequest request) {
    log.warn("Unauthorized access: {}", ex.getMessage());
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    pd.setType(URI.create(PROBLEM_BASE_URL + "/forbidden"));
    pd.setTitle("Forbidden");
    pd.setInstance(URI.create(request.getRequestURI()));
    return pd;
  }

  @ExceptionHandler(BusinessRuleViolationException.class)
  public ProblemDetail handleBusinessRuleViolationException(
      BusinessRuleViolationException ex, HttpServletRequest request) {
    log.warn("Business rule violation: {}", ex.getMessage());
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    pd.setType(URI.create("about:blank"));
    pd.setTitle("Bad Request");
    pd.setInstance(URI.create(request.getRequestURI()));
    pd.setProperty("message", ex.getMessage());
    return pd;
  }

  @ExceptionHandler(NoSuchElementException.class)
  public ProblemDetail handleNoSuchElementException(
      NoSuchElementException ex, HttpServletRequest request) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    pd.setType(URI.create(PROBLEM_BASE_URL + "/not-found"));
    pd.setTitle("Not Found");
    pd.setInstance(URI.create(request.getRequestURI()));
    return pd;
  }

  @ExceptionHandler(IllegalStateException.class)
  public ProblemDetail handleIllegalStateException(
      IllegalStateException ex, HttpServletRequest request) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    pd.setType(URI.create(PROBLEM_BASE_URL + "/illegal-state"));
    pd.setTitle("Illegal State");
    pd.setInstance(URI.create(request.getRequestURI()));
    return pd;
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ProblemDetail handleAccessDeniedException(
      AccessDeniedException ex, HttpServletRequest request) {
    log.warn("Access denied: {}", ex.getMessage());
    ProblemDetail pd =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.FORBIDDEN,
            "You do not have permission to access this resource. Please check your role and try again.");
    pd.setType(URI.create(PROBLEM_BASE_URL + "/access-denied"));
    pd.setTitle("Access Denied");
    pd.setInstance(URI.create(request.getRequestURI()));
    return pd;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleException(Exception ex, HttpServletRequest request) {
    log.error("Unexpected error occurred", ex);
    ProblemDetail pd =
        ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    pd.setType(URI.create(PROBLEM_BASE_URL + "/internal-error"));
    pd.setTitle("Internal Server Error");
    pd.setInstance(URI.create(request.getRequestURI()));
    return pd;
  }
}
