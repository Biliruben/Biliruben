package biliruben.tools.xml.tcx.object;


import org.xml.sax.Attributes;

import com.biliruben.tools.xml.XmlUtil;


public class Device extends Creator {

    public static final String ELEMENT_UNIT_ID = "UnitId";
    public static final String ELEMENT_PRODUCT_ID = "ProductID";
    Version _version;
    String _unitId;
    String _productId;
    
    public Device(AbstractTcxObject parent, Attributes attributes) {
        super(parent, attributes);
    }
    
    @Override
    protected void endElement(String elementName) {
        super.endElement(elementName);
        if (ELEMENT_UNIT_ID.equals(elementName)) {
            _unitId = getLastCharacters();
        } else if (ELEMENT_PRODUCT_ID.equals(elementName)) {
            _productId = getLastCharacters();
        }
    }

    @Override
    protected void startElement(String elementName, Attributes attributes) {
        if (Version.ELEMENT_VERSION.equals(elementName)) {
            _version = new Version(this);
            _current = _version;
        }
    }

    @Override
    protected String internalToXml(int indent, String indentString) {
        StringBuilder builder = new StringBuilder();
        if (_unitId != null) {
            builder.append(XmlUtil.getIndent(indent, indentString));
            builder.append(XmlUtil.buildCdataTag(ELEMENT_UNIT_ID, _unitId));
        }
        
        if (_productId != null) {
            builder.append(XmlUtil.getIndent(indent, indentString));
            builder.append(XmlUtil.buildCdataTag(ELEMENT_PRODUCT_ID, _productId));
        }
        if (_version != null) {
            builder.append(_version.toXml(indent, indentString));
        }

        return builder.toString();
    }

}
