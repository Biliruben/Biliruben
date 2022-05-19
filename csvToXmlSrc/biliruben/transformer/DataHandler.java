package biliruben.transformer;

import java.util.Map;

import javax.naming.OperationNotSupportedException;

public interface DataHandler {

    public void flushObject() throws TransformException;

    public void handleOperation(Map<String, String> data, Directive directive) throws TransformException, OperationNotSupportedException;

    public void validateDirective(Directive directive);

    public void setProcessor(DataProcessor processor);

    public void preProcess();
}
