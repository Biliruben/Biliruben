package biliruben.games.ninjawarz.command;

import java.io.IOException;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;

public class FightGenbu extends NinjaCommand {

    public FightGenbu(NinjaBot bot, String commandName) {
        super(bot, commandName, "Attack Genbu!");
        // TODO Auto-generated constructor stub
    }

    @Override
    public void execute() throws NinjaBotException {
        try {
            getBot().addFighter("genbu", 1, false);
        } catch (IOException e) {
            getBot().logError(e);
        }
    }

}
