package biliruben.games.ninjawarz.command;

import java.util.Arrays;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;

public class StopFightingCommand extends NinjaCommand {

    public static final String TRIGGERS = "triggers";
    public static final String QUEUE = "queue";
    private static final String[] SELECTIONS = {TRIGGERS, QUEUE};

    public StopFightingCommand(NinjaBot bot, String commandName) {
        super(bot, commandName, "Stops fighting and stops processing the queue and / or trigger evaluation");
        // TODO Auto-generated constructor stub
    }

    @Override
    public void execute() throws NinjaBotException {
        if (_arguments.length == 0) {
            getBot().pauseQueue();
            getBot().pauseTriggers();
        } else {
            if (QUEUE.startsWith(_arguments[0])) {
                getBot().pauseQueue();
            } else if (TRIGGERS.startsWith(_arguments[0])) {
                getBot().pauseTriggers();
            } else {
                throw new NinjaBotException("Only the following arguments are supported: " + Arrays.toString(SELECTIONS));
            }
        }
    }

}
