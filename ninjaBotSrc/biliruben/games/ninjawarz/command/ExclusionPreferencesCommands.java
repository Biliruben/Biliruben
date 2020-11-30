package biliruben.games.ninjawarz.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;
import biliruben.games.ninjawarz.Util;
import biliruben.games.ninjawarz.api.AbstractNinjaBotGroup;
import biliruben.games.ninjawarz.command.DeleteCommand.Deletable;


public class ExclusionPreferencesCommands extends AbstractNinjaBotGroup {

    public static String EXCLUDE_ASSIST = "excludeAssist";
    public static String EXCLUDE_FIGHT = "excludeFight";
    public static String PREFER_ASSIST = "preferAssist";

    public ExclusionPreferencesCommands(NinjaBot bot) {
        super(bot);
        addDeletables();
    }
    
    private void addDeletables() {
        DeleteCommand deleteCommand = Util.findOrCreateDeleteCommand(getBot());
        deleteCommand.add(new RemoveExcludeClanFromAssistDeletable(getBot()));
        deleteCommand.add(new RemoveExcludeClanFromFightDeletable(getBot()));
        deleteCommand.add(new RemovePreferClanForAssistDeletable(getBot()));
    }

    public static class ExcludeClanFromFightCommand extends NinjaCommand {
        public ExcludeClanFromFightCommand(NinjaBot bot, String commandName) {
            super(bot, commandName, "Excludes a clan from being fought");
        }

        @Override
        public void execute() throws NinjaBotException {
            if (_arguments.length < 1) {
                throw new NinjaBotException("At least one clan name or id must be specified");
            }
            for (String cid : _arguments) {
                try {
                    getBot().addExcludedFighter(cid);
                } catch (IOException e) {
                    throw new NinjaBotException(e);
                }
            }
        }
    }

    public static class RemoveExcludeClanFromFightDeletable implements Deletable {
        private NinjaBot _bot;

        public RemoveExcludeClanFromFightDeletable(NinjaBot bot) {
            this._bot = bot;
        }

        @Override
        public String getObjectType() {
            return EXCLUDE_FIGHT;
        }
        
        @Override
        public void callDelete(String[] args) throws NinjaBotException {
            if (args.length < 1) {
                throw new NinjaBotException("At least one clan name or id must be specified");
            }
            for (String cid : args) {
                try {
                    _bot.removeExcludedFighter(cid);
                } catch (IOException e) {
                    throw new NinjaBotException(e);
                }
            }
            
        }
    }
    
    public static class ExcludeClanFromAssistCommand extends NinjaCommand {
        public ExcludeClanFromAssistCommand(NinjaBot bot, String commandName) {
            super(bot, commandName, "Excludes a clan (ally) from being assisted");
        }

        @Override
        public void execute() throws NinjaBotException {
            if (_arguments.length < 1) {
                throw new NinjaBotException("At least one clan name or id must be specified");
            }
            for (String cid : _arguments) {
                try {
                    getBot().addExcludedAssist(cid);
                } catch (IOException e) {
                    throw new NinjaBotException(e);
                }
            }
        }
    }

    public static class RemoveExcludeClanFromAssistDeletable implements Deletable {
        private NinjaBot _bot;

        public RemoveExcludeClanFromAssistDeletable(NinjaBot bot) {
            this._bot = bot;
        }

        @Override
        public void callDelete(String[] args) throws NinjaBotException {
            if (args.length < 1) {
                throw new NinjaBotException("At least one clan name or id must be specified");
            }
            for (String cid : args) {
                try {
                    this._bot.removeExcludedAssist(cid);
                } catch (IOException e) {
                    throw new NinjaBotException(e);
                }
            }
        }

        @Override
        public String getObjectType() {
            return EXCLUDE_ASSIST;
        }
    }
    
    public static class PreferClanForAssistCommand extends NinjaCommand {
        public PreferClanForAssistCommand(NinjaBot bot, String commandName) {
            super(bot, commandName, "Prefers a clan (ally) for being assisted");
        }

        @Override
        public void execute() throws NinjaBotException {
            if (_arguments.length < 1) {
                throw new NinjaBotException("At least one clan name or id must be specified");
            }
            for (String cid : _arguments) {
                try {
                    getBot().addPreferredAssist(cid);
                } catch (IOException e) {
                    throw new NinjaBotException(e);
                }
            }
        }
    }

    public static class RemovePreferClanForAssistDeletable implements Deletable {
        private NinjaBot _bot;
        
        public RemovePreferClanForAssistDeletable(NinjaBot bot) {
            this._bot = bot;
        }

        @Override
        public void callDelete(String[] args) throws NinjaBotException {
            if (args.length < 1) {
                throw new NinjaBotException("At least one clan name or id must be specified");
            }
            for (String cid : args) {
                try {
                    this._bot.removePreferredAssist(cid);
                } catch (IOException e) {
                    throw new NinjaBotException(e);
                }
            }
        }

        @Override
        public String getObjectType() {
            return PREFER_ASSIST;
        }
    }
    
    @Override
    public Collection<NinjaCommand> getCommands() {
        List<NinjaCommand> commands = new ArrayList<NinjaCommand>();
        commands.add(new ExcludeClanFromFightCommand(getBot(), "excludeFight"));
        commands.add(new ExcludeClanFromAssistCommand(getBot(), "excludeAssist"));
        commands.add(new PreferClanForAssistCommand(getBot(), "preferAssist"));
        return commands;
    }


}
