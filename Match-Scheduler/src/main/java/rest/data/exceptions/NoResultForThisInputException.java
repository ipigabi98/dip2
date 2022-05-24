package rest.data.exceptions;

public class NoResultForThisInputException extends Exception {
    public NoResultForThisInputException() {
        super("There is no output for this given input (yet).");
    }
}
