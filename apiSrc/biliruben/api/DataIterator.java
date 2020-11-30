package biliruben.api;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Class that iterates over a data file and provides a data token, delimited by newlines.  Tokens can repeat
 * or they can be guaranteed unique
 * @author trey.kirk
 *
 */
public class DataIterator implements Iterator<String> {
    
    private boolean _uniqueValues;
    private Iterator<String> _randomizedIt;
    private List<String> _orderedList;
    private Random _rand;
    
    
    public DataIterator(String file, boolean uniqueValues) throws IOException {
        _uniqueValues = uniqueValues;
        buildList(file);
        _rand = new Random(System.currentTimeMillis());
    }
    
    private void buildList(String file) throws IOException {
        
        BufferedReader reader = new BufferedReader(new FileReader(file), 1024);
        String token = null;
        List<String> tokens = new ArrayList<String>();
        if (reader.ready()) {
        do {
            token = reader.readLine();
            if (token != null) {
                tokens.add(token);
            }
        } while(token != null);
        }
        // create an ordered list as a copy
        _orderedList = new ArrayList<String>(tokens);
        // Now we randomize the order
        Collections.shuffle(tokens, new Random(System.currentTimeMillis()));
        _randomizedIt = tokens.iterator();
    }

    @Override
    public boolean hasNext() {
        // if we're not required to use unique values, we always have a unique value at the ready
        if (_uniqueValues) {
            return _randomizedIt.hasNext();
        } else {
            return true;
        }
    }

    @Override
    public String next() {
        if (_uniqueValues) {
            return _randomizedIt.next();
        } else {
            int nextIndex = _rand.nextInt(_orderedList.size());
            return _orderedList.get(nextIndex);
        }
    }

    @Override
    public void remove() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("We don't do that");
    }

    
}
