package br.com.granacerta.controller;

import br.com.granacerta.service.RegistroNaoEncontradoException;
import br.com.granacerta.service.RegraNegocioException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converte erros em respostas JSON no formato RFC 9457 (application/problem+json).
 * Os erros padrão do Spring MVC (método não permitido, parâmetro faltando etc.)
 * já são tratados pela classe base.
 */
@RestControllerAdvice
public class TratadorDeErros extends ResponseEntityExceptionHandler {

    @ExceptionHandler(RegistroNaoEncontradoException.class)
    public ResponseEntity<ProblemDetail> naoEncontrado(RegistroNaoEncontradoException e) {
        return problema(HttpStatus.NOT_FOUND, "Registro não encontrado", e.getMessage());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ProblemDetail> regraNegocio(RegraNegocioException e) {
        return problema(HttpStatus.UNPROCESSABLE_ENTITY, "Regra de negócio", e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> integridade(DataIntegrityViolationException e) {
        return problema(HttpStatus.CONFLICT, "Conflito de dados",
                "Não foi possível concluir: este registro está ligado a outros dados ou duplica um já existente.");
    }

    /** Falhas do Bean Validation: devolve { "erros": { campo: mensagem } }. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> erros = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe -> erros.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
        ex.getBindingResult().getGlobalErrors().forEach(ge -> erros.putIfAbsent(ge.getObjectName(), ge.getDefaultMessage()));

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, "Um ou mais campos estão inválidos.");
        pd.setTitle("Dados inválidos");
        pd.setProperty("erros", erros);
        return handleExceptionInternal(ex, pd, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status,
                "Corpo da requisição ilegível: confira se o JSON está bem formado e se datas, números e opções (enums) têm valores válidos.");
        pd.setTitle("Requisição inválida");
        return handleExceptionInternal(ex, pd, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
                                                        HttpStatusCode status, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status,
                "Valor inválido para o parâmetro \"" + ex.getPropertyName() + "\": " + ex.getValue());
        pd.setTitle("Parâmetro inválido");
        return handleExceptionInternal(ex, pd, headers, status, request);
    }

    private static ResponseEntity<ProblemDetail> problema(HttpStatus status, String titulo, String detalhe) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detalhe);
        pd.setTitle(titulo);
        return ResponseEntity.status(status).body(pd);
    }
}
