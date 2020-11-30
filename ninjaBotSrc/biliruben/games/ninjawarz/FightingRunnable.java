package biliruben.games.ninjawarz;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import biliruben.games.ninjawarz.api.NinjaRunnable;
import biliruben.games.ninjawarz.object.Battle;
import biliruben.io.HTTPConnection;

/**
 * Runnable used to handle the fighting.  The fight command is the only
 * command intended to have its own runnable.  Everything else can be wrapped in
 * {@link CommandRunnable} instead
 * @author trey.kirk
 *
 */
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
                Battle battle = doFight(i + 1);
                if (battle == null || !"error".equals(battle.getResult())) {
                    // visit the hospital if we know it's not an error or we don't know anything at all
                    doHeal();
                }
                // always do the delay.  Otherwise a series of error resulting battles will spam
                // the server
                doDelay(); 

                getBot().getOutput().println("\n" + NinjaBot.LINE_SEP);

            } catch (IOException e1) {
                e1.printStackTrace(getBot().getOutput());
                e1.printStackTrace(new PrintStream(getBot().getLoggingStream()));
                _running = false;
            }
            while (_paused && _running) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // don't care
                }
            }

        }
        /* Battle report should suffice
        try {
            if (_running) {
                doNews();
            }
            
        } catch (IOException e1) {
            e1.printStackTrace(getBot().getOutput());
            e1.printStackTrace(new PrintStream(getBot().getLoggingStream()));
            _running = false;
        }
        */

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

    private Battle doFight(int count) throws IOException {
        getBot().getOutput().println(_fighterId + ": Fight #" + count);
        HTTPConnection fightConnection = getBot().getConnectionManager().getFightConnection(_fighterId, getBot().getOutput());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        fightConnection.setOutputStream(bos);
        fightConnection.connect();
        String battleJSON = bos.toString();
        Battle battle = null;
        try {
            battle = getBot().reportBattle(getBot().getOutput(), battleJSON);
        } catch (Exception e) {
            getBot().logError(e);
        }
        return battle;
    }

    private void doHeal() throws IOException {
        getBot().getOutput().println("Healing...");
        getBot().getOutput().flush(); // can get a little out of order
        HTTPConnection healConnection = getBot().getConnectionManager().getHealConnection(getBot().getOutput());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        healConnection.setOutputStream(bos);
        healConnection.connect();
        String healJSON = bos.toString();
        getBot().reportHeal(getBot().getOutput(), healJSON);
    }

    private void doNews() throws IOException {
        String news = getBot().getNews();
        getBot().getOutput().print(news);
        getBot().getOutput().println();
        String status = getBot().getStatus();
        getBot().getOutput().println(status);
        getBot().getOutput().println();
    }

    private void doDelay() {
        try {
            getBot().getOutput().println("Resting Ninjas for " + _delay + " seconds");
            Thread.sleep (_delay * 1000);
        } catch (InterruptedException e) {
            //e.printStackTrace(getBot().getOutput());
            //don't care
        }
    }

    @Override
    public String getDescription() {
        StringBuffer buff = new StringBuffer();
        buff.append("Fighting ").append(_fighterId).append(" ").append(_fights).append(" times with ").append(_delay).append(" seconds rest.");
        return buff.toString();
    }


}
