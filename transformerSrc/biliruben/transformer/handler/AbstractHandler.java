package biliruben.transformer.handler;

import java.net.URI;
import java.util.Map;
import java.util.Properties;

import javax.naming.OperationNotSupportedException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import biliruben.transformer.DataProcessor;
import biliruben.transformer.Directive;
import biliruben.transformer.MappedMimeType;
import biliruben.transformer.TransformException;
import biliruben.transformer.Util;
import biliruben.transformer.adapter.AbstractTransformerSourceAdapter;

/**
 * Parent class for DataHandler implementation. Ensures Directives are handled such that parent Directives are handled before
 * children are.
 * @author trey.kirk
 *
 * @param <N> The node type each Directive path is indicating
 */
public abstract class AbstractHandler<N> implements DataHandler, MappedMimeType {

    private static Log log = LogFactory.getLog(AbstractHandler.class);
    protected DataProcessor processor;
    protected URI templateUri;

    @Override
    public void handleOperation(Map<String, String> data, Directive directive) throws OperationNotSupportedException, TransformException {
        log.debug("Processing directive: " + directive);
        if (directive.isProcessed()) {
            log.debug("Skipping already processed: " + directive);
            return;
        }
        try {
            // Determine the parent node we'll attach or find the target node to or in
            Directive parentDirective = directive.getParent();
            // Determine if the parent has been processed first, which it needs to be
            if (parentDirective != null) {
                /*
                 * If parent directive is not null, replace the incoming 'node' with the
                 * actual parent node
                 * 
                 * The parent is a directive with an XPath and value, could be:
                 * /sailpoint/Identity/Links/Link/Attributes/Map/entry/@key
                 * with a source:value of literal:email
                 * 
                 * In which case, I need to first resolve the value and then
                 * use that value to append the parent xpath to find the parent node
                 * 
                 * In this example, /sailpoint/Identity/Links/Link/Attributes/Map/entry/@key
                 * becomes
                 * /sailpoint/Identity/Links/Link/Attributes/Map/entry[key='email']
                 */
                if (!parentDirective.isProcessed()) {
                    // ensure we've processed the parent first
                    handleOperation(data, parentDirective);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Continuing directive: " + directive);
            }
            N node = findTargetNode(data, directive);
            // if 'node' is null, then the handler does not have a place for this
            // directive; Skip it
            if (node == null) {
                log.warn("No valid Node found for: " + directive);
                return;
            }
            // process it
            switch (directive.getOperation()) {
            case append:
                applyAppend (data, node, directive);
                break;
            case update:
                // we need to update the matchedNode
                applyUpdate (data, node, directive);
                break;
            case updateOrAppend:
                applyUpdate (data, node, directive, true);
                break;
            case appendUnique:
                applyAppend (data, node, directive, true);
                break;
            default: throw new IllegalStateException(directive.getOperation() + " is not supported");
            }
            if (log.isDebugEnabled()) {
                log.debug("After directive: " + directive);
                logObject();
            }
            directive.setProcessed(true);
            
        } catch (Exception e) {
            throw new TransformException(e);
        }
    }

    /**
     * Performs an Append operation to the target node
     * @param data Data Map to apply
     * @param node Target node to operate on
     * @param directive Directive describing how to apply the data
     * @param forceUnique When true, will only Append when the target value is not already present. When false, will
     * always append
     * @throws OperationNotSupportedException Thrown when a DataHandler has not implemented the operation
     * @throws TransformException
     */
    protected void applyAppend(Map<String, String> data, N node, Directive directive, boolean forceUnique) throws OperationNotSupportedException, TransformException {
        throw new OperationNotSupportedException(directive.getOperation() + " is not supported");
    }

    /**
     * Performs an Update operation to the target node
     * @param data Data Map to apply
     * @param node Target node to operate on
     * @param directive Directive describing how to apply the data
     * @param fallBackToAppend When true, will append (create) the value when the intended path doesn't exist. When false, will
     * only update existing values.
     * @throws OperationNotSupportedException Thrown when a DataHandler has not implemented the operation
     * @throws TransformException
     */
    protected void applyUpdate(Map<String, String> data, N node, Directive directive, boolean fallBackToAppend) throws OperationNotSupportedException, TransformException {
        throw new OperationNotSupportedException(directive.getOperation() + " is not supported");
    }

    /**
     * Used to determine the target node the directive describes. The DataHandler must determine, based on the
     * path any defined parent, what the intended node to operate on is. The current data set is provided in
     * order to determine a location path that is context aware.
     * @param data
     * @param directive
     * @return
     * @throws TransformException
     */
    protected abstract N findTargetNode(Map<String, String> data, Directive directive) throws TransformException;

    /**
     * When called, the DataHandler is intended to log the current document. The log level is at the implementation's
     * discretion.
     */
    protected abstract void logObject();

    /**
     * performs @{@link #applyAppend(Map, Object, Directive, boolean)} with fallBackToAppend set to false
     */
    protected void applyUpdate(Map<String, String> data, N node, Directive directive) throws OperationNotSupportedException, TransformException {
        applyUpdate (data, node, directive, false);
    }

    /**
     * performs @{@link #applyUpdate(Map, Object, Directive, boolean)} with forceUnique set to false
     */
    protected void applyAppend(Map<String, String> data, N node, Directive directive) throws OperationNotSupportedException, TransformException {
        applyAppend (data, node, directive, false);
    }

    public void setProcessor(DataProcessor processor) {
        this.processor = processor;
    }

    /**
     * Optional method the implementation may define. When not defined, this is a no-op. It will be called by the 
     * {@link DataProcessor} just prior to iterating the data source in order to allow the DataHandler to
     * perform any last minute initialization.
     */
    @Override
    public void preProcess() {
        // Successors aren't required to implement this method; Make this a no-op
    }

    @Override
    public void setTemplateURI(URI templateUri) {
        this.templateUri = templateUri;
        setTemplateURIInner();
    }

    protected abstract void setTemplateURIInner();

    public static Class<? extends AbstractHandler> getHandler(String forMimeType) {
        log.trace("getTransformer: forMimeType = " + forMimeType);
        Class<? extends MappedMimeType> foundClass = Util.getClassForMimeType(forMimeType, AbstractHandler.class);
        Class<? extends AbstractHandler> foundAdapterClass = null;
        if (foundClass != null && AbstractHandler.class.isAssignableFrom(foundClass)) {
            foundAdapterClass = (Class<? extends AbstractHandler>) foundClass;
        }
        log.trace("Returning: " + foundClass);
        return foundAdapterClass;
    }

    public static void main(String[] args) {
        // Default properties
        Properties props = new Properties();
        props.setProperty("log4j.appender.stdout","org.apache.log4j.ConsoleAppender");
        props.setProperty("log4j.appender.stdout.Target","System.out");
        props.setProperty("log4j.appender.stdout.layout","org.apache.log4j.PatternLayout");
        props.setProperty("log4j.appender.stdout.layout.ConversionPattern","%d{ISO8601} %5p %t %c{4}:%L - %m%n");
        props.setProperty("log4j.rootLogger", "debug" + ",stdout");

        PropertyConfigurator.configure(props);
        System.out.println(getHandler("xml"));
    }
}