package biliruben.html;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import biliruben.files.FileHandler;

public class ExtractBrickListPriceHandler implements FileHandler {
    
    // CashBrick (937) Qty: 32 Each: ~US $0.02 (EUR 0.017)   Catalog Entry | Add to My Wanted List
    private static final String PATTERN = ".*Each: ~US \\$(\\d*\\.\\d\\d).*";
    private Pattern _pattern;
    private Map<String, Double> _prices;


    //3. Locate search result table
    
    //4. For each row, Locate Cell with price element
    
    //5. Extract the exact price
    
    public ExtractBrickListPriceHandler() {
        _pattern = Pattern.compile(PATTERN, Pattern.MULTILINE);
        _prices = new HashMap<String, Double>();
    }


    @Override
    public void handleFile(File file) {
        System.out.println("Reading " + file);
        try {
            List<Double> prices = new ArrayList<Double>();
            Document html = Jsoup.parse(file, null);
            if (html != null) {
                Element body = html.body();
                if (body != null) {
                    // Target table is BODY.CENTER.DIV.TABLE.TABLE
                    // Table TR looks a lot like:
                    // <TR BGCOLOR="FFFFFF" CLASS="tm">
                    Elements tmClasses = body.getElementsByClass("tm");
                    for (Element tmClass : tmClasses) {
                        Elements fontElements = tmClass.getElementsByTag("FONT");
                        if (fontElements != null) {
                            for (Element fontElement : fontElements) {
                                String text = fontElement.text();
                                Matcher matcher = _pattern.matcher(text);
                                if (matcher.matches()) {
                                    String price = matcher.group(1);
                                    //System.out.println(price);
                                    prices.add(Double.valueOf(price));
                                }
                            }
                        }
                    }
                }
            }
            
            double average = average(prices);
            _prices.put(file.getName().split("\\.")[0], average);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public Map<String, Double> getPrices() {
        return _prices;
    }


    private double average(List<Double> prices) {
        if (prices == null || prices.isEmpty()) {
            return 0L;
        }
        double sum = 0L;
        for (Double d : prices) {
            sum += d;
        }
        return sum / prices.size();
    }
}
