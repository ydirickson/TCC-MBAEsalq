package br.com.tcc.assinatura.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
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
  public ResponseEntity<ProblemDetail> handleValidation(Exception ex, HttpServletRequest request) {
    var bindingResult = ex instanceof MethodArgumentNotValidException manv
      ? manv.getBindingResult()
      : ((BindException) ex).getBindingResult();

    String listaErros = bindingResult.getFieldErrors().stream()
      .map(ApiExceptionHandler::formatarErroCampo)
      .collect(Collectors.joining("; "));

    String mensagemGeral = "Erro de validacao de dados";
    String detalhes = mensagemGeral + (listaErros.isEmpty() ? "" : " -> " + listaErros);

    ProblemDetail body = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    body.setTitle(mensagemGeral);
    body.setDetail(listaErros.isEmpty() ? mensagemGeral : listaErros);
    body.setInstance(URI.create(request.getRequestURI()));
    if (!listaErros.isEmpty()) {
      body.setProperty("errors", bindingResult.getFieldErrors().stream()
        .map(ApiExceptionHandler::formatarErroCampo)
        .collect(Collectors.toList()));
    }

    log.info("Erro de validacao em {} -> {}", request.getRequestURI(), detalhes);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ProblemDetail> handleCorpoInvalido(HttpMessageNotReadableException ex, HttpServletRequest request) {
    String detalhe = ex.getMostSpecificCause() != null
        ? ex.getMostSpecificCause().getMessage()
        : "Corpo da requisicao ilegivel ou incompleto";

    ProblemDetail body = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    body.setTitle("Corpo da requisicao invalido");
    body.setDetail(detalhe);
    body.setInstance(URI.create(request.getRequestURI()));

    log.warn("Corpo invalido em {} -> {}", request.getRequestURI(), detalhe);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleGenerico(Exception ex, HttpServletRequest request) {
    ProblemDetail body = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    body.setTitle("Erro interno ao processar a requisicao");
    body.setDetail(ex.getMessage());
    body.setInstance(URI.create(request.getRequestURI()));

    log.error("Erro inesperado em {} -> {}", request.getRequestURI(), ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }

  private static String formatarErroCampo(FieldError error) {
    return error.getField() + ": " + error.getDefaultMessage();
  }
}
