package biliruben.games.ninjawarz;

public class NinjaBotException extends Exception {

    /**
     * Catch-all exception.  Use mainly for errant command processing
     */
    private static final long serialVersionUID = 3795560486825976267L;

    public NinjaBotException() {
        super();
    }

    public NinjaBotException(String message) {
        super(message);
    }

    public NinjaBotException(Throwable cause) {
        super(cause);
    }

    public NinjaBotException(String message, Throwable cause) {
        super(message, cause);
    }

}
