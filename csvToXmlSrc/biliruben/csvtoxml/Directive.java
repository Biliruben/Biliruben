package biliruben.csvtoxml;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

class Directive {
    
    private String xpathExpression;
    private boolean multi = false;
    private String recSource;

    public static Set<String> extractDirectives(Properties properties) {
        Enumeration<String> propertyNames = (Enumeration<String>) properties.propertyNames();
        Set<String> directives = new HashSet<String>();
        while (propertyNames.hasMoreElements()) {
            String propertyName = propertyNames.nextElement();
            String[] tokens = propertyName.split("\\.");
            // Don't add a directive for the property 'xpath'; that'd be dumb, probably
            if (tokens.length > 1 && "xpath".equals(tokens[tokens.length - 1])) {
                directives.add(tokens[0]);
            }
        }
        return directives;
    }

    public Directive(String name, Properties properties) {
        this.xpathExpression = properties.getProperty(name + ".xpath");
        if (this.xpathExpression == null) {
            throw new NullPointerException ("Null XPath defined for directive " + name);
        }
        String multiStr = properties.getProperty(name + ".multi", "false");
        // truthy is 'true' and 'yes'; Everything else is falsey
        if (multiStr.equalsIgnoreCase("true") || multiStr.equalsIgnoreCase("yes")) {
            this.multi = true;
        }
        this.recSource = properties.getProperty(name + ".recSource");
        if (this.recSource == null) {
            throw new NullPointerException ("Null recSource for directive " + name);
        }
    }

    public String getXPathExpression() {
        return this.xpathExpression;
    }

    public boolean isMulti() {
        return this.multi;
    }

    public String getRecSource() {
        return this.recSource;
    }

    @Override
    public String toString() {
        String ret = xpathExpression + ":" + recSource + ":" + multi;
        return ret;
    }
}