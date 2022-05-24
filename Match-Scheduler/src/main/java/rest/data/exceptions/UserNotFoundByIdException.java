package rest.data.exceptions;

public class UserNotFoundByIdException extends Exception {
    public static final String MESSAGE = "User not found with the given id";

    public UserNotFoundByIdException() {
        super(MESSAGE);
    }
}
