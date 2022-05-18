package biliruben.csvtoxml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPathException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import com.biliruben.util.csv.CSVSource;
import com.biliruben.util.csv.CSVSource.CSVType;
import com.biliruben.util.csv.CSVSourceImpl;

import biliruben.tools.xml.DOMWrapper;


/**
 * Given a CSV file, an XML template, and a set of Directive Properties, create a new XML file with each CSV object
 * represented as an XML object.
 * @author trey.kirk
 *
 */
public class CSVToXML {

    public static void main(String[] args) throws Exception {
        // get the CSV file
        /*
         * CSV files will have multi-valued attibutes either on a single line w/ a secondary delimiter
         * or spanned across mulitiple lines. The CSV reader should return a CSVRecord with all the
         * hard work abstracted
         */
        
        // get the XML template
        /*
         * Just an XML file with directives in the XML file. Some directives will be simple key/value
         * replacements. Others would need to indicate elements that will be repeated for multi-valued
         * attributes. There might be other directives, but I can't think of them right now.
         */
        
        // Read through the CSV file and build XML templates
        /*
         * Here's where we'll actually iterate through our CSVRecord for each data object
         */
        
        // Stream the XML objects to a new XML file
        /*
         * Likely a part of the above step; done as we iterate.
         */
        String baseDir = "C:\\GITRoot\\Biliruben\\csvToXmlSrc\\biliruben\\csvtoxml\\";
        String xmlFileName = baseDir + "CSVtoXML.xml";
        String csvFileName = baseDir + "CSVtoXML.csv";
        String propertiesFileName = baseDir + "CSVtoXML.properties";
        

            File f = new File ("log4j.properties");
            if (f.exists()) {
                PropertyConfigurator.configure("log4j.properties");
            } else {
                // Default properties
                Properties props = new Properties();
                props.setProperty("log4j.appender.stdout","org.apache.log4j.ConsoleAppender");
                props.setProperty("log4j.appender.stdout.Target","System.out");
                props.setProperty("log4j.appender.stdout.layout","org.apache.log4j.PatternLayout");
                props.setProperty("log4j.appender.stdout.layout.ConversionPattern","%d{ISO8601} %5p %t %c{4}:%L - %m%n");
                props.setProperty("log4j.rootLogger","debug,stdout");

                PropertyConfigurator.configure(props);
            }

        CSVToXML thingy = new CSVToXML(propertiesFileName, xmlFileName, csvFileName);
        thingy.process();
    }

    private String csvFile;
    private Map<String, Directive> directives;
    private XPathUtil xpathUtil;
    private Document document;
    private Properties properties;
    private String documentDirective;
    private String xmlTemplateFileName;
    private String lastDocumentValue;

    private static Log log = LogFactory.getLog(CSVToXML.class);

    /**
     * Sets up the CSVToXML processor by reading the Properties file and building the Directives. The
     * XML file is also parsed and the Document is built.
     * @param propertiesFile
     * @param xmlTemplateFile
     * @param csvFile
     * @throws IOException
     */
    public CSVToXML(String propertiesFile, String xmlTemplateFile, String csvFile) throws IOException {
        this.properties = new Properties();
        this.properties.load(new FileReader(getFile(propertiesFile)));
        this.documentDirective = this.properties.getProperty("documentDirective");
        if (this.documentDirective == null) {
            // let's call this a required property. That requirement can be worked around by setting
            // any always-unique column as the document
            throw new NullPointerException ("documentDirective property is required!");
        }
        this.csvFile = csvFile;
        this.xpathUtil = new XPathUtil();
        // get the Document
        this.xmlTemplateFileName = xmlTemplateFile;
        parseXml();
        buildDirectives();

    }

    private void parseXml() throws IOException {
        this.document = DOMWrapper.parseXml(getFile(this.xmlTemplateFileName)).getDomObj();
    }
    
