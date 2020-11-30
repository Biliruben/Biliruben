package biliruben.games.ninjawarz.command;

import java.io.IOException;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;

public class BuyMagic extends NinjaCommand {

    
    public BuyMagic(NinjaBot bot, String name) {
        super(bot, name, "Purchases the requested magic");
    }
    @Override
    public void execute() throws NinjaBotException {
        if (_arguments.length != 1) {
            throw new NinjaBotException("Exactly one integer argument must be supplied");
        }
        try {
            int sid = Integer.valueOf(_arguments[0]);
            getBot().buyMagic(sid);
            
        } catch (NumberFormatException e) {
            throw new NinjaBotException("Exactly one integer argument must be supplied");
        } catch (IOException e) {
            getBot().logError(e);
        }
    }

}
