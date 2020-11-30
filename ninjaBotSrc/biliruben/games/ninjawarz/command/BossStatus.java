package biliruben.games.ninjawarz.command;

import java.io.IOException;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;

public class BossStatus extends NinjaCommand {

    public BossStatus(NinjaBot bot, String commandName) {
        super(bot, commandName, "Displays the avaialbe bosses");
    }

    @Override
    public void execute() throws NinjaBotException {
        // no argument
        try {
            getBot().showBosses();
        } catch (IOException e) {
            getBot().logError(e);
        }
        
    }

}
