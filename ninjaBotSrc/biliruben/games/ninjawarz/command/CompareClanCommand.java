package biliruben.games.ninjawarz.command;

import java.io.IOException;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;
import biliruben.games.ninjawarz.object.Clan;

public class CompareClanCommand extends NinjaCommand {

    public CompareClanCommand(NinjaBot bot, String commandName) {
        super(bot, commandName, "Reduces your clan and their clan to a damage and toughness rating and prints the comparison");
        // TODO Auto-generated constructor stub
    }

    @Override
    public void execute() throws NinjaBotException {
        if (_arguments.length < 1) {
            throw new NinjaBotException("A clan ID must be specified");
        }

        try {
            for (String cid : _arguments) {
                Clan them = getBot().getClan(cid);
                if (them != null) {
                    getBot().printClanComparison(them);
                } else {
                    getBot().getOutput().println("That clan does not exist!");
                    return;
                }
            }
        } catch (IOException e) {
            getBot().logError(e);
        }

    }

}
