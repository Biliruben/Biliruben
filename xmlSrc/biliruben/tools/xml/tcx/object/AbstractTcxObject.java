package biliruben.tools.xml.tcx.object;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public abstract class AbstractTcxObject extends DefaultHandler {

    private static final String XML_INDENT = "   ";
    protected AbstractTcxObject _current;
    private StringBuilder _cData;
    private StringBuilder _lastCData;
    protected AbstractTcxObject _parent;
    
    protected abstract void startElement(String elementName, Attributes attributes);
    protected abstract void endElement(String elementName);
    protected abstract String toXml(int indent, String indentString);
    
    public AbstractTcxObject(AbstractTcxObject parent) {
        _current = this;
        _parent = parent;
    }
    
    public String toXml() {
        return toXml(0, XML_INDENT);
    }
    
    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        startElement(localName, attributes);
        _cData = new StringBuilder();
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        super.endElement(uri, localName, qName);
        if (_cData.length() > 0) {
            _lastCData = _cData;
        }
        endElement(localName);
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        super.characters(ch, start, length);
        if (_cData == null) {
            _cData = new StringBuilder();
        }
        _cData.append(ch, start, length);
    }
    
    protected String getLastCharacters() {
        if (_lastCData != null) {
            return _lastCData.toString().replace("&", "&amp;").replace("<", "&lt;");
        } else {
            return null;
        }
        
    }
    
    public DefaultHandler getHandler() {
        if (this == _current) {
            return this;
        } else if (_current == _parent) {
            return _parent;
        } else {
            return _current.getHandler();
        }
    }
}
 