package exception;


/**
 * Indicates the data already exists in the database
 */
public class AlreadyTakenException extends DataAccessException {
    public AlreadyTakenException(String message) {
        super(message);
    }
}