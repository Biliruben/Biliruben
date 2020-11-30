package biliruben.games.ninjawarz.object;

import java.io.IOException;

import biliruben.games.ninjawarz.NinjaBotException;

public class HordeAvailableTimer extends AbstractTrigger implements Timer {

    public HordeAvailableTimer() {
        super();
    }

    @Override
    protected boolean internalEvaluate() throws NinjaBotException {
        try {
            Clan me = getBot().getClan();
            if (me.getLevel() >= 18 && me.getHorde_in() == 0) {
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
        return "00:10:00";
    }

}
