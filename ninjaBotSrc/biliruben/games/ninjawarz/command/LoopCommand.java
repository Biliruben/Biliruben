package biliruben.games.ninjawarz.command;

import biliruben.games.ninjawarz.NinjaBot;

public class LoopCommand extends NinjaCommand {

    public LoopCommand(NinjaBot bot, String commandName) {
        super(bot, commandName, "Loops a command.  You will likely need to clear the queue to stop looping");
    }

    @Override
    public void execute() {
        getBot().loopCommand(_arguments);
    }

}
