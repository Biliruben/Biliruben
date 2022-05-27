package biliruben.transformer;

public class TransformException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 42L;

    public TransformException(Exception causedBy) {
        super(causedBy);
    }

    public TransformException(String message) {
        super(message);
    }

    public TransformException(String message, Throwable cause) {
        super(message, cause);
    }
}
