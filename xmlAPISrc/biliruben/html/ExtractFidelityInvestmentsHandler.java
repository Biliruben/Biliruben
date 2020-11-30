package biliruben.html;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.biliruben.util.csv.CSVRecord;
import com.biliruben.util.csv.CSVUtil;

import biliruben.files.FileHandler;
import biliruben.tools.CSVPrint;
import biliruben.util.Util;

public class ExtractFidelityInvestmentsHandler implements FileHandler{

    @Override
    public void handleFile(File file) {
        System.out.println("Reading " + file);
        try {
            Document html = Jsoup.parse(file, null);
            List<Map<String, String>> investments = new ArrayList<Map<String, String>>();
            if (html != null) {
                Element annRetEl = html.getElementById("annRet");
                Elements rows = annRetEl.getElementsByTag("tr");
                if (!Util.isEmpty(rows)) {
                    for (Element row : rows) {
                        Map<String, String> investment = new HashMap<String, String>();
                        int cellCount = 0;
                        Elements cells = row.getElementsByTag("td");
                        if (!Util.isEmpty(cells)) {
                            investments.add(investment);
                            for (Element cell : cells) { 
                                cellCount++;
                                switch (cellCount) {
                                case 1:
                                    Elements aTags = cell.getElementsByTag("a");
                                    if (aTags != null && aTags.size() > 0) {
                                        // just need the first one
                                        Element aTag = aTags.get(0);
                                        if (Util.isEmpty(aTag.id())) {
                                            // we only care about the <a> tags without ids
                                            String description = aTag.text();
                                            //System.out.println("a: " + aTag);
                                            //System.out.println("cdata: " + description);
                                            investment.put("description", description);
                                        }
                                    }
                                    break;
                                case 2:
                                    // td 2: Asset Class
                                    String asset = cell.text();
                                    investment.put("asset", asset);
                                    break;
                                case 3:
                                    // td3: CAtegory
                                    String category = cell.text();
                                    investment.put("category", category);
                                    break;
                                case 4:
                                    // td4: 1 year
                                    String year1 = cell.text();
                                    investment.put("1 year", year1);
                                    break;
                                case 5:
                                    // td5: 3 years
                                    String year3 = cell.text();
                                    investment.put("3 year", year3);
                                    break;
                                case 6:
                                    // td6: 5 year
                                    String year5 = cell.text();
                                    investment.put("5 year", year5);
                                    break;
                                case 7:
                                    // td7: 10 Year / LOF
                                    String lof = cell.text();
                                    investment.put("10 year / LOF", lof);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
           String[] fields = new String[] {"description", "asset", "category", "1 year", "3 year", "5 year", "10 year / LOF"}; 
           CSVRecord rec = new CSVRecord(fields);
           for (Map<String, String> investment : investments) {
               rec.addLine(investment);
           }
           for (String line : rec){
               System.out.print(line);
           }
        } catch (IOException e) {
            System.out.println("Error processing file: " + file);
            e.printStackTrace();
        }
    }

}
