package biliruben.games.ninjawarz.object;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import biliruben.games.ninjawarz.NinjaBotException;
import biliruben.games.ninjawarz.api.Event;

public class ErrorEvent extends AbstractTrigger implements Event {

    @Override
    public boolean matchEvent(JSONObject eventData) {
        if (eventData.getError() != null) {
            setMatchedObject(eventData);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Collection<Class<? extends JSONObject>> getEventClasses() {
        List<Class<? extends JSONObject>> classes = new ArrayList<Class<? extends JSONObject>>();
        classes.add(JSONObject.class);
        return classes;
    }

    @Override
    protected boolean internalEvaluate() throws NinjaBotException {
        // not a timer, so always return false
        return false;
    }

}
