package rest.data.exceptions;

public class UserNotFoundByEmailException extends Exception {
    public static final String MESSAGE = "User not found with the given email";

    public UserNotFoundByEmailException() {
        super(MESSAGE);
    }
}
