package biliruben.games.ninjawarz.object;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import biliruben.games.ninjawarz.api.Event;

public class LossEvent extends AbstractTrigger implements Event{

    @Override
    public boolean matchEvent(JSONObject eventData) {
        if (eventData instanceof Battle) {
            Battle battle = (Battle)eventData;
            if (!"win".equals(battle.getResult())) {
                setMatchedObject(battle.getOpponent());
                return true;
            }
        }
        return false;
    }

    @Override
    public Collection<Class<? extends JSONObject>> getEventClasses() {
        List<Class<? extends JSONObject>> classes = new ArrayList<Class<? extends JSONObject>>();
        classes.add(Battle.class);
        return classes;
    }

}
