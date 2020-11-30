package biliruben.xml;

import java.io.File;
import java.io.IOException;
import java.util.Stack;

import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.DOMOutputter;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.biliruben.tools.xml.BreadCrumbHandler;
import com.biliruben.tools.xml.DOMSerializer;
import com.biliruben.tools.xml.LightEntityResolver;


/**
 * 
 * @author trey.kirk
 *
 */
public class SortXmlHandler extends BreadCrumbHandler {
	
	static class SortableElement implements Comparable<Element> {

		
		
		public SortableElement(Element el, String sortingAttr) {
			
		}
		
		public int compareTo(Element yours) {
			// TODO Auto-generated method stub
			return 0;
		}
	}

	/**
	 * Increments the level by 1.
	 *
	 */
	private void addToLevel() {
		addToLevel(1);
	}

	int _level = 0;
	
	/**
	 * Increments the level by increment.  Can't
	 * imagine how this would be used beyond incrementing
	 * by 1, but hey, here it is.
	 * @param increment
	 */
	private void addToLevel(int increment) {
		_level += increment;
	}
	
	Stack<Element> _parents;
	private Document _doc;
	private int _counter;
	private String _systemId;
	private String _publicId;
	private int _lastLevel;
	private String _uniqueAttr;
	private boolean _useNumbers; 

	/**
	 * Call-back method called from {@link XMLReader#parse(InputSource)}
	 * to parse source text data.
	 */
	public void characters(char[] characters, int start, int length) {
		// As we parse elements, we'll sometimes get text between elements: <ref>myTextHere</ref>
		// This call-back method persists that text.

			StringBuffer sb = new StringBuffer(length);
			for (int i = 0; i < length; i++) {
				sb.append(characters[start + i]);
			}

			// ignore text that's all white space
			if (sb.toString().matches("^\\s*$")) {

			} else {
				Element element = (Element) _parents.peek();
				element.addContent(sb.toString());
			}
			sb = null;

	}


	/**
	 * Call-back method called from {@link XMLReader#parse(InputSource)}
	 * to signify the end of the xml document.  This method takes this
	 * objects backed ArrayList that stores the list of files created
	 * by SplitXml and serializes it to ./import.lh
	 */
	public void endDocument() throws SAXException {

		// serialize new Document here

	}

	/**
	 * Call-back method called from {@link XMLReader#parse(InputSource)}
	 * to signify the end of an xml element.  This method determines if
	 * the element that just closed is an element that is one level below
	 * the original highest level.  If so, the completed jdom {@link org.jdom.Document}
	 * is serialized using {@link DOMSerializer#serialize(org.w3c.dom.Document, java.io.OutputStream)}
	 * Note: DOMOutputter is used to convert from org.jdom.Document to
	 * org.w3c.dom.Document.
	 * 
	 * Otherwise, just decrement the integer tracking our cursor level and
	 * continue building the jdom {@link org.jdom.Document}.
	 */
	public void endElement(String namespaceURI, String localName, String qName)
	throws SAXException {
		// Before level is decremented, we check for a Level 2 object.
		// TODO: come back to this

			// Finalize the jdom Document and, serialize, update the file list.
			Element rootEl = _doc.getRootElement();
			_counter++;

			// Hack:  Our DOMSerializer serializes org.w3c.dom.Document objects, not
			// jdom Document objects.  But jdom was nice enough to provide a converter:
			// DOMOutputter#output
			try {
				DOMSerializer serializer = new DOMSerializer();
				DocType type = _doc.getDocType();
				type.setPublicID(getId("PUBLIC"));
				type.setSystemID(getId("SYSTEM"));
				org.w3c.dom.Document myDoc = new DOMOutputter().output(_doc);

					try {
						serializer.serialize(myDoc, file);
					} catch (Exception e) {
						e.printStackTrace();
					}
				

			} catch (JDOMException e) {
				e.printStackTrace();
			}

			_doc = null;
			// parents needs to be "unwound"
			_parents.removeAllElements();

		// Done with this level, move up
		addToLevel(-1);
	}

	/**
	 * 
	 * @param identifier - SYSTEM or PUBLIC
	 * @return The system or public dtd id
	 */
	public String getId(String identifier) {
		// Ya, this is kinda silly.  Return to this and make it more xml worthy
		if (identifier.equalsIgnoreCase("SYSTEM")) {
			return _systemId;
		}

		if (identifier.equalsIgnoreCase("PUBLIC")) {
			return _publicId;
		}

		return null;
	}

