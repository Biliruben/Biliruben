package com.biliruben.tools.xml;

import java.util.Map;

import org.xml.sax.Attributes;

/**
 * Utility methods that I tend to re-implement over and over
 * @author trey.kirk
 *
 */
public class XmlUtil {

    public static String getIndent(int indent, String indentString) {
        StringBuilder indentStringBuilder = new StringBuilder();
        for (;indent > 0; indent--) {
            indentStringBuilder.append(indentString);
        }
        return indentStringBuilder.toString();
    }

    public static String getAttributeString (Map<String, String> attributes) {
        StringBuilder builder = new StringBuilder();
        if (attributes != null) {
            for (String key : attributes.keySet()) {
                String value = attributes.get(key);
                if (value != null && !"".equals(value)) {
                    builder.append(key).append("=\"").append(value.trim()).append("\" ");
                }
            }
        }
        return builder.toString();
    }
        
    public static String buildCdataTag(String tag, String cData) {
        StringBuilder builder = new StringBuilder();
        builder.append("<").append(tag).append(">").append(cData).append("</").append(tag).append(">\n");
        return builder.toString();
    }
}
 