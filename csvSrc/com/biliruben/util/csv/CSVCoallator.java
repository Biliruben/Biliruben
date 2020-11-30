package com.biliruben.util.csv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import biliruben.util.Util;

import com.biliruben.util.csv.CSVSource.CSVType;

/**
 * Takes a CSV Source and creates a mapping hierarchy
 * @author trey.kirk
 *
 */
public class CSVCoallator {
    
    private static Logger _log = Logger.getLogger(CSVCoallator.class);

    private CSVSource _src;
    private String _key;
    private String _membership;
    private String _collation;
    private Map<String, Set<String>> _keyToMembershipMap;
    private Map<String, String> _membershipToCollation;
    private Set<String> _roots;
    

    public CSVCoallator(CSVSource src, String key, String membership, String collation) {
        if (src == null || src.getType() != CSVType.WithHeader) {
            throw new IllegalArgumentException("Invalid CSVSource. Must be non-null and with headers");
        }
        this._key = key;
        this._membership = membership;
        this._collation = collation;
        this._src = src;
        _keyToMembershipMap = new HashMap<String, Set<String>>();
        _membershipToCollation = new HashMap<String, String>();
    }
    
    private void createMembershipMap() {
        _roots = new TreeSet<String>();
        for (Map<String, String> line : _src) {
            String key = line.get(this._key);
            String membership = line.get(this._membership);
            String collation = line.get(this._collation);
            if (Util.isEmpty(membership)) {
                // this is a root
                _roots.add(key);
            } else {
                Set<String> existingMembership = _keyToMembershipMap.get(membership);
                if (existingMembership == null) {
                    existingMembership = new TreeSet<String>();
                    _keyToMembershipMap.put(membership, existingMembership);
                }
                existingMembership.add(key);
            }
            _membershipToCollation.put(key, collation);
        }
    }
    
    public Map<String, String> getHierarchyMap() {
        createMembershipMap();
        // read through the roots Set and get the list of memberships,
        // push in the current root's collation value
        // recurse through the membership list
        Map<String, String> collationMap = new HashMap<String, String>();

        for (String root : _roots) {
            Stack<String> collationBuilder = new Stack<String>();
            String value = _membershipToCollation.get(root);
            collationBuilder.push(value);
            Set<String> members = _keyToMembershipMap.get(root);
            Map<String, String> rootMap = new HashMap<String, String>();
            rootMap.put(root, value);
            getHierarchyMapInner(rootMap, collationBuilder, members);
            collationMap.putAll(rootMap);
        }
        return collationMap;
    }
    
    private void getHierarchyMapInner(Map<String, String> currentMap, Stack<String> collationBuilder, Set<String> currentMembers) {
        for (String member : currentMembers) {
            String membershp = _membershipToCollation.get(member);
            collationBuilder.push(membershp);
            String flattenedCoallation = flattenBuilder(collationBuilder);
            currentMap.put(member, flattenedCoallation);
            Set<String> members = _keyToMembershipMap.get(member);
            if (!Util.isEmpty(members)) {
                getHierarchyMapInner(currentMap, collationBuilder, members);
            }
            // done iterating this members, pop the builder and move to the next element
            collationBuilder.pop();
        }
    }
    
    private String flattenBuilder(Stack<String> collationBuilder) {
        StringBuilder builder = new StringBuilder();
        // need to reverse it, so:
        Stack<String> toReverse = new Stack<String>();
        for (String el : collationBuilder) {
            toReverse.push(el);
        }
        while (!toReverse.isEmpty()) {
            String collation = toReverse.pop();
            builder.append(collation).append(",");
        }
        
        // strip the trailing ","
        builder.deleteCharAt(builder.length()- 1);
        return builder.toString();
    }

}
