package biliruben.games.ninjawarz.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import biliruben.games.ninjawarz.Configuration;
import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.Util;
import biliruben.games.ninjawarz.object.AbstractTrigger;
import biliruben.games.ninjawarz.object.Timer;

public class TriggerService {

    public static final String TRIGGER_KEY_PREFIX = "trigger.";

    private NinjaBot _bot;

    public static List<Trigger> readTriggers(Configuration configuration) throws JsonParseException, JsonMappingException, IOException, ClassNotFoundException {
        Map<String, String> props = configuration.getProperties(TRIGGER_KEY_PREFIX);
        List<Trigger> triggers = new ArrayList<Trigger>();
        // don't care about the property keys as their arbitrary
        for (String propertyValue : props.values()) {
            //class:json
            String[] tokens = propertyValue.split(":", 2);
            String className = tokens[0];
            String json = tokens[1];
            try {
                Class clazz = Class.forName(className);
                Trigger trigger = readTriggerJson(json, clazz);
                triggers.add(trigger);
            } catch (Exception e) {
                // had a problem loading class
                e.printStackTrace(System.err);
            }
        }
        return triggers;
    }

    public TriggerService(NinjaBot bot) {
        _bot = bot;
        try {
            List<Trigger> triggers = readTriggers(_bot.getConfiguration());
            for (Trigger trigger : triggers) {
                _bot.addTrigger(trigger);
            }
        } catch (Exception e) {
            bot.logError(e);
        }
    }

    /**
     * Reads the triggers defined in NinjaBot and updates the Properties with the trigger
     * json values
     */
    public void updateProperties() {
        // step 1: remove the current trigger properties
        Map<String, String> triggerProps = _bot.getConfiguration().getProperties(TRIGGER_KEY_PREFIX);
        for (String property : triggerProps.keySet()) {
            _bot.getConfiguration().removeProperty(TRIGGER_KEY_PREFIX + property); 
        }
        // step 2: add the current trigger properties
        List<Trigger> triggers = _bot.getTriggers();
        for (Trigger trigger : triggers) {
            String json = trigger.toPropertyString();
            _bot.getConfiguration().setProperty(TRIGGER_KEY_PREFIX + trigger.getId(), trigger.getClass().getName() + ":" + json);
        }
    }

    public void printTriggers(Collection<Trigger> triggers) {
        
        int ID_COL_LEN = 15;
        int TYPE_COL_LEN = 30;
        int EXPIRE_COL_LEN = 31;
        int COMMAND_COL_LEN = 20;
        int NEXT_CHECK_COL_LEN = 31;
        int DATA_COL_LEN = 10;

        _bot.getOutput().printf("%-" + ID_COL_LEN + "s", "ID");
        _bot.getOutput().printf("%-" + TYPE_COL_LEN + "s", "Trigger");
        _bot.getOutput().printf("%-" + NEXT_CHECK_COL_LEN + "s", "Next Check");
        //_bot.getOutput().printf("%-" + EXPIRE_COL_LEN + "s", "Expires");
        _bot.getOutput().printf("%-" + COMMAND_COL_LEN + "s", "Commands");
        _bot.getOutput().printf("%-" + DATA_COL_LEN + "s", "Data");

        _bot.getOutput().println();

        _bot.getOutput().printf("%-" + ID_COL_LEN + "s", Util.repeatChar(ID_COL_LEN - 1, '-'));
        _bot.getOutput().printf("%-" + TYPE_COL_LEN + "s", Util.repeatChar(TYPE_COL_LEN - 1, '-'));
        _bot.getOutput().printf("%-" + NEXT_CHECK_COL_LEN + "s", Util.repeatChar(NEXT_CHECK_COL_LEN - 1, '-'));
        //_bot.getOutput().printf("%-" + EXPIRE_COL_LEN + "s", Util.repeatChar(EXPIRE_COL_LEN - 1, '-'));
        _bot.getOutput().printf("%-" + COMMAND_COL_LEN + "s", Util.repeatChar(COMMAND_COL_LEN - 1, '-'));
        _bot.getOutput().printf("%-" + DATA_COL_LEN + "s", Util.repeatChar(DATA_COL_LEN - 1, '-'));

        _bot.getOutput().println();

        if (triggers != null && triggers.size() > 0) {
            for (Trigger trigger : triggers) {
                _bot.getOutput().printf("%-" + ID_COL_LEN + "s", trigger.getId());
                _bot.getOutput().printf("%-" + TYPE_COL_LEN + "s", trigger.getClass().getSimpleName());
                String wakeup = "";
                if (trigger instanceof Timer) {
                    wakeup = new Date(((Timer)trigger).getWakeupTime()).toString();
                }
                _bot.getOutput().printf("%-" + NEXT_CHECK_COL_LEN + "s", wakeup);
                //_bot.getOutput().printf("%-" + EXPIRE_COL_LEN + "s", new Date(trigger.getExpiration()));
                _bot.getOutput().printf("%-" + COMMAND_COL_LEN + "s", Arrays.toString(trigger.getCommandStrings()));
                _bot.getOutput().printf("%-" + DATA_COL_LEN + "s", trigger.getTriggerData());
                _bot.getOutput().println();
            }
        } else {
            _bot.getOutput().println("No triggers found");
        }

    }

    public static <T extends Trigger> Trigger readTriggerJson(String propertyValue, Class<T> clazz) throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        Trigger trigger = null;
        trigger = mapper.readValue(propertyValue, clazz);
        return trigger;
    }


}
