package rest.data.exceptions;

public class TryAgainLaterException extends Exception {
    public static final String MESSAGE = "Something occurred. Please try again later.";

    public TryAgainLaterException() {
        super(MESSAGE);
    }
}
