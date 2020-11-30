package biliruben.games.ninjawarz.command;

import java.io.IOException;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;

public class FightSmallGirl extends NinjaCommand {

    public FightSmallGirl(NinjaBot bot, String commandName) {
        super(bot, commandName, "Attack the girl with the lunch box!");
    }

    @Override
    public void execute() throws NinjaBotException {
        try {
            getBot().addFighter("small%5Fgirl", 1, false);
        } catch (IOException e) {
            getBot().logError(e);
        }
    }

}
