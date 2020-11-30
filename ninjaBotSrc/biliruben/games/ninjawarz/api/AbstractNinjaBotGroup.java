package biliruben.games.ninjawarz.api;

import biliruben.games.ninjawarz.NinjaBot;

public abstract class AbstractNinjaBotGroup implements CommandGroup {

    private NinjaBot _bot;
    
    public AbstractNinjaBotGroup(NinjaBot bot) {
        _bot = bot;
    }
    
    protected NinjaBot getBot() {
        return _bot;
    }

}
