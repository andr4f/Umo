package unimag.proyect.exceptions;

public class InactiveEntityException extends RuntimeException {
    public InactiveEntityException(String message) {
        super(message);
    }

    public InactiveEntityException(String entity, Object id) {
        super(entity + " with id " + id + " is not active");
    }
}
