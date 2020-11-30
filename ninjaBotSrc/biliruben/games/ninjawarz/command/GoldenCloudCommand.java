package biliruben.games.ninjawarz.command;

import java.io.IOException;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;

public class GoldenCloudCommand extends NinjaCommand {

    public GoldenCloudCommand(NinjaBot bot, String commandName) {
        super(bot, commandName, "Visit the random golden cloud");
        // TODO Auto-generated constructor stub
    }

    @Override
    public void execute() throws NinjaBotException {
        try {
            getBot().visitGoldenCloud();
        } catch (IOException e) {
            getBot().logError(e);
        }
    }

}
