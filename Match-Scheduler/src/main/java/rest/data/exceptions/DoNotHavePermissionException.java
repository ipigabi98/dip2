package rest.data.exceptions;

public class DoNotHavePermissionException extends Exception {
    public DoNotHavePermissionException() {
        super("You do not have permission to this file.");
    }
}
