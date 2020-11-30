package biliruben.tools.xml.tcx.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;

import com.biliruben.tools.xml.XmlUtil;

public class Activity extends AbstractTcxObject{
    
    public static final String ELEMENT_NOTES = "Notes";
    public static final String ELEMENT_ID = "Id";
    public static final String ELEMENT_ACTIVITY = "Activity";
    public static final String ATTR_SPORT = "Sport";
    private String _sport;
    private String _id;
    private List<Lap> _laps;
    private String _notes;
    private Creator _creator;
    
    public Activity(Attributes attributes) {
        super(null);
        _sport = attributes.getValue(ATTR_SPORT);
    }
    
    @Override
    protected void startElement(String elementName, Attributes attributes) {
        if (Lap.ELEMENT_LAP.equals(elementName)) {
            _current = new Lap(attributes, this);
            if (_laps == null) {
                _laps = new ArrayList<Lap>();
            }
            _laps.add((Lap)_current);
        } else if (Creator.ELEMENT_CREATOR.equals(elementName)) {
            _creator = Creator.buildCreator(this, attributes);
            _current = _creator;
        }
        
    }
    
    @Override
    protected void endElement(String elementName) {
        if (ELEMENT_ID.equals(elementName)) {
            _id = getLastCharacters();
        } else if(ELEMENT_NOTES.equals(elementName)) {
            _notes = getLastCharacters();
        }
    }

    @Override
    protected String toXml(int indent, String indentString) {
        StringBuilder builder = new StringBuilder();
        builder.append(XmlUtil.getIndent(indent, indentString));
        builder.append("<").append(ELEMENT_ACTIVITY).append(" ");
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(ATTR_SPORT, _sport);
        builder.append(XmlUtil.getAttributeString(attributes));
        builder.append(">\n");
        indent++;
        if (_id != null) {
            builder.append(XmlUtil.getIndent(indent, indentString));
            builder.append(XmlUtil.buildCdataTag(ELEMENT_ID, _id));
        }
        if (_laps != null && !_laps.isEmpty()) {
            for (Lap lap : _laps) {
                builder.append(lap.toXml(indent, indentString));
            }
        }
        if (_notes != null) {
            builder.append(XmlUtil.getIndent(indent, indentString));
            builder.append(XmlUtil.buildCdataTag(ELEMENT_NOTES, _notes));
        }
        
        // Training!?
        
        if (_creator != null) {
            builder.append(_creator.toXml(indent, indentString));
        }
        indent--;
        builder.append(XmlUtil.getIndent(indent, indentString));
        builder.append("</").append(ELEMENT_ACTIVITY).append(">\n");
        return builder.toString();
    }

}
