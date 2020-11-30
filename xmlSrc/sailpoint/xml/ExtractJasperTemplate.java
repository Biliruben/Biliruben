package sailpoint.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.biliruben.tools.xml.XMLProcessor;
import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionLegend;
import com.sun.org.apache.xerces.internal.dom.DOMInputImpl;

/**
 * Tool to parse exported JasperTemplate XMl files and extract the native jasperReport XML object.  Useful
 * to extract existing templates from IIQ and put into a format that can be loaded into iReport or just directly
 * edited with minimal complications.
 * 
 * Required SailPoint Services APIs:
 * 	GetOpts 1.2
 * 	XmlAPI 1.1
 * @author trey.kirk
 *
 */
public class ExtractJasperTemplate extends DefaultHandler {

	private static GetOpts _opts;
	private static final String OPT_DESIGN_ELEMENT = "element";
	private static final String OPT_JASPER_XML = "xml";
	
	public static void init(String[] args) {
		
		_opts = new GetOpts(ExtractJasperTemplate.class);
		
		OptionLegend legend = new OptionLegend(OPT_JASPER_XML);
		legend.setRequired(true);
		legend.setDescription("XML file with embedded JasperResult XML");
		_opts.addLegend(legend);
		
		legend = new OptionLegend (OPT_DESIGN_ELEMENT);
		legend.setRequired(false);
		legend.setDescription("Element containing the embeded xml");
		_opts.addLegend(legend);
		
		_opts.parseOpts(args);
	}
	
	/**
	 * @param args
	 * @throws SAXException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws SAXException, IOException {
		init(args);
		
		ExtractJasperTemplate myHandler = new ExtractJasperTemplate();
		String xmlFileName = _opts.getStr(OPT_JASPER_XML);
		String element = _opts.getStr(OPT_DESIGN_ELEMENT);
		if (element != null) {
			myHandler.setElementName(element);
		}
		XMLProcessor processor = new XMLProcessor(xmlFileName, myHandler);
		processor.parse();
	}
	
	private boolean _capturing = false;
	private final String JASPER_ELEMENT = "DesignXml";
	private String _element = JASPER_ELEMENT;
	private StringBuffer _characterBuff;
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		super.characters(ch, start, length);
		if (_capturing) {
			_characterBuff.append(ch, start, length);
		}
	}
	
	private void serializeXml () throws ClassCastException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
		// load the implementation
		DOMImplementationRegistry registry;
		
			registry = DOMImplementationRegistry.newInstance();
			DOMImplementationLS lsImpl = (DOMImplementationLS)registry.getDOMImplementation("LS");

			// make teh parser
			LSParser parser = lsImpl.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
			DOMConfiguration config = parser.getDomConfig();
			config.setParameter("validate", false);

			// make teh doc
			//Document doc = parser.parseURI("file:///" + args[0]);
			LSInput input = new DOMInputImpl("", "", "", new StringReader(_characterBuff.toString()), "UTF-8");
			Document doc = parser.parse(input);	
			
			String name = doc.getDocumentElement().getAttribute("name");
			
			// rite it to te fiel
			LSSerializer serializer = lsImpl.createLSSerializer();
			
			DOMConfiguration serializerConfig = serializer.getDomConfig();
			serializerConfig.setParameter("format-pretty-print", true);
			
			LSOutput output = lsImpl.createLSOutput();
			output.setEncoding("UTF-8");
			output.setCharacterStream(new FileWriter(new File("JasperTemplate_" + name + ".jrxml")));
			
			serializer.write(doc, output);
	}
	
	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		super.endElement(uri, localName, name);
		if (_element.equals(localName)) {
			// and we're exiting, stop catpuring
			_capturing = false;
			try {
				serializeXml();
			} catch (ClassCastException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setElementName (String element) {
		if (element != null) {
			_element = element;
		}
	}
	
	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, name, attributes);
		if (_element.equals(localName)) {
			// we've entered the element who's text is embedded xml, start capturing
			_capturing = true;
			_characterBuff = new StringBuffer(2048);
		}
	}
}
