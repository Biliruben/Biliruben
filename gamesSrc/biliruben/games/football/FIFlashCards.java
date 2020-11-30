package biliruben.games.football;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionLegend;

import biliruben.games.football.object.Article;
import biliruben.games.football.object.Interpretation;
import biliruben.games.football.object.Rule;
import biliruben.games.football.object.Section;
import biliruben.util.Util;

public class FIFlashCards {

    private static final String OPT_TESTS = "tests";
    private static final String OPT_RULE = "rule";
    private static final String OPT_FILE = "file";
    private static final String OPT_INCLUDE_3_AND_1 = "3and1";
    private static GetOpts _opts;
    private static Scanner _scanner;
    private static Boolean _test3and1;

    public static void main(String[] args) throws IOException {
        init(args);
        _scanner = new Scanner(System.in);
        String fileName = _opts.getStr(OPT_FILE);
        int ruleNumber = Integer.valueOf(_opts.getStr(OPT_RULE));
        int tests = Integer.valueOf(_opts.getStr(OPT_TESTS));
        _test3and1 = Boolean.valueOf(_opts.getStr(OPT_INCLUDE_3_AND_1));
        File file = new File(fileName);
        FIParser parser = new FIParser(file);
        List<Rule> rules = parser.getRules();
        Rule rule = Rule.getRule(ruleNumber, rules);
        if (rule != null) {
            testRule(tests, rule);
        } else {
            System.out.println("Rule " + ruleNumber + " wasn't found!");
            return;
        }
    }
    
    private static void presentInterpretation(PrintStream out, Interpretation interpretation) {
        Article article = (Article) interpretation.getParent();
        out.println(article.printUp());
        out.println("\n" + interpretation.getCase());
    }

    private static boolean testInterpretation(Interpretation interpretation) {
        boolean correct = false;
        presentInterpretation(System.out, interpretation);
        System.out.println("Ruling?");
        String answer = _scanner.nextLine();
        System.out.println("\n");
        System.out.println(interpretation.getRuling());
        System.out.println("\nYou said:\n" + answer);
        if (_test3and1) {
            testThreeAndOne(interpretation);
        }
        System.out.print("Were you correct? [y/n] ");
        String response = _scanner.nextLine();
        char choice = response != null ? response.charAt(0) : 'n';
        correct = choice == 'y';
        return correct;
    }
    
    private static int makeChoice(PrintStream out, String question, List<String> choices) {
        if (Util.isEmpty(choices)) {
            throw new NullPointerException("No choices were provided.");
        }
        out.println(question);
        out.print("> ");
        for (int i = 0; i < choices.size(); i++) {
            out.println(i + ". " + choices.get(i));
        }
        char response = _scanner.nextLine().charAt(0);
        int intResponse = Integer.valueOf(response);
        if (intResponse < 1 || intResponse > choices.size()) {
            throw new IllegalArgumentException(intResponse + " is not a valid choice.");
        }
        return intResponse;
    }
    
