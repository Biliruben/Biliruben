package biliruben.transformer;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Describes how the CSVToXML processor handles XML Nodes in conjunction with a provided
 * data source. The data is a Map of Strings which is expected to be built from CSV. However,
 * that map may be constructed by any means. Properties will define how a Directive is to
 * be constructed.
 * @author trey.kirk
 *
 */
public class Directive {
    
    private String path;
    private boolean multi = false;
    private String source;
    private Operation operation;
    private String value;
    private String name;
    private String parentName;
    private boolean processed;
    private String parentElement;
    private Directive parent;

    public enum Operation {
        update,
        append,
        appendUnique,
        updateOrAppend;
    }

    /**
     * Uses the Properties provided to determine the defined directives. When the
     * property 'directives' is provided, a comma separated list will define the
     * Directives to build. If not provided, the list is inferred based on the
     * defined properties, using the 'path' sub-property as a key identifier for
     * directives.
     * @param properties
     * @return
     */
    public static List<Directive> extractDirectives(Map properties) {
        Set<String> directiveNames = new HashSet<String>();
        String directiveProperty = (String)properties.get(Constants.PROPERTY_DIRECTIVES);
        if (directiveProperty != null) {
            for (String directiveName : directiveProperty.split(",")) {
                directiveNames.add(directiveName);
            }
        } else {
            for (Object propertyNameObj : properties.keySet()) {
                String propertyName = (String)propertyNameObj;
                String[] tokens = propertyName.split("\\.");
                // Don't add a directive for the property 'path'; that'd be dumb, probably
                if (tokens.length > 1 && Constants.PROPERTY_XPATH.equals(tokens[tokens.length - 1])) {
                    directiveNames.add(tokens[0]);
                }
            }
        }

        Map<String, Directive> directives = new HashMap<String, Directive>();
        for (String directiveName : directiveNames) {
            Directive d = new Directive (directiveName, properties);
            directives.put(directiveName, d);
        }
        // now correlate the parents
        for (Directive directive : directives.values()) {
            String parentName = directive.getParentName();
            if (parentName != null) {
                Directive parentDirective = directives.get(parentName);
                directive.setParent(parentDirective);
            }
        }

        return new ArrayList<Directive>(directives.values());
    }

    private void setParent(Directive parentDirective) {
        this.parent = parentDirective;
    }

    /**
     * Constructor. A typical implementation would expect the caller to use 
     * {@link #extractDirectives(Properties)} instead of constructing directly
     * @param name
     * @param properties
     */
    Directive(String name, Map properties) {
        this.processed = false;
        this.path = (String)properties.get(name + "." + Constants.PROPERTY_XPATH);
        String defaultSource = (String)properties.get(Constants.PROPERTY_SOURCE);
        String defaultOperation = (String)properties.get(Constants.PROPERTY_OPERATION);
        
        if (this.path == null) {
            throw new NullPointerException ("Null Path defined for directive " + name);
        }

        String multiStr = (String)properties.get(name + "." + Constants.PROPERTY_MULTI);
        if (multiStr == null) {
            multiStr = "false";
        }
        // truthy is 'true' and 'yes'; Everything else is falsey
        if (multiStr.equalsIgnoreCase("true") || multiStr.equalsIgnoreCase("yes")) {
            this.multi = true;
        }
        this.source = (String)properties.get(name + "." + Constants.PROPERTY_SOURCE);
        if (this.source == null) {
            this.source = defaultSource;
        }
        if (this.source == null) {
            throw new NullPointerException ("Null source for directive " + name);
        }

        String opVal = (String)properties.get(name + "." + Constants.PROPERTY_OPERATION);
        if (opVal == null) {
            opVal = defaultOperation;
        }
        if (opVal == null) {
            throw new NullPointerException ("Null operation for directive " + name);
        }
        this.operation = Operation.valueOf(opVal);
        this.value = (String)properties.get(name + "." + Constants.PROPERTY_VALUE);
        if (this.value == null) {
            throw new NullPointerException ("Null value for directive " + name);
        }
        this.parentName = (String)properties.get(name + "." + Constants.PROPERTY_PARENT);
        this.name = name;
        this.parentElement = (String)properties.get(name + "." + Constants.PROPERTY_PARENT_PATH);
    }

    public String getPath() {
        return this.path;
    }

    /*
     * I don't think this is useful
     */
    private boolean isMulti() {
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

    public Directive getParent() {
        return this.parent;
    }

    public String getParentName() {
        return this.parentName;
    }

    public Operation getOperation() {
        return this.operation;
    }

    /**
     * CSVToXML processor needs to mark a Directive as processed in order to avoid
     * multiple applications. This way it can easily handle parent Directives without
     * pre-ordering the Directive population.
     * @return
     */
    public boolean isProcessed() {
        return this.processed;
    }

    public void setProcessed(boolean isProcessed) {
        this.processed = isProcessed;
    }

    public String getParentElement() {
        return this.parentElement;
    }

    /**
     * Returns the value for this Directive, leveraging the provided dataSource
     * if necessary.
     * @param dataSource
     * @return
     */
    public String deriveValue(Map<String, String> dataSource) {
        // if the source is set to 'literal', we just return whatever is in 'value'. Otherwise,
        // we use 'value' as the key to find the real value in the data source
        // As we add more sources, this might have to be smarter
        if (Constants.VALUE_LITERAL.equalsIgnoreCase(this.source)) {
            return this.value;
        } else if (Constants.VALUE_DATA_SOURCE.equalsIgnoreCase(this.source)) {
            return dataSource.get(this.value);
        }
        // what are you doing here?
        return null;
    }

    @Override
    public String toString() {
        String ret = name + ":" + path + ":" + operation + ":" + source + ":" + value;
        return ret;
    }
}