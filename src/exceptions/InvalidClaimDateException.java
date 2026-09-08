package exceptions;

public class InvalidClaimDateException extends Exception {
    private static final long serialVersionUID = 1L;
    public InvalidClaimDateException(String message) {
        super(message);
    }
}
