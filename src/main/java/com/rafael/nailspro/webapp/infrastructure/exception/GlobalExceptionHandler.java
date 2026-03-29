package com.rafael.nailspro.webapp.infrastructure.exception;

import io.sentry.Sentry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.List;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<StandardError> business(BusinessException e, HttpServletRequest request) {
        return buildResponse(e, request, HttpStatus.BAD_REQUEST, "Erro de validação", List.of(e.getMessage()));
    }

    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<StandardError> auth(TokenRefreshException e, HttpServletRequest request) {
        return buildResponse(e, request, HttpStatus.UNAUTHORIZED, "Erro de autenticação", List.of(e.getMessage()));
    }

    @ExceptionHandler(LoginException.class)
    public ResponseEntity<StandardError> auth(LoginException e, HttpServletRequest request) {
        return buildResponse(e, request, HttpStatus.UNAUTHORIZED, "Erro de autenticação", List.of(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardError> dtoValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
        List<String> errorMessages = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();

        if (errorMessages.isEmpty()) {
            errorMessages = List.of("Algum dos dados informados não é valido, revise e tente novamente.");
        }

        return buildResponse(e, request, HttpStatus.BAD_REQUEST, "Erro de validação", errorMessages);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<StandardError> singleParamValidation(ConstraintViolationException e, HttpServletRequest request) {
        List<String> errorMessages = e.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .toList();

        if (errorMessages.isEmpty()) {
            errorMessages = List.of("Algum dos dados informados não é valido, revise e tente novamente.");
        }

        return buildResponse(e, request, HttpStatus.BAD_REQUEST, "Erro de validação", errorMessages);
    }

    @ExceptionHandler(ProfessionalBusyException.class)
    public ResponseEntity<StandardError> busy(ProfessionalBusyException e, HttpServletRequest request) {
        return buildResponse(e, request, HttpStatus.CONFLICT, "Erro de validação", List.of(e.getMessage()));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<StandardError> userAlreadyExists(UserAlreadyExistsException e, HttpServletRequest request) {
        return buildResponse(e, request, HttpStatus.BAD_REQUEST, "Usuário já cadastrado", List.of(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardError> genericUnknownError(Exception e, HttpServletRequest request) {
        Sentry.captureException(e);
        return buildResponse(e, request, HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor",
                List.of("Ocorreu um erro inesperado. Contate o suporte."));
    }

    private ResponseEntity<StandardError> buildResponse(
            Exception e,
            HttpServletRequest request,
            HttpStatus status,
            String errorTitle,
            List<String> messages) {

        logError(e, request, status);

        StandardError err = new StandardError(
                Instant.now(),
                status.value(),
                errorTitle,
                messages,
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(err);
    }

    private void logError(Exception e, HttpServletRequest request, HttpStatus status) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        if (status.is5xxServerError()) {
            log.error(
                    "[INTERNAL ERROR]: STATUS: {} - PATH: {} - METHOD: {}",
                    status,
                    uri,
                    method,
                    e
            );
        } else {
            log.error(
                    "[APP-WARN]: STATUS: {} - PATH: {} - METHOD: {}",
                    status,
                    uri,
                    method,
                    e
            );
        }
    }
}