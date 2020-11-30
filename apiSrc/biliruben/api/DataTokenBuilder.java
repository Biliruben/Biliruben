package biliruben.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This functor will take in a list of data types, defined as known Enums, as a pattern.  It will then
 * use that pattern and the underlying {@link DataIterator}s to build a new token
 * @author trey.kirk
 *
 */
public class DataTokenBuilder implements Iterable<String> {

    private class TokenIterator implements Iterator<String> {

        
        @Override
        public boolean hasNext() {
            // we have a next if all of our Datatizers do
            boolean hasNext = true;
            Iterator<DataIterator> datatizers = _datatizerMap.values().iterator();
            while (hasNext && datatizers.hasNext()) {
                hasNext = hasNext && datatizers.next().hasNext();
            }
            return hasNext;
        }

        @Override
        public String next() {
            // build the next token
            StringBuilder buff = new StringBuilder();
            for (Object nextToken : _patternStack) {
                if (nextToken instanceof String) {
                    buff.append(nextToken);
                } else if (nextToken instanceof DataType) {
                    DataType dt = (DataType)nextToken;
                    DataIterator d = _datatizerMap.get(dt);
                    buff.append(d.next());
                }
            }
            return buff.toString();
        }

        @Override
        public void remove() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Nooooooooooooope!");
        }
        
    }
    
    public enum DataType {
        FirstName("firstNames.txt"),
        LastName("lastNames.txt"),
        Job("jobs.txt"),
        Place("places.txt"),
        Street("streets.txt"),
        Title("titles.txt");
        
        private String _fileName;

        DataType(String fileName) {
            _fileName = fileName;
        }
        
        public String getFileName() {
            return _fileName;
        }
        
    }
    
    private Map<DataType, DataIterator> _datatizerMap;
    private Boolean _uniqueValues;
    private List _patternStack;
    private File _baseDir;

    public DataTokenBuilder(File base, String pattern, Boolean unique) throws IOException {
        if (base == null || !base.exists() || !base.isDirectory()) {
            throw new IOException("Base directory is invalid, not a directory, or null: " + base);
        }
        _baseDir = base;
        _datatizerMap = new HashMap<DataType, DataIterator>();
        _uniqueValues = unique;
        _patternStack = new ArrayList();
        compile(pattern);
    }
    
    private void addDataType(DataType dataType) throws IOException {
        if (_datatizerMap.get(dataType) == null) {
            String file = _baseDir + File.separator + dataType.getFileName();
            DataIterator d = new DataIterator(file, _uniqueValues);
            _datatizerMap.put(dataType, d);
        }
    }
    
    private void compile(String pattern) throws IOException {
        // [FirstName] to the [LastName] of the [Place] who does the [Title]
        // shall we create a stack of tokens, each being a literal or a DataType
        StringBuilder buff = new StringBuilder(); 
        for (byte b : pattern.getBytes()) {
            if (b == '[') {
                // about to read a keyword, drop the current buffer into the stack as is
                _patternStack.add(buff.toString());
                buff = new StringBuilder();
            } else if (b == ']') {
                String dataTypeString = buff.toString();
                DataType dt = DataType.valueOf(dataTypeString);
                // make sure we add it to the map
                addDataType(dt);
                _patternStack.add(dt);
                buff = new StringBuilder();
            } else {
                // just tack it into the buffer
                buff.append((char)b);
            }
        }
    }

    @Override
    public Iterator<String> iterator() {

        return new TokenIterator();
    }
}
