package biliruben.transformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    private Source source;
    private Operation operation;
    private String value;
    private String name;
    private String parentName;
    private boolean processed;
    private String parentElement;
    private Directive parent;

    private static Log log = LogFactory.getLog(Directive.class);

    public enum Operation {
        update,
        append,
        appendUnique,
        updateOrAppend;
    }

    public enum Source {
        literal,
        data
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
    public static List<Directive> extractDirectives(Map<String, Object> properties) {
        Set<String> directiveNames = new HashSet<String>();
        String directiveProperty = (String)properties.get(Constants.PROPERTY_DIRECTIVES);
        log.debug("directiveProperty: " + directiveProperty);
        if (directiveProperty != null) {
            log.debug("Building user specified");
            for (String directiveName : directiveProperty.split(",")) {
                directiveNames.add(directiveName);
            }
        } else {
            log.debug("Discovering");
            for (Object propertyNameObj : properties.keySet()) {
                Object directivePropertiesObj = properties.get(propertyNameObj);
                if (directivePropertiesObj != null && directivePropertiesObj instanceof Map) {
                    // every directive must have a path defined, so that's our key
                    String path = (String) ((Map)directivePropertiesObj).get(Constants.PROPERTY_XPATH);
                    if (path != null) {
                        directiveNames.add((String) propertyNameObj);
                    }
                }
            }
        }

        Map<String, Directive> directives = new HashMap<String, Directive>();
        Map<String, String> directiveDefaults = new HashMap<String, String>();
        for (String key : properties.keySet()) {
            Object value = properties.get(key);
            if (value instanceof String) {
                directiveDefaults.put(key, (String)value);
            }
        }
        for (String directiveName : directiveNames) {
            // bit of a dance. we need the base properties in the map to work as "defaults" while the sub-map
            // properties work as specific values
            Map<String, String> directiveProperties = new HashMap<String, String>(directiveDefaults);
            directiveProperties.putAll((Map<? extends String, ? extends String>) properties.get(directiveName));
            Directive d = new Directive (directiveName, directiveProperties);
            log.debug("Built:" + d);
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

        log.debug ("Extracted directives: " + directives.values());
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
    Directive(String name, Map<String, String> properties) {
        this.processed = false;
        this.path = (String)properties.get(Constants.PROPERTY_XPATH);
        
        if (this.path == null) {
            throw new NullPointerException ("Null Path defined for directive " + name);
        }

        String multiStr = (String)properties.get(Constants.PROPERTY_MULTI);
        if (multiStr == null) {
            multiStr = "false";
        }
        // truthy is 'true' and 'yes'; Everything else is falsey
        if (multiStr.equalsIgnoreCase("true") || multiStr.equalsIgnoreCase("yes")) {
            this.multi = true;
        }
        String source = (String)properties.get(Constants.PROPERTY_SOURCE);
        if (source == null) {
            throw new NullPointerException ("Null source for directive " + name);
        }
        this.source = Source.valueOf(source);

        String opVal = (String)properties.get(Constants.PROPERTY_OPERATION);
        if (opVal == null) {
            throw new NullPointerException ("Null operation for directive " + name);
        }

        this.operation = Operation.valueOf(opVal);
        this.value = (String)properties.get(Constants.PROPERTY_VALUE);
        if (this.value == null) {
            throw new NullPointerException ("Null value for directive " + name);
        }
        this.parentName = (String)properties.get(Constants.PROPERTY_PARENT);
        this.name = name;
        this.parentElement = (String)properties.get(Constants.PROPERTY_PARENT_PATH);
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

    public Source getSource() {
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
        log.debug("Deriving value\nFrom dataSource: " + dataSource + "\nfor Directive: " + this);
        String value = null;
        // if the source is set to 'literal', we just return whatever is in 'value'. Otherwise,
        // we use 'value' as the key to find the real value in the data source
        // As we add more sources, this might have to be smarter
        switch (this.source) {
        case data:
            value = dataSource.get(this.value);
            break;
        case literal:
            value = this.value;
            break;
        }
        log.debug("Derived value: " + value);
        return value;
    }

    @Override
    public String toString() {
        String ret = name + ":" + path + ":" + operation + ":" + source + ":" + value;
        return ret;
    }
}