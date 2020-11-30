package biliruben.games.ninjawarz.command;

import java.io.IOException;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;

public class FightMechagenbu extends NinjaCommand {

    public FightMechagenbu(NinjaBot bot, String commandName) {
        super(bot, commandName, "Attack MechaGenbu!");
        // TODO Auto-generated constructor stub
    }

    @Override
    public void execute() throws NinjaBotException {
        try {
            getBot().addFighter("mechagenbu", 1, false);
        } catch (IOException e) {
            getBot().logError(e);
        }
    }

}
