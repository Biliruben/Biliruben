package biliruben.api;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic JSON parser utility.  Decodes a given JSON string into a generic object, namely a List, Map, String, Boolean, and/or Number.
 * Next pass I might coerce a Map into an Object (given a target Class)
 * @author trey.kirk
 *
 */
public class JSONParser {

    private static class JSONParseException extends Exception {
        public JSONParseException(String msg) {
            super(msg);
        }

        public JSONParseException(String msg, Exception wrapped) {
            super (msg, wrapped);
        }
    };


    private  InputStream _stream;
    private  char _ch;
    private  int _pos;
    private  int _ln;

    /**
     * Not thread safe!
     */
    public  Object parse(String json) throws JSONParseException {
        _stream = new ByteArrayInputStream(json.getBytes());
        return startParse();
    }

    /**
     * Not thread safe!
     */
    public  Object parse(InputStream stream) throws JSONParseException {
        _stream = stream;
        return startParse();
    }
    
    public <T> T parse(Class<T> clazz, InputStream stream) throws JSONParseException {
        Map map = (Map)parse(stream);
        T ret = convertMapToClass(clazz, map);
        return ret;
    }
    
    private <T> T convertMapToClass(Class<T> clazz, Map map) throws JSONParseException {
        try {
            T newInstance = clazz.newInstance();
            for (Object keyObj : map.keySet()) {
                String key = (String)keyObj;
                Object value = map.get(key);
                // find the setter method that takes one arg
                // If we find multiples, get pissed and refactor this
                // to take a configuration object to control method mapping
                Method[] methods = clazz.getMethods();
                List<Method> foundMethods = new ArrayList<Method>();
                for (Method m : methods) {
                    if (m.getName().equalsIgnoreCase("set" + key) && m.isAccessible()) {
                        Class<?>[] params = m.getParameterTypes();
                        if (params.length == 1) {
                            foundMethods.add(m);
                        }
                    }
                }
                if (foundMethods.size() != 1) {
                    throw new JSONParseException("Error converting JSON value into map, more than one setter method was found for class: " + clazz);
                }
                // if we were more robust, we might look for an accessible field.  Then again, we'd probably be using a proper Property Bean API
                // If the value is null, just set it and move on. Otherwise: 
                // Check the parameter type.  If it is a Map or a List, we'll have to convert the collection's elements... so that's fun
                // if it's a String, Boolean, Number, cast our value into that and set it
                // otherwise, recurse into its json map
            }
        } catch (Exception e) {
            throw new JSONParseException("Error converting map into class instance.", e);
        }
    }

    /*
     * Returns a string representation of where in the stream the cursor is
     */
    private  String getPosition() {
        return _ln + ":" + _pos;
    }

    /*
     * Internal entry method for parsing
     */
    private  Object startParse() throws JSONParseException {
        _pos = 0;
        _ln = 1;
        Object obj = value();
        if (_ch == '}' || _ch == '"' || _ch == ']' || _ch ) {
            // these are anchor characters that their respective methods couldn't arbitrarily increment past
            // do that now
            whitespace();
        }
        // and then check for trailing characters
        if ((byte)_ch != -1) {
            throw new JSONParseException("Extra trailing characters at pos "  + getPosition() + ": " + _ch);
        }
        return obj;
    }

    /*
     * Increments the cursor until the next non-whitespace character is found.  Doesn't do any validation
     * that whitespace is legal.  The caller will have to know when whitespace can be ignored.
     */
    private  void whitespace() throws JSONParseException {
        do {
            next();
            if (_ch == '\n') {
                _pos = 0;
                _ln++;
            }
        } while (_ch == ' ' || _ch == '\n' || _ch == '\t' || _ch == '\r');
    }

    /*
     * Confirms that the current character is of the class of characters specified.  If not, it will
     * increment the cursor until it does or until the stream is exhausted.  If the stream is exhausted,
     * an exception will be thrown.
     */
    private  char next(char... chars) throws JSONParseException {
        // by the time I sorted and  binary searched, I could've iterated
        boolean found = false;
        while (true) {
            for (char chr : chars) {
                if (chr == _ch) {
                    found = true;
                    return _ch;
                }
            }
            next();
            verifyStream();
        }
    }

    /*
     * Basic increment of the stream's cursor.  Wraps any IOExceptions into a JSONParseException
     */
    private  char next() throws JSONParseException {
        try {
            _ch = (char) _stream.read();
        } catch (IOException e) {
            throw new JSONParseException("Error reading at position " + getPosition(), e);
        }
        _pos++;
        return _ch;
    }

    /*
     * Main delegation method that uses the current cursor to determine what type of value is immediately
     * following.
     */
    private  Object value() throws JSONParseException {
        whitespace();
        switch (_ch) {
        case '{': return nextObject();
        case '[': return nextArray();
        case 't':
        case 'f': return nextBoolean();
        // The word 'null' is expected for nulls, so an empty JSONValue is never expected (ala: {"keyValue":} is an illegal JSON text)
        case 'n': return nextNull();
        case '-':
        case '.':
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9': return nextNumber();
        case '"': return nextString();
        }

        throw new JSONParseException("Unexpected character encountered at position " + getPosition());
    }

