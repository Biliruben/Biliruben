package biliruben.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Bag of static utilities
 */
public class Util {
    
    public static boolean isEmpty(String value) {
        return value == null || "".equals(value.trim());
    }
    
    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty(); 
    }
    
    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }
    
    public static boolean equals(Object o1, Object o2) {
        if (o1 == null) {
            return (o2 == null);
        } else if (o2 == null) {
            // well, we know o1's not null
            return false;
        } else {
            // no nulls, yay!
            return o1.equals(o2);
        }
    }

    public static boolean isEmpty(Map<String, String> _staticAttrs) {
        return _staticAttrs == null || _staticAttrs.isEmpty();
    }

    public static String getRomanNumeral(int number) {
        StringBuilder buff = new StringBuilder();
        while (number >= 900) {
            buff.append("M");
            number -= 1000;
        }
        while(number < 0) {
            buff.insert(buff.length() - 1, "C");
            number += 100;
        }
        while(number >=400) {
            buff.append("D");
            number -= 500;
        }
        while (number < 0) {
            buff.insert(buff.length() - 1, "C");
            number += 100;
        }
        while(number >= 90) {
            buff.append("C");
            number -= 100;
        }
        while (number < 0) {
            buff.insert(buff.length() - 1, "X");
            number += 10;
        }
        while (number >= 40) {
            buff.append("L");
            number -= 50;
        }
        while (number < 0) {
            buff.insert(buff.length() - 1, "X");
            number += 10;
        }
        while (number >= 9) {
            buff.append("X");
            number -= 10;
        }
        while (number < 0) {
            buff.insert(buff.length() - 1, "I");
            number += 1;
        }
        while (number >= 4) {
            buff.append("V");
            number -= 5;
        }
        while (number < 0) {
            buff.insert(buff.length() - 1, "I");
            number += 1;
        }
        
        while (number >= 1) {
            buff.append("I");
            number -= 1;
        }

        return buff.toString();
    }

    public static <T extends Object> Iterable<T> nullSafeIterator(Iterable<T> iterable) {
        if (iterable == null) {
            return new ArrayList<T>();
        } else {
            return iterable;
        }
    }
    
    
    public static void main(String[] args) {
        // roman numeral test
        testRoman(2372, "MMCCCLXXII");
        testRoman(1997, "MCMXCVII");
        testRoman(49, "XLIX");
        testRoman(4, "IV");
        testRoman(728, "DCCXXVIII");
        testRoman(499, "CDXCIX");
    }
    
    private static void testRoman(int number, String testNumeral) {
        String numeral = getRomanNumeral(number);
        System.out.println(number + " = " + numeral);
        if (!testNumeral.equals(numeral)) {
            throw new AssertionError(numeral + " does not match: " + testNumeral);
        }
    }
}
