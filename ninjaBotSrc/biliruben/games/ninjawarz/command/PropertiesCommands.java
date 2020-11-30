package biliruben.games.ninjawarz.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import biliruben.games.ninjawarz.Configuration;
import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;
import biliruben.games.ninjawarz.Util;
import biliruben.games.ninjawarz.api.CommandGroup;

public class PropertiesCommands implements CommandGroup {

    public static class SetCommand extends NinjaCommand {
        public SetCommand(NinjaBot bot, String name) {
            super(bot, name, "Configuration command");
        }

        @Override
        public void execute() throws NinjaBotException {
            // typical set command should have two arguments: property and value
            if (_arguments.length < 1) {
                throw new NinjaBotException("At least 1 argument must be provided with the " + getName() + " command: property value");
            } else if (_arguments.length > 1) {
                String property = _arguments[0];
                String value = _arguments[1];
                getBot().addProperty(property, value);
            } else {
                // exactly 1 argument; let's remove the property
                String property = _arguments[0];
                getBot().removeProperty(property);
            }
        }
    }
    
    public static class PropertiesCommand extends NinjaCommand {
        
        public PropertiesCommand(NinjaBot bot, String name) {
            super(bot, name, "Displays the current configuration");
        }

        @Override
        public void execute() throws NinjaBotException {
            Configuration config = getBot().getConfiguration();
            int KEY_COL_LEN = 30;
            int VALUE_COL_LEN = 40;
            getBot().getOutput().printf("%-" + KEY_COL_LEN + "s", "Property");
            getBot().getOutput().printf("%-" + VALUE_COL_LEN + "s", "Value");
            getBot().getOutput().println();
            getBot().getOutput().printf("%-" + KEY_COL_LEN + "s", Util.repeatChar(KEY_COL_LEN - 1, '-'));
            getBot().getOutput().printf("%-" + VALUE_COL_LEN + "s", Util.repeatChar(VALUE_COL_LEN - 1, '-'));
            getBot().getOutput().println();
            for (String key : config.getKeys()) {
                getBot().getOutput().printf("%-" + KEY_COL_LEN + "s", key);
                getBot().getOutput().printf("%-" + VALUE_COL_LEN + "s", config.getString(key));
                getBot().getOutput().println();
            }
        }
    }

    private NinjaBot _bot;
    
    public PropertiesCommands(NinjaBot bot) {
        _bot = bot;
    }

    @Override
    public Collection<NinjaCommand> getCommands() {
        List<NinjaCommand> commands = new ArrayList<NinjaCommand>();
        commands.add(new SetCommand(_bot, "set"));
        commands.add(new PropertiesCommand(_bot, "configuration"));
        return commands;
    }

}
