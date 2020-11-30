package biliruben.html;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import biliruben.files.FileHandler;

public class ExtractBrickListDescriptionHandler implements FileHandler {
    
    private Map<String, String> _descriptions;

    public ExtractBrickListDescriptionHandler() {
        _descriptions = new HashMap<String, String>();
    }

    @Override
    public void handleFile(File file) {
        System.out.println("Reading " + file);
        try {
            
            Document html = Jsoup.parse(file, null);
            if (html != null) {
                Element imgLink = html.getElementById("imgLink0");
                if (imgLink != null) {
                    Elements imgTags = imgLink.getElementsByTag("IMG");
                    if (imgTags != null && !imgTags.isEmpty()) {
                        for (Element imgTag : imgTags) {
                            String title = imgTag.attr("TITLE");
                            if (title != null && !"".equals(title)) {
                                _descriptions.put(file.getName().split("\\.")[0], title);
                                return;
                            }
                        }
                    }
                }
            
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public Map<String, String> getDescriptions() {
        return _descriptions;
    }

}
