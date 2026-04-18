package unimag.proyect.api.error;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;

public record ApiError(
    @JsonFormat(shape = JsonFormat.Shape.STRING) OffsetDateTime timestamp,
    int status,
    String error,
    String message,
    String path,
    List<FieldViolation> violations
) {
    public record FieldViolation(String field, String message) {}

    public static ApiError of(HttpStatus status, String message, String path, List<FieldViolation> violations) {
        return new ApiError
        (OffsetDateTime.now(),
        status.value(),
        status.getReasonPhrase(),
        message,
        path,
        violations);
    }
} 
