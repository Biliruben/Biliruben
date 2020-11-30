package biliruben.games.ninjawarz.command;

import java.io.IOException;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;

public class FightAllCommand extends NinjaCommand {

    public FightAllCommand(NinjaBot bot, String commandName) {
        super(bot, commandName, "Fights all of the opponents in a level offset by yours");
    }

    @Override
    public void execute() throws NinjaBotException {
        int levelDelta = NinjaBot.DEFAULT_LEVEL_DELTA;
        if (_arguments.length > 0) {
            try {
                levelDelta = Integer.valueOf(_arguments[0]);
            } catch (NumberFormatException e) {
                throw new NinjaBotException("Could not parse argument as integer: " + _arguments[0]);
            }
        }
        try {
            getBot().fightAllOpponents(levelDelta);
        } catch (IOException e) {
            getBot().getOutput().println("Error: " + e.getMessage());
            getBot().logError(e);
        }
    }

}
