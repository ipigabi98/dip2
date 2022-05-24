package rest.data.exceptions;

public class InvalidXMLException extends Exception {
    public static final String MESSAGE = "XML is invalid.";

    public InvalidXMLException(String message) {
        super(MESSAGE + " " + message);
    }
}
