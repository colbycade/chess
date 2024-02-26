package dataAccess.exception;


/**
 * Indicates the http request was bad
 */
public class BadRequestException extends DataAccessException {
    public BadRequestException(String message) {
        super(message);
    }
}