package biliruben.games.football.object;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import biliruben.util.Util;

public class Interpretation extends AbstractRuleElement {
    
    private static Pattern INTERPRETATION_RULING_PATTERN = Pattern.compile("^(.*)\\s*RULING:\\s*(.*)$", Pattern.DOTALL);
    private static int CASE_GROUP = 1;
    private static int RULING_GROUP = 2;
    
    private String _numeral;
    private String _case;
    private String _ruling;

    public Interpretation(String numeral) {
        // no title
        super(null);
        this._numeral = numeral;
    }
    
    public String getNumeral() {
        return this._numeral;
    }
    
    public void setRawText(String rawText) {
        Matcher m = INTERPRETATION_RULING_PATTERN.matcher(rawText);
        if (m.matches()) {
            _case = m.group(CASE_GROUP);
            _ruling = m.group(RULING_GROUP);
        }
    }
    
    public String getCase() {
        return _case;
    }
    
    public String getRuling() {
        return _ruling;
    }
    
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        buff.append(_numeral).append(". ").append(_case).append(" RULING: ").append(_ruling).append("\n");
        return buff.toString();
    }
    
    @Override
    public Interpretation copy() {
        Interpretation copy = new Interpretation(_numeral);
        copy._case = _case;
        copy._ruling = _ruling;
        return copy;
    }
    
    @Override
    public boolean equals(Object obj) {
        boolean matches = super.equals(obj);
        if (matches) {
            Interpretation yours = (Interpretation)obj;
            matches = Util.equals(yours.getCase(), this._case) &&
                    Util.equals(yours._ruling, this._ruling) &&
                    Util.equals(yours.getNumeral(), this._numeral);
        }
        return matches;
    }
    
    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash += this._case.hashCode() * 7;
        hash += this._ruling.hashCode() * 5;
        hash += this._numeral.hashCode() * 11;
        return hash;
    }
}
