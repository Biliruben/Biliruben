package biliruben.games.football;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionLegend;

import biliruben.games.football.object.AbstractRuleElement;
import biliruben.games.football.object.Interpretation;
import biliruben.games.football.object.Rule;

public class ParseRulesApp {


    private static GetOpts _opts;

    public static void main(String[] args) throws IOException {
        init(args);
        String newFile = _opts.getStr("file");
        File file = new File(newFile);
        FIParser parser = new FIParser(file);
        List<Rule> rules = parser.getRules();
        /*
        for (Rule rule : rules) {
            System.out.println(rule.print());
        }
        */
        Rule rule = Rule.getRule(10, rules);
        //System.out.println(rule.print());
        
        // now the reverse print
        Interpretation element = rule.getSection(1).getArticle(5).getEntries().get(0);
        Interpretation element_dupe = rule.getSection(1).getArticle(5).getEntries().get(0);
        Interpretation element2 = rule.getSection(1).getArticle(5).getEntries().get(2);
        Interpretation element3 = rule.getSection(2).getArticle(2).getEntries().get(2);
        Interpretation element4 = rule.getSection(2).getArticle(2).getEntries().get(5);
        Rule copyUp = (Rule)element.copyUp();
        System.out.println("___________________________________________________" + copyUp.print());
        copyUp.merge(element_dupe.copyUp());
        System.out.println("___________________________________________________" + copyUp.print());
        copyUp.merge(element2.copyUp());
        System.out.println("___________________________________________________" + copyUp.print());
        copyUp.merge(element3.copyUp());
        System.out.println("___________________________________________________" + copyUp.print());
        copyUp.merge(element4.copyUp());
        System.out.println("___________________________________________________" + copyUp.print());
        
    }
    
    private static void init(String[] args) {
        _opts = new GetOpts(ParseRulesApp.class);
        OptionLegend legend = new OptionLegend("file");
        legend.setDescription("text file of rules to parse");
        legend.setRequired(true);
        _opts.addLegend(legend);
        
        _opts.parseOpts(args);
    }

}
