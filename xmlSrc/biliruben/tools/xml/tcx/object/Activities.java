package biliruben.tools.xml.tcx.object;

import java.util.List;

import org.xml.sax.Attributes;

import com.biliruben.tools.xml.XmlUtil;


public class Activities extends AbstractTcxObject {

    private List<Activity> _activites;
    public static final String ELEMENT_ACTIVITIES = "Activities";

    public Activities(List<Activity> activities) {
        super(null);
        _activites = activities;
    }

    @Override
    protected void startElement(String elementName, Attributes attributes) {
        // not used as a parsed object

    }

    @Override
    protected void endElement(String elementName) {
        // not used as a parsed object

    }

    @Override
    protected String toXml(int indent, String indentString) {

        StringBuilder builder = new StringBuilder();
        builder.append(XmlUtil.getIndent(indent, indentString));

        builder.append("<").append(ELEMENT_ACTIVITIES).append(">\n");
        indent++;
        for (Activity activity : _activites) {
            builder.append(activity.toXml(indent, indentString));
        }

        indent--;
        builder.append("</").append(ELEMENT_ACTIVITIES).append(">\n");
        return builder.toString();
    }

}
