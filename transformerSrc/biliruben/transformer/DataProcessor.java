package biliruben.transformer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Given a CSV file, an XML template, and a set of Directive Properties, create a new XML file with each CSV object
 * represented as an XML object.
 * @author trey.kirk
 *
 */
public class DataProcessor {

    private Map<String, Directive> directives;
    private Map properties;
    private String documentDirectiveName;
    private String lastDocumentValue;
    private Iterable<Map<String, String>> dataSource;
    private DataHandler handler;

    private static Log log = LogFactory.getLog(DataProcessor.class);

    public DataProcessor (Map properties, Iterable<Map<String, String>> dataSource, DataHandler handler) {
        handler.setProcessor(this);
        this.dataSource = dataSource;
        this.handler = handler;
        this.properties = properties;
        this.documentDirectiveName = (String)this.properties.get(Constants.PROPERTY_DOCUMENT_DIRECTIVE);
        if (this.documentDirectiveName == null) {
            // let's call this a required property. That requirement can be worked around by setting
            // any always-unique column as the document
            throw new NullPointerException (Constants.PROPERTY_DOCUMENT_DIRECTIVE + " property is required!");
        }
    }

    public Map<String, Directive> getDirectives() {
        return new HashMap<String, Directive>(this.directives);
    }

    public Directive getDocumentDirective() {
        return this.directives.get(this.documentDirectiveName);
    }

    /*
     * Validation includes:
     * - No circular references via the parent attribute
     * - All XPaths compile
     */
    private void validateDirectives() {
        for (Directive d : directives.values()) {
            validateParents(d);
            // nothing to evaluate from the return, only that it works.
            // an invalid XPath will generate an exception
            handler.validateDirective(d);
        }
        // validate the documentDirective.
        Directive docDirective = directives.get(this.documentDirectiveName);
        if (docDirective == null) {
            throw new IllegalArgumentException ("No documentDirective found for: " + this.documentDirectiveName);
        }
    }

    private void validateParents(Directive directive) {
        HashSet<Directive> parents = new HashSet<Directive>();
        Directive ogDirective = directive;
        while (directive.getParent() != null) {
            Directive thisParent = directive.getParent();
            // false means the Set was not modified because the value already existed
            boolean noParent = parents.add(thisParent);
            if (!noParent) {
                throw new IllegalArgumentException ("Illegal circular parent reference for: " + ogDirective);
            }
            directive = directives.get(thisParent.getName());
        }
    }

    private void buildDirectives() throws FileNotFoundException, IOException {
        log.debug("Building directives from: " + this.properties);
        List<Directive> directiveList = Directive.extractDirectives(this.properties);
        this.directives = new HashMap<String, Directive>();
        for (Directive d : directiveList) {
            this.directives.put(d.getName(), d);
        }
        validateDirectives();
        log.debug("Directives: " + this.directives);
    }

    public void process() throws TransformException {
        try {
            if (this.directives == null) {
                buildDirectives();
            }
            this.handler.preProcess();
            Map<String, String> lastData = null;
            for (Map<String, String> data : this.dataSource) {
                if (lastData == null) {
                    lastData = data;
                }
                // first test for flush
                Directive documentDirective = directives.get(this.documentDirectiveName);
                if (this.lastDocumentValue == null) {
                    lastDocumentValue = documentDirective.deriveValue(data);
                }
                String currentDocumentValue = documentDirective.deriveValue(data);
                if (!currentDocumentValue.equals(this.lastDocumentValue)) {
                    this.handler.flushObject();
                }
                // now process the data
                processData(data);
                // reset the directives
                resetDirectives();
            }

            // done iterating the data; Perform one last flush
            this.handler.flushObject();
        } catch (Exception e) {
            throw new TransformException(e);
        }
    }

    private void resetDirectives() {
        for (Directive d : this.directives.values()) {
            d.setProcessed(false);
        }
    }

    private void processData (Map<String, String> data) throws TransformException {
        for (Directive directive : this.directives.values()) {
            try {
                handler.handleOperation(data, directive);
            } catch (OperationNotSupportedException oe) {
                throw new TransformException(oe);
            }
        }
    }
}