package biliruben.tools.xml.tcx.object;

import org.xml.sax.Attributes;

import com.biliruben.tools.xml.XmlUtil;

public class HeartRateBpm extends AbstractTcxObject {
    
    public static final String ELEMENT_HEART_RATE_BPM = "HeartRateBpm";
    public static final String ELEMENT_VALUE = "value";
    private String _value;

    public HeartRateBpm(AbstractTcxObject parent) {
        super(parent);
    }

    @Override
    protected void startElement(String elementName, Attributes attributes) {

    }

    @Override
    protected void endElement(String elementName) {
        if (ELEMENT_HEART_RATE_BPM.equals(elementName)) {
            _current = _parent;
        } else if (ELEMENT_VALUE.equals(elementName)) {
            _value = getLastCharacters();
        }
            
    }

    @Override
    protected String toXml(int indent, String indentString) {
        StringBuilder builder = new StringBuilder();
        builder.append(XmlUtil.getIndent(indent, indentString));
        builder.append("<").append(ELEMENT_HEART_RATE_BPM).append(">\n");
        indent++;
        if (_value != null) {
            builder.append(XmlUtil.getIndent(indent, indentString));
            builder.append(XmlUtil.buildCdataTag(ELEMENT_VALUE, _value));
        }
        
        indent--;
        builder.append("</").append(ELEMENT_HEART_RATE_BPM).append(">\n");
        
        return builder.toString();
    }

}