    private static boolean testThreeAndOne(Interpretation interpretation) {
        boolean correct = false;
        presentInterpretation(System.out, interpretation);
        System.out.println("Is this a penalty?");
        String response = _scanner.nextLine();
        char choice = response != null ? response.charAt(0) : 'n';
        if (choice == 'n') {
            correct = true;
            return correct;
        }
        // 3 and 1 applies
        // What kind of play

        // Who fouled?
        // 1. Team in possession
        // 2. Team not in possession
        List<String> choices = new ArrayList<String>();
        choices.add("Team in possession");
        choices.add("Team not in possession");
        int whoFouled = makeChoice(System.out, "Who fouled?", choices);

        // 1. Running
        // 2. Passing
        // 3. Kicking
        choices = new ArrayList<String>();
        choices.add("Running play");
        choices.add("Passing play");
        choices.add("Kicking play");
        int play = makeChoice(System.out, "What kind of play was it?", choices);

        int runningEnd = 0;
        int psk = 0;
        choices = new ArrayList<String>();
        switch (play) {
            // Running - Where did the play end
            // 1. Behind the NZ
            // 2. Beyond the NZ
            // 3. Without the NZ
            // 4. In the EZ, after team possession
            case 1: choices.add("Behind the NZ");
                choices.add("Beyond the NZ / There is no NZ");
                choices.add("In the EZ after a turnover");
                runningEnd = makeChoice(System.out, "Where did the related run end?", choices);
                break;
            // Kicking - Is it PSK?
            case 3: choices.add("Yes");
                choices.add("No");
                psk = makeChoice(System.out, "Is the foul goverened by PSK?", choices);
                break;
        }

        choices = new ArrayList<String>();
        choices.add("Previous Spot");
        choices.add("Succeeding Spot");
        choices.add("End Of The Related Run");
        choices.add("Goal Line");
        choices.add("Postscrimmage Kick Spot");

        // What's the basic spot?
        int basicSpot = makeChoice(System.out, "Where is the basic spot?", choices);
        
        // did they fould behind or beyond the BS?
        choices = new ArrayList<String>();
        choices.add("Before The Basic Spot");
        choices.add("Beyond the Basic Spot");
        int whereFouled = makeChoice(System.out, "Where is the foul in relation to the Basic Spot?", choices);
        
        // Where is the penalty enforced?
        choices.add("Basic Spot");
        choices.add("Spot of the Foul");
        int whereEnforced = makeChoice(System.out, "Where is the penalty enforced from?", choices);
        
        // There are two things to test here:
        //  - Basic Spot
        //  - Enforcement from BS or SOF
        int actualBasicSpot = 0;
        switch(play) {
            case 1: // running play
                switch(runningEnd) {
                    case 1:
                }
        }
        return correct;
    }

    private static void testRule(int tests, Rule rule) {
        Rule failedRule = rule.copy();
        List<Interpretation> allInterpretations = new ArrayList<Interpretation>();
        for (Section section : rule.getEntries()) {
            for (Article article : section.getEntries()) {
                allInterpretations.addAll(article.getEntries());
            }
        }

        Collections.shuffle(allInterpretations);
        int actualTests = tests < allInterpretations.size() ? tests : allInterpretations.size(); // which ever is smaller
        System.out.println("Total " + allInterpretations.size() + " interpretations in " + rule);
        System.out.println("Testing " + actualTests + " interpretations.");
        Iterator<Interpretation> iterator = allInterpretations.iterator();
        List<Interpretation> incorrectResponses = new ArrayList<Interpretation>();
        for (int i = 0; i < actualTests && iterator.hasNext(); i++) {
            Interpretation next = iterator.next();
            int questionNumber = i + 1;
            System.out.println("Question #" + questionNumber + ".");
            if (!testInterpretation(next)) {
                incorrectResponses.add(next);
                failedRule.merge(next.copyUp());
            }
            System.out.println("____________________________________________________________________");
        }
        System.out.println("Incorrect rulings: ");
        /*
        for (Interpretation wrongAnswer : incorrectResponses) {
            System.out.println(wrongAnswer.printUp());
        }
        */
        System.out.println(failedRule.print());
        
    }

    private static void init(String[] args) {
        _opts = new GetOpts(GetOpts.class);

        OptionLegend legend = new OptionLegend(OPT_FILE, "File to parse");
        legend.setRequired(true);
        _opts.addLegend(legend);

        legend = new OptionLegend(OPT_RULE, "Rule to test");
        legend.setInteractive(true);
        _opts.addLegend(legend);

        legend = new OptionLegend(OPT_TESTS, "Number of tests");
        legend.setRequired(false);
        legend.setDefaultValue("10");
        _opts.addLegend(legend);
        
        legend = new OptionLegend(OPT_INCLUDE_3_AND_1, "Include testing 3-and-1");
        legend.setFlag(true);
        _opts.addLegend(legend);

        _opts.parseOpts(args);
    }

}
