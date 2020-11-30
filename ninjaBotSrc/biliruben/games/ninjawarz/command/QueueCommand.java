package biliruben.games.ninjawarz.command;

import biliruben.games.ninjawarz.NinjaBot;

public class QueueCommand extends NinjaCommand {

    public QueueCommand(NinjaBot bot, String commandName) {
        super(bot, commandName, "Enqueues a command");
        // TODO Auto-generated constructor stub
    }

    @Override
    public void execute() {
        getBot().queueCommand(_arguments);
    }

}
