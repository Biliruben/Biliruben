package biliruben.games.ninjawarz.object;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/*
 * 
 {"result":"success","spoils":{"gold":53937,"karma":0},"wait":14400}
 */

@JsonIgnoreProperties(ignoreUnknown=true)
public class DaimyoVisit extends JSONObject {

    private String result;
    private Spoils spoils;
    private int wait;
    private String error;
    
    public String getResult() {
        return result;
    }
    public void setResult(String result) {
        this.result = result;
    }
    public Spoils getSpoils() {
        return spoils;
    }
    public void setSpoils(Spoils spoils) {
        this.spoils = spoils;
    }
    public int getWait() {
        return wait;
    }
    public void setWait(int wait) {
        this.wait = wait;
    }
    public String getError() {
        return error;
    }
    public void setError(String error) {
        this.error = error;
    }
    
    

}
