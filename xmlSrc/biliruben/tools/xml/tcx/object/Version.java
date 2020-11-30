package biliruben.tools.xml.tcx.object;

import org.xml.sax.Attributes;

import com.biliruben.tools.xml.XmlUtil;

public class Version extends AbstractTcxObject {

    public Version(AbstractTcxObject parent) {
        super(parent);
    }

    public static final String ELEMENT_VERSION = "Version";
    public static final String ELEMENT_VERSION_MAJOR = "VersionMajor";
    public static final String ELEMENT_VERSION_MINOR = "VersionMinor";
    public static final String ELEMENT_BUILD_MAJOR = "BuildMajor";
    public static final String ELEMENT_BUILD_MINOR = "BuildMinor";
    private String _buildMajor;
    private String _buildMinor;
    private String _versionMajor;
    private String _versionMinor;
    

    @Override
    protected void endElement(String elementName) {
        if (ELEMENT_BUILD_MAJOR.equals(elementName)) {
            _buildMajor = getLastCharacters();
        } else if (ELEMENT_BUILD_MINOR.equals(elementName)) {
            _buildMinor = getLastCharacters();
        } else if (ELEMENT_VERSION_MAJOR.equals(elementName)) {
            _versionMajor = getLastCharacters();
        } else if (ELEMENT_VERSION_MINOR.equals(elementName)) {
            _versionMinor = getLastCharacters();
        } else if (ELEMENT_VERSION.equals(elementName)) {
            _current = _parent;
        }
    }

    @Override
    protected String toXml(int indent, String indentString) {
        StringBuilder builder = new StringBuilder();
        builder.append(XmlUtil.getIndent(indent, indentString));
        builder.append("<").append(ELEMENT_VERSION).append(">\n");
        indent++;
        if (_versionMajor != null) {
            builder.append(XmlUtil.getIndent(indent, indentString));
            builder.append(XmlUtil.buildCdataTag(ELEMENT_VERSION_MAJOR, _versionMajor));
        }
        
        if (_versionMinor != null) {
            builder.append(XmlUtil.getIndent(indent, indentString));
            builder.append(XmlUtil.buildCdataTag(ELEMENT_VERSION_MINOR, _versionMinor));
        }
        
        if (_buildMajor != null) {
            builder.append(XmlUtil.getIndent(indent, indentString));
            builder.append(XmlUtil.buildCdataTag(ELEMENT_BUILD_MAJOR, _buildMajor));
        }
        
        if (_buildMinor != null) {
            builder.append(XmlUtil.getIndent(indent, indentString));
            builder.append(XmlUtil.buildCdataTag(ELEMENT_BUILD_MINOR, _buildMinor));
        }
        
        indent--;
        builder.append(XmlUtil.getIndent(indent, indentString));
        builder.append("</").append(ELEMENT_VERSION).append(">\n");
        return builder.toString();
    }

    @Override
    protected void startElement(String elementName, Attributes attributes) {
        // No op
    }

}
