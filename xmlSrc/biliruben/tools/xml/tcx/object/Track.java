package biliruben.tools.xml.tcx.object;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;

import com.biliruben.tools.xml.XmlUtil;

public class Track extends AbstractTcxObject {

    public static final String ELEMENT_TRACK = "Track";
    private List<TrackPoint> _trackPoints;
    
    public Track(Lap parent) {
        super(parent);
    }

    @Override
    protected void startElement(String elementName, Attributes attributes) {
        if (TrackPoint.ELEMENT_TRACK_POINT.equals(elementName)) {
            if (_trackPoints == null) {
                _trackPoints = new ArrayList<TrackPoint>();
            }
            TrackPoint point = new TrackPoint(this, attributes);
            _current = point;
            _trackPoints.add(point);
        }
    }

    @Override
    protected void endElement(String elementName) {
        if (ELEMENT_TRACK.equals(elementName)) {
            _current = _parent;
        }
    }

    @Override
    protected String toXml(int indent, String indentString) {
        StringBuilder builder = new StringBuilder();
        builder.append(XmlUtil.getIndent(indent, indentString));
        builder.append("<").append(ELEMENT_TRACK).append(">\n");
        indent++;
        
        if (_trackPoints != null) {
            for (TrackPoint point : _trackPoints) {
                builder.append(point.toXml(indent, indentString));
            }
        }
        indent--;
        builder.append(XmlUtil.getIndent(indent, indentString));
        builder.append("</").append(ELEMENT_TRACK).append(">\n");
        return builder.toString();
    }

}
