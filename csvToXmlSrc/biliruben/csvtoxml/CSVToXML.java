package biliruben.csvtoxml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import com.biliruben.util.csv.CSVIllegalOperationException;
import com.biliruben.util.csv.CSVSource;
import com.biliruben.util.csv.CSVSource.CSVType;
import com.biliruben.util.csv.CSVSourceImpl;
import com.biliruben.util.csv.CSVSourceObject;

import biliruben.tools.xml.DOMWrapper;


/**
 * Given a CSV file and an XML template, create a new XML file with each CSV object
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
    private String documentColumn;
    private String lastDocumentValue;
    private String xmlTemplateFileName;

    private static Log log = LogFactory.getLog(CSVToXML.class);
    // So I'm writing a processor. Would a SAX Parser fit? It would essentially hold a CSVRecord iterator and for each record, parse
    // the XML template with a SAXParser. As we parse, for any element w/ directives, we create or modify the current Node. Otherwise 
    // we'd just direct the unparsed Node to the new XML object (Document).
    //
    // Using an XML parser sounds good, but that means the directives have to be valid XML. Well, let's also acknowledge I'm just writing
    // an XML transformer that doesn't use XSLT, because now I'm landing on the directives being abstracted from the XML template and making
    // them part of their own input vehicle (properties?)
    //
    // So a DOM parser is probably fine, just need to pull up Document and test if each node has a defined XPath
    
    public CSVToXML(String propertiesFile, String xmlTemplateFile, String csvFile) throws IOException {
        this.properties = new Properties();
        this.properties.load(new FileReader(getFile(propertiesFile)));
        this.documentColumn = this.properties.getProperty("document");
        if (this.documentColumn == null) {
            // let's call this a required property. That requirement can be worked around by setting
            // any always-unique column as the document
            throw new NullPointerException ("document property is required!");
        }
        this.csvFile = csvFile;
        this.xpathUtil = new XPathUtil();
        // get the Document
        this.xmlTemplateFileName = xmlTemplateFile;
        parseXml();

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

    private void buildDirectives() throws FileNotFoundException, IOException {
        List<Directive> directiveList = Directive.extractDirectives(this.properties);
        this.directives = new HashMap<String, Directive>();
        for (Directive d : directiveList) {
            this.directives.put(d.getName(), d);
        }
        log.debug("Directives: " + this.directives);
    }

    public void process() throws Exception {
        // build the directives
        if (this.directives == null) {
            buildDirectives();
        }
        
        // build the CSVSource
        CSVSource csvSource = new CSVSourceImpl(getFile(csvFile), CSVType.WithHeader);

        // process each line
        for (Map<String, String> data : csvSource) {
            log.debug("CSV: " + data);
            // need to test for write first; If the data has moved to the next object,
            // we don't want to munge the document with new shtuff
            writeDocument(data);
            processData(data, document);
            if (log.isDebugEnabled()) {
                log.debug("document after CSV processing: ");
                log.debug(getLogDocument());
            }
        }
        writeDocument(null);
    }

    private String getLogDocument() {
        StringWriter writer = new StringWriter();
        try {
            doWrite(writer);
        } catch (Exception e) {
            // just log it
            log.error(e.getMessage(), e);
        }
        return writer.toString();
    }

    private void doWrite (Writer writer) throws Exception {
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

    private void writeDocument(Map<String, String> data) throws Exception {
        boolean write = false;
        if (data == null) {
            write = true;
        } else {
            String documentValue = data.get(this.documentColumn);
            if (documentValue == null) {
                throw new NullPointerException ("Document column null for data line: " + data);
            }
            if (this.lastDocumentValue == null) {
                this.lastDocumentValue = documentValue;
            }
            if (!this.lastDocumentValue.equals(documentValue)) {
                write = true;
            }
        }
        if (write) {
            // do write
            doWrite(new FileWriter(new File("c:\\temp\\test.xml")));
            parseXml();
        }
    }
    /*
     * Processes the data Map and updates the document as defined by directives
     */
    private void processData (Map<String, String> data, Document document) throws XPathException {
        // iterate over each directive and apply the operation
        for (Directive directive : this.directives.values()) {
            log.debug("Processing directive: " + directive);
            processDirective (data, document, directive);
        }
    }

    /*
     * Apply the directive to the node using the data source. This may result in applying
     * a parent's directive as well
     */
    private void processDirective (Map<String, String> data, Node node, Directive directive) throws XPathException {
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
            String parentXpath = parentDirective.getXPathExpression();
            String propertyName = xpathUtil.getProperty(parentXpath);
            if (propertyName != null) {
                String parentElementPath = xpathUtil.getXpathOfElement(parentXpath);
                NodeList nodes = findNodes(node, parentElementPath, propertyName, parentDirective.deriveValue(data));
                // node list; Just assume the first one - don't get technical on this
                if (nodes != null && nodes.getLength() > 0) {
                    node = nodes.item(0);
                }
            }
            // Now that we have the parentNode specifically, process this directive
        }
        processDirectiveInner (data, node, directive);
    }

    private void processDirectiveInner (Map<String, String> data, Node node, Directive directive) throws XPathException {

        // find the node to process
        NodeList nodes = findNodes (node, directive.getXPathExpression());
        // we only want one node
        Node matchedNode = null;
        if (nodes != null && nodes.getLength() > 0) {
            matchedNode = nodes.item(0);
        }
        if (matchedNode == null) {
            matchedNode = xpathUtil.findExistingParentNode(node, directive.getXPathExpression());
        }

        switch (directive.getOperation()) {
        case append:
            applyAppend (data, matchedNode, directive);
            break;
        case update:
            // we need to update the matchedNode
            applyUpdate (data, matchedNode, directive);
            break;
        case updateOrAppend:
            applyUpdate (data, matchedNode, directive, true);
            break;
        default: throw new IllegalStateException(directive.getOperation() + " is not supported");
        }

    }

    private void applyUpdate (Map<String, String> data, Node toNode, Directive directive, boolean fallBackToAppend) throws XPathException {
        if (toNode == null) {
            if (fallBackToAppend) {
                applyAppend (data, toNode, directive);
            } else {
                // nothing to do here
                return;
            }
        }
        toNode.setNodeValue(directive.deriveValue(data));
    }


    private void applyUpdate (Map<String, String> data, Node toNode, Directive directive) throws XPathException {
        applyUpdate (data, toNode, directive, false);
    }

    private void applyAppend (Map<String, String> data, Node toNode, Directive directive) throws XPathException {
        /*
         * We likely have an xpath that indicates a property or text. From this expath, we first
         * need to determine the owning Element. From there, we need to find the existing parent
         * of the Element's hierarchy. For example, our template XML may look like
         * <xml>
         *    <Identity>
         *       <Attributes/>
         *    </Identity>
         * </xml>
         * 
         * we might have /xml/Identity/Attributes/entry/List/String/text(). That means:
         *  - the owning element is /xml/Identity/Attributes/entry/List/String
         *  - the existing parent element is /xml/Identity/Attributes
         *  - Leaving us to create entry/List/String
         */
        String directiveXpath = directive.getXPathExpression();
        String parentXpath = xpathUtil.findExistingParentXpath(toNode, directiveXpath);
        if (parentXpath.equals(directive.getXPathExpression())) {
            // we actually found a sibling. We need the parent element of this
            // element
            String elementOnlyXpath = xpathUtil.getXpathOfElement(directiveXpath);
            // either we were already an element xpath, or we were an attribute or text. Either way
            // elementOnlyXpath is the Element of the sibling. We need its parent; drop the last token
            parentXpath = xpathUtil.dropLastToken(elementOnlyXpath);
        }
        Node parentNode = xpathUtil.findNodes(toNode, parentXpath).item(0);
        // now I need to create the hierarchy
        String relativeXpath = xpathUtil.diffXpath(parentXpath, directive.getXPathExpression());
        if (!relativeXpath.equals("")) {
            toNode = xpathUtil.createNodeHierarchy(relativeXpath, document, parentNode);
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
            xpath = xpath + "[" + property + "='" + value + "']";
        }
        foundNodes = xpathUtil.findNodes(fromNode, xpath);
        return foundNodes;
    }

    private void poc_process() throws IOException, CSVIllegalOperationException, XPathExpressionException, ClassNotFoundException, InstantiationException, IllegalAccessException, ClassCastException {
        /*
         * For tomorrowTrey:
         * You've got a proof of concept working with this block. Need to now extend it so taht
         * - the Document is parsed and serialized for each CSVObject
         * - serialization to LSSerial is to a String instead of directly to a file... well, or does it?
         * - Do the actual string substitution bit
         */
        if (this.directives == null) {
            buildDirectives();
        }
        CSVSourceObject csvSrc = new CSVSourceObject(getFile(this.csvFile), "name");
        Map<String, Object> csvObj = csvSrc.getNextObject();
        while (csvObj != null) {
            System.out.println("csv: " + csvObj);
            for (Directive directive : this.directives.values()) {
                System.out.println(directive);
                XPath xpath = XPathFactory.newInstance().newXPath();
                XPathExpression expression = xpath.compile(directive.getXPathExpression());
                Object matchedNodesObj = expression.evaluate(document, XPathConstants.NODESET);
                NodeList matchedNodes = (NodeList)matchedNodesObj;
                for (int i = 0; i < matchedNodes.getLength(); i++) {
                    Node node = matchedNodes.item(i);
                    System.out.println("node: " + node);
                    node.setNodeValue("shmegma");
                }
            }
            
            csvObj = csvSrc.getNextObject();
        }
        DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        DOMImplementationLS lsImpl = (DOMImplementationLS)registry.getDOMImplementation("LS");

        LSSerializer serializer = lsImpl.createLSSerializer();
        serializer.getDomConfig().setParameter("format-pretty-print", true);            
        LSOutput output = lsImpl.createLSOutput();
        output.setCharacterStream(new FileWriter(new File("c:\\temp\\test.out")));
        serializer.write(document, output);
    }
    
}