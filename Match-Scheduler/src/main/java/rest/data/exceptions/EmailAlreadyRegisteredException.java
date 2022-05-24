package rest.data.exceptions;

public class EmailAlreadyRegisteredException extends Exception {
    public static final String MESSAGE = "Someone has already registered with this email.";

    public EmailAlreadyRegisteredException() {
        super(MESSAGE);
    }
}
