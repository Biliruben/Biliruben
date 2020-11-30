package biliruben.apps.xml;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class SortXml {

	private DocumentBuilder _builder;
	
	public Document sort(File xmlFile) throws SAXException, IOException {
		Document doc = parse(xmlFile);
		doc = sortDocument(doc);
	
		return doc;
	}
	
	private Document parse(File xmlFile) throws SAXException, IOException {
		Document doc = null;
		doc = _builder.parse(xmlFile);
		return doc;
	}
	
	private Document sortDocument(Document doc) {
		// given a list of entities (using xpath?), sort the immidiate sub-entities of each by...?
		//    ...by element name?
		//    ...by attribute values?
		
		
		return doc;
	}
}
