package com.biliruben.util.csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import biliruben.util.Util;

public class CSVSourceObject extends CSVSourceImpl {

    private String _index;
    private Map<String, Object> _currentObject;
    
    public CSVSourceObject(File inputFile, char delim, String index)
            throws FileNotFoundException {
        super(inputFile, CSVType.WithHeader, delim);
        setIndex(index);
    }

    public CSVSourceObject(File inputFile, String index)
            throws FileNotFoundException {
        super(inputFile, CSVType.WithHeader);
        setIndex(index);
    }

    public CSVSourceObject(Reader input, char delim, String index) {
        super(input, CSVType.WithHeader, delim);
        setIndex(index);
    }

    public CSVSourceObject(Reader input, String index) {
        super(input, CSVType.WithHeader);
        setIndex(index);
    }

    public CSVSourceObject(String inputCsvData, char delim, String index) {
        super(inputCsvData, CSVType.WithHeader, delim);
        setIndex(index);
    }

    public CSVSourceObject(String inputCsvData, String index) {
        super(inputCsvData, CSVType.WithHeader);
        setIndex(index);
    }
    
    public void setIndex(String index) {
        this._index = index;
    }
    
    /**
     * Returns a Map representing merged data over multiple lines of CSV data
     * @return
     * @throws IOException
     * @throws CSVIllegalOperationException
     */
    public Map<String, Object> getNextObject() throws IOException, CSVIllegalOperationException {
        if (Util.isEmpty(this._index)) {
            throw new IllegalArgumentException("Cannot create object without index attribute");
        }
        boolean needNextMap = true;
        Map<String, Object> returnMap = null;
        while (needNextMap) {
            Map<String, String> nextMap = getNextLineAsMap();
            if (nextMap == null) {
                return null;
            }
            if (this._currentObject == null) {
                // For the first map, don't try so hard
                _currentObject = new HashMap<String, Object>(nextMap);
            } else {
                if (Util.isEmpty(nextMap.get(this._index))) {
                    throw new IllegalStateException("Data line does not have an indexable value: " + nextMap);
                }
                if (nextMap.get(_index).equals(_currentObject.get(_index))) {
                    // merge'em
                    mergeMap(nextMap);
                } else {
                    // new map! 
                    returnMap = _currentObject;
                    _currentObject = new HashMap<String, Object>(nextMap);
                    needNextMap = false;
                }
            }
        }

        return returnMap;
    }
    
    private void mergeMap(Map<String, String> fromMap) {
        // we've already made a match for the index attribute, so no need to check for that here. All we do here is detect
        // new values for existing values and convert them into Lists
        for (String key : fromMap.keySet()) {
            String fromValue = fromMap.get(key);
            Object currentValue = _currentObject.get(key);
            if (currentValue instanceof List) {
                ((List)currentValue).add(fromValue);
            } else {
                // it's not a list. careful about nulls
                if (!Util.equals(fromValue, currentValue)) {
                    // we got some differences
                    List<String> newValue = new ArrayList<String>();
                    newValue.add((String)currentValue);
                    newValue.add(fromValue);
                    _currentObject.put(key, newValue);
                }
            }
        }
    }
}
