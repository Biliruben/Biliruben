package biliruben.games.ninjawarz.command;

import java.io.IOException;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;

public class ShowStatsCommand extends NinjaCommand {

    public ShowStatsCommand(NinjaBot bot, String commandName) {
        super(bot, commandName, "Displays the current session statistics");
        // TODO Auto-generated constructor stub
    }

    @Override
    public void execute() throws NinjaBotException {
        try {
            getBot().showStats();
        } catch (IOException e) {
            throw new NinjaBotException(e);
        }
    }

}
