package fit.tatakae.infrastructure.web.exception;

import fit.tatakae.domain.exception.*;
import fit.tatakae.infrastructure.web.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

// Perimeter interceptor: no business exception ever reaches the client as a server stack trace.
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException exception,
                                                        HttpServletRequest request) {
        return build(exception.getMessage(), "RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler({DuplicateFriendshipException.class, DuplicateUserException.class})
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException exception,
                                                        HttpServletRequest request) {
        return build(exception.getMessage(), "RESOURCE_ALREADY_EXISTS", HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler({
            FraudulentSessionException.class,
            SelfFriendshipException.class,
            InvalidFriendshipTransitionException.class
    })
    public ResponseEntity<ErrorResponse> handleBusinessRuleViolation(RuntimeException exception,
                                                                     HttpServletRequest request) {
        return build(exception.getMessage(), "BUSINESS_RULE_VIOLATION", HttpStatus.UNPROCESSABLE_ENTITY, request);
    }

    @ExceptionHandler({
            InvalidUserException.class,
            InvalidFriendshipException.class,
            InconsistentSessionException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ErrorResponse> handleInvalidData(RuntimeException exception,
                                                           HttpServletRequest request) {
        return build(exception.getMessage(), "INVALID_REQUEST", HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception,
                                                          HttpServletRequest request) {
        List<String> details = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .sorted()
                .toList();
        return ResponseEntity.badRequest().body(ErrorResponse.of(
                "The request body failed validation",
                "VALIDATION_ERROR",
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                details));
    }

    // An unmapped route is a client mistake, not a server failure: it must never fall through to the 500 handler.
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleUnknownRoute(NoResourceFoundException exception,
                                                            HttpServletRequest request) {
        return build("No endpoint is mapped to " + request.getRequestURI(),
                "ENDPOINT_NOT_FOUND", HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMethod(HttpRequestMethodNotSupportedException exception,
                                                                 HttpServletRequest request) {
        return build("Method " + exception.getMethod() + " is not supported by " + request.getRequestURI(),
                "METHOD_NOT_ALLOWED", HttpStatus.METHOD_NOT_ALLOWED, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception,
                                                            HttpServletRequest request) {
        return build("Parameter " + exception.getName() + " does not accept the value " + exception.getValue(),
                "INVALID_REQUEST", HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException exception,
                                                              HttpServletRequest request) {
        return build("The request body is malformed or uses an unsupported value",
                "MALFORMED_REQUEST", HttpStatus.BAD_REQUEST, request);
    }

    // Last line of defence: unexpected failures are logged by the server, never echoed back to the client.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        return build("An unexpected error occurred while processing the request",
                "INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    private ResponseEntity<ErrorResponse> build(String message, String code, HttpStatus status,
                                                HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(ErrorResponse.of(message, code, status.value(), request.getRequestURI()));
    }
}
