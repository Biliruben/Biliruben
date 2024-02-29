package biliruben.transformer.handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPathException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import com.biliruben.tools.xml.DOMWrapper;

import biliruben.transformer.Constants;
import biliruben.transformer.Directive;
import biliruben.transformer.TransformException;
import biliruben.transformer.xml.XPathUtil;

/**
 * XML object handler. Uses a template XML to layout the overall structure of the target object(s)
 * with properties, elements, and other nodes. Directives may describe new elements as well as
 * updating existing nodes.
 * @author trey.kirk
 *
 */
public class DocumentHandler extends AbstractHandler<Node> {

    /*
     * The Document we'll be building, add to as described by the data source
     */
    private Document workingDocument;
    
    /*
     * Utility object for Xpath operations
     */
    private XPathUtil xpathUtil;
    
    /*
     * The template Document we'll build our resulting objects from
     */
    private Document templateDocument;

    private Node newParentNode;
    
    /*
     * It's better than bad, it's good!
     */
    private static Log log = LogFactory.getLog(DocumentHandler.class);
    

    public static String MIME_TYPE = "text/xml";

    public DocumentHandler() {
        // build templateDocument
        this.xpathUtil = new XPathUtil();
    }

    @Override
    public void configure(Map<String, Object> properties) {
        // No-op
        log.info("configure: no configuration operation defined for " + this);
    }

    @Override
    protected void setTemplateURIInner() {
        try {
            this.templateDocument = parseXml();
        } catch (IOException e) {
            // an IOException should just blow it up
            throw new RuntimeException(e);
        }
    }
    /**
     * Callback from the Processor. Allows us to do any setup work that we couldn't handle in the constructor
     */
    @Override
    public void preProcess() {
        try {
            //this.workingDocument = cloneFromDocument(this.templateDocument);
            this.workingDocument = cloneTemplate();
        } catch (Exception e) {
            // Convert any Exception into a Runtime and bale
            throw new RuntimeException(e);
        }
    }

    /**
     * Called by the processor when a new object has been reached and the current has to be "written"
     * @throws TransformException
     */
    @Override
    public void flushObject() throws TransformException {
        try {
            // Extract the document node from our template Document and adopt into the outDoc
            Node processedNode = getDocumentNode(this.templateDocument);
            // this node needs to be migrated from the template document to the output document
            // Get the parent node of document Node's parent element.
            Node parentNode = processedNode.getParentNode();
            if (this.newParentNode == null) {
                String parentPath = xpathUtil.getXpath(parentNode);
                this.newParentNode = xpathUtil.findExistingParentNode(this.workingDocument, parentPath);
                if (log.isDebugEnabled()) {
                    log.debug("newParentNode = " + newParentNode != null ? xpathUtil.getXpath(newParentNode) : null);
                }
                if (this.newParentNode == null) {
                    // the parent node will be the document
                    this.newParentNode = this.workingDocument;
                }
            }
            /*
             * Imma leave this bullshit here so I remember the pain. Why not 
             * adoptNode? Because adoptNode will discard default values and only preserve
             * the ones the 'processedNode' explicitly set. If one were to have serialized
             * 'processedNode' prior to this operation, all values would suddenly be set
             * and nobody would be none the wiser. In practice, this meant if I didn't have
             * l4j set at DEBUG, the workingDocument would get empty Attribute values for
             * those that weren't explicitly set by a directive.
             * How dumb is that?
             */
            //this.workingDocument.adoptNode(processedNode);
            Node importedNode = this.workingDocument.importNode(processedNode, true);
            newParentNode.appendChild(importedNode);
            // resets the document
            this.templateDocument = parseXml();
        } catch (Exception e) {
            throw new TransformException (e);
        }
    }

    /**
     * Validates the Directive. This handler requires that the 'path' property can
     * successfully be compiled.
     * @param directive
     */
    @Override
    public void validateDirective(Directive directive) {
        try {
            String path = directive.getPath();
            if ("".equals(path.trim())) {
                // "" isn't an xPath that compiles. But we'd like the Directive
                // define a path that is easy to work with when treated as a relative
                // path. So allow the Directive definition to be lax in its syntax
                // and we'll just pivot as required
                path = XPathUtil.SELF_PATH;
            }
            xpathUtil.compile(path);
        } catch (XPathException xp) {
            throw new IllegalArgumentException ("Error parsing XPath for " + directive, xp);
        }
    }
    
