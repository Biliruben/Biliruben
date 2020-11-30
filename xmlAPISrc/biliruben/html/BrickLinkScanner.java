package biliruben.html;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import biliruben.util.Util;

import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionLegend;
import com.biliruben.util.csv.CSVRecord;
import com.biliruben.util.csv.CSVSource;
import com.biliruben.util.csv.CSVSourceImpl;
import com.biliruben.util.csv.CSVUtil;

public class BrickLinkScanner {

    public static final String PARAM_LOC = "loc";
    public static final String PARAM_SS = "ss";
    public static final String PARAM_RPP = "rpp";
    //private static final String ALPHA_BRICKLINK_AJAX = "http://alpha.bricklink.com/ajax/clone/catalogifs.ajax?rpp=1000&ss=US&nosuperlot=1&loc=US&ccName=";
    private static final String ALPHA_BRICKLINK_AJAX = "http://alpha.bricklink.com/ajax/clone/catalogifs.ajax?nosuperlot=1&cond=N";
    private static final String URL_BRICKLINK_COM_FOR_PART = "http://alpha.bricklink.com/pages/clone/catalogitem.page?rpp=500&view=buy&ccName=";
    private String _csvQuantityField;
    private static final int BRICKSET_TIMEOUT = 10000;
    private static final String URL_BRICKSET_COM = "http://brickset.com/parts/";
    private String _csvPartNumberField;
    private static final String ALPHA_BRICKLINK_AJAX_CCNAME_PARAM = "ccName";
    private static final boolean DEBUG_JSON = true;
    private static GetOpts _opts;
    private static String _partsListFile;
    private Map<String, Integer> _partsList;
    private Map<String, Document> _brickLinkDocs;
    private static String _outputCsv;
    private static HashMap<String, String> _staticParamMap;
    private Map<String, String> _paramMap;
    
    public BrickLinkScanner(String partsListFile) throws IOException {
        this(partsListFile, "part number", "quantity");
    }
    
    public BrickLinkScanner(String partsListFile, String partNumberFieldName, String qtyFieldName) throws IOException {
        setPartNumberField(partNumberFieldName);
        setQuantityField(qtyFieldName);
        readInList(partsListFile);
        _paramMap = new HashMap<String, String>();
    }
    
    private void setPartNumberField(String fieldName) {
        this._csvPartNumberField = fieldName;
    }
    
    private void setQuantityField(String fieldName) {
        this._csvQuantityField = fieldName;
    }
    
    public static Map<String, String> getDefaultParamMap() {
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put(PARAM_LOC, "US");
        paramMap.put(PARAM_RPP, "1000");
        paramMap.put(PARAM_SS, "US");
        return paramMap;
    }
    
    public Map<String, String> getParamMap() {
        return _paramMap;
    }
    
    public void setParamMap(Map<String, String> paramMap) {
        _paramMap = new HashMap<String, String>(paramMap);
    }
    
    private static void init(String[] args) {
        getOpts();
        
        _opts.parseOpts(args);
        
        _partsListFile = _opts.getStr("partsList");
        _outputCsv = _opts.getStr("csvOut");
        _staticParamMap = new HashMap<String, String>();
        if (!Util.isEmpty(_opts.getStr(PARAM_LOC))) {
            _staticParamMap.put(PARAM_LOC, _opts.getStr(PARAM_LOC));
        }
        if (!Util.isEmpty(_opts.getStr(PARAM_RPP))) {
            _staticParamMap.put(PARAM_RPP, _opts.getStr(PARAM_RPP));
        }
        if (!Util.isEmpty(_opts.getStr(PARAM_SS))) {
            _staticParamMap.put(PARAM_SS, _opts.getStr(PARAM_SS));
        }
    }
    
