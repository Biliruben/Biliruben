package biliruben.games.ninjawarz.command;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;

public class LogCommand extends NinjaCommand {

    public LogCommand(NinjaBot bot, String commandName) {
        super(bot, commandName, "Enables or disables logging extended JSON output");
        // TODO Auto-generated constructor stub
    }

    @Override
    public void execute() throws NinjaBotException {
        String enableValue = "true"; 
        if (_arguments != null && _arguments.length > 0) {
            enableValue = _arguments[0];
        }
        Boolean doLogging = Boolean.valueOf(enableValue);
        getBot().setLogging(doLogging);
    }
}