    @Override
    protected void applyUpdate (Map<String, String> data, Node toNode, Directive directive) throws TransformException {
        applyUpdate (data, toNode, directive, false);
    }

    @Override
    protected void applyUpdate (Map<String, String> data, Node toNode, Directive directive, boolean fallBackToAppend) throws TransformException {
        try {
            if (fallBackToAppend) {
                // toNode is the parent, which might be the parent element or the actual node to update
                // So compare toNode with the directive's xpath Node. If they're the same, update.
                //
                // If not, append
                NodeList nodeList = xpathUtil.findNodes(toNode, xpathUtil.buildLocatorPath(directive, data, processor.getDirectives()));
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
        } catch (Exception e) {
            throw new TransformException(e);
        }
    }

    @Override
    protected void applyAppend (Map<String, String> data, Node toNode, Directive directive) throws TransformException {
        applyAppend (data, toNode, directive, false);
    }
    /*
     * Add whatever node defined by the xpath to the target node. Any inferred hierarchy will be created as
     * well.
     */
    @Override
    protected void applyAppend (Map<String, String> data, Node toNode, Directive directive, boolean unique) throws TransformException {
        try {
            // Ok, toNode is the nearest parent. Or it's the target Node. It might even be what should be
            // a sibling. 
            // Create the relative hierarchy. The one catch is if the unique flag is set, in which case
            // we need to look in 'toNode' for the xpath we're looking for. To do that, we need
            // the relative path of toNode and directive.getXpath()

            String directiveXpath = directive.getPath();
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
                        relativePath = XPathUtil.SELF_PATH;
                    }
                    String lastToken = xpathUtil.getLastToken(directiveXpath);
                    searchXpath = xpathUtil.appendLocatorPath(relativePath, lastToken, directive.deriveValue(data));
                    //searchXpath = relativePath + "[" + lastToken + "='" + directive.deriveValue(data) + "']";
                }
                NodeList nodes = findNodes (toNode, searchXpath);
                if (nodes.getLength() > 0) {
                    log.debug("Skipping append for existing xpath: " + directiveXpath);
                    return;
                }
            }
            // now I need to create the hierarchy
            String relativeXpath = xpathUtil.diffXpath(directive.getPath(), toNodeXpath);
            if (!relativeXpath.equals("")) {
                toNode = xpathUtil.createNodeHierarchy(relativeXpath, templateDocument, toNode);
            }
            toNode.setNodeValue(directive.deriveValue(data));
        } catch (Exception e) {
            throw new TransformException(e);
        }
    }

    private NodeList findNodes (Node fromNode, String xpathExpression) throws XPathException {
        return findNodes (fromNode, xpathExpression, null, null);
    }

    private NodeList findNodes (Node fromNode, String xpath, String property, String value) throws XPathException {
        if (log.isDebugEnabled()) {
            log.debug("findNodes: fromNode = " + xpathUtil.getXpath(fromNode) + "\n\txpath = " + xpath + "\n\tproperty = " + property + "\n\tvalue = " + value);
        }
        NodeList foundNodes = null;
        if (property != null) {
            xpath = this.xpathUtil.getXpathOfElement(xpath);
            // update the the xpath to use the property/value pair as a filter
            xpath = xpathUtil.appendLocatorPath(xpath, property, value);
            //xpath = xpath + "[@" + property + "='" + value + "']";
        }
        foundNodes = xpathUtil.findNodes(fromNode, xpath);
        if (log.isDebugEnabled()) {
            StringBuffer buff = new StringBuffer();
            for (int i = 0; i < foundNodes.getLength(); i++) {
                Node n = foundNodes.item(i);
                buff.append(xpathUtil.getXpath(n)).append("; ");
            }
            log.debug("Returning (" + foundNodes.getLength() + " nodes): " + buff.toString());
        }
        return foundNodes;
    }

    
    /*
     * Determine the most appropriate target Node using an input Node as the starting point
     */
    protected Node findTargetNode(Map<String, String> data, Directive directive) throws TransformException {
        Node node = this.templateDocument;
        try {
            // If there's a parent defined, find the node relating to the parent. Otherwise, just move on
            // processing against the provided Node
            Directive parentDirective = directive.getParent();
            if (parentDirective != null) {
                // Find the parent node
                String parentLocatorPath = xpathUtil.buildLocatorPath(parentDirective, data, this.processor.getDirectives());
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
                    matchedNode = xpathUtil.findExistingParentNode(node, directive.getPath());
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
                    log.debug("Using parent specified by " + Constants.PROPERTY_PARENT_PATH);
                    node = nodes.item(0);
                } else {
                    log.debug("No nodes found with " + Constants.PROPERTY_PARENT_PATH);
                    log.debug("\n\tnodeXpath = " + nodeXpath + "\n\trelativePath = " + relativePath);
                }
            }
        } catch (Exception e) {
            throw new TransformException(e);
        }
        return node;
    }

    private void logDocument(Node document) {
        StringWriter writer = new StringWriter();
        try {
            doWrite(writer, document);
            log.debug(writer.toString());
        } catch (Exception e) {
            log.error("Error logging Document", e);
        }
    }

    private String getLogDocument(Node document) {
        StringWriter writer = new StringWriter();
        try {
            //doWrite(writer, this.templateDocument);
            doWrite(writer, document);
        } catch (Exception e) {
            // just log it
            log.error(e.getMessage(), e);
        }
        return writer.toString();
    }

    private Document parseXml() throws IOException {
        // We don't have a file name, we have a URI
        File file = new File(this.templateUri);
        if (!file.exists()) {
            throw new FileNotFoundException(this.templateUri.getPath());
        }

        return DOMWrapper.parseXml(file).getDomObj();
    }

    public void write(Writer writer) throws Exception {
        purgeWhitespace(this.workingDocument);
        doWrite (writer, this.workingDocument);
    }

    /*
     * Aids in better formatting the Document before serialization. Evaluates all
     * text() nodes and truncates the value when it is only whitespace
     */
    private void purgeWhitespace(Node node) {
        NodeList children = node.getChildNodes();
        int length = children.getLength();
        for (int i = 0; i < length; i++) {
            Node child = children.item(i);
            if (child instanceof Text) {
                Text text = (Text)child;
                String nodeValue = text.getNodeValue();
                if (nodeValue != null && "".equals(nodeValue.trim())) {
                    text.setNodeValue("");
                }
            } else {
                // little recursion never hurt nobody
                purgeWhitespace(child);
            }
        }
    }

    private void doWrite (Writer writer, Node node) throws Exception {
        DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        DOMImplementationLS lsImpl = (DOMImplementationLS)registry.getDOMImplementation("LS");
        LSSerializer serializer = lsImpl.createLSSerializer();
        serializer.getDomConfig().setParameter("format-pretty-print", true);
        LSOutput output = lsImpl.createLSOutput();
        output.setCharacterStream(writer);
        serializer.write(node, output);
        writer.flush();
        writer.close();
    }

    // Clones the Template XML by parsing it and removing the document object (i.e. the 
    // template data). The result is an "empty" document
    private Document cloneTemplate() throws XPathException, IOException, TransformException {
        Document clone = parseXml();
        // This is a full clone of the source Document, which has template datat in it. So the next
        // step is to REMOVE any node(s?) defined in our properties as the document node
        Node docNode = getDocumentNode(clone);
        // This node isn't likely to be the root node. So we need to remove it from its immediate parent
        // Element
        docNode.getParentNode().removeChild(docNode);
        // We would like to preserve the doctype
        return clone;
    }

    /*
     * Defunct method but has some use to hang around for now. Describes how to clone a Document but has
     * the limitation of not conveying the original DocType with it.
     */
    private Document cloneFromDocument(Document document) throws TransformerException, XPathException, TransformException {
        TransformerFactory tfactory = TransformerFactory.newInstance();
        Transformer tx = tfactory.newTransformer();
        DOMSource src = new DOMSource(document);
        DOMResult result = new DOMResult();
        tx.transform(src, result);
        Document clone = (Document)result.getNode();
        DocumentType docType = document.getDoctype();
        src = new DOMSource(docType);
        result = new DOMResult();
        tx.transform(src, result);
        /*
         * This doesn't work
         *
        DocumentType cloneDocType = (DocumentType)result.getNode();
        clone.adoptNode(cloneDocType);
        */
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
    private Node getDocumentNode(Document fromDocument) throws XPathException, TransformException {
        String elementXpath = xpathUtil.getXpathOfElement(this.processor.getDocumentDirective().getPath());
        NodeList nodeList = xpathUtil.findNodes(fromDocument, elementXpath);
        if (nodeList.getLength() == 0) {
            throw new TransformException(elementXpath + " not found in document");
        }
        Node docNode = xpathUtil.findNodes(fromDocument, elementXpath).item(0);
        return docNode;
    }

    @Override
    protected void logObject() {
        logDocument(this.templateDocument);
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }
}
