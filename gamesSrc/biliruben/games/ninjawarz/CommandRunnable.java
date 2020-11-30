package biliruben.games.ninjawarz;

/**
 * Runnable that will hold a NinjaCommand.  
 * @author trey.kirk
 *
 */
public class CommandRunnable extends NinjaRunnable {

    private String _command;
    private String _args;
    
    public CommandRunnable(NinjaBot bot, String commandString, String arguments) {
        super(bot);
        _command = commandString;
        _args = arguments;
    }
    

    @Override
    public void run() {
        getBot().processCommand(_command, _args);
    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub

    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub

    }

    @Override
    public void shutdown() {
        // TODO Auto-generated method stub

    }

}
