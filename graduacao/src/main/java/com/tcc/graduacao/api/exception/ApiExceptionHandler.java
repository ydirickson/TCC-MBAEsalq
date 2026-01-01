package com.tcc.graduacao.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ApiExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

  @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
  public ResponseEntity<ErrorResponse> handleValidation(Exception ex, HttpServletRequest request) {
    var bindingResult = ex instanceof MethodArgumentNotValidException manv
        ? manv.getBindingResult()
        : ((BindException) ex).getBindingResult();

    String detalhes = bindingResult.getFieldErrors().stream()
        .map(ApiExceptionHandler::formatarErroCampo)
        .collect(Collectors.joining("; "));

    ErrorResponse body = ErrorResponse.of(
        HttpStatus.BAD_REQUEST,
        "Parametros inválidos",
        detalhes,
        request.getRequestURI());

    log.warn("Erro de validacao em {} -> {}", request.getRequestURI(), detalhes);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleCorpoInvalido(HttpMessageNotReadableException ex, HttpServletRequest request) {
    String detalhe = ex.getMostSpecificCause() != null
        ? ex.getMostSpecificCause().getMessage()
        : "Corpo da requisicao ilegivel ou incompleto";

    ErrorResponse body = ErrorResponse.of(
        HttpStatus.BAD_REQUEST,
        "Corpo da requisicao invalido",
        detalhe,
        request.getRequestURI());

    log.warn("Corpo invalido em {} -> {}", request.getRequestURI(), detalhe);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenerico(Exception ex, HttpServletRequest request) {
    ErrorResponse body = ErrorResponse.of(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Erro interno ao processar a requisicao",
        ex.getMessage(),
        request.getRequestURI());

    log.error("Erro inesperado em {} -> {}", request.getRequestURI(), ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }

  private static String formatarErroCampo(FieldError error) {
    return error.getField() + ": " + error.getDefaultMessage();
  }

  public record ErrorResponse(Instant timestamp, int status, String error, String message, String path) {
    static ErrorResponse of(HttpStatus status, String errorMessage, String detalhe, String path) {
      return new ErrorResponse(
          Instant.now(),
          status.value(),
          status.getReasonPhrase(),
          detalhe != null ? detalhe : errorMessage,
          path);
    }
  }
}
