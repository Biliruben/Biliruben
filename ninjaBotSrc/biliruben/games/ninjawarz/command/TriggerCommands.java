package biliruben.games.ninjawarz.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;
import biliruben.games.ninjawarz.Util;
import biliruben.games.ninjawarz.api.CommandGroup;
import biliruben.games.ninjawarz.api.Event;
import biliruben.games.ninjawarz.api.Trigger;
import biliruben.games.ninjawarz.command.DeleteCommand.Deletable;
import biliruben.games.ninjawarz.object.Condition.EventCondition;
import biliruben.games.ninjawarz.object.Condition.TimerCondition;
import biliruben.games.ninjawarz.object.Timer;

public class TriggerCommands implements CommandGroup {


    public static final String TYPE_TRIGGER = "trigger";

    private static List<NinjaCommand> scanForCommands(Scanner input, NinjaBot bot, String prompt) {

        String commandStr = null;
        List<NinjaCommand> commands = new ArrayList<NinjaCommand>();
        do {
            bot.getOutput().print(prompt);
            commandStr = input.nextLine();
            if (!"end".equalsIgnoreCase(commandStr)) {
                String[] tokens = commandStr.split(" ", 2);
                String command = tokens[0];
                String arguments = tokens.length > 1 ? tokens[1] : null;
                NinjaCommand ninjaCommand = bot.processCommand(command, arguments);
                if (ninjaCommand != null) {
                    commands.add(ninjaCommand);
                }
            }
        } while (!"end".equalsIgnoreCase(commandStr));

        return commands;

    }
    
    public static Event createEventFromInput(NinjaBot bot) {
        Event event = null;
        bot.getOutput().println("\nEvent Conditions:");
        // show a list of triggers with names being the response desired
        for (EventCondition t : EventCondition.values()) {
            bot.getOutput().println(t);
        }
        bot.getOutput().print("\n");
        String prompt = "Type the condition desired or 'end'> ";
        boolean found = false;
        Scanner input = new Scanner(System.in);
        while (!found) {
            bot.getOutput().print(prompt);

            String name = input.nextLine();
            if ("end".startsWith(name)) {
                return null;
            }

            for (EventCondition conditions : EventCondition.values()) {
                if (conditions.toString().toLowerCase().startsWith(name.toLowerCase())) {
                    event = conditions.getInstance(bot);
                    break;
                }
            }
            if (event == null) {
                bot.getOutput().println("No condition of that name was found");
            } else {
                found = true;
            }
        }

        // trigger implements gathering input data
        if (event != null) {
            bot.getOutput().println("Selected: " + event.getClass().getSimpleName());
            Map<String, Object> data = event.createTriggerData(input);
            if (data != null) {
                event.setTriggerData(data);
            }
            
            // What command(s) should be run?
            bot.getOutput().println("\nType the command(s) to execute when the event fires.  Type 'end' when finished.");
            prompt = "(nextCommand)$ ";
            List<NinjaCommand> commands = scanForCommands(input, bot, prompt);
            if (commands.size() > 0) {
                event.setCommands(commands.toArray(new NinjaCommand[commands.size()]));
            }

            // Iterations
            int executions = 0;
            boolean validInput = false;
            String executionPossibles = "(1 == once, 2 == twice, (etc) -1 == no limit)";
            bot.getOutput().println("How many times should this event run? " + executionPossibles);
            do {
                bot.getOutput().print("(executions)$ ");
                String executionsString = input.nextLine();
                try {
                    executions = Integer.valueOf(executionsString);
                    if (executions <= 0 && !(executions == Event.EXECUTION_INIFINITE)) {
                        bot.getOutput().println(executions + " is not a valid choice: " + executionPossibles);
                    } else {
                        validInput = true;
                    }
                } catch (NumberFormatException e) {
                    bot.getOutput().println("An integer value must be supplied!");
                }
            } while (!validInput);
            event.setExecutions(executions);

        }

        return event;
    }

