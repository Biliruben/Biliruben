package biliruben.transformer;

import java.util.Map;

import javax.naming.OperationNotSupportedException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractHandler<N> implements DataHandler {

    private static Log log = LogFactory.getLog(AbstractHandler.class);
    protected DataProcessor processor;

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

    protected void applyAppend(Map<String, String> data, N node, Directive directive, boolean forceUnique) throws OperationNotSupportedException, TransformException {
        throw new OperationNotSupportedException(directive.getOperation() + " is not supported");
    }

    protected void applyUpdate(Map<String, String> data, N node, Directive directive, boolean fallBackToAppend) throws OperationNotSupportedException, TransformException {
        throw new OperationNotSupportedException(directive.getOperation() + " is not supported");
    }

    protected abstract N findTargetNode(Map<String, String> data, Directive directive) throws TransformException;

    protected abstract void logObject();

    protected void applyUpdate(Map<String, String> data, N node, Directive directive) throws OperationNotSupportedException, TransformException {
        applyUpdate (data, node, directive, false);
    }

    protected void applyAppend(Map<String, String> data, N node, Directive directive) throws OperationNotSupportedException, TransformException {
        applyAppend (data, node, directive, false);
    }

    public void setProcessor(DataProcessor processor) {
        this.processor = processor;
    }
}
