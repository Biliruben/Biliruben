package biliruben.tools.xml.tcx.object;

import org.xml.sax.Attributes;

import com.biliruben.tools.xml.XmlUtil;

public class Position extends AbstractTcxObject {

    public static final String ELEMENT_POSITION = "Position";
    public static final String ELEMENT_LATITUDE_DEGREES = "LatitudeDegrees";
    public static final String ELEMENT_LONGITUDE_DEGREES = "LongitudeDegrees";
    private String _lat;
    private String _long;

    public Position(TrackPoint trackPoint) {
        super(trackPoint);
    }

    @Override
    protected void startElement(String elementName, Attributes attributes) {
        
    }

    @Override
    protected void endElement(String elementName) {
        if (ELEMENT_POSITION.equals(elementName)) {
            _current = _parent;
        } else if (ELEMENT_LATITUDE_DEGREES.equals(elementName)) {
            _lat = getLastCharacters();
        } else if (ELEMENT_LONGITUDE_DEGREES.equals(elementName)) {
            _long = getLastCharacters();
        }
    }

    @Override
    protected String toXml(int indent, String indentString) {
        StringBuilder builder = new StringBuilder();
        builder.append(XmlUtil.getIndent(indent, indentString));
        builder.append("<").append(ELEMENT_POSITION).append(">\n");
        indent++;
        if (_lat != null) {
            builder.append(XmlUtil.getIndent(indent, indentString));
            builder.append(XmlUtil.buildCdataTag(ELEMENT_LATITUDE_DEGREES, _lat));
        }
        if (_long != null) {
            builder.append(XmlUtil.getIndent(indent, indentString));
            builder.append(XmlUtil.buildCdataTag(ELEMENT_LONGITUDE_DEGREES, _long));
        }
        indent--;
        builder.append(XmlUtil.getIndent(indent, indentString));
        builder.append("</").append(ELEMENT_POSITION).append(">\n");
        return builder.toString();
    }

}
