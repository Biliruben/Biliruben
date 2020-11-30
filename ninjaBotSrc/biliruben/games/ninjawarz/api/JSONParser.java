package biliruben.games.ninjawarz.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.Util;
import biliruben.games.ninjawarz.object.Clan;
import biliruben.games.ninjawarz.object.JSONObject;

/**
 * Wraps the Jackson JSON parser and parses provided JSON into a {@link JSONObject}.  Since
 * JSONParser is the broker between JSON data and the object, it becomes the single point
 * of focus for event handling
 * @author trey.kirk
 *
 */
public class JSONParser {

    private ObjectMapper _mapper;
    private NinjaBot _bot;
    private TriggerEvaluator _eventHandler;

    public JSONParser(NinjaBot bot) {
        _mapper = new ObjectMapper();
        _bot = bot;
        _eventHandler = _bot.getTriggerEvaluator();
    }
    
    public void handleEvent(JSONObject object) {
        if (object != null) {
            _eventHandler.evaluateEvent(object);
        }
    }
    
    protected String filterNonsense(String json) {
        // This method is used to alter the json pre-parsing in a manner that voids any anticipatable problems.
        // For example, some json comes in with a class representation of data.  Other times, instead of a null
        // value, it simply comes in as 'false'.  This causes grief with any bean that relies on this object.  So
        // convert known cases to null values
        //
        // like "bosses":false
        String ret = json.replace(",\"bosses\":false", "");  // Some day I may need to do some regex validation as to avoid
                                                             // pruning a legit 'bosses' boolean field
        ret = json.replace(",\"bosses\":[]", "");
        return ret;
    }

    public <T extends JSONObject> T parseJSON (String json, Class<T> type) throws IOException {
        T obj = null;
        _bot.getLoggingStream().println("JSON: " + json);
        // text sent to us is html escaped.  Undo
        json = Util.descape(json);
        json = filterNonsense(json);
        obj = _mapper.readValue(json, type);
        handleEvent(obj);
        return obj;
    }

    public <T extends JSONObject> List<T> parseJSONList (String json, Class<T> type) throws IOException {
        _bot.getLoggingStream().println("JSON: " + json);
        json = filterNonsense(json);
        JsonParser jp = new JsonFactory().createJsonParser(json);
        jp.nextToken();

        List<T> objects = new ArrayList<T>();
            while (jp.nextToken() != JsonToken.END_ARRAY) {
                T obj = _mapper.readValue(jp, type);
                objects.add(obj);
                handleEvent(obj);
            }
        return objects;
    }

    public List<Clan> parseOpponentListJSON(String opponentJSON) throws IOException {
        List<Clan> opps = new ArrayList<Clan>();
        _bot.getLoggingStream().println("JSON: " + opponentJSON);
        opponentJSON = filterNonsense(opponentJSON);
            JsonParser jp = new JsonFactory().createJsonParser(opponentJSON);
            jp.nextToken();

            while (jp.nextToken() != JsonToken.END_ARRAY) {
                Clan him = _mapper.readValue(jp, Clan.class);
                opps.add(him);
            }
        return opps;
    }

    public String toJson(Object fromObject) throws JsonGenerationException, JsonMappingException, IOException {
        String json = _mapper.writeValueAsString(fromObject);
        return json;
    }

}
