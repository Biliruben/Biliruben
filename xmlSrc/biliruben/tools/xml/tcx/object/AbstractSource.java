package biliruben.tools.xml.tcx.object;

import com.biliruben.tools.xml.XmlUtil;


public abstract class AbstractSource extends AbstractTcxObject {

    protected static final String ELEMENT_NAME = "Name";
    protected String _name;
    
    public AbstractSource(AbstractTcxObject parent) {
        super(parent);
    }
    
    @Override
    protected void endElement(String elementName) {
        if (ELEMENT_NAME.equals(elementName)) {
            _name = getLastCharacters();
        }
    }

    @Override
    protected String toXml(int indent, String indentString) {
        // "our" toXml method just returns the 'Name' element
        StringBuilder builder = new StringBuilder();
        builder.append(XmlUtil.getIndent(indent, indentString));
        builder.append(XmlUtil.buildCdataTag(ELEMENT_NAME, _name));
        return builder.toString();
    }

}
