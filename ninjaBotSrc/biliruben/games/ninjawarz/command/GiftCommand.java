package biliruben.games.ninjawarz.command;

import java.io.IOException;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;

public class GiftCommand extends NinjaCommand {

    public GiftCommand(NinjaBot bot, String commandName) {
        super(bot, commandName, "Attempts to gift somthing");
        // TODO Auto-generated constructor stub
    }

    @Override
    public void execute() throws NinjaBotException {
        // TODO Auto-generated method stub
        try {
            getBot().getGift();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            getBot().logError(e);
        }
    }

}
