package biliruben.games.ninjawarz.command;

import java.util.Arrays;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;

public class StartFightingCommand extends NinjaCommand {
    
    private static final String[] SELECTIONS = {StopFightingCommand.QUEUE, StopFightingCommand.TRIGGERS};

    public StartFightingCommand(NinjaBot bot, String commandName) {
        super(bot, commandName, "Resumes fighting and queue processing");
    }

    @Override
    public void execute() throws NinjaBotException {
        if (_arguments.length == 0) {
            getBot().resumeQueue();
            getBot().resumeTriggers();
        } else {
            String selection = _arguments[0];
            if (StopFightingCommand.QUEUE.toLowerCase().startsWith(selection)) {
                getBot().resumeQueue();
            } else if (StopFightingCommand.TRIGGERS.toLowerCase().startsWith(selection)) {
                getBot().resumeTriggers();
            } else {
                throw new NinjaBotException("Only the following arguments are supported: " + Arrays.toString(SELECTIONS));
            }
        }
    }

}
