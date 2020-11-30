package biliruben.games.ninjawarz.object;

import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import biliruben.games.ninjawarz.NinjaBotException;

// TOODO
public class MagicAvailableTimer extends AbstractTrigger implements Timer {

    public static final String KEY_TRIGGER_DATA_SID = "sid";

    @Override
    public Map<String, Object> createTriggerData(Scanner fromInput) {
        getBot().showMagic(false);
        getBot().getOutput().println("Select the magic to test for:");
        String prompt = "(sid)$ ";
        getBot().getOutput().print(prompt);
        int sid = -1;
        do {
            String sidStr = fromInput.nextLine();
            try {
                sid = Integer.valueOf(sidStr);
            } catch (NumberFormatException e) {
                getBot().getOutput().println("An integer value is required");
            }
        } while (sid == -1);
        Map<String, Object> map = new TreeMap<String, Object>();
        map.put(KEY_TRIGGER_DATA_SID, sid);
        return map;
    }
    
    @Override
    public String getRecommendedFrequency() {
        // an hour sounds good
        return "1:00:00";
    }

    @Override
    protected boolean internalEvaluate() throws NinjaBotException {
        try {
            Clan me = getBot().getClan();
            Magic magic = getBot().getMagic((Integer)getTriggerData().get(KEY_TRIGGER_DATA_SID));
            if ((me.getMagic() == null || me.getMagic().getSid() == 0) && magic != null && magic.getMin_level() <= me.getLevel()) {
                setMatchedObject(magic);
                return true;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return false;
    }
}
