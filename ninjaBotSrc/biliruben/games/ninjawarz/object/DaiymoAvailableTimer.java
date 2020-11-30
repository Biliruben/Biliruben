package biliruben.games.ninjawarz.object;

import java.io.IOException;

import biliruben.games.ninjawarz.NinjaBotException;

public class DaiymoAvailableTimer extends AbstractTrigger implements Timer {
    
    public DaiymoAvailableTimer() {
        super();
    }

    @Override
    protected boolean internalEvaluate() throws NinjaBotException {
        try {
            Clan me = getBot().getClan();
            if (me.getDaimyo_gift() == 0) {
                setMatchedObject(me);
                return true;
            }
        } catch (IOException e) {
            throw new NinjaBotException(e);
        }
        return false;
    }

    @Override
    public String getRecommendedFrequency() {
        return "4:00:00";
    }

}
