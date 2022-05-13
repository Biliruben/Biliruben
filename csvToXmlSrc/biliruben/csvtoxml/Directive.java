package biliruben.csvtoxml;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

class Directive {
    
    private String xpathExpression;
    private boolean multi = false;
    private String source;
    private Operation operation;
    private String value;
    private String name;
    private String parent;

    public enum Operation {
        update,
        append,
        updateOrAppend;
    }

    public static List<Directive> extractDirectives(Properties properties) {
        Enumeration<String> propertyNames = (Enumeration<String>) properties.propertyNames();
        Set<String> directiveNames = new HashSet<String>();
        while (propertyNames.hasMoreElements()) {
            String propertyName = propertyNames.nextElement();
            String[] tokens = propertyName.split("\\.");
            // Don't add a directive for the property 'xpath'; that'd be dumb, probably
            if (tokens.length > 1 && "xpath".equals(tokens[tokens.length - 1])) {
                directiveNames.add(tokens[0]);
            }
        }

        List<Directive> directives = new ArrayList<Directive>();
        for (String directiveName : directiveNames) {
            Directive d = new Directive (directiveName, properties);
            directives.add(d);
        }
        return directives;
    }

    public Directive(String name, Properties properties) {
        this.xpathExpression = properties.getProperty(name + ".xpath");
        String defaultSource = properties.getProperty("source");
        String defaultOperation = properties.getProperty("operation");
        
        if (this.xpathExpression == null) {
            throw new NullPointerException ("Null XPath defined for directive " + name);
        }
        String multiStr = properties.getProperty(name + ".multi", "false");
        // truthy is 'true' and 'yes'; Everything else is falsey
        if (multiStr.equalsIgnoreCase("true") || multiStr.equalsIgnoreCase("yes")) {
            this.multi = true;
        }
        this.source = properties.getProperty(name + ".source", defaultSource);
        if (this.source == null) {
            throw new NullPointerException ("Null source for directive " + name);
        }
        String opVal = properties.getProperty(name + ".operation", defaultOperation);
        if (opVal == null) {
            throw new NullPointerException ("Null operation for directive " + name);
        }
        this.operation = Operation.valueOf(opVal);
        this.value = properties.getProperty(name + ".value");
        if (this.value == null) {
            throw new NullPointerException ("Null value for directive " + name);
        }
        this.parent = properties.getProperty(name + ".parent");
        this.name = name;
    }

    public String getXPathExpression() {
        return this.xpathExpression;
    }

    public boolean isMulti() {
        return this.multi;
    }

    public String getSource() {
        return this.source;
    }

    public String getValue() {
        return this.value;
    }

    public String getName() {
        return this.name;
    }

    public String getParent() {
        return this.parent;
    }

    public Operation getOperation() {
        return this.operation;
    }

    public String deriveValue(Map<String, String> dataSource) {
        // if the source is set to 'literal', we just return whatever is in 'value'. Otherwise,
        // we use 'value' as the key to find the real value in the data source
        // As we add more sources, this might have to be smarter
        if ("literal".equalsIgnoreCase(this.source)) {
            return this.value;
        } else if ("csv".equalsIgnoreCase(this.source)) {
            return dataSource.get(this.value);
        }
        // what are you doing here?
        return null;
    }

    @Override
    public String toString() {
        String ret = name + ":" + xpathExpression + ":" + source + ":" + value;
        return ret;
    }
}