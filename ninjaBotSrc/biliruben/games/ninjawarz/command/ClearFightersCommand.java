package biliruben.games.ninjawarz.command;

import biliruben.games.ninjawarz.NinjaBot;

public class ClearFightersCommand extends NinjaCommand {

    public ClearFightersCommand(NinjaBot bot, String commandName) {
        super(bot, commandName, "Clears the queue");
        // TODO Auto-generated constructor stub
    }

    @Override
    public void execute() {
        getBot().clearQueue();
    }

}
