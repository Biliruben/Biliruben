package biliruben.games.ninjawarz.command;

import java.io.IOException;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;

public class VisitDaimyoCommand extends NinjaCommand {

    public VisitDaimyoCommand(NinjaBot bot, String commandName) {
        super(bot, commandName, "Visit the Daimyo and ask for a gift");
        // TODO Auto-generated constructor stub
    }

    @Override
    public void execute() throws NinjaBotException {
        try {
            getBot().visitDaimyo();
        } catch (IOException e) {
            throw new NinjaBotException(e);
        }
    }

}