    private File getFile(String fileName) throws FileNotFoundException {
        File file = new File(fileName);
        if (!file.exists()) {
            throw new FileNotFoundException(fileName);
        }
        return file;
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
            try {
                xpathUtil.compile(d.getXPathExpression());
            } catch (XPathException xp) {
                throw new IllegalArgumentException ("Error parsing XPath for " + d, xp);
            }
        }
        // validate the documentDirective. This Directive must have a source set to CSV
        // with a value
        Directive docDirective = directives.get(this.documentDirective);
        if (docDirective == null) {
            throw new IllegalArgumentException ("No documentDirective found for: " + this.documentDirective);
        }
        if (!docDirective.getSource().equals("csv")) {
            throw new IllegalArgumentException ("docDirective must define specify 'csv' as source");
        }
    }

    private void validateParents(Directive directive) {
        TreeSet<String> parents = new TreeSet<String>();
        Directive ogDirective = directive;
        while (directive.getParent() != null) {
            String thisParent = directive.getParent();
            // false means the Set was not modified because the value already existed
            boolean noParent = parents.add(thisParent);
            if (!noParent) {
                throw new IllegalArgumentException ("Illegal circular parent reference for: " + ogDirective);
            }
            directive = directives.get(thisParent);
        }
    }

    private void buildDirectives() throws FileNotFoundException, IOException {
        List<Directive> directiveList = Directive.extractDirectives(this.properties);
        this.directives = new HashMap<String, Directive>();
        for (Directive d : directiveList) {
            this.directives.put(d.getName(), d);
        }
        validateDirectives();
        log.debug("Directives: " + this.directives);
    }

    private Document cloneFromDocument() throws TransformerException, XPathException {
        TransformerFactory tfactory = TransformerFactory.newInstance();
        Transformer tx = tfactory.newTransformer();
        DOMSource src = new DOMSource(this.document);
        DOMResult result = new DOMResult();
        tx.transform(src, result);
        Document clone = (Document)result.getNode();
        // This is a full clone of the source Document, which has template datat in it. So the next
        // step is to REMOVE any node(s?) defined in our properties as the document node
        Node docNode = getDocumentNode(clone);
        // This node isn't likely to be the root node. So we need to remove it from its immediate parent
        // Element
        docNode.getParentNode().removeChild(docNode);
        return clone;
    }

    /*
     * Returns the Node specified by the documentDirective xpath
     */
    private Node getDocumentNode(Document fromDocument) throws XPathException {
        Directive docDirective = directives.get(this.documentDirective);
        String elementXpath = xpathUtil.getXpathOfElement(docDirective.getXPathExpression());
        // null doc nodes are not allowed. So just let an NPE fly if it happens
        // (BAD PROGRAMMER!)
        Node docNode = xpathUtil.findNodes(fromDocument, elementXpath).item(0);
        return docNode;
    }
    /**
     * Processes the CSV file
     * @return Document new Document that contains newly defined Nodes for each object defined
     *                  by the CSV data
     * @throws IOException 
     * @throws FileNotFoundException 
     * @throws XPathException 
     * @throws TransformerException 
     * @throws Exception
     */
    public Document process() throws CSVToXMLException {
        // build the directives
        Document outDoc = null;
        try {
            if (this.directives == null) {
                buildDirectives();
            }

            // build the CSVSource
            CSVSource csvSource = new CSVSourceImpl(getFile(csvFile), CSVType.WithHeader);

            // Construct the new Document
            outDoc = cloneFromDocument();
            // process each line
            Map<String, String> lastData = null;
            for (Map<String, String> data : csvSource) {
                lastData = data;
                log.debug("CSV: " + data);
                // First operation is to update the outputDocument
                updateOutDoc(outDoc, data);
                processData(data, document);
                if (log.isDebugEnabled()) {
                    log.debug("document after CSV processing: ");
                    log.debug(getLogDocument());
                }
                resetDirectives();
            }
            // exhausted the CSV document; Do one last update to the out document
            updateOutDoc (outDoc, lastData, true);
        } catch (Exception e) {
            throw new CSVToXMLException(e);
        }
        return outDoc;
    }

    private void updateOutDoc (Document outputDocument, Map<String, String> data) throws XPathException, IOException {
        updateOutDoc(outputDocument, data, false);
    }

    private void updateOutDoc (Document outputDocument, Map<String, String> data, boolean finalUpdate) throws XPathException, IOException {
        Directive documentDirective = directives.get(this.documentDirective);
        if (this.lastDocumentValue == null) {
            lastDocumentValue = documentDirective.deriveValue(data);
            // nothing to do after this
            return;
        }
        String currentDocumentValue = documentDirective.deriveValue(data);
        if (finalUpdate || !currentDocumentValue.equals(lastDocumentValue)) {
            // we have a new document object. Migrate the previousone to our output

            // Extract the document node from our template Document and adopt into the outDoc
            Node processedNode = getDocumentNode(this.document);
            // this node needs to be migrated from the template document to the output document
            // Get the parent node of document Node's parent element.
            Node parentNode = processedNode.getParentNode();
            String parentPath = xpathUtil.getXpath(parentNode);
            Node newParentNode = xpathUtil.findExistingParentNode(outputDocument, parentPath);
            outputDocument.adoptNode(processedNode);
            newParentNode.appendChild(processedNode);
            // resets the document
            parseXml();
        }

        log.debug("Output document:");
        logDocument(outputDocument);
        lastDocumentValue = currentDocumentValue;
    }

    private void resetDirectives() {
        for (Directive d : this.directives.values()) {
            d.setProcessed(false);
        }
    }

    private String getLogDocument() {
        StringWriter writer = new StringWriter();
        try {
            doWrite(writer, this.document);
        } catch (Exception e) {
            // just log it
            log.error(e.getMessage(), e);
        }
        return writer.toString();
    }

    private void doWrite (Writer writer, Document document) throws Exception {
        DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        DOMImplementationLS lsImpl = (DOMImplementationLS)registry.getDOMImplementation("LS");
        LSSerializer serializer = lsImpl.createLSSerializer();
        serializer.getDomConfig().setParameter("format-pretty-print", true);
        LSOutput output = lsImpl.createLSOutput();
        output.setCharacterStream(writer);
        serializer.write(document, output);
        writer.flush();
        writer.close();
    }

    private void logDocument() {
        logDocument(this.document);
    }

    private void logDocument(Document document) {
        StringWriter writer = new StringWriter();
        try {
            doWrite(writer, document);
            log.debug(writer.toString());
        } catch (Exception e) {
            log.error("Error logging Document", e);
        }
    }

    /*
     * Processes the data Map and updates the document as defined by directives
     */
    private void processData (Map<String, String> data, Document document) throws XPathException {
        // iterate over each directive and apply the operation
        for (Directive directive : this.directives.values()) {
            processDirective (data, document, directive);
        }
    }

    /*
     * Apply the directive to the node using the data source. This may result in applying
     * a parent's directive as well
     */
    private void processDirective (Map<String, String> data, Node node, Directive directive) throws XPathException {
        log.debug("Processing directive: " + directive);
        if (directive.isProcessed()) {
            log.debug("Skipping already processed: " + directive);
            return;
        }
        // Determine the parent node we'll attach or find the target node to or in
        String parent = directive.getParent();
        // If there's a parent defined, find the node relating to the parent. Otherwise, just move on
        // processing against the provided Node
        if (parent != null) {
            /*
             * If parent directive is not null, replace the incoming 'node' with the
             * actual parent node
             */
            /*
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
            Directive parentDirective = this.directives.get(parent);
            if (!parentDirective.isProcessed()) {
                // ensure we've processed the parent first
                processDirective(data, node, parentDirective);
            }
            // The parent has been processed, now find the parent node
            String parentLocatorPath = xpathUtil.buildLocatorPath(parentDirective, data, directives);
            NodeList nodes = findNodes(node, parentLocatorPath);
            // node list; Just assume the first one - don't get technical on this
            if (nodes != null && nodes.getLength() > 0) {
                node = nodes.item(0);
            } else {
                // no parent found! null the node
                node = null;
            }
            // Now that we have the parentNode specifically, process this directive
        } else {
            // in the first clause, we used the parent directive to determine what node to modify. If
            // there is no parent node, we sniff out the best choice from the document
            // find the node to process
            //
            // We have to be careful of Elements that are siblings of what's intended to be a new
            // sibling.
            Node matchedNode = null;
            // Couldn't find an existing node given the xpath. Find the best match.
            if (matchedNode == null) {
                // don't let the name fool you; it will find existing target/sibling nodes, also
                matchedNode = xpathUtil.findExistingParentNode(node, directive.getXPathExpression());
            }
            node = matchedNode;
        }
        // now that we have a parent node, whatever it is, see if we defined a parentElement to attach to
        if (directive.getParentElement() != null) {
            // determine the relative path from 'node' to the parentElement and search in 'node' for that element
            log.debug("Checking parent element: " + directive.getParentElement());
            String nodeXpath = xpathUtil.getXpath(node);
            String relativePath = xpathUtil.diffXpath(directive.getParentElement(), nodeXpath);
            NodeList nodes = xpathUtil.findNodes(node, relativePath);
            if (nodes.getLength() > 0) {
                log.debug("Using parent specified by parentElement");
                node = nodes.item(0);
            }
        }
        // if 'node' is null, then the XML template does not have a place for this
        // directive; Skip it
        if (node == null) {
            log.warn("No valid Node found for: " + directive);
            return;
        }
        processDirectiveInner (data, node, directive);
    }

    private void processDirectiveInner (Map<String, String> data, Node node, Directive directive) throws XPathException {

        // what are we doing here? if node is the Document, the directive spells out where things go
        // if node is a some other node, we've done the foot work to find the target. 
        // 
        // ah, pull all of this up
        
        

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
            logDocument();
        }
        directive.setProcessed(true);
    }

    private void applyUpdate (Map<String, String> data, Node toNode, Directive directive, boolean fallBackToAppend) throws XPathException {
        // toNode is the parent, which might be the parent element or the actual node to update
        // So compare toNode with the directive's xpath Node. If they're the same, update.
        //
        // If not, append
        if (fallBackToAppend) {
            NodeList nodeList = xpathUtil.findNodes(toNode, xpathUtil.buildLocatorPath(directive, data, directives));
            Node directiveNode = null;
            if (nodeList.getLength() > 0) {
                directiveNode = nodeList.item(0);
            }
            if (directiveNode == null || !directiveNode.equals(toNode)) {
                // toNode isn't a match; We need to append
                applyAppend (data, toNode, directive);
                return;
            }
            // else flow through to setNodeValue
        }
        toNode.setNodeValue(directive.deriveValue(data));
    }


    private void applyUpdate (Map<String, String> data, Node toNode, Directive directive) throws XPathException {
        applyUpdate (data, toNode, directive, false);
    }

    private void applyAppend (Map<String, String> data, Node toNode, Directive directive) throws XPathException {
        applyAppend (data, toNode, directive, false);
    }
    /*
     * Add whatever node defined by the xpath to the target node. Any inferred hierarchy will be created as
     * well.
     */
    private void applyAppend (Map<String, String> data, Node toNode, Directive directive, boolean unique) throws XPathException {
        // Ok, toNode is the nearest parent. Or it's the target Node. It might even be what should be
        // a sibling. 
        // Create the relative hierarchy. The one catch is if the unique flag is set, in which case
        // we need to look in 'toNode' for the xpath we're looking for. To do that, we need
        // the relative path of toNode and directive.getXpath()

        String directiveXpath = directive.getXPathExpression();
        String elementOnlyXpath = xpathUtil.getXpathOfElement(directiveXpath);
        String toNodeXpath = xpathUtil.getXpath(toNode);
        /*
         * Now a little magic to figure out the real target Element.
         * - if toNode and directiveXpath are identical AND toNode is an Attr
         *      - we have to create a sibling Element of Attr's owning Element; Unique must be true!
         *      ! toNode becomes toNode.getOwningElement().getParent().
         * - if toNode and directiveXpath are different and toNode is an Attr
         *      - This can't happen. toNode is "the closest parent" we could find; it
         *        cannot be an Attr and a parent of anything that is not exactly the same
         * - if toNode and directiveXpath are different and directiveXpath is an Attr
         *      - Assume the parent correlation has already occurred and toNode is the
         *        intended parent Element; do nothing
         */
        if (toNodeXpath.equals(directiveXpath) && toNode instanceof Attr) {
            toNode = ((Attr)toNode).getOwnerElement().getParentNode();
            toNodeXpath = xpathUtil.getXpath(toNode);
        }
 
        // at this point, toNode must be an Element.
        if (!(toNode instanceof Element)) {
            throw new IllegalArgumentException (toNode + " must be type " + Element.class);
        }

        // if 'unique = true', check if the xpath exists in the parentNode. If it does, we're done
        if (unique) {
            String searchXpath = directiveXpath;
            if (!directiveXpath.equals(elementOnlyXpath)) {
                // use a relative path from the parent path
                String relativePath = xpathUtil.diffXpath(elementOnlyXpath, toNodeXpath);
                if (relativePath.equals("")) {
                    relativePath = "self::node()";
                }
                String lastToken = xpathUtil.getLastToken(directiveXpath);
                searchXpath = relativePath + "[" + lastToken + "='" + directive.deriveValue(data) + "']";
            }
            NodeList nodes = findNodes (toNode, searchXpath);
            if (nodes.getLength() > 0) {
                log.debug("Skipping append for existing xpath: " + directiveXpath);
                return;
            }
        }
        // now I need to create the hierarchy
        String relativeXpath = xpathUtil.diffXpath(directive.getXPathExpression(), toNodeXpath);
        if (!relativeXpath.equals("")) {
            toNode = xpathUtil.createNodeHierarchy(relativeXpath, document, toNode);
        }
        toNode.setNodeValue(directive.deriveValue(data));
    }

    private NodeList findNodes (Node fromNode, String xpathExpression) throws XPathException {
        return findNodes (fromNode, xpathExpression, null, null);
    }

    private NodeList findNodes (Node fromNode, String xpath, String property, String value) throws XPathException {
        NodeList foundNodes = null;
        if (property != null) {
            xpath = this.xpathUtil.getXpathOfElement(xpath);
            // update the the xpath to use the property/value pair as a filter
            xpath = xpath + "[@" + property + "='" + value + "']";
        }
        foundNodes = xpathUtil.findNodes(fromNode, xpath);
        return foundNodes;
    }
}