    public static GetOpts getOpts() {
        _opts = new GetOpts(BrickLinkScanner.class);
        
        OptionLegend legend = new OptionLegend("partsList");
        legend.setRequired(true);
        legend.setDescription("CSV file of parts list with quantities column");
        _opts.addLegend(legend);
        
        legend = new OptionLegend("csvOut");
        legend.setRequired(true);
        legend.setDescription("CSV data output file");
        _opts.addLegend(legend);
        
        legend = new OptionLegend(PARAM_RPP);
        legend.setRequired(false);
        legend.setDescription("\"1000\"");
        _opts.addLegend(legend);
        
        legend = new OptionLegend(PARAM_SS);
        legend.setRequired(false);
        legend.setDescription("\"US\"");
        _opts.addLegend(legend);
        
        legend = new OptionLegend(PARAM_LOC);
        legend.setRequired(false);
        legend.setDescription("\"US\"");
        _opts.addLegend(legend);
        
        return _opts;
    }
    
    public Map<String, List<Map>> getAllPartsData() throws IOException {
        Map<String, List<Map>> partsData = new HashMap<String, List<Map>>();
        File debugFile = new File("c:\\temp\\brickLinkScanner." + System.currentTimeMillis() % 10000 + ".json");
        System.out.println("Scanner debug: " + debugFile.getAbsolutePath());
        FileWriter writer = new FileWriter(debugFile);
        for (String part : _partsList.keySet()) {
            // GET from bricklink.com jSON and store. How to store?
            if (DEBUG_JSON) {
                String json = getPartJson(part, _partsList.get(part));
                writer.write(part + "," + json + "\n");
            }
            //System.out.println(part + ": " + json);
            // CSV data of all JSON fields
            List<Map> partData = getPartData(part, _partsList.get(part));
            partsData.put(part, partData);
        }
        writer.flush();
        writer.close();
        return partsData;
    }

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        // Read in parts list; iterate
        init(args);
        BrickLinkScanner scanner = new BrickLinkScanner(_partsListFile);
        scanner.setParamMap(_staticParamMap);
        File outputFile = new File(_outputCsv);
        FileWriter writer = new FileWriter(outputFile);
        String[] fields = null;;
        Map<String, List<Map>> partsData = scanner.getAllPartsData();
        for (String part : partsData.keySet()) {
            try{
                List<Map> partData = partsData.get(part);
                CSVRecord jsonCsv = scanner.convertMapToCsv(partData, part);
                if (fields == null) {
                    fields = jsonCsv.getOutputFields();
                } else {
                    jsonCsv.setFields(fields);
                    jsonCsv.setIncludeFieldNames(false);
                }
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                CSVUtil.exportToCsv(jsonCsv, bos);
                writer.write(bos.toString());
            } catch (Exception e) {
                System.out.println("Error on part: " + part);
                e.printStackTrace();
            }
        }
        writer.flush();
        writer.close();
    }
    
    public String fetchDescription(String part) throws IOException {
        Document html = getBricklinkDocument(part);
        if (html != null) {
            Element itemNameTitle = html.getElementById("item-name-title");
            if (itemNameTitle != null) {
                return itemNameTitle.text();
            }
        }
        return null;
    }
    
    private Document getBricklinkDocument(String part) throws IOException {
        if (_brickLinkDocs == null) {
            _brickLinkDocs = new HashMap<String, Document>();
        }
        if (_brickLinkDocs.containsKey(part)) {
            return _brickLinkDocs.get(part);
        }
        // else
        URL brickLink = new URL(URL_BRICKLINK_COM_FOR_PART + part);
        Document html = Jsoup.parse(brickLink, BRICKSET_TIMEOUT);
        if (html == null) {
            // ensures we still only try once
            html = new Document(brickLink.toString());
        }
        _brickLinkDocs.put(part, html);
        return html;
    }
    
    private String fetchItemNumber(String part) throws IOException {
        Document html = getBricklinkDocument(part);
        Element idViewSmallTemplate = html.getElementById("_idViewSmallTemplate");
        if (idViewSmallTemplate != null) {
            Elements hrefs = idViewSmallTemplate.getElementsByAttribute("href");
            if (hrefs != null && !hrefs.isEmpty()) {
                for (Element hrefElement : hrefs) {
                    String href = hrefElement.attr("href");
                    String itemNo = getQueryParam(new URL("http://" + href), "itemNo");
                    if (itemNo != null && !"".equals(itemNo.trim())) {
                        // found it!
                        return itemNo;
                    }
                }
            }
        }
        return null;
    }
    
    protected static List<Map> parseJson(String json) throws IOException {
        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createJsonParser(json);
        List objectList = null;
        while (!parser.isClosed() && objectList == null) {
            JsonToken token = parser.nextToken();
            if (JsonToken.FIELD_NAME.equals(token) && "list".equals(parser.getCurrentName())) {
                objectList = parseList(parser.nextToken(), parser);
            }
        }
        if (objectList == null || objectList.isEmpty()) {
            // add an empty map
            objectList = new ArrayList();
            objectList.add(new HashMap());
        }
        return objectList;
    }
    
    public List<Map> getPartData(String part, int quantityRequired) throws IOException {
        String json = getPartJson(part, quantityRequired);
        List<Map> objectList = parseJson(json);
        return objectList;
    }
    
    private CSVRecord convertMapToCsv(List<Map> data, String part) throws JsonParseException, IOException {
        // convert list of Maps to CSV
        CSVRecord csv = new CSVRecord();
        for (Map map : data) {
            map.put("part", part);
            String description = fetchDescription(part);
            map.put("description", description);
            map.put("itemId", fetchItemNumber(part));
            csv.addLine(map);
        }
        return csv;
    }
    
    private static List parseList(JsonToken jsonToken, JsonParser parser) throws JsonParseException, IOException {
        if (!JsonToken.START_ARRAY.equals(jsonToken)) {
            throw new IllegalArgumentException("Not at start of an Array");
        }
        List list = new ArrayList();
        jsonToken = parser.nextToken();
        while (!parser.isClosed() && !JsonToken.END_ARRAY.equals(jsonToken)) {
            Object value = null;
            if (JsonToken.START_OBJECT.equals(jsonToken)) {
                value = parseObject(jsonToken, parser);
                list.add(value);
            }
            // a better developer would parse the other scalar values, but fuck that
            jsonToken = parser.nextToken();
        }
        return list;
    }
    
    private static Map parseObject(JsonToken jsonToken, JsonParser parser) throws JsonParseException, IOException {
        if (!JsonToken.START_OBJECT.equals(jsonToken)) {
            throw new IllegalArgumentException("Not at the start of an Object");
        }
        Map object = new HashMap();
        jsonToken = parser.nextToken();
        while (!parser.isClosed() && !JsonToken.END_OBJECT.equals(jsonToken)) {
            // a better developer would parse for other objects and lists, but fuck that
            String key = parser.getCurrentName();
            JsonToken valueToken = parser.nextToken();
            String value = parser.getText();
            if (value != null) {
                // for strings that look like "US $5.23", ditch the "US $" bit
                value = value.replace("US $", "");
            }
            object.put(key, value);
            jsonToken = parser.nextToken();
        }
        return object;
    }
    
    protected String getPartJson(String part, int quantity) throws IOException {
        // http://alpha.bricklink.com/pages/clone/catalogitem.page?view=buy&ccName=300401
        // redirects to...
        // http://alpha.bricklink.com/ajax/clone/catalogifs.ajax?itemid=149&idColor=85&ss=US&minqty=185&nosuperlot=1&loc=US&ccName=4211069
        /*
         *  43
down vote
accepted
    

You need to cast the URLConnection to HttpURLConnection and instruct it to not follow the redirects by setting HttpURLConnection#setInstanceFollowRedirects() to false. You can also set it globally by HttpURLConnection#setFollowRedirects().
         */
        
        URL sourceUrl = new URL(URL_BRICKLINK_COM_FOR_PART + part);
        URLConnection connection = sourceUrl.openConnection();
        connection.connect();
        // invokes the redirect
        connection.getInputStream();
        URL redirect = connection.getURL();
        //System.out.println(redirect);
        // typical redirect:
        // http://alpha.bricklink.com/pages/clone/catalogitem.page?id=280&idColor=1&view=buy&ccName=300401
        // convert into:
        // http://alpha.bricklink.com/ajax/clone/catalogifs.ajax?itemid=149&idColor=85&ss=US&minqty=185&nosuperlot=1&loc=US&ccName=4211069
        String idColor = getQueryParam(redirect, "idColor");
        String itemId = getQueryParam(redirect, "id");
        StringBuilder ajaxUrlBuff = new StringBuilder(ALPHA_BRICKLINK_AJAX);
        if (_paramMap.containsKey(ALPHA_BRICKLINK_AJAX_CCNAME_PARAM) || 
                _paramMap.containsKey("color") ||
                _paramMap.containsKey("itemId")) {
            throw new IllegalArgumentException("Reserved work specified in paramMap");
        }
        for (String paramKey : _paramMap.keySet()) {
            ajaxUrlBuff.append("&").append(paramKey).append("=").append(_paramMap.get(paramKey));
        }

        ajaxUrlBuff.append("&").append(ALPHA_BRICKLINK_AJAX_CCNAME_PARAM).append("=").append(part);
        if (idColor != null) {
            ajaxUrlBuff.append("&color=").append(idColor);
        }
        
        if (itemId != null) {
            ajaxUrlBuff.append("&itemid=").append(itemId);
        }
        
        if (quantity > 0 && !_paramMap.containsKey("minqty")) {
            ajaxUrlBuff.append("&minqty=").append(quantity);
        }
        //System.out.println(ajaxUrlBuff.toString());
        URL ajaxUrl = new URL(ajaxUrlBuff.toString());
        connection = ajaxUrl.openConnection();
        connection.connect();
        InputStream input = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        StringBuffer json = new StringBuffer();
        String str;
        while (null != (str = reader.readLine())) {
            json.append(str);
        }
        input.close();
        //System.out.println(json.toString());
        
        return json.toString();
    }
    
    /*
     * Query param is assumed to have an int value, makes parsing easier
     */
    private static String getQueryParam(URL fromUrl, String param) {
        String query = "&" + fromUrl.getQuery();
        String patternRegex = "^.*&" + param + "=(\\d+).*$";
        Pattern pattern = Pattern.compile(patternRegex, Pattern.DOTALL);
        // prepend the & to use it as a defacto anchor
        Matcher matcher = pattern.matcher(query);
        String queryParamValue = null;
        if (matcher.matches()) {
            queryParamValue = matcher.group(1);
        }
        return queryParamValue;
    }
    
    private static int getColorId(String part) throws IOException {
        String urlText = URL_BRICKSET_COM + part;
        URL url = new URL(urlText);
        Document document = Jsoup.parse(url, BRICKSET_TIMEOUT);
        Elements featureBoxElements = document.getElementsByClass("featurebox");
        // it's the first featurebox
        if (featureBoxElements != null && !featureBoxElements.isEmpty()) {
            Element featureBox = featureBoxElements.get(0);
            // hyperlink element is of class 'plain'. Get all those and find the one we want
            Elements plainElements = featureBox.getElementsByClass("plain");
            Iterator<Element> plainIter = plainElements.iterator();
            while (plainIter.hasNext()) {
                Element nextPlain = plainIter.next();
                if (nextPlain.tag().getName().equals("a") && nextPlain.attr("href").contains("colorPart")) {
                    // found it!
                    String colorIdUrlText = nextPlain.attr("href");
                    String patternRegex = ".*\\\\?.*colorPart=(\\d+)";
                    Pattern pattern = Pattern.compile(patternRegex);
                    Matcher matcher = pattern.matcher(colorIdUrlText);
                    if (matcher.matches()) {
                        String colorIdText = matcher.group(1);
                        int colorId = Integer.valueOf(colorIdText);
                        return colorId;
                    }
                }
            }
        }
        
        // get here? then we didn't find it. return -1
        return -1;
    }
    
    private void readInList(String partsListFile) throws IOException {
        File file = new File(partsListFile);
        if (!file.exists()) {
            throw new FileNotFoundException(_partsListFile);
        }
        _partsList = new HashMap<String, Integer>();
        CSVSource csv = new CSVSourceImpl(file, CSVSource.CSVType.WithHeader);
        for (Map<String, String> line : csv) {
            String part = line.get(_csvPartNumberField);
            String qtyStr = line.get(_csvQuantityField);
            if (part != null && !"".equals(part.trim())) {
                _partsList.put(part, Integer.valueOf(qtyStr));
            }
        }
    }
    
    protected int getQtyForPart(String part) {
        return _partsList.get(part);
    }
    

}
