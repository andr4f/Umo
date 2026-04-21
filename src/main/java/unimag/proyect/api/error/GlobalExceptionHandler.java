package unimag.proyect.api.error;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolationException;

import unimag.proyect.exceptions.BusinessException;
import unimag.proyect.exceptions.DuplicateResourceException;
import unimag.proyect.exceptions.InactiveEntityException;
import unimag.proyect.exceptions.InvalidDateRangeException;
import unimag.proyect.exceptions.InvalidStateTransitionException;
import unimag.proyect.exceptions.ResourceNotFoundException;
import unimag.proyect.exceptions.ScheduleConflictException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ─── Custom exceptions ──────────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(ResourceNotFoundException ex, WebRequest req) {
        var body = ApiError.of(
            HttpStatus.NOT_FOUND,
            ex.getMessage(),
            req.getDescription(false),
            List.of()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex, WebRequest req) {
        var body = ApiError.of(
            HttpStatus.UNPROCESSABLE_ENTITY,
            ex.getMessage(),
            req.getDescription(false),
            List.of()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ApiError> handleInvalidStateTransition(InvalidStateTransitionException ex, WebRequest req) {
        var body = ApiError.of(
            HttpStatus.valueOf(422),
            ex.getMessage(),
            req.getDescription(false),
            List.of()
        );
        return ResponseEntity.status(HttpStatus.valueOf(422)).body(body);
    }

    @ExceptionHandler(InactiveEntityException.class)
    public ResponseEntity<ApiError> handleInactiveEntity(InactiveEntityException ex, WebRequest req) {
        var body = ApiError.of(
            HttpStatus.valueOf(422),
            ex.getMessage(),
            req.getDescription(false),
            List.of()
        );
        return ResponseEntity.status(HttpStatus.valueOf(422)).body(body);
    }

    @ExceptionHandler(ScheduleConflictException.class)
    public ResponseEntity<ApiError> handleScheduleConflict(ScheduleConflictException ex, WebRequest req) {
        var body = ApiError.of(
            HttpStatus.CONFLICT,
            ex.getMessage(),
            req.getDescription(false),
            List.of()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> handleDuplicateResource(DuplicateResourceException ex, WebRequest req) {
        var body = ApiError.of(
            HttpStatus.CONFLICT,
            ex.getMessage(),
            req.getDescription(false),
            List.of()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<ApiError> handleInvalidDateRange(InvalidDateRangeException ex, WebRequest req) {
        var body = ApiError.of(
            HttpStatus.BAD_REQUEST,
            ex.getMessage(),
            req.getDescription(false),
            List.of()
        );
        return ResponseEntity.badRequest().body(body);
    }

    // ─── Spring / Jakarta validation exceptions ─────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, WebRequest req) {
        List<ApiError.FieldViolation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ApiError.FieldViolation(fe.getField(), fe.getDefaultMessage()))
                .toList();

        var body = ApiError.of(
            HttpStatus.BAD_REQUEST,
            "Validation failed",
            req.getDescription(false),
            violations
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, WebRequest req) {
        List<ApiError.FieldViolation> violations = ex.getConstraintViolations().stream()
                .map(cv -> new ApiError.FieldViolation(
                        cv.getPropertyPath().toString(),
                        cv.getMessage()))
                .toList();

        var body = ApiError.of(
            HttpStatus.BAD_REQUEST,
            "Constraint violation",
            req.getDescription(false),
            violations
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest req) {
        String message = String.format("Parameter '%s' must be of type '%s'",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

        var body = ApiError.of(
            HttpStatus.BAD_REQUEST,
            message,
            req.getDescription(false),
            List.of()
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParam(MissingServletRequestParameterException ex, WebRequest req) {
        String message = String.format("Required parameter '%s' of type '%s' is missing",
                ex.getParameterName(), ex.getParameterType());

        var body = ApiError.of(
            HttpStatus.BAD_REQUEST,
            message,
            req.getDescription(false),
            List.of()
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, WebRequest req) {
        var body = ApiError.of(
            HttpStatus.METHOD_NOT_ALLOWED,
            "HTTP method '" + ex.getMethod() + "' is not supported for this endpoint",
            req.getDescription(false),
            List.of()
        );
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, WebRequest req) {
        var body = ApiError.of(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "Media type '" + ex.getContentType() + "' is not supported",
            req.getDescription(false),
            List.of()
        );
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex, WebRequest req) {
        var body = ApiError.of(
            HttpStatus.CONFLICT,
            "Data integrity violation: a database constraint was violated",
            req.getDescription(false),
            List.of()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // ─── Fallback genérico ──────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, WebRequest req) {
        var body = ApiError.of(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred",
            req.getDescription(false),
            List.of()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

}
