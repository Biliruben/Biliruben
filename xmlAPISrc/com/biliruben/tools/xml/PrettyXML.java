package com.biliruben.tools.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class PrettyXML extends DefaultHandler {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		/**
		DefaultHandler prettyXml = new PrettyXML();

		LightEntityResolver resolver = new LightEntityResolver();
		try {
			XMLReader reader = reader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
			reader.setEntityResolver(resolver);
			reader.setContentHandler(prettyXml);


		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		 */


		// load the implementation
		DOMImplementationRegistry registry;
		try {
			registry = DOMImplementationRegistry.newInstance();
			DOMImplementationLS lsImpl = (DOMImplementationLS)registry.getDOMImplementation("LS");
			

			// make teh parser
			LSParser parser = lsImpl.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
			DOMConfiguration config = parser.getDomConfig();
			config.setParameter("validate", false);
			config.setParameter("resource-resolver", new NonDTDLSResolver(lsImpl));			

			// make teh doc
			Document doc = parser.parseURI("file:///" + args[0]);

			// right it to te fiel
			LSSerializer serializer = lsImpl.createLSSerializer();
			serializer.getDomConfig().setParameter("format-pretty-print", true);			
			LSOutput output = lsImpl.createLSOutput();
			output.setCharacterStream(new FileWriter(new File(args[0] + ".out")));
			

			serializer.write(doc, output);
		} catch (ClassCastException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	public PrettyXML() {

	}

}
