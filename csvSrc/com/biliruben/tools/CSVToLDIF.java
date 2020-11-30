package com.biliruben.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import biliruben.util.Util;

import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionLegend;
import com.biliruben.util.csv.CSVIllegalOperationException;
import com.biliruben.util.csv.CSVSource;
import com.biliruben.util.csv.CSVSource.CSVType;
import com.biliruben.util.csv.CSVSourceImpl;
import com.biliruben.util.csv.CSVSourceObject;

public class CSVToLDIF {

    private static final String OPT_INDEX = "index";
    private static final String OPT_LDIF = "ldif";
    private static final String OPT_CSV = "csv";
    private static final String OPT_DN_RDN = "dnRdns";
    private static final String OPT_PREFIX_RN = "prefixRdn";
    private static final String OPT_DN_STATIC_ATTR = "staticDnAttr";
    private static final String OPT_OUTPUT_FIELDS = "outputFields";
    private static final String OPT_MAP_FIELDS = "mapFields";
    private static final String OPT_OBJECT_TYPE = "objectType";
    private static GetOpts _opts;


    /**
     * @param args
     * @throws IOException 
     * @throws CSVIllegalOperationException 
     */
    public static void main(String[] args) throws IOException, CSVIllegalOperationException {
        // read opts
        init(args);
        
        // parse CSV
        // Output to ldif
        String index = _opts.getStr(OPT_INDEX);
        String csvFilename = _opts.getStr(OPT_CSV);
        String ldifFilename = _opts.getStr(OPT_LDIF);
        String dnRns = _opts.getStr(OPT_DN_RDN);
        String staticDnAttrs = _opts.getStr(OPT_DN_STATIC_ATTR);
        String prependedAttrs = _opts.getStr(OPT_PREFIX_RN);
        String rawMappings = _opts.getStr(OPT_MAP_FIELDS);
        File csvFile = new File(csvFilename);
        CSVSourceObject csvSrc = new CSVSourceObject(csvFile, index);
        FileWriter fw = new FileWriter(new File(ldifFilename));
        
        CSVToLDIF doer = new CSVToLDIF();
        doer.setCsvSource(csvSrc);
        doer.setLdifOutput(fw);
        doer.setDnRdns(dnRns);
        doer.addPrependedRns(prependedAttrs);
        doer.setStaticDnAttrs(staticDnAttrs);
        if (!Util.isEmpty(rawMappings)) {
            Map<String, String> mappings = convertMappings(rawMappings);
            doer.setMappings(mappings);
        }
        doer.writeLdif();

    }

    private Map<String, String> _mappings;

    private void setMappings(Map<String, String> mappings) {
        _mappings = mappings;
    }

    private static Map<String, String> convertMappings(String rawMappings) {
        // csv list of key value pairs
        Map<String, String> mappings = new HashMap<String, String>();
        CSVSource line = new CSVSourceImpl(rawMappings, CSVType.WithOutHeader);
        String[] pairs = line.getCurrentLine();
        for (String pair : pairs) {
            String[] tokens = pair.split(",");
            mappings.put(tokens[0], tokens[1]);
        }
        return mappings;
    }

    private List<String> _prependedAttrs;

    private void addPrependedRns(String prependedAttrs) {
        if (_prependedAttrs == null) {
            _prependedAttrs = new ArrayList<String>();
        }
        if (!Util.isEmpty(prependedAttrs)) {
            CSVSource csv = new CSVSourceImpl(prependedAttrs, CSVType.WithOutHeader);
            String[] attrs = csv.getCurrentLine();
            for (String attr : attrs) {
                _prependedAttrs.add(attr);
            }
        }
    }

    private Map<String, String> _staticAttrs;
    
    public void setStaticDnAttrs(String staticAttrs) throws IOException, CSVIllegalOperationException {
        if (_staticAttrs == null) {
            _staticAttrs = new HashMap<String, String>();
        }
        if (!Util.isEmpty(staticAttrs)) {
            CSVSource csv = new CSVSourceImpl(staticAttrs, CSVType.WithOutHeader);
            String[] attrs = csv.getCurrentLine();
            for (String staticAttr : attrs) {
                String[] tokens = staticAttr.split("=", 2);
                _staticAttrs.put(tokens[0], tokens[1]);
                addPrefixRn(tokens[0]);
            }
        }
    }

    private void addPrefixRn(String rn) {
        if (_prependedAttrs == null) {
            _prependedAttrs = new ArrayList<String>();
        }
        _prependedAttrs.add(rn);
    }

