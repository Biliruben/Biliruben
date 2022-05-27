package biliruben.transformer.handler;

import java.io.Writer;
import java.net.URI;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

import biliruben.transformer.DataProcessor;
import biliruben.transformer.Directive;
import biliruben.transformer.TransformException;

public interface DataHandler {

    public void flushObject() throws TransformException;

    public void handleOperation(Map<String, String> data, Directive directive) throws TransformException, OperationNotSupportedException;

    public void validateDirective(Directive directive);

    public void setProcessor(DataProcessor processor);

    public void preProcess();

    public void setTemplateURI(URI templateUri);

    public void write(Writer writer) throws Exception;
}
