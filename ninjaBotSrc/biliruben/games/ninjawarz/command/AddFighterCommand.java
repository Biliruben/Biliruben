package biliruben.games.ninjawarz.command;

import java.io.IOException;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;

public class AddFighterCommand extends NinjaCommand {

    public AddFighterCommand(NinjaBot bot, String commandName) {
        super(bot, commandName, "Adds the next fighter into the queue");
    }

    @Override
    public void execute() throws NinjaBotException {
        try {
            if (_arguments == null || _arguments.length < 1) {
                throw new NinjaBotException("Need fighter id!");
            } else {

                if (_arguments.length > 1) {
                    int iterations = 0;
                    try {
                        iterations = Integer.valueOf(_arguments[1]);
                    } catch (NumberFormatException e) {
                        throw new NinjaBotException("An integer value must be supplied when specifying the number of fights: " + _arguments[1]);
                    }
                    getBot().addFighter(_arguments[0], iterations);
                } else {
                    getBot().addFighter(_arguments[0]);
                }

            }
        } catch (IOException e) {
            throw new NinjaBotException(e);
        }
    }

}
