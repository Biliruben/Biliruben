package biliruben.games.football;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import biliruben.games.football.object.Article;
import biliruben.games.football.object.Interpretation;
import biliruben.games.football.object.Rule;
import biliruben.games.football.object.Section;

public class FIParser {
    
    private static final String KEY_APPROVED_RULING = "approvedRuling";
    private static final String KEY_NUMBER = "number";
    private static final String KEY_TITLE = "title";
    private List<Rule> _rules;
    private File _file;
    private Map<String, String> _working;
    private Section _currentSection;
    private StringBuilder _irBuffer;
    private boolean _inRule;
    private StringBuilder _ruleTitleBuff;
    private Rule _currentRule;
    private Article _currentArticle;
    private boolean _inArticle;
    private Interpretation _currentIr;
    private boolean _inIr;

    private static Pattern RULE_PATTERN = Pattern.compile("^\\s*RULE (\\d+)*\\s*$");
    private static int RULE_NUMBER_GROUP = 1;

    private static Pattern SECTION_PATTERN = Pattern.compile("^\\s*Section (\\d+)\\.\\s+(.*)\\s*$");
    private static int SECTION_NUMBER_GROUP = 1;
    private static int SECTION_TITLE_GROUP = 2;
    
    private static Pattern ARTICLE_PATTERN = Pattern.compile("^(.*)[�—]ARTICLE\\s*(\\d+)\\s*$");
    private static int ARTICLE_TITLE_GROUP = 1;
    private static int ARTICLE_NUMBER_GROUP  = 2;
    
    private static Pattern APPROVED_RULING_PATTERN = Pattern.compile("\\s*Approved Ruling\\s*(.*)$");
    private static int APPROVED_RULING_GROUP = 1;
    
    private static Pattern INTERPRETATION_PATTERN = Pattern.compile("([IVX]+)\\.\\s+(.*)$");
    private static int INTERPRETATION_NUMBER_GROUP = 1;
    private static int INTERPRETATION_TEXT_GROUP = 2;
    
    private static Pattern ONLY_WS_PATTERN = Pattern.compile("^\\s*$");

    public FIParser(File file) {
        this._file = file;
    }
    
    public List<Rule> getRules() throws IOException {
        if (this._rules == null) {
            parseFile();
        }
        return new ArrayList<Rule>(this._rules);
    }

    private void parseFile() throws IOException {
        // parse each line of the file
        // for each line, match on a series of patterns and and call hanlder methods
        String line = null;
        InputStreamReader reader = new InputStreamReader(new FileInputStream(_file), StandardCharsets.US_ASCII);
        BufferedReader buffer = null;
        _rules = new ArrayList<Rule>();
        try {
            buffer = new BufferedReader(reader);
            do {
                line = buffer.readLine();
                if (line != null) {
                    Matcher matcher = ONLY_WS_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        // all white space, move on
                        continue;
                    }
                    matcher = RULE_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        String ruleNumberStr = matcher.group(1);
                        handleRule(ruleNumberStr);
                        continue;
                    }
                    matcher = SECTION_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        String sectionNumberStr = matcher.group(SECTION_NUMBER_GROUP);
                        String sectionTitle = matcher.group(SECTION_TITLE_GROUP);
                        handleSection(sectionNumberStr, sectionTitle);
                        continue;
                    }
                    matcher = APPROVED_RULING_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        String approvedRuling = matcher.group(APPROVED_RULING_GROUP);
                        handleApprovedRuling(approvedRuling);
                        continue;
                    }
                    matcher = ARTICLE_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        String articleNumber = matcher.group(ARTICLE_NUMBER_GROUP);
                        String articleTitle = matcher.group(ARTICLE_TITLE_GROUP);
                        handleArticle(articleNumber, articleTitle);
                        continue;
                    }
                    matcher = INTERPRETATION_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        String irNumber = matcher.group(INTERPRETATION_NUMBER_GROUP);
                        String irText = matcher.group(INTERPRETATION_TEXT_GROUP);
                        handleIr(irNumber, irText);
                        continue;
                    }
                    // did you get here? It's just random text
                    handleText(line);
                }
            } while (line != null);
            if (_inIr) {
                finishIr();
            }
        } finally {
            buffer.close();
        }
    }
    
    private void handleText(String text) {
        // depending on where we are in the parse, this is a title or some interpretations
        if (_inRule) {
            _ruleTitleBuff.append(text.trim()).append("\n");
        } else {
            // IR instead
            _irBuffer.append(text.trim()).append("\n");
        }
    }
    
    private void finishIr() {
        _currentIr.setRawText(_irBuffer.toString().trim());
    }
    
    private void handleIr(String numeral, String text) {
        if (_inIr) {
            finishIr();
        }
        _irBuffer = new StringBuilder();
        _irBuffer.append(text.trim()).append("\n"); // strip newlines?
        _currentIr = new Interpretation(numeral);
        _currentArticle.addEntry(_currentIr);
        _inIr = true;
    }
    
    private void handleArticle(String number, String title) {
        if (_inIr) {
            finishIr();
            _inIr = false;
        }
        _working = new HashMap<String, String>();
        _working.put(KEY_NUMBER, number);
        _working.put(KEY_TITLE, title);
    }
    
    private void handleApprovedRuling(String approvedRuling) {
        _working.put(KEY_APPROVED_RULING, approvedRuling);
        _currentArticle = new Article(Integer.valueOf(_working.get(KEY_NUMBER)), _working.get(KEY_TITLE), approvedRuling);
        _currentSection.addEntry(_currentArticle);
    }
    
    private void handleSection(String numberStr, String title) {
        if (_inIr) {
            finishIr();
            _inIr = false;
        }
        if (_inRule) {
            // build the rule
            _currentRule = new Rule(Integer.valueOf(_working.get(KEY_NUMBER)), _ruleTitleBuff.toString().trim());
            _rules.add(_currentRule);
            _inRule = false;
        }
        _inArticle = false;
        _currentSection = new Section(Integer.valueOf(numberStr), title);
        _currentRule.addEntry(_currentSection);
    }
    
    private void handleRule (String ruleNumberStr) {
        _working = new HashMap<String, String>();
        _working.put(KEY_NUMBER, ruleNumberStr);
        _ruleTitleBuff = new StringBuilder();
        _inRule = true;
    }
}
