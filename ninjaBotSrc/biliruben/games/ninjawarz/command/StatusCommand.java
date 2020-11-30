package biliruben.games.ninjawarz.command;

import biliruben.games.ninjawarz.NinjaBot;

public class StatusCommand extends NinjaCommand {

    public StatusCommand(NinjaBot bot, String commandName) {
        super(bot, commandName, "Shows the current status");
        // TODO Auto-generated constructor stub
    }

    @Override
    public void execute() {
        getBot().showStatus();
    }

}
