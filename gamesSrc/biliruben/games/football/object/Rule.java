package biliruben.games.football.object;

import java.util.Collection;

public class Rule extends AbstractRuleElement<Section>{
    
    private int _number;
    
    public static Rule getRule(int ruleNumber, Collection<Rule> rules) {
        for (Rule rule : rules) {
            if (rule.getNumber() == ruleNumber) {
                return rule;
            }
        }
        return null;
    }
    
    public Rule(int number, String title) {
        super(title);
        this._number = number;
    }

    public int getNumber() {
        return _number;
    }
    
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        buff.append("\nRULE ").append(_number).append("\n").append(getTitle()).append("\n");
        return buff.toString();
    }
    
    public Section getSection(int sectionNumber) {
        for (Section section : getEntries()) {
            if (section.getNumber() == sectionNumber) {
                return section;
            }
        }
        return null;
    }

    @Override
    public Rule copy() {
        Rule copy = new Rule(_number, getTitle());
        return copy;
    }
    
    
    @Override
    public boolean equals(Object obj) {
        boolean matches = super.equals(obj);
        if (matches) {
            // it's a Rule and its title matches
            matches = ((Rule)obj)._number == this._number;
        }
        return matches;
    }
    
    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash += _number * 7;
        return hash;
    }
}
