package dev.jsamuelap.oikonomiaapi.shared.web;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import dev.jsamuelap.oikonomiaapi.shared.domain.exception.AuthenticationException;
import dev.jsamuelap.oikonomiaapi.shared.domain.exception.ConflictException;
import dev.jsamuelap.oikonomiaapi.shared.domain.exception.DomainException;
import dev.jsamuelap.oikonomiaapi.shared.domain.exception.NotFoundException;

import tools.jackson.databind.exc.ValueInstantiationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
  public static final String BAD_REQUEST_TITLE = "Solicitud invalida";

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ProblemDetail handleInvalidBodyException(HttpMessageNotReadableException ex) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    problem.setTitle(BAD_REQUEST_TITLE);

    String detail = extractCustomMessage(ex).orElse("No se pudo leer el cuerpo de la solicitud");
    problem.setDetail(detail);
    return problem;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleArgumentNotValidException(MethodArgumentNotValidException ex) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    problem.setTitle(BAD_REQUEST_TITLE);
    problem.setDetail("El cuerpo de la solicitud no es valido");

    List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream().map(this::toErrorMap).toList();
    problem.setProperty("errors", errors);

    return problem;
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ProblemDetail handleArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    problem.setTitle(BAD_REQUEST_TITLE);
    String message = String.format("El parámetro '%s' en la URL tiene un tipo de dato incorrecto o formato invalido",
      ex.getName());
    problem.setDetail(message);
    return problem;
  }

  @ExceptionHandler(AuthenticationException.class)
  public ProblemDetail handleAuthenticationException(AuthenticationException ex) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
    problem.setTitle("Error de autorización");
    return problem;
  }

  @ExceptionHandler(NotFoundException.class)
  public ProblemDetail handleNotFound(RuntimeException ex) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    problem.setTitle("Recurso no encontrado");
    return problem;
  }

  @ExceptionHandler(ConflictException.class)
  public ProblemDetail handleConflictException(ConflictException ex) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    problem.setTitle(BAD_REQUEST_TITLE);
    return problem;
  }

  @ExceptionHandler(DomainException.class)
  public ProblemDetail handleDomainException(DomainException ex) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    problem.setTitle(BAD_REQUEST_TITLE);
    return problem;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleUnknownException(Exception ex) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    problem.setTitle("Error del servidor");
    problem.setDetail("Ocurrió un error inesperado");
    return problem;
  }

  private Map<String, String> toErrorMap(FieldError error) {
    return Map.of("field", error.getField(), "message",
      error.getDefaultMessage() != null ? error.getDefaultMessage() : "Valor inválido");
  }

  private Optional<String> extractCustomMessage(Throwable ex) {
    Throwable cause = ex.getCause();

    if (cause instanceof ValueInstantiationException valueInstantiation
      && valueInstantiation.getCause() instanceof IllegalArgumentException illegalArg) {
      return Optional.ofNullable(illegalArg.getMessage());
    }

    return Optional.empty();
  }
}
