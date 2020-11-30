package biliruben.games.ninjawarz.command;

import java.io.IOException;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;

public class SellAllCommand extends NinjaCommand {

    public SellAllCommand(NinjaBot bot, String commandName) {
        super(bot, commandName, "Sells all items of one type");
        // TODO Auto-generated constructor stub
    }

    @Override
    public void execute() throws NinjaBotException {
        // This is going to be a runnable
        if (_arguments.length < 1) {
            throw new NinjaBotException("An ItemID must be provided!");
        }
        try {
            for (String arg : _arguments) {
                int itemId = Integer.valueOf(arg);
                getBot().sellAllItem(itemId);
            }
        } catch (NumberFormatException e) {
            throw new NinjaBotException("An integer must be provided for ItemID");
        } catch (IOException e) {
            e.printStackTrace(getBot().getOutput());
            throw new NinjaBotException(e);
        }
    }

}
