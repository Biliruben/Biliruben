package biliruben.games.ninjawarz.object;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Result extends JSONObject {

    private String result;
    private int sold_for;
    private String error;
    private Spoils spoils;
    
    public String getResult() {
        return result;
    }
    public void setResult(String result) {
        this.result = result;
    }
    public int getSold_for() {
        return sold_for;
    }
    public void setSold_for(int sold_for) {
        this.sold_for = sold_for;
    }
    public String getError() {
        return error;
    }
    public void setError(String error) {
        this.error = error;
    }
    public Spoils getSpoils() {
        return spoils;
    }
    public void setSpoils(Spoils spoils) {
        this.spoils = spoils;
    }
    
    
}
