package biliruben.transformer;

public class TransformException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 42L;

    public TransformException(Exception causedBy) {
        super(causedBy);
    }
}
