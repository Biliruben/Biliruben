package biliruben.games.ninjawarz.command;

import biliruben.games.ninjawarz.NinjaBot;

public class HelpCommand extends NinjaCommand {

    public HelpCommand(NinjaBot bot, String commandName) {
        super(bot, commandName, "Displays usability help");
    }

    @Override
    public void execute() {
        if (_arguments.length > 0) {
            getBot().showHelp(_arguments[0]);
        }
        getBot().showHelp();
    }

}