	/**
	 * @return the previous element's level
	 */
	private int getLastLevel() {
		return _lastLevel;
	}

	/**
	 * 
	 * @return the current level
	 */
	public int getLevel() {
		return _level;
	}

	/**
	 * Sets a public or system id to the provided dtd.
	 * This method is called by {@link #getDocType(String)} when
	 * building the DocType object.
	 * @param identifier "SYSTEM" or "PUBLIC"
	 * @param dtd DTD to assign to the provided id
	 */
	public void setId(String identifier, String dtd) {
		if (identifier.equalsIgnoreCase("SYSTEM")) {
			_systemId = dtd;
		}

		if (identifier.equalsIgnoreCase("PUBLIC")) {
			_publicId = dtd;
		}
	}

	/**
	 * Sets the last level visited
	 * @param level Last level visited
	 */
	private void setLastLevel(int level) {
		_lastLevel = level;
	}
	
	/**
	 * Returns the DocType object to be incorporated into the jdom Document
	 * being built.
	 * @param element Name of the DocType element
	 * @return DocType object
	 */
	private DocType getDocType(String element) {
		// we need to return the same DocType as the inbound xml, not what we specified
		// as a property.  This way we can use the dummy EntityResolver

		return new DocType(element, getId("PUBLIC"), getId("SYSTEM"));
	}


	/**
	 * Major worker method.  Call-back method called from
	 * {@link XMLReader#parse(InputSource)} as it parses the
	 * original xml file.  For each element started, the level
	 * is examined:<br>
	 * <br>
	 *  - if the level is the 1st level, indicating the root element
	 *  of our original, then the element is ignored.<br>
	 *  - if the level is the 2nd level, it indicates an element we
	 *  want to make as the root element to a new jdom Document.<br>
	 *  - higher levels are nested appropriately in the
	 *  jdom Document.
	 *   
	 */
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		addToLevel();
		Element element = new Element(localName);
		for (int i = 0; i < atts.getLength(); i++) {
			element.setAttribute(atts.getLocalName(i), atts.getValue(i));
		}
		/*
		if (getLevel() == 2) {
			// Level 2, new "top" level object. Create a Document object
			// and commence with the populating.
			_doc = new Document(element, getDocType(localName));

			setLastLevel(getLevel());
			_parents.push(element);
		}
		*/

			// All other elements gotta get served here
			if (getLastLevel() >= getLevel()) {
				// Last level is equal or higher than what level is,
				// that means we have to pop out dead-beat parents until
				// we get to one we like.
				//
				// Ex. if lastLevel == 5 and level == 3, level 2 is our
				// parent element. That element is 1st in our stack of
				// (currently) 4. So (lastLevel - level + 1) parents need
				// to be popped off.
				int levelsToPop = getLastLevel() - getLevel() + 1;
				for (int i = 0; i < levelsToPop; i++) {
					_parents.pop();
				}
			}

			// Meet the parent
			Element parent = (Element) _parents.peek();
			parent.addContent(element);
			_parents.push(element);
			setLastLevel(getLevel());

	}

	/**
	 * @return the _uniqueAttr
	 */
	public String getUniqueAttr() {
		return _uniqueAttr;
	}
	/**
	 * @param attr the _uniqueAttr to set
	 */
	public void setUniqueAttr(String attr) {
		_uniqueAttr = attr;
	}
	/**
	 * @return the _useNumbers
	 */
	public boolean isUsingNumbers() {
		return _useNumbers;
	}
	/**
	 * @param numbers the _useNumbers to set
	 */
	public void setUseNumbers(boolean numbers) {
		_useNumbers = numbers;
	}

	LightEntityResolver _resolver = new LightEntityResolver();
	
	@Override
	public InputSource resolveEntity(String publicId, String systemId)
	throws IOException, SAXException {
		// TODO Auto-generated method stub
		if (_publicId == null || _systemId == null) {
			setId("PUBLIC", publicId);
			setId("SYSTEM", systemId);
			if (systemId.matches("file:.*")) {
				File f = new File (systemId);
				if (!new File(systemId).exists()) {
					setId("SYSTEM", publicId);
				}
			}
		}
		return _resolver.resolveEntity(publicId, systemId);
	}
}
