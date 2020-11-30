package biliruben.tools.xml.tcx.object;

import org.xml.sax.Attributes;

import com.biliruben.tools.xml.XmlUtil;

public class Application extends Creator {
    
    /*
     * <xsd:complexType name="Application_t">
        <xsd:annotation>
            <xsd:documentation>Identifies a PC software application.</xsd:documentation>
        </xsd:annotation>
        <xsd:complexContent>
            <xsd:extension base="AbstractSource_t">
                <xsd:sequence>
                    <xsd:element name="Build" type="Build_t"/>
                    <xsd:element name="LangID" type="LangID_t"/>
                    <xsd:element name="PartNumber" type="PartNumber_t"/>
                </xsd:sequence>
            </xsd:extension>
        </xsd:complexContent>
    </xsd:complexType>
     */
    
    public static final String ELEMENT_LANG_ID = "LangID";
    public static final String ELEMENT_PART_NUMBER = "PartNumber";
    private Build _build;
    private String _lang;
    private String _partNumber;
    
    public Application(AbstractTcxObject parent, Attributes attributes) {
        super(parent, attributes);
    }

    
    @Override
    protected String internalToXml(int indent, String indentString) {
        StringBuilder builder = new StringBuilder();
        if (_build != null) {
            builder.append(_build.toXml(indent, indentString));
        }
        if (_lang!= null) {
            builder.append(XmlUtil.getIndent(indent, indentString));
            builder.append(XmlUtil.buildCdataTag(ELEMENT_LANG_ID, _lang));
        }
        
        if (_partNumber != null) {
            builder.append(XmlUtil.getIndent(indent, indentString));
            builder.append(XmlUtil.buildCdataTag(ELEMENT_PART_NUMBER, _partNumber));
        }
        
        return builder.toString();
    }

    @Override
    protected void startElement(String elementName, Attributes attributes) {
        if (Build.ELEMENT_BUILD.equals(elementName)) {
            _build = new Build(this);
            _current = _build;
        }
    }
    
    @Override
    protected void endElement(String elementName) {
        super.endElement(elementName);
        if (ELEMENT_LANG_ID.equals(elementName)) {
            _lang = getLastCharacters();
        } else if (ELEMENT_PART_NUMBER.equals(elementName)) {
            _partNumber = getLastCharacters();
        }
    }

}
