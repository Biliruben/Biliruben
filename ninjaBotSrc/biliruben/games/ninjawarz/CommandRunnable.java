package biliruben.games.ninjawarz;

import biliruben.games.ninjawarz.api.NinjaRunnable;
import biliruben.games.ninjawarz.command.NinjaCommand;


/**
 * Runnable that will hold a NinjaCommand.  
 * @author trey.kirk
 *
 */
public class CommandRunnable extends NinjaRunnable {

    private String _command;
    private String _args;
    private boolean _paused;

    public CommandRunnable(NinjaBot bot, String commandString, String arguments) {
        super(bot);
        _command = commandString;
        _args = arguments;
        _paused = false;
    }


    @Override
    public void run() {
        while (_paused) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                // don't care
            }
        }
        NinjaCommand ninjaCommand = getBot().processCommand(_command, _args);
        getBot().dispatchCommand(ninjaCommand);
    }

    @Override
    public void pause() {
        _paused = true;
    }

    @Override
    public void resume() {
        _paused = false;
    }

    @Override
    public void shutdown() {
        // noop

    }


    @Override
    public String getDescription() {
        StringBuffer buff = new StringBuffer();
        buff.append("Command: ").append(_command);
        if (_args != null && _args.length() > 0) {
            buff.append(" ").append(_args);
        }
        return buff.toString();
    }

}
