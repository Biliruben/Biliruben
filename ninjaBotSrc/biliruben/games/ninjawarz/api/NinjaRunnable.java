package biliruben.games.ninjawarz.api;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.threads.ThreadRunner.TRRunnable;

/**
 * Runnable used to encapsulate a NinjaCommand for execution in the action queue
 * @author trey.kirk
 *
 */
public abstract class NinjaRunnable implements TRRunnable {
    private NinjaBot _bot;
    protected boolean _running;

    
    public NinjaRunnable(NinjaBot bot) {
        _bot = bot;
    }


    public NinjaBot getBot() {
        return _bot;
    }
    
    public abstract void run();

    public abstract void pause();

    public abstract void resume();

    public abstract void shutdown();
    
    public abstract String getDescription();

}