    /*
     * Ensures the remainder of 'n' is 'ull'.  Throws a JSONParseException if not.
     */
    private  Object nextNull() throws JSONParseException {
        for (char c : "ull".toCharArray()) {
            next(c);
        }
        return null;
    }

    /*
     * Given a JSON object, parses over each tuple of tokens epecting the following in
     * each tuple:
     * - a String that will be the key
     * - the separator ':'
     * - a JSONValue that will be the value
     * After each tuple, a ',' or closing '}' is expected
     */
    private  Map nextObject() throws JSONParseException {
        Map map = new HashMap();
        while (_ch != '}') {
            verifyStream();
            // first token is a string
            next('"');
            String key = nextString();
            // second token is a value separator
            next(':');
            // third token is anything
            Object value = value();
            map.put(key, value);
            next(',','}');
        }
        return map;
    }

    /*
     * Similar to the object routine, this method parses each token
     * to be any object, followed by either a ',' or a ']'.
     */
    private  List nextArray() throws JSONParseException {
        // instead of a true array, return a List.  Easier to cast.
        List theList = new ArrayList();
        while (_ch != ']') {
            verifyStream();
            Object nextObject = value();
            theList.add(nextObject);
            next(',',']');
        }
        return theList;
    }

    /*
     * The lead characters are 't' or 'f', if 't', ensure the
     * remainder is 'rue'.  Else assumes the first char is 'f' and
     * then ensures the remainder is 'alse'.  Should either condition
     * pass, the related boolean is passed.  Otherwise an exception
     * will be thrown.
     */
    private  Boolean nextBoolean() throws JSONParseException {
        if (_ch == 't') {
            for (char c : "rue".toCharArray()) {
                verifyStream();
                next(c);
            }
            return true;
        } else {
            for (char c : "alse".toCharArray()) {
                verifyStream();
                next(c);
            }
            return false;
        }
        // Unreachable code:  Either a 'true' or 'false' will be passed or next(c) will
        // throw a nasty exception.
    }

    private  Number nextNumber() throws JSONParseException {
        // we'll either return a Long or a Double
        StringBuilder numberBuilder = new StringBuilder();
        String numbers = "0123456789.-";
        while (numbers.indexOf(_ch) > -1) {
            verifyStream();
            numberBuilder.append(_ch);
            next();
        }
        // got a period in it?  Return a Double
        Number num = null;
        if (numberBuilder.toString().contains(".")) {
            try {
                num = Double.valueOf(numberBuilder.toString());
            } catch (Exception e) {
                throw new JSONParseException("Error parsing Double from '" + numberBuilder.toString() + " at pos: " + getPosition(), e);
            }
        } else {
            try {
                num = Long.valueOf(numberBuilder.toString());
            } catch (Exception e) {
                throw new JSONParseException("Error parsing Long from '" + numberBuilder.toString() + " at pos: " + getPosition(), e);
            }
        }
        if (num == null) {
            throw new JSONParseException ("Null number found at pos " + getPosition() + "!");
        }
        return num;
    }

    /*
     * Checks the cursor to see if it's at the end of the stream.  If so, throw an error (we assume
     * it's too early to be doing that)
     */
    private  void verifyStream() throws JSONParseException {
        if ((byte)_ch == -1) {
            // We don't expect to reach the eof here
            throw new JSONParseException("Premature EOF reached!");
        }
    }

    /*
     * It's been determined that the current character is an escaped character (ala it has a leading
     * '\').  This method maps the escaped character to its actual value.  Well, for a few characters,
     * anyways.  There's probably more escape values to map here, but whatever
     */
    private  char escaped() {
        switch (_ch) {
        case 'b': return '\b';
        case 'f': return '\f';
        case 'n': return '\n';
        case 'r': return '\r';
        case 't': return '\t';
        default: return _ch;
        }
    }

    /*
     * Given a starting '"', pull the remaining characters into a StringBuilder
     * until another '"' is reached.
     */
    private  String nextString() throws JSONParseException {
        StringBuilder build = new StringBuilder();
        do {
            next();
            verifyStream();
            while (_ch == '\\') {
                // escape attempt!
                next();
                build.append(escaped());
                next();
                verifyStream();
            }
            if (_ch != '"') {
                build.append(_ch);
            }
        } while (_ch != '"');
        return build.toString();
    }

    /*
     * Testing main method
     */
    public static void main(String[] args) throws Exception {
        String fileName = "c:\\temp\\test.json";
        File f = new File(fileName);
        FileInputStream str = new FileInputStream(f);
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(str);
        System.out.println(obj);
    }

}
