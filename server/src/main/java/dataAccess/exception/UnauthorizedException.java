package dataAccess.exception;

/**
 * Indicates the invalid auth token provided
 */
public class UnauthorizedException extends DataAccessException {
    public UnauthorizedException(String message) {
        super(message);
    }
}