    public static Timer createTimerFromInput(NinjaBot bot) {
        Timer timer = null;
        bot.getOutput().println("\nTimer Conditions:");
        // show a list of triggers with names being the response desired
        for (TimerCondition t : TimerCondition.values()) {
            bot.getOutput().println(t);
        }
        bot.getOutput().print("\n");
        String prompt = "Type the condition desired or 'end'> ";
        boolean found = false;
        Scanner input = new Scanner(System.in);
        while (!found) {
            bot.getOutput().print(prompt);

            String name = input.nextLine();
            if ("end".startsWith(name)) {
                return null;
            }

            for (TimerCondition conditions : TimerCondition.values()) {
                if (conditions.toString().toLowerCase().startsWith(name.toLowerCase())) {
                    timer = conditions.getInstance(bot);
                    break;
                }
            }
            if (timer == null) {
                bot.getOutput().println("No condition of that name was found");
            } else {
                found = true;
            }
        }

        // trigger implements gathering input data
        if (timer != null) {
            bot.getOutput().println("Selected: " + timer.getClass().getSimpleName());
            Map<String, Object> data = timer.createTriggerData(input);
            if (data != null) {
                timer.setTriggerData(data);
            }
            // Get the frequency
            String recommended = timer.getRecommendedFrequency();
            bot.getOutput().println("\nHow frequent should this timer run?  (5:00:00 = 5 hrs, 5:00 = 5 minutes, 5 = 5 seconds)");
            bot.getOutput().print("[Leave blank for " + recommended + "]> ");
            String freqTime = input.nextLine();
            if (freqTime == null || freqTime.trim().equals("")) {
                freqTime = recommended;
            }
            try {
                long freq = Util.timeStringToLong(freqTime) * 1000;
                bot.getOutput().println("Setting frequency to " + freq + " milliseconds");
                timer.setFrequency(freq);
            } catch (Exception e) {
                bot.logError(e);
            }

            // set wakeup for right now
            timer.setWakeupTime(System.currentTimeMillis());
            
            // Finally, what command(s) should be run?
            bot.getOutput().println("\nType the command(s) to execute when the timer fires.  Type 'end' when finished.");
            prompt = "(nextCommand)$ ";
            List<NinjaCommand> commands = scanForCommands(input, bot, prompt);
            if (commands.size() > 0) {
                timer.setCommands(commands.toArray(new NinjaCommand[commands.size()]));
            }
            int executions = 0;
            boolean validInput = false;
            String executionPossibles = "(1 == once, 2 == twice, (etc) -1 == no limit)";
            bot.getOutput().println("How many times should this timer run? " + executionPossibles);
            do {
                bot.getOutput().print("(executions)$ ");
                String executionsString = input.nextLine();
                try {
                    executions = Integer.valueOf(executionsString);
                    if (executions <= 0 && !(executions == Event.EXECUTION_INIFINITE)) {
                        bot.getOutput().println(executions + " is not a valid choice: " + executionPossibles);
                    } else {
                        validInput = true;
                    }
                } catch (NumberFormatException e) {
                    bot.getOutput().println("An integer value must be supplied!");
                }
            } while (!validInput);
            timer.setExecutions(executions);
        }

        return timer;
    }
    

    public static class AddTimerCommand extends NinjaCommand {

        public AddTimerCommand(NinjaBot bot, String name) {
            super(bot, name, "Adds a timer");
            setExtendedHelp("When invoked, will start the 'Add Timer' wizard that will take you through creating a new timer");
        }

        @Override
        public void execute() throws NinjaBotException {
            // triggers are complicated, take them through a weezard if there are no args
            // if there are args, just do eet
            // if we're in the wrong thread and there are no args, avoid the weezard

            /*
            if (_arguments.length > 0) {
                // we got args, so?
                // so come back to this
            }
             */
            Timer trigger = createTimerFromInput(getBot());
            if (trigger != null) {
                getBot().addTrigger(trigger);
            }
        }

    }
    
    public static class AddEventCommand extends NinjaCommand {

        public AddEventCommand(NinjaBot bot, String name) {
            super (bot, name, "Adds an event");
            setExtendedHelp("When invoked, will start the 'Add Event' wizard that will take you through creating a new event");
        }
        
        @Override
        public void execute() throws NinjaBotException {
            Event event = createEventFromInput(getBot());
            if (event != null) {
                getBot().addTrigger(event);
            }
        }
        
    }

    public static class DeleteTriggersDeletable implements Deletable {

        private NinjaBot _bot;
        
        public DeleteTriggersDeletable(NinjaBot bot) {
            _bot = bot;
        }
        
        public String getObjectType() {
            return TYPE_TRIGGER;
        }
        
        public void callDelete(String[] _arguments) throws NinjaBotException {
            if (_arguments.length < 1) {
                throw new NinjaBotException("An integer id must be provided");
            }
            try {
                long id = Long.valueOf(_arguments[0]);
                Trigger trigger = _bot.getTrigger(id);
                _bot.removeTrigger(trigger);
            } catch (NumberFormatException e) {
                throw new NinjaBotException("An integer value must be provided");
            }
        }

    }

    private NinjaBot _bot;

    public TriggerCommands(NinjaBot bot) {
        _bot = bot;
        // This is the best hook we have to add our 'delete' event (and likely upcoming 'add' as well
        DeleteTriggersDeletable deletableTrigger = new DeleteTriggersDeletable(bot);
        DeleteCommand command = Util.findOrCreateDeleteCommand(bot);
        command.add(deletableTrigger);
    }

    @Override
    public Collection<NinjaCommand> getCommands() {
        List<NinjaCommand> commands = new ArrayList<NinjaCommand>();
        commands.add(new AddTimerCommand(_bot, "addTimer"));
        commands.add(new AddEventCommand(_bot, "addEvent"));
        return commands;
    }

}
