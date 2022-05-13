package biliruben.csvtoxml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import com.biliruben.util.csv.CSVIllegalOperationException;
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

    public static void main(String[] args) throws XPathExpressionException, IOException, CSVIllegalOperationException, ClassNotFoundException, InstantiationException, IllegalAccessException, ClassCastException {
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
        String xmlFileName = "c:\\GITRoot\\Biliruben\\spWorkstationSrc\\biliruben\\tools\\CSVtoXML.xml";
        String csvFileName = "c:\\GITRoot\\Biliruben\\spWorkstationSrc\\biliruben\\tools\\CSVtoXML.csv";
        String propertiesFileName = "c:\\GITRoot\\Biliruben\\spWorkstationSrc\\biliruben\\tools\\CSVtoXML.properties";
        //CSVToXML thingy = new CSVToXML(propertiesFileName, xmlFileName, csvFileName);
        //thingy.process();
        Properties properties = new Properties();
        properties.load(new FileReader(new File(propertiesFileName)));
        
        System.out.println(Directive.extractDirectives(properties));
    }

    private String propertiesFile;
    private String xmlTemplateFile;
    private String csvFile;
    private ArrayList<Directive> directives;

    // So I'm writing a processor. Would a SAX Parser fit? It would essentially hold a CSVRecord iterator and for each record, parse
    // the XML template with a SAXParser. As we parse, for any element w/ directives, we create or modify the current Node. Otherwise 
    // we'd just direct the unparsed Node to the new XML object (Document).
    //
    // Using an XML parser sounds good, but that means the directives have to be valid XML. Well, let's also acknowledge I'm just writing
    // an XML transformer that doesn't use XSLT, because now I'm landing on the directives being abstracted from the XML template and making
    // them part of their own input vehicle (properties?)
    //
    // So a DOM parser is probably fine, just need to pull up Document and test if each node has a defined XPath
    
    public CSVToXML(String propertiesFile, String xmlTemplateFile, String csvFile) {
        this.propertiesFile = propertiesFile;
        this.xmlTemplateFile = xmlTemplateFile;
        this.csvFile = csvFile;
    }
    
    private void buildDirectives() throws FileNotFoundException, IOException {
        Properties properties = new Properties();
        properties.load(new FileReader(new File(this.propertiesFile)));
        String directivesCsv = properties.getProperty("directives");
        if (directivesCsv == null) {
            throw new NullPointerException("No directive names provided ('directive' property)");
        }
        CSVSourceImpl csvImpl = new CSVSourceImpl(directivesCsv, CSVType.WithOutHeader);
        String[] names = csvImpl.getNextLine();
        this.directives = new ArrayList<Directive>();
        for (String name : names) {
            Directive directive = new Directive (name, properties);
            this.directives.add(directive);
        }
    }
    
    private File getFile(String fileName) throws FileNotFoundException {
        File file = new File(fileName);
        if (!file.exists()) {
            throw new FileNotFoundException(fileName);
        }
        return file;
    }

    public void process() throws IOException, CSVIllegalOperationException, XPathExpressionException, ClassNotFoundException, InstantiationException, IllegalAccessException, ClassCastException {
        /*
         * For tomorrowTrey:
         * You've got a proof of concept working with this block. Need to now extend it so taht
         * - the Document is parsed and serialized for each CSVObject
         * - serialization to LSSerial is to a String instead of directly to a file... well, or does it?
         * - Do the actual string substitution bit
         */
        buildDirectives();
        Document document = DOMWrapper.parseXml(getFile(this.xmlTemplateFile)).getDomObj();
        CSVSourceObject csvSrc = new CSVSourceObject(getFile(this.csvFile), "name");
        Map<String, Object> csvObj = csvSrc.getNextObject();
        while (csvObj != null) {
            System.out.println("csv: " + csvObj);
            for (Directive directive : this.directives) {
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