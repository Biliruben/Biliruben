package biliruben.games.ninjawarz.command;

import java.io.IOException;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;

public class BuyWeaponCommand extends NinjaCommand {

    public BuyWeaponCommand(NinjaBot bot, String commandName) {
        super(bot, commandName, "Buys a specified weapon from the weapon shop");
    }

    @Override
    public void execute() throws NinjaBotException {
        if (_arguments.length < 1) {
            throw new NinjaBotException("An ItemID must be specified!");
        }
        try {

            int itemId = Integer.valueOf(_arguments[0]);
            int purchases = 1;
            if (_arguments.length > 1) {
                purchases = Integer.valueOf(_arguments[1]);
            }
            for (int i = 0; i < purchases; i++) {
                getBot().buyWeapon(itemId);
            }

        } catch (NumberFormatException e) {
            throw new NinjaBotException("An integer must be supplied for the ItemId and quantity.");
        } catch (IOException e) {
            getBot().logError(e);
        }
    }

}
