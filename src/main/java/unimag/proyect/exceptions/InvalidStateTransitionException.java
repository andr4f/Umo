package unimag.proyect.exceptions;

public class InvalidStateTransitionException extends RuntimeException {
    public InvalidStateTransitionException(String message) {
        super(message);
    }

    public InvalidStateTransitionException(String entity, String currentState, String targetState) {
        super(entity + " cannot transition from " + currentState + " to " + targetState);
    }
}
