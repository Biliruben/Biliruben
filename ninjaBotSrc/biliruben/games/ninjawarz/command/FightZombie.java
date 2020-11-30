package biliruben.games.ninjawarz.command;

import java.io.IOException;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;

public class FightZombie extends NinjaCommand {

    public FightZombie(NinjaBot bot, String commandName) {
        super(bot, commandName, "Attack the horde!");
        // TODO Auto-generated constructor stub
    }

    @Override
    public void execute() throws NinjaBotException {
        try {
            getBot().addFighter("horde", 1, true);
        } catch (IOException e) {
            getBot().logError(e);
        }
    }

}
