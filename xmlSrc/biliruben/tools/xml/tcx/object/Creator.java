package biliruben.tools.xml.tcx.object;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;

import com.biliruben.tools.xml.XmlUtil;

public abstract class Creator extends AbstractSource {

    public static final String ELEMENT_CREATOR = "Creator";
    private String _type;

    public Creator(AbstractTcxObject parent, Attributes attributes) {
        super(parent);
        _type = attributes.getValue("xsi:type");
    }
    
    public static Creator buildCreator(AbstractTcxObject parent, Attributes attributes) {
        //xsi:type="Application_t"
        String type = attributes.getValue("xsi:type");
        if ("Application_t".equals(type)) {
            return new Application(parent, attributes);
        } else if ("Device_t".equals(type)) {
            return new Device(parent, attributes);
        } else {
            return null;
        }
    }

    @Override
    protected void endElement(String elementName) {
        super.endElement(elementName);
        if (ELEMENT_CREATOR.equals(elementName)) {
            _current = _parent;
        }
    }
    
    protected abstract String internalToXml(int indent, String indentString);

    @Override
    protected String toXml(int indent, String indentString) {
        StringBuilder builder = new StringBuilder();
        builder.append(XmlUtil.getIndent(indent, indentString));
        builder.append("<").append(ELEMENT_CREATOR).append(" ");
        
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("xsi:type", _type);
        builder.append(XmlUtil.getAttributeString(attributes));
        builder.append(">\n");
        indent++;
        builder.append(super.toXml(indent, indentString));
        builder.append(internalToXml(indent, indentString));
        indent--;
        builder.append(XmlUtil.getIndent(indent, indentString));
        builder.append("</").append(ELEMENT_CREATOR).append(">\n");
        return builder.toString();
    }

}