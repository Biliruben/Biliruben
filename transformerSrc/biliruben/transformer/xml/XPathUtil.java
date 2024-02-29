package biliruben.transformer.xml;

import java.io.IOException;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import biliruben.transformer.Directive;

/**
 * Utility to handle enough XPath operations in order to make {@link biliruben.transformer.handler.DocumentHandler} work
 * @author trey.kirk
 *
 */
public class XPathUtil {
    private XPathFactory factory;
    private static Log log = LogFactory.getLog(XPathUtil.class);
    public static String SELF_PATH = "self::node()";
    
    public XPathUtil() {
        this.factory = XPathFactory.newInstance();
    }

    /**
     * Returns the last token of the xpath expression
     */
    public String getLastToken (String xpathExpression) {
        // a property value might have a slash in it, and thus
        // the xpath /foo/bar [bas = 'her/derp'] would be
        // cause for consternation. Something to come back to.
        String[] tokens = xpathExpression.split("/");
        return tokens[tokens.length - 1];
    }

    /**
     * Returns a property name if the XPath expression ends in '@property'. 
     * TODO: This should be smarter than "just the last property". Feels like we
     * could really expand on modeling out the XPath expression into a structure
     * we could traverse and provide all sorts of information about
     * @param xpathExpression
     * @return
     */
    public String getProperty (String xpathExpression) {
        String lastToken = getLastToken (xpathExpression);
        if (lastToken.startsWith("@")) {
            return lastToken.substring(1);
        }
        return null;
    }

    /**
     * Given an XPath expression, return the XPath expression that corresponds to an
     * Element. If the provided expression already indicates an Element, it may return
     * the same value. Otherwise properties and other indicators of a non-element
     * node are removed
     * @param forXpathExpression
     * @return
     */
    public String getXpathOfElement (String forXpathExpression) {
        if (forXpathExpression == null || "".equals(forXpathExpression.trim())) {
            // shove off w/ that noise
            return forXpathExpression;
        }
        // regex is too smart; be dumb
        String[] tokens = forXpathExpression.split("/");
        String lastToken = tokens[tokens.length - 1];
        // the last token might currently be an Attr "@id"
        //  text: "text()"
        //  or a locator: "element[@id = '123']"
        //
        // We are going to build a pattern that simply matches everything
        // before the non-element data of the last token
        String patternStr = null;
        if (lastToken.startsWith("@") || lastToken.endsWith("()")) {
            // ignore the entire thing
            patternStr = "(.*)/\\Q" + lastToken + "\\E"; 
        } else if (lastToken.contains("[")) {
            // it has (at least) a locator. Just get everything leading up to the first [
            lastToken = lastToken.substring(0, lastToken.indexOf("["));
            patternStr = "(.*/\\Q" + lastToken + "\\E).*"; 
        } else {
            // it's an element; leave it alone
            patternStr = "(.*)";
        }
        Pattern pattern = Pattern.compile(patternStr);
        Matcher m = pattern.matcher(forXpathExpression);
        if (m.matches()) {
            forXpathExpression = m.group(1);
        }
        return forXpathExpression;
    }

    public NodeList findNodes (Node fromNode, String xpath) throws XPathException {
        Object results = factory.newXPath().evaluate(xpath, fromNode, XPathConstants.NODESET);
        NodeList nodeList = (NodeList)results;
        return nodeList;
    }

    public Node findExistingParentNode (Node node, String xpath) throws XPathException {
        String parentXpath = findExistingParentXpath(node, xpath);
        if (parentXpath == null || "".equals(parentXpath.trim())) {
            // there is no parent xpath and this will explode. Just return null
            return null;
        }
        Node parentNode = findNodes(node, parentXpath).item(0);
        return parentNode;
    }