    private static void init(String[] args) {
        _opts = new GetOpts(CSVToLDIF.class);
        OptionLegend legend = new OptionLegend(OPT_CSV);
        legend.setRequired(true);
        legend.setDescription("Input CSV file");
        _opts.addLegend(legend);
        
        legend = new OptionLegend(OPT_LDIF);
        legend.setRequired(true);
        legend.setDescription("Output LDIF file");
        _opts.addLegend(legend);
        
        legend = new OptionLegend(OPT_INDEX);
        legend.setRequired(true);
        legend.setDescription("Index attribute to merge CSV data");
        _opts.addLegend(legend);
        
        legend = new OptionLegend(OPT_PREFIX_RN);
        legend.setRequired(false);
        legend.setMulti(false);
        legend.setDescription("DN attributes that are already prepended with their own attribute key name");
        _opts.addLegend(legend);
        
        legend = new OptionLegend(OPT_DN_STATIC_ATTR);
        legend.setRequired(false);
        legend.setMulti(false);
        legend.setDescription("DN attributes that have static value for all entries. key=value format");
        _opts.addLegend(legend);
        
        legend = new OptionLegend(OPT_DN_RDN);
        legend.setDescription("RDNs that compose the DN");
        legend.setMulti(false);
        legend.setRequired(false);
        _opts.addLegend(legend);
        
        legend = new OptionLegend(OPT_OUTPUT_FIELDS);
        legend.setMulti(false);
        legend.setDescription("Comma delimited values of fields to output in order. Note that 'dn' is always outputted as the first attribute and distinguishedName is always included");
        legend.setDefaultValue("changetype,cn,sn,givenName,description,name,sAMAccountName,objectCategory");
        _opts.addLegend(legend);
        
        legend = new OptionLegend(OPT_MAP_FIELDS);
        legend.setMulti(false);
        legend.setRequired(false);
        legend.setDescription("CSV key value pairs of the csv column to target ldif attribute: acctId=cn,fullName=displayName,first=name");
        _opts.addLegend(legend);
        
        _opts.setDescriptionTail("Given the potential complexity of command line options, one should consider using properties files to pass options");
        _opts.parseOpts(args);
    }

    private CSVSourceObject _src;
    private Writer _ldifOut;
    //private List<String> _prefixRns;
    private List<String> _dnRdns;
    
    
    public CSVToLDIF() {
        
    }
    
    
    public void setCsvSource(CSVSourceObject src) {
        this._src = src;
    }
    
    public void setLdifOutput(Writer out) {
        this._ldifOut = out;
    }
    
    public void setDnRdns(String dnRdns) {
        if (this._dnRdns == null) {
            this._dnRdns = new ArrayList<String>();
        }
        if (!Util.isEmpty(dnRdns)) {
            CSVSource csv = new CSVSourceImpl(dnRdns, CSVType.WithOutHeader);
            String[] rns = csv.getCurrentLine();
            for (String rn : rns) {
                this._dnRdns.add(rn);
            }
        }
    }
    
    private String getRdn(String key, Map<String, Object> dataMap) {
        Object value = dataMap.get(key);
        if (value instanceof String) {
            return getRdn(key, (String)value);
        } else {
            //?
            return null;
        }
    }
    
    private String getRdn(String key, String value) {
        StringBuilder buff = new StringBuilder();
        String mappedKey = getMappedKey(key);
        buff.append(mappedKey).append(" : ");
        /*
        if (!Util.isEmpty(_prefixRdns)) {
            if (_prefixRdns.contains(key)) {
                buff.append(key).append("=");
            }
        }
        */
        buff.append(value);
        return buff.toString();
    }
    
    private void mergeStaticAttrs(Map<String, Object> dataMap) {
        if (!Util.isEmpty(_staticAttrs)) {
            for (String key : _staticAttrs.keySet()) {
                // note: static attributes overwrites data
                dataMap.put(key, _staticAttrs.get(key));
            }
        }
    }
    
    private String getDn(Map<String, Object> dataMap) {
        mergeStaticAttrs(dataMap);
        StringBuilder dn = new StringBuilder();
        dn.append("dn: ");
        if (!Util.isEmpty(_dnRdns)) {
            for (String dnRdn : _dnRdns) {
                String rnValue = (String)dataMap.get(dnRdn);
                if (!Util.isEmpty(rnValue)) {
                    if (Util.isEmpty(_prependedAttrs) || !_prependedAttrs.contains(dnRdn)) {
                        dn.append(dnRdn).append("=");
                    }
                    dn.append(rnValue).append(",");
                }
            }
            dn.deleteCharAt(dn.length()-1);
            dn.append("\n");
        } else {
            // if they didn't provide dnrns, then just return any value
            // that's udner key 'dn'
            dn.append(dataMap.get("dn"));
        }
        return dn.toString();
    }
    
    public void writeLdif() throws IOException, CSVIllegalOperationException {
        Map<String, Object> next = _src.getNextObject();
        while (next != null) {
            StringBuilder buff = new StringBuilder();
            buff.append(getDn(next));
            for (String key : next.keySet()) {
                Object value = next.get(key);
                if (value instanceof String) {
                    buff.append(getRdn(key, next)).append("\n");
                } else {
                    if (value instanceof Collection) {
                        Collection c = (Collection)value;
                        for (Object nextValue : c) {
                            buff.append(getRdn(key, (String)nextValue)).append("\n");
                        }
                    }
                }
            }
            buff.append("\n");
            _ldifOut.write(buff.toString());
            next = _src.getNextObject();
        }
        _ldifOut.flush();
        _ldifOut.close();
    }
    
    private void writeObjectType(StringBuilder buff) {
        
    }

    private String getMappedKey(String key) {
        String mappedKey = key;
        if (_mappings != null && _mappings.containsKey(key)) {
            mappedKey = _mappings.get(key);
        }
        return mappedKey;
    }
}
