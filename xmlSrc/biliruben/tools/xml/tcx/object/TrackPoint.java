package biliruben.tools.xml.tcx.object;

import org.xml.sax.Attributes;

import com.biliruben.tools.xml.XmlUtil;

public class TrackPoint extends AbstractTcxObject {
    
    public static final String ELEMENT_TIME = "Time";
    public static final String ELEMENT_ALTITUDE_METERS = "AltitudeMeters";
    public static final String ELEMENT_DISTANCE_METERS = "DistanceMeters";
    public static final String ELEMENT_CADENCE = "Cadence";
    public static final String ELEMENT_SENSOR_STATE = "SensorState";
    private Position _position;
    private HeartRateBpm _heartRateBpm;
    private String _time;
    private String _altitudeMeters;
    private String _distanceMeters;
    private String _cadence;
    private String _sensorState;
    public static final String ELEMENT_TRACK_POINT = "Trackpoint";
    
    public TrackPoint(Track parent, Attributes attributes) {
        super(parent);
    }

    @Override
    protected void startElement(String elementName, Attributes attributes) {
        
        if (Position.ELEMENT_POSITION.equals(elementName)) {
            _position = new Position(this);
            _current = _position;
        } else if (HeartRateBpm.ELEMENT_HEART_RATE_BPM.equals(elementName)) {
            _heartRateBpm = new HeartRateBpm(this);
            _current = _heartRateBpm;
        }
    }

    @Override
    protected void endElement(String elementName) {
        if (ELEMENT_TRACK_POINT.equals(elementName)) {
            _current = _parent;
        } else if (ELEMENT_TIME.equals(elementName)) {
            _time = getLastCharacters();
        } else if (ELEMENT_ALTITUDE_METERS.equals(elementName)) {
            _altitudeMeters = getLastCharacters();
        } else if (ELEMENT_DISTANCE_METERS.equals(elementName)) {
            _distanceMeters = getLastCharacters();
        } else if (ELEMENT_CADENCE.equals(elementName)) {
            _cadence = getLastCharacters();
        } else if (ELEMENT_SENSOR_STATE.equals(elementName)) {
            _sensorState = getLastCharacters();
        }
    }

    @Override
    protected String toXml(int indent, String indentString) {
        StringBuilder builder = new StringBuilder();
        builder.append(XmlUtil.getIndent(indent, indentString));
        builder.append("<").append(ELEMENT_TRACK_POINT).append(">\n");
        indent++;
        if (_time != null) {
            builder.append(XmlUtil.getIndent(indent, indentString));
            builder.append(XmlUtil.buildCdataTag(ELEMENT_TIME, _time));
        }
        if (_position != null) {
            builder.append(_position.toXml(indent, indentString));
        }
        if (_altitudeMeters != null) {
            builder.append(XmlUtil.getIndent(indent, indentString));
            builder.append(XmlUtil.buildCdataTag(ELEMENT_ALTITUDE_METERS, _altitudeMeters));
        }
        if (_distanceMeters != null) {
            builder.append(XmlUtil.getIndent(indent, indentString));
            builder.append(XmlUtil.buildCdataTag(ELEMENT_DISTANCE_METERS, _distanceMeters));
        }
        /*
        if (_heartRateBpm != null) {
            builder.append(_heartRateBpm.toXml(indent, indentString));
        }
        */
        if (_cadence != null) {
            builder.append(XmlUtil.getIndent(indent, indentString));
            builder.append(XmlUtil.buildCdataTag(ELEMENT_CADENCE, _cadence));
        }
        if (_sensorState != null) {
            builder.append(XmlUtil.getIndent(indent, indentString));
            builder.append(XmlUtil.buildCdataTag(ELEMENT_SENSOR_STATE, _sensorState));
        }
        indent--;
        builder.append(XmlUtil.getIndent(indent, indentString));
        builder.append("</").append(ELEMENT_TRACK_POINT).append(">\n");
        return builder.toString();
    }

}
