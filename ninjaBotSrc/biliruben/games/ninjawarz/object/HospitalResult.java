package biliruben.games.ninjawarz.object;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class HospitalResult extends JSONObject {

    /* {"result":"success","gold_spent":18803} */
    private String result;
    private int gold_spent;
    private String error;
    private boolean isError = false;
    
    public String getResult() {
        return result;
    }
    public void setResult(String result) {
        this.result = result;
    }
    public int getGold_spent() {
        return gold_spent;
    }
    public void setGold_spent(int gold_spent) {
        this.gold_spent = gold_spent;
    }
    
    public void setError(String error) {
        if (error != null && !"".equals(error.trim())) {
            setIsError(true);
        }
        this.error = error;
    }
    
    public String getError() {
        return this.error;
    }
    
    public boolean isError() {
        return isError ;
    }
    
    public void setIsError(boolean isError) {
        this.isError = isError;
    }
}
