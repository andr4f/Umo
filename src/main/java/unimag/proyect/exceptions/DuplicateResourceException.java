package unimag.proyect.exceptions;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String field, String value) {
        super("Duplicate value '" + value + "' for field '" + field + "'");
    }
}
