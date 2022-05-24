package rest.data.exceptions;

public class IllegalRequestException extends Exception {
    public IllegalRequestException() {
        super("You do not have the permission to do things like this.");
    }
}
