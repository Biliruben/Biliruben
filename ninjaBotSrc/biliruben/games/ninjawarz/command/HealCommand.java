package biliruben.games.ninjawarz.command;

import java.io.IOException;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;

public class HealCommand extends NinjaCommand {

    public HealCommand(NinjaBot bot, String commandName) {
        super(bot, commandName, "Go to the hospital");
        // TODO Auto-generated constructor stub
    }

    @Override
    public void execute() throws NinjaBotException {
        try {
            getBot().heal();
        } catch (IOException e) {
            throw new NinjaBotException(e);
        }
    }

}
