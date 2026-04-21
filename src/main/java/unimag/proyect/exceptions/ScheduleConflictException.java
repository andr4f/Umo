package unimag.proyect.exceptions;

public class ScheduleConflictException extends RuntimeException {
    public ScheduleConflictException(String message) {
        super(message);
    }

    public ScheduleConflictException(String entity, String detail) {
        super(entity + " schedule conflict: " + detail);
    }
}