    public String dropLastToken (String fromXpath) {
        String lastTokenPattern = "(.*)/[^/]+$";
        Pattern p = Pattern.compile(lastTokenPattern);
        Matcher matcher = p.matcher(fromXpath);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            // k, just return "/"
            return "/";
        }
    }

    /**
     * Returns the xpath of the closest existing parent in relation to the provided xpath
     * @param node
     * @param xpath
     * @return
     * @throws XPathException 
     */
    public String findExistingParentXpath (Node node, String xpath) throws XPathException {
        // apply the xpath to the node, if a value is found, return the xpath
        // otherwise, drop the last token and loop until there are no further elements
        // in the xpath, in which case the parent must be '/'
        boolean found = false;
        while (!found && !xpath.equals("")) {
            XPathExpression expression = factory.newXPath().compile(xpath);
            found = (Boolean)expression.evaluate(node, XPathConstants.BOOLEAN);
            if (!found) {
                // drop the last token of the xpath
                xpath = dropLastToken(xpath);
            }
        }
        return xpath;
    }

    /**
     * Returns the relative xpath of 'xpath' as compared to 'parentPath'
     * @param xpath
     * @param parentPath
     * @return
     */
    public String diffXpath (String xpath, String parentPath) {
        log.debug("diffXpath: xpath = " + xpath + " parentPath = " + parentPath);
        if (xpath == null) throw new NullPointerException ("xpath cannot be null");
        if (parentPath == null) throw new NullPointerException ("parentPath cannot be null");
        // If either path ends with a /, just strip it off; it makes things less complicated
        if (parentPath.endsWith("/")) {
            parentPath = parentPath.substring(0, parentPath.length() - 1);
        }
        if (xpath.endsWith("/")) {
            xpath = xpath.substring(0, xpath.length() - 1);
        }

        // returns the relative xpath that differentiates xpaths 1 and 2
        if (!xpath.startsWith(parentPath)) {
            throw new IllegalArgumentException (parentPath + " must be a common path of " + xpath);
        }
        String diffXpath = "";
        if (!xpath.equals(parentPath)) {
            diffXpath = xpath.substring(parentPath.length() + 1);
        }
        log.debug("Returning diffXpath = " + diffXpath);
        return diffXpath;
    }

    /**
     * Uses the provided data source to build a precise locator path, incorporating parent
     * location data where required.
     * @param forDirective
     * @param data
     * @param directives
     * @return
     */
    public String buildLocatorPath (Directive forDirective, Map<String, String> data, Map<String, Directive> directives) {
        String myPath = buildLocatorPath(forDirective, data);
        Directive myParent= forDirective.getParent();
        if (myParent!= null) {
            // convert my path into a relative path to my parent
            String parentElementPath = getXpathOfElement(myParent.getPath());
            String myRelativePath = diffXpath(myPath, parentElementPath);
            // RECURSION!!!
            myPath = buildLocatorPath (myParent, data, directives) + "/" + myRelativePath;
        }
        return myPath;
    }

    /**
     * Appends the xpath with the locator syntax for the provided property. propertyName may
     * be bare or preprended with '@'
     */
    public String appendLocatorPath (String xpath, String propertyName, String propertyValue) {
        String bareName = propertyName.replaceFirst("^@", "");
        String propertyXpath = xpath + "[@" + bareName + "='" + propertyValue +"']";
        return propertyXpath;
    }

    /**
     * Builds the locator path for just the directive. Does not incorporate any parent
     * location markers
     * @param forDirective
     * @param data
     * @return
     */
    public String buildLocatorPath (Directive forDirective, Map<String, String> data) {
        log.debug("Building locatorPath (data = " + data + ", forDirective = " + forDirective);
        String xpath = forDirective.getPath();
        String elementPath = getXpathOfElement(xpath);
        String last = getLastToken(xpath);
        if (last.startsWith("@")) {
            // it's a property
            xpath = elementPath;
            xpath = xpath + "[" + last + " = '" + forDirective.deriveValue(data) + "']";
        } else if (last.endsWith("()")) {
            // text
            xpath = elementPath;
            xpath = xpath + "[" + last + " = '" + forDirective.deriveValue(data) + "']";
        }
        log.debug("xpath: " + xpath);
        return xpath;
    }
    /**
     * Given a relative xpath and parent Node, create a hierarchy of element 
     * nodes and attach them to the parentNode. The return value is the element
     * indicated by the relative xpath
     * @param xpath
     * @param document
     * @param parentNode
     * @return Created child node at the end of the hierarchy
     */
    public Node createNodeHierarchy (String xpath, Document document, Node parentNode) {
        // break up the xpath into tokens, create an element for each token and attach
        // that element to the parentNode. Loop with the new node now being the parent
        if (log.isDebugEnabled()) {
            log.debug("createNodeHierarchy: xpath = " + xpath + "; document = " + document + "; parentNode = " + getXpath(parentNode));
        }
        String[] tokens = xpath.split("/");
        for (String token : tokens) {
            log.debug("Creating node for token = " + token);
            Node node = null;
            if (token.equals("text()"))  {
                node = document.createTextNode(null);
                Element parentElement = (Element)parentNode;
                parentElement.appendChild(node);
            } else if (token.startsWith("comment()")) {
                node = document.createComment(null);
                Element parentElement = (Element)parentNode;
                parentElement.appendChild(node);
            } else if (token.startsWith("@")) {
                node = document.createAttribute(token.substring(1));
                Attr att = (Attr)node;
                Element parentElement = (Element)parentNode;
                parentElement.setAttributeNode(att);
            } else {
                node = document.createElement(token);
                parentNode.appendChild(node);
            }
            parentNode = node;
        }
        // by now, the 'parentNode' is the last Element we created. Aka the lowest child.
        // so just return that
        return parentNode;
    }

    /**
     * Builds an xpath expression for the provided node
     * @param forNode
     * @return
     */
    public String getXpath (Node forNode) {
        StringBuilder buff = new StringBuilder();
        Stack<Node> stack = new Stack<Node>();
        while (forNode != null) {
            stack.push(forNode);
            if (forNode instanceof Attr) {
                forNode = ((Attr)forNode).getOwnerElement();
            } else {
                forNode = forNode.getParentNode();
            }
        }
        // Build the path now
        while (!stack.isEmpty()) {
            Node stackNode = stack.pop();
            if (stackNode instanceof Element) {
                buff.append(stackNode.getNodeName()).append("/");
            } else if (stackNode instanceof Attr) {
                buff.append("@" + stackNode.getNodeName());
            } else if (stackNode instanceof Text) {
                buff.append("text()");
            } else if (stackNode instanceof Document) {
                buff.append("/");
            } else {
                // dunno what to do
                throw new IllegalArgumentException(stackNode + " is unsupported");
            }
        }
        if (buff.charAt(buff.length() - 1) == '/') {
            buff.deleteCharAt(buff.length() - 1);
        }
        return buff.toString();
    }

    public XPathExpression compile(String xpath) throws XPathException {
        XPathExpression expression = factory.newXPath().compile(xpath);
        return expression;
    }
    public static void main (String[] args) throws SAXException, IOException, ParserConfigurationException, XPathException {
        XPathUtil util = new XPathUtil();
        String xpath2 = "/sailpoint/Identity/Attributes/Map/entry/value/List/String/text[key = 'fart']";
        System.out.println("elementonly xpath2: " + util.getXpathOfElement(xpath2));
        
        
    }

}
