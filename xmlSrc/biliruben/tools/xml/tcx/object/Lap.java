package biliruben.tools.xml.tcx.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;

import com.biliruben.tools.xml.XmlUtil;

public class Lap extends AbstractTcxObject {

    
    public static final String ELEMENT_LAP = "Lap";
    public static final String ELEMENT_TOTAL_TIME_SECONDS = "TotalTimeSeconds";
    public static final String ELEMENT_DISTANCE_METERS = "DistanceMeters";
    public static final String ELEMENT_MAXIMUM_SPEED = "MaximumSpeed";
    public static final String ELEMENT_CALORIES = "Calories";
    public static final String ELEMENT_AVERAGE_HEART_RATE_BPM = "AverageHeartRateBpm";
    public static final String ELEMENT_MAXIMUM_HEART_RATE_BPM = "MaximumHeartRateBpm";
    public static final String ELEMENT_INTENSITY = "Intensity";
    public static final String ELEMENT_CADENCE = "Cadence";
    public static final String ELEMENT_TRIGGER_METHOD = "TriggerMethod";
    public static final String ELEMENT_TRACK = "Track";
    public static final String ELEMENT_NOTES = "Notes"; //cdata
    
    public static final String ATTR_START_TIME = "StartTime";
    private String _startTime;
    private String _totalTimeSeconds;
    private String _distanceMeters;
    private String _maximumSpeed;
    private String _calories;
    private String _notes;
    private String _intensity;
    private String _cadence;
    private String _triggerMethod;
    private List<Track> _tracks;

    public Lap(Attributes attributes, Activity parent) {
        super(parent);
        _startTime = attributes.getValue(ATTR_START_TIME);
    }

    @Override
    protected void startElement(String elementName, Attributes attributes) {
        if (ELEMENT_TRACK.equals(elementName)) {
            if (_tracks == null) {
                _tracks = new ArrayList<Track>();
            }
            Track t = new Track(this);
            _tracks.add(t);
            _current = t;
        }
    }

    @Override
    protected void endElement(String elementName) {
        if (ELEMENT_TOTAL_TIME_SECONDS.equals(elementName)) {
            _totalTimeSeconds = getLastCharacters();
        } else if (ELEMENT_DISTANCE_METERS.equals(elementName)) {
            _distanceMeters = getLastCharacters();
        } else if (ELEMENT_MAXIMUM_SPEED.equals(elementName)) {
            _maximumSpeed = getLastCharacters();
        } else if (ELEMENT_CALORIES.equals(elementName)) {
            _calories = getLastCharacters();
        } else if (ELEMENT_NOTES.equals(elementName)) {
            _notes = getLastCharacters();
        } else if (ELEMENT_INTENSITY.equals(elementName)) {
            _intensity = getLastCharacters();
        } else if (ELEMENT_CADENCE.equals(elementName)) {
            _cadence = getLastCharacters();
        } else if (ELEMENT_TRIGGER_METHOD.equals(elementName)) {
            _triggerMethod = getLastCharacters();
        } else if (ELEMENT_LAP.equals(elementName)) {
            _current = _parent;
        }
    }

    @Override
    protected String toXml(int indent, String indentString) {
        StringBuilder builder = new StringBuilder();
        builder.append(XmlUtil.getIndent(indent, indentString));
        builder.append("<").append(ELEMENT_LAP).append(" ");
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(ATTR_START_TIME, _startTime);
        builder.append(XmlUtil.getAttributeString(attributes));
        builder.append(">\n");
        indent++;
        if (_totalTimeSeconds != null) {
            builder.append(XmlUtil.getIndent(indent, indentString));
            builder.append(XmlUtil.buildCdataTag(ELEMENT_TOTAL_TIME_SECONDS, _totalTimeSeconds));
        }
        
        if (_distanceMeters != null) {
            builder.append(XmlUtil.getIndent(indent, indentString));
            builder.append(XmlUtil.buildCdataTag(ELEMENT_DISTANCE_METERS, _distanceMeters));
        }
        
        if (_maximumSpeed != null) {
            builder.append(XmlUtil.getIndent(indent, indentString));
            builder.append(XmlUtil.buildCdataTag(ELEMENT_MAXIMUM_SPEED, _maximumSpeed));
        }
        
        if (_cadence != null) {
            builder.append(XmlUtil.getIndent(indent, indentString));
            builder.append(XmlUtil.buildCdataTag(ELEMENT_CADENCE, _cadence));
        }
        
        if (_calories != null) {
            builder.append(XmlUtil.getIndent(indent, indentString));
            builder.append(XmlUtil.buildCdataTag(ELEMENT_CALORIES, _calories));
        }
        
        if (_intensity != null) {
            builder.append(XmlUtil.getIndent(indent, indentString));
            builder.append(XmlUtil.buildCdataTag(ELEMENT_INTENSITY, _intensity));
        }
        
        if (_triggerMethod != null) {
            builder.append(XmlUtil.getIndent(indent, indentString));
            builder.append(XmlUtil.buildCdataTag(ELEMENT_TRIGGER_METHOD, _triggerMethod));
        }

        if (_tracks != null && _tracks.size() > 0) {
            for (Track track : _tracks) {
                builder.append(track.toXml(indent, indentString));
            }
        }
        
        if (_notes != null) {
            builder.append(XmlUtil.getIndent(indent, indentString));
            builder.append(XmlUtil.buildCdataTag(ELEMENT_NOTES, _notes));
        }

        indent--;
        builder.append(XmlUtil.getIndent(indent, indentString));
        builder.append("</").append(ELEMENT_LAP).append(">\n");
        return builder.toString();
    }

}
