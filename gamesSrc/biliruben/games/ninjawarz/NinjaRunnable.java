package biliruben.games.ninjawarz;

import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;

import biliruben.files.HTTPConnection;

import com.biliruben.tools.threads.ThreadRunner.TRRunnable;

public abstract class NinjaRunnable implements TRRunnable {
    private NinjaBot _bot;
    private HTTPConnection _newsConnection;
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
    

}