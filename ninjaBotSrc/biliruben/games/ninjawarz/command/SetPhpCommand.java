package biliruben.games.ninjawarz.command;

import biliruben.games.ninjawarz.NinjaBot;


public class SetPhpCommand extends NinjaCommand {

    public SetPhpCommand(NinjaBot bot, String commandName) {
        super(bot, commandName, "Sets the phpSessionId");
        // TODO Auto-generated constructor stub
    }

    @Override
    public void execute() {
        if (_arguments == null || _arguments.length == 0) {
            getBot().showPhpSession();
        } else {
            getBot().setPhpSession(_arguments[0]);
        }
    }

}
