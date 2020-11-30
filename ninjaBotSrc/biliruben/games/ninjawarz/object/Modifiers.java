package biliruben.games.ninjawarz.object;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Modifiers extends JSONObject {
    
    private long modified_power;
    private long modified_magic;
    public long getModified_power() {
        return modified_power;
    }
    public void setModified_power(long modified_power) {
        this.modified_power = modified_power;
    }
    public long getModified_magic() {
        return modified_magic;
    }
    public void setModified_magic(long modified_magic) {
        this.modified_magic = modified_magic;
    }
    
    

}
