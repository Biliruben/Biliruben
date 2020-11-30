package biliruben.games.ninjawarz.command;

import java.io.IOException;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;

public class SellItemCommand extends NinjaCommand {

    public SellItemCommand(NinjaBot bot, String commandName) {
        super(bot, commandName, "Sells an item using the item's ID");
        // TODO Auto-generated constructor stub
    }

    @Override
    public void execute() throws NinjaBotException {
        if (_arguments.length < 1) {
            throw new NinjaBotException("An ItemID must be provided!");
        }
        int itemId = 0;
        try {
            for (String arg : _arguments) {
                itemId = Integer.valueOf(arg);
                try {
                    getBot().sellItem(itemId);
                } catch (IOException e) {
                    getBot().logError(e);
                }
            }
        } catch (NumberFormatException e) {
            throw new NinjaBotException("An integer must be provided for the ItemId!");
        }
    }

}
