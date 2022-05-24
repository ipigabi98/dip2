package rest.data.exceptions;

public class RoleDoesNotExistException extends Exception {
    public static final String MESSAGE = "Role does not exist.";

    public RoleDoesNotExistException() {
        super(MESSAGE);
    }
}
