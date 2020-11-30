package biliruben.games.ninjawarz.command;

import biliruben.games.ninjawarz.NinjaBot;

public class ShutdownCommand extends NinjaCommand {

    public ShutdownCommand(NinjaBot bot, String commandName) {
        super(bot, commandName, "Shutdown the bot");
        // TODO Auto-generated constructor stub
    }

    @Override
    public void execute() {
        getBot().shutdown();
    }

}
