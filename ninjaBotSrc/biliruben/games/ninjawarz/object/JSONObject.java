package biliruben.games.ninjawarz.object;


/**
 * The basis for all other objects deriving from a JSON text.  As it turns out, most objects were implemented after it was
 * realized they all shared result and error attributes.  Thus why so many override these values
 * TODO: clean that up
 * TODO: since all of our POJOs derive from JSON, I could implement a Map-like interface to the Abstract parent (this) such that 'get'
 *  and 'set' can be used for any possible property.  This would simplify the architecture and increase flexibility as the object model
 *  changes.  But it introduces a layer of obfuscation that will make using the POJOs a bit of a mystery. ...somthing to think about
 *  anyways.
 * @author trey.kirk
 * 
 */
public class JSONObject {
    
    private String result;
    private String error;

    public String getResult() {
        return result;
    }
    public void setResult(String result) {
        this.result = result;
    }
    public String getError() {
        return error;
    }
    public void setError(String error) {
        this.error = error;
    }
    
    public String getId() {
        // for those successors who have no real id
        return "0";
    }

}
