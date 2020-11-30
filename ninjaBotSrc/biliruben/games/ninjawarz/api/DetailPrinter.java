package biliruben.games.ninjawarz.api;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;

import biliruben.games.ninjawarz.Util;

public class DetailPrinter {

    public static class ColumnConfig {

        private Map<String, Integer> _columns;
        private List<String> _fields;
        private Map<String, String> _beanMap;
        
        public ColumnConfig() {
            _columns = new HashMap<String, Integer>();
            _beanMap = new HashMap<String, String>();
        }
        
        public ColumnConfig(Map<String, Integer> columnDescription, String[] fields) {
            _columns = new HashMap<String, Integer>(columnDescription);
            setFields(fields);
            _beanMap = new HashMap<String, String>();
        }
        
        public ColumnConfig(ColumnConfig fromConfig) {
            _columns = new HashMap<String, Integer>(fromConfig._columns);
            _fields = new ArrayList<String>(fromConfig._fields);
            _beanMap = new HashMap<String, String>(fromConfig._beanMap);
        }

        public void setColumn(String field, int length) {
            _columns.put(field, length);
            if (!_fields.contains(field)) {
                _fields.add(field);
            }
        }
        
        public void addPropertyMap(String fieldName, String beanName) {
            _beanMap.put(fieldName, beanName);
        }
        
        public void removePropertyMap(String fieldName) {
            _beanMap.remove(fieldName);
        }
        
        public String getFieldBean(String fieldName) {
            String beanName = _beanMap.get(fieldName);
            beanName = beanName == null ? fieldName : beanName;
            return beanName;
        }
        
        public void removeColumn(String field) {
            _columns.remove(field);
            _fields.remove(field);
        }
        
        public int getLength(String field) {
            return _columns.get(field);
        }
        
        public List<String> getFields() {
            return _fields;
        }
        
        public void setFields(String[] fields) {
            _fields = Arrays.asList(fields);
        }
    }

    private static final char HEADER_CHAR = '-';

    private ColumnConfig _columnConfig;
    private PrintStream _print;
    private boolean _printHeader;
    private boolean _headerPrinted;
    
    // so I have an bean object, I need to print data bout that object in a pretty way
    // to do taht, I need to know:
    // - what attributes to print
    // - in what order to print them
    // - from what objects to get the information from
    //
    // other tid bits:
    // - sometimes, I want to print the column headers, sometimes I don't; When I print column
    //   headers, I want a separator
    // - sometimes, there are attributes I want to highlight for some values / objects by using
    //   an astrick that is offset
    
    // The how:  This feels similar to how we do CSV reading / writing.  I need:
    // - A column config object.  It will explain what fields to print and their column
    //   lengths.
    // - Beans to print, as an ordered list.
    // - class configuration, to tell me if to print a header or not
    // - Each bean can have a column config hanging off of it to allow a custom shift of the
    //   column sizes.  Actually, maybe a print method that takes in the bean and an overloaded
    //   print method that includes a column config
    
    public DetailPrinter(PrintStream output) {
        this(output, new ColumnConfig());
    }
    
    public DetailPrinter(PrintStream output, ColumnConfig config) {
        _print = output;
        _printHeader = true;
        _headerPrinted = false;
        _columnConfig = config;
    }
    
    public void setPrintHeader(boolean printHeader) {
        _printHeader = printHeader;
    }
    
    public void printLn(Object data) {
        this.printLn(data, _columnConfig);
    }
    
    public void printLn(Object data, ColumnConfig config) {
        if (_printHeader && !_headerPrinted) {
            printHeader(config);
        }
        for (String field : config.getFields()) {
            String value = null;
            try {
                Object objValue = null;
                if (data instanceof Map) {
                    // generic version
                    objValue = ((Map)data).get(field);
                } else {
                    // bean version
                    String beanField = config.getFieldBean(field);
                    objValue = PropertyUtils.getSimpleProperty(data, beanField);
                }
                
                if (objValue == null) {
                    value = "";
                } else {
                    value = objValue.toString();
                }
            } catch (Exception e) {
                // These exceptions are generally problems about the method being un-accessable
                // and methods not found.  Or more simply put, the caller provided us a sucky field
                //
                // instead of an error, just print the field name
                value = field;
            }
            _print.printf("%-" + config.getLength(field) + "s", value);
        }
        _print.println();
    }
    
    private String getDisplayableBeanField (String value) {
        // proper casing is first putting spaces before each currently captalized word
        if (value == null) {
            return null;
        } else if ("".equals(value.trim())) {
            return value;
        } // that takes care of the null values, empty values, and all whitespace values
        StringBuffer buff = new StringBuffer();
        for (byte b : value.getBytes()) {
            if (b >= 'A' && b <= 'Z') {
                buff.append(" ");
            }
            buff.append((char)b);
        }
        // now properly case each token (ok, at this point just the first token needs help)
        byte[] bytes = buff.toString().getBytes();
        if (bytes[0] >= 'a' && bytes[0] <= 'z') {
            // capalization is nothing but a shift negatively
            bytes[0] = (byte) (bytes[0] - (byte)32);
        }
        // so we're done
        return new String(bytes).trim();
    }
    
    protected void printHeader(ColumnConfig config) {
        for (String field : config.getFields()) {
            _print.printf("%-" + config.getLength(field) + "s", getDisplayableBeanField(field));
        }
        _print.println();
        // headers come with a line separator
        for (String field : config.getFields()) {
            _print.printf("%-" + config.getLength(field) + "s", Util.repeatChar(config.getLength(field) - 1, HEADER_CHAR));
        }
        _print.println();
        _headerPrinted = true;
    }
    
}
