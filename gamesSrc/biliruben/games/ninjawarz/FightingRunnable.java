package biliruben.games.ninjawarz;

import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;

import biliruben.files.HTTPConnection;

import com.biliruben.tools.threads.ThreadRunner.TRRunnable;

public class FightingRunnable extends NinjaRunnable {


    private String _fighterId;
    private boolean _paused;
    private int _fights = NinjaBot.DEFAULT_FIGHTS;
    private int _delay= NinjaBot.DEFAULT_DELAY;

    public FightingRunnable(NinjaBot bot, String fighterId) {
        super(bot);
        _fighterId = fighterId;
    }

    public void setDelay(int delay) {
        _delay = delay;
    }

    public void setFights(int fights) {
        _fights = fights;
    }

    /* (non-Javadoc)
     * @see biliruben.games.ninjawarz.NinjaRunnable#run()
     */
    @Override
    public void run() {
        _paused = false;
        _running = true;

        for (int i = 0; i < _fights && _running; i++) {
            try {
                doFight(i + 1);
                doHeal();
            } catch (IOException e1) {
                e1.printStackTrace(getBot().getOutput());
                e1.printStackTrace(new PrintStream(getBot().getLoggingStream()));
                _running = false;
            }
            if (_running) {
                doDelay();
            }
            while (_paused && _running) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // don't care
                }
            }

        }
        try {
            if (_running) {
                doNews();
            }
        } catch (IOException e1) {
            e1.printStackTrace(getBot().getOutput());
            e1.printStackTrace(new PrintStream(getBot().getLoggingStream()));
            _running = false;
        }

    }
    
    


    
    

    /* (non-Javadoc)
     * @see biliruben.games.ninjawarz.NinjaRunnable#pause()
     */
    @Override
    public void pause() {
        _paused = true;
    }

    /* (non-Javadoc)
     * @see biliruben.games.ninjawarz.NinjaRunnable#resume()
     */
    @Override
    public void resume() {
        _paused = false;
    }

    /* (non-Javadoc)
     * @see biliruben.games.ninjawarz.NinjaRunnable#shutdown()
     */
    @Override
    public void shutdown() {
        _running = false;
    }

    private void doFight(int count) throws IOException {
        getBot().getOutput().println(_fighterId + ": Fight #" + count);
        HTTPConnection fightConnection = getBot().getFightConnection(_fighterId);
        fightConnection.connect();
        getBot().getLoggingStream().write("\n".getBytes());
    }

    private void doHeal() throws IOException {
        getBot().getOutput().println("Healing...");
        HTTPConnection healConnection = getBot().getHealConnection();
        healConnection.connect();
        getBot().getLoggingStream().write("\n".getBytes());
    }

    private void doNews() throws IOException {
        String news = getBot().getNews();
        getBot().getOutput().print(news);
        String status = getBot().getStatus();
        getBot().getOutput().println(status);
    }

    private void doDelay() {
        try {
            Thread.sleep (_delay * 1000);
        } catch (InterruptedException e) {
            //e.printStackTrace(getBot().getOutput());
            //don't care
        }
    }


}
