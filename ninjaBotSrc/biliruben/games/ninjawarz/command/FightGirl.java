package biliruben.games.ninjawarz.command;

import java.io.IOException;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;

public class FightGirl extends NinjaCommand {

    public FightGirl(NinjaBot bot, String commandName) {
        super(bot, commandName, "Attack the girl with the sword!");
        // TODO Auto-generated constructor stub
    }

    @Override
    public void execute() throws NinjaBotException {
        try {
            getBot().addFighter("girl", 1, false);
        } catch (IOException e) {
            getBot().logError(e);
        }
    }

}
