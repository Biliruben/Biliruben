package biliruben.csvtoxml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XPathUtil {
    private XPathFactory factory;
    
    public XPathUtil() {
        this.factory = XPathFactory.newInstance();
    }

    /**
     * Returns the last token of the xpath expression
     */
    public String getLastToken (String xpathExpression) {
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
        // "the last token, delimited by /, ends in ()"
        String functionPattern = "(.*)/\\w+\\(\\)$";
        // "the last token, delimited by /, starts with @"
        String propertyPattern = "(.*)/@\\w+$";
        List<String> patterns = new ArrayList<String>();
        patterns.add(propertyPattern);
        patterns.add(functionPattern);
        for (String patternStr : patterns) {
            Pattern pattern = Pattern.compile(patternStr);
            Matcher m = pattern.matcher(forXpathExpression);
            if (m.matches()) {
                forXpathExpression = m.group(1);
            }
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
        while (!found) {
            XPathExpression expression = factory.newXPath().compile(xpath);
            found = (Boolean)expression.evaluate(node, XPathConstants.BOOLEAN);
            if (!found) {
                // drop the last token of the xpath
                xpath = dropLastToken(xpath);
            }
        }
        return xpath;
    }

    public String diffXpath (String xpath1, String xpath2) {
        if (xpath1 == null) throw new NullPointerException ("xpath1 cannot be null");
        if (xpath2 == null) throw new NullPointerException ("xpath2 cannot be null");
        // Both paths needs to be reduced to their lowest element paths (they may be 
        //  parameters or text
        String xpath1el = getXpathOfElement(xpath1);
        String last = getLastToken(xpath1);
        String xpath2el = getXpathOfElement(xpath2);

        // returns the relative xpath that differentiates xpaths 1 and 2
        String specificXpath = xpath1el;
        String generalXpath = xpath2el;
        if (xpath2el.length() > xpath1el.length()) {
            specificXpath = xpath2el;
            last = getLastToken(xpath2);
            generalXpath = xpath1el;
        }
        if (specificXpath.equals(generalXpath)) {
            // empty string for 'no difference'
            return "";
        }

        if (!specificXpath.startsWith(generalXpath)) {
            throw new IllegalArgumentException (generalXpath + " must be a common path of " + specificXpath);
        }
        String diffXpath = specificXpath.substring(generalXpath.length() + 1);
        if (last.contains("@") || last.contains("()")) {
            // the last token was a property or text(). That means it got stripped
            // before we did the diff. Tack it back on.
            diffXpath = diffXpath + "/" + last;
        }
        return diffXpath;
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
        String[] tokens = xpath.split("/");
        for (String token : tokens) {
            Node node = null;
            if (token.equals("text()"))  {
                node = document.createTextNode(null);
            } else if (token.startsWith("@")) {
                node = document.createAttribute(token.substring(1));
            } else {
                node = document.createElement(token);
            }
            parentNode.appendChild(node);
            parentNode = node;
        }
        // by now, the 'parentNode' is the last Element we created. Aka the lowest child.
        // so just return that
        return parentNode;
    }

    public static void main (String[] args) throws SAXException, IOException, ParserConfigurationException, XPathException {
        XPathUtil util = new XPathUtil();

        String baseDir = "C:\\GITRoot\\Biliruben\\csvToXmlSrc\\biliruben\\csvtoxml\\";
        String xmlFileName = baseDir + "CSVtoXML.xml";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse (xmlFileName);

        String xpath1 = "/sailpoint/Identity/Links/Link/@identity";

        System.out.println("xpath1: " + xpath1);
        System.out.println("xpath1 parent: " + util.findExistingParentXpath(document, xpath1));
        String xpath2 = "/sailpoint/Identity/Attributes/Map/entry/value/List/String/text()";
        String xpath3 = "/sailpoint/Identity/Links/Link/";
        System.out.println("elementonly xpath2: " + util.getXpathOfElement(xpath2));
        System.out.println("elementOnly xpath3: " + util.getXpathOfElement(xpath3));
        
        
    }

}
