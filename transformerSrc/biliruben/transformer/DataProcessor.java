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

import biliruben.transformer.handler.DataHandler;

/**
 * Processes a data source, normalized as a Iterable of Map<String, String>. Each Map<String, String> is processed
 * against a collection of Directives which describe how to apply that data to a target document. A DataHandler
 * will be tasked with applying the Diretive to the target format. For example, given a CSV file and an XML DataHandler,
 * one may use a template XML file in order to create a series of XML "documents", each populated with data from
 * the CSV file.
 * @author trey.kirk
 * @see {@link CSVtoXML}
 */
public class DataProcessor {

    // mapping of directive to directive names
    private Map<String, Directive> directives;
    // configuration properties. All processor directives are at the base group (not prepended)
    private Map properties;
    // directive that indicates what a "document" is. When the corresponding value for this directive
    // changes, the processor will be flushed
    private String documentDirectiveName;
    // How we keep up with the "document"
    private String lastDocumentValue;
    // the data source we'll be iterating over, provided by the sourceAdapter
    private Iterable<Map<String, String>> dataSource;
    // the DataHandler converting the source to the target document
    private DataHandler handler;

    private static Log log = LogFactory.getLog(DataProcessor.class);

    /**
     * Constructor
     * @param properties Map of properties to configure the DataProcessor
     * @param dataSource Iterable of data to convert
     * @param handler Handler that interprets the incoming data and applies Directives
     */
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

    /**
     * Returns the map of Directives
     * @return
     */
    public Map<String, Directive> getDirectives() {
        return new HashMap<String, Directive>(this.directives);
    }

    /**
     * Returns the Directive that describes how to handle the document value
     * @return
     */
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

    /*
     * Extracts the directive map from the provided properties. The directive properties should be grouped under the 'directive' key
     */
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

    /**
     * Processes the data source
     * @throws TransformException
     */
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
                    lastDocumentValue = currentDocumentValue;
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

    /*
     * Resets the Directives to mark them as not having been processed. This needs to happen whenever
     * we detect that we're building the next document
     */
    private void resetDirectives() {
        for (Directive d : this.directives.values()) {
            d.setProcessed(false);
        }
    }

    /*
     * Processes a single line of data
     */
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