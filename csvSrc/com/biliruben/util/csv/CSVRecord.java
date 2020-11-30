package com.biliruben.util.csv;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.OperationNotSupportedException;

/**
 * Object to support outputting data to a CSV format.  Intended use is to received maps of data representing each
 * 'line' of data.  This object will be iterable with each iteration returning a string value of each line delimited
 * by commas.
 * @author trey.kirk
 *
 */
public class CSVRecord implements Iterator<String>, Iterable<String>{

    private static final String DEFAULT_DELIMITER = ",";
    private List<String> _fields;
    private List<Map<String, String>> _maps;
    private String[] _outputFields;
    private int _iterationPos = -1;
    private boolean _doOutputFields = true;
    private String _delimiter;
    private String _nullValue;
    private boolean _noQuotes = false;

    /**
     * Default constructor.  Since this object is intended to have data added as we go, it's difficult to anticipate
     * anything providable at instantiation time.  So the only constructor will be a default one.
     */
    public CSVRecord() {
        _fields = new ArrayList<String>();
        _maps = new ArrayList<Map<String, String>>();
        _delimiter = DEFAULT_DELIMITER;
    }



    /**
     * We can restrict what fields are output and / or what order they're output via a String array of fields
     * @param outputFields
     */
    public CSVRecord(String[] outputFields) {
        this();
        _outputFields = outputFields;
    }

    public void setNullValue(String value) {
        _nullValue = value;
    }
    
    public String getNullValue() {
        return _nullValue;
    }

    public void setDelimiter(String delimiter) {
        if (delimiter == null) {
            throw new NullPointerException("Delimiter cannot be null!");
        }
        _delimiter = delimiter;
    }

    public void addLine(Map<String, String> line) {
        appendFields(line.keySet());
        _maps.add(line);
    }
    
    public void setFields(String[] fields) {
        _outputFields = fields;
    }

    private void appendFields(Set<String> fields) {
        for (String field : fields) {
            if (!(_fields.contains(field))) {
                _fields.add(field);
            }
        }
    }

    public void resetIteration() {
        _iterationPos = -1;
    }

    public void setIncludeFieldNames(boolean includeFields) {
        _doOutputFields  = includeFields;
    }

    public boolean hasNext() {
        if (_doOutputFields && _iterationPos == -1) {
            return true;  // we can always output the header
        }
        if (_maps.size() <= _iterationPos) {
            return false; // maps list aint' big enough
        } else {
            return true;
        }
    }

    public String next() {
        if (_iterationPos == -1 && _doOutputFields) {
            _iterationPos++;
            return toCsv(getOutputFields());
        }
        if (_iterationPos == -1) {
            _iterationPos++;
        }
        String returnValue = toCsv(_maps.get(_iterationPos));
        _iterationPos++;
        return returnValue;
    }

    private String toCsv(Map<String, String> map) {
        String[] fields = getOutputFields();
        String[] values = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            String value = map.get(fields[i]);
            if (value == null || value.trim().equals("")) {
                value = _nullValue;
            }
            values[i] = value;
        }
        return toCsv(values);
    }

    private String toCsv(String[] outputFields) {
        // converts the provided String array to csv values
        StringBuffer buff = new StringBuffer();
        for (int i = 0; i < outputFields.length; i++) {
            buff.append(quoted(outputFields[i]));
            if (i < outputFields.length - 1) {
                buff.append(_delimiter);
            }
        }
        return buff.toString() + "\n";
    }

    private String quoted(String quoteMe) {
        if (_noQuotes) {
            return quoteMe;
        }
        // All double-quotes are expected to be escaped with another double-quote
        // If the string contains the delimiter, double-quotes, or a line break, it should be double-quoted
        if (quoteMe == null) {
            quoteMe = "";
        }
        // escape double-quotes
        quoteMe = quoteMe.replaceAll("\"", "\"\"");

        boolean quoteIt = false;
        // quote if has quotes
        if ((quoteMe.contains("\"")) ||
                (quoteMe.contains("\n")) ||
                (quoteMe.contains(_delimiter))) {
            quoteIt = true;
        }
        if (quoteIt) {
            return "\"" + quoteMe + "\"";
        } else {
            return quoteMe;
        }
    }

    public void remove() {
        throw new RuntimeException(new OperationNotSupportedException());
    }

    public String[] getOutputFields() {
        if (_outputFields == null) {
            _outputFields = _fields.toArray(new String[_fields.size()]); 
        }
        return _outputFields;
    }

    public Iterator<String> iterator() {
        return this;
    }
    
    /**
     * If set to true, fields will not be quoted.
     * @param noQuotes
     */
    public void setNoQuotes(boolean noQuotes) {
        _noQuotes = noQuotes;
    }
}
