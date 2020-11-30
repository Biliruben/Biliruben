/**
 * 
 */
package com.biliruben.tools.xml;

import java.io.File;
import java.io.FileWriter;

import org.w3c.dom.DOMConfiguration;

import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSSerializer;

/**
 * @author trey.kirk
 *
 */
public class LSDomSerializer {
	
	public static void main(String[] args){
		
		try {
			// load the implementation
			DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
			DOMImplementationLS lsImpl = (DOMImplementationLS)registry.getDOMImplementation("LS");
			
			// make teh parser
			LSParser parser = lsImpl.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
			DOMConfiguration config = parser.getDomConfig();
			config.setParameter("validate", false);
			
			// make teh doc
			Document doc = parser.parseURI("file:///C:/temp/Templates.xml");
			
			// right it to te fiel
			LSSerializer serializer = lsImpl.createLSSerializer();
			LSOutput output = lsImpl.createLSOutput();
			output.setCharacterStream(new FileWriter(new File("c:/temp/new.xml")));
			serializer.write(doc, output);
			
			
		} catch (Throwable e) {
			throw new RuntimeException (e);
		}
		
	}

}
