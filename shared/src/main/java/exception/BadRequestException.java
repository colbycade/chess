package exception;


/**
 * Indicates the http request or its parameters were invalid
 */
public class BadRequestException extends DataAccessException {
    public BadRequestException(String message) {
        super(message);
    }
}