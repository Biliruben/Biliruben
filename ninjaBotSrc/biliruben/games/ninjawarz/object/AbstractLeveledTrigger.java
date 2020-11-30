package biliruben.games.ninjawarz.object;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import biliruben.games.ninjawarz.NinjaBotException;
import biliruben.games.ninjawarz.api.Event;

abstract class AbstractLeveledTrigger extends AbstractTrigger {

    public static final String KEY_TRIGGER_DATA_TARGET_LEVEL = "currentLevel";

    public AbstractLeveledTrigger() {
        super();
    }

    @Override
    protected boolean internalEvaluate() throws NinjaBotException {
        Clan clan = null;
        try {
            clan = getBot().getClan();
        } catch (IOException e) {
            getBot().logError(e); // let the next test throw us out
        }
        if (clan == null) {
            throw new NinjaBotException("Unabled to evaluate trigger, clan is null");
        }
        Map<String, Object> data = getTriggerData();
        Long targetLevel = Long.valueOf(String.valueOf(data.get(KEY_TRIGGER_DATA_TARGET_LEVEL)));
        if (targetLevel == null) {
            // hasn't been set yet.  Return false and set this level as the current for the next eval
            getBot().getOutput().println("No target level set, setting to " + clan.getLevel());
            data.put(KEY_TRIGGER_DATA_TARGET_LEVEL, new Long(clan.getLevel()));
        } else {
            try {
                long currentLevel = getBot().getClan().getLevel();
                if (currentLevel > targetLevel) {
                    // in addition to returning true, set the next target level
                    data.put(KEY_TRIGGER_DATA_TARGET_LEVEL, currentLevel);
                    setMatchedObject(clan);
                    return true;
                }
            } catch (IOException e) {
                throw new NinjaBotException(e);
            }
        }
        return false;
    }
    

    @Override
    public String toString() {
        return "Leveled Trigger: " + getTriggerData();
    }
    
    private boolean testBattle(Battle battle) {
        // leveled flag is part of the Spoils or part of the battle
        if (battle.isLeveled()) {
            return true;
        } else {
            if (battle.getSpoils() != null) {
                return battle.getSpoils().isLeveled();
            } else {
                // battle is false, no spoils
                return false;
            }
        }
    }

    @Override
    public boolean matchEvent(JSONObject eventData) {
        if (eventData instanceof Battle) {
            boolean matched = testBattle((Battle)eventData);
            if(matched) {
                setMatchedObject(((Battle)eventData).getOpponent());
            }
            return matched;
        } else {
            return false;
        }
    }

    @Override
    public Collection<Class<? extends JSONObject>> getEventClasses() {
        List<Class<? extends JSONObject>> clazzes = new ArrayList<Class<? extends JSONObject>>();
        clazzes.add(Battle.class);
        return clazzes;
    }

    @Override
    public String getRecommendedFrequency() {
        return "00:05:00";
    }

}
