package biliruben.games.ninjawarz.object;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;
import biliruben.games.ninjawarz.api.Event;
import biliruben.games.ninjawarz.command.NinjaCommand;

@JsonIgnoreProperties({"bot", "commands", "ready", "expired", "recommendedFrequency", "matchedObject", "eventClasses"})
public abstract class AbstractTrigger implements Comparable<AbstractTrigger> {
    
    private static final long DEFAULT_EXPIRATION_DURATION = 24 * 60 * 60 * 1000; // 1 day in miliseconds
    private long _expiration;
    private Map<String, Object> _data;
    protected long _wakeupTime;
    protected long _frequency;
    private NinjaBot _bot;
    private long _id;
    private String[] _commandStrings;
    private long _nextId;
    private boolean _expires = false; // set to true to allow the trigger to expire
    private int _executions;
    private JSONObject _matchedObject;

    {
        _nextId = new Date().getTime() / 1000;
    }
    
    /**
     * This constructor is intended to be used by serial / deserializer.  The bot should be provided
     * after construction
     */
    public AbstractTrigger() {
        _expiration = System.currentTimeMillis() + DEFAULT_EXPIRATION_DURATION;
        _data = new HashMap<String, Object>();
        _commandStrings = new String[0];
        // first wakeup is in 5 seconds
        _wakeupTime = System.currentTimeMillis() + 5000;
        _frequency = Timer.FREQ_MED; // every 5 minutes
        setBot(NinjaBot.getInstance());
        setNextId();
    }
    
    public void setBot(NinjaBot bot) {
        _bot = bot;
    }
    
    public NinjaBot getBot() {
        return _bot;
    }
    
    private void setNextId() {
        _id = _nextId;
        _nextId++;
    }
    
    public void setCommands(NinjaCommand[] commands) {
        _commandStrings = new String[commands.length];
        for (int i = 0; i < _commandStrings.length; i++) {
            NinjaCommand command = commands[i];
            StringBuffer commandString = new StringBuffer();
            commandString.append(command.getName());
            if (command.getArguments() != null && command.getArguments().length > 0) {
                commandString.append(":");
                for (String argument : command.getArguments()) {
                    commandString.append(argument + " ");
                }
            }
            _commandStrings[i] = commandString.toString();
        }
    }
    
    public NinjaCommand[] getCommands() {
        List<NinjaCommand> commands = new ArrayList<NinjaCommand>();
        for (String commandString : _commandStrings) {
            String[] tokens = commandString.split(":");
            String commandName = tokens[0];
            String arguments = null;
            if (tokens.length > 1) {
                arguments = tokens[1];
            }
            NinjaCommand command = _bot.processCommand(commandName, arguments);
            if (command != null) {
                commands.add(command);
            }
            setCommands(commands.toArray(new NinjaCommand[commands.size()]));
        }
        return commands.toArray(new NinjaCommand[commands.size()]);
    }
    
    /**
     * Bean getter to be used with the JSON serializer as to avoid serializing commands
     * directly
     * @return
     */
    public String[] getCommandStrings() {
        return _commandStrings;
    }
    
    /**
     * Bean setter to be used with JSON serializer as to avoid serializing commands
     * @param commandStrings
     */
    public void setCommandStrings(String[] commandStrings) {
        _commandStrings = commandStrings;
    }
    
    public void setFrequency(long frequency) {
        _frequency = frequency;
    }
    
    public long getFrequency() {
        return _frequency;
    }
    
    public long getExpiration() {
        return _expiration;
    }
    
    public void setExpiration(long expiration) {
        _expiration = expiration;
    }
    
    public void setTriggerData(Map<String, Object> data) {
        _data = data;
    }
    
    public Map<String, Object> getTriggerData() {
        return _data;
    }
    
    public boolean isExpired() {
        boolean expired = _expires && _expiration < System.currentTimeMillis();
        return expired;
    }
    
    public boolean isExpires() {
        return _expires;
    }
    
    public void setExpires(boolean expires) {
        _expires = expires;
    }
    
    public void setExecutions(int executions) {
        _executions = executions;
    }
    
    public int getExecutions() {
        return _executions;
    }
    
    public boolean isReady() {
        return _wakeupTime < System.currentTimeMillis();
    }
    
    public long getWakeupTime() {
        return _wakeupTime;
    }
    
    public void setWakeupTime(long wakeupTime) {
        _wakeupTime = wakeupTime;
    }
    
    public void incrementWakeup() {
        _wakeupTime = System.currentTimeMillis() + _frequency;
    }
    
    protected boolean internalEvaluate() throws NinjaBotException {
        return false;
    }
    
    public boolean evaluate() throws NinjaBotException {
        boolean matched = internalEvaluate();
        return matched;
    }
    
    public String toString() {
        return this.getClass().getSimpleName() + ":" + _id;
    }
    
    public String toPropertyString() {
        StringBuffer buff = new StringBuffer();
        //property string is just a json
        ObjectMapper mapper = new ObjectMapper();
        try {
            String value = mapper.writeValueAsString(this);
            buff.append(value);
        } catch (Exception e) {
            _bot.logError(e);
        }
        return buff.toString();
    }
    
    public void setId(long id) {
        _id = id;
    }
    
    public long getId() {
        return _id;
    }
    
    public Map<String, Object> createTriggerData(Scanner fromInput) {
        // default implementation is no data
        return null;
    }

    @Override
    public int compareTo(AbstractTrigger yours) {
        if (yours == null) {
            // I'm better
            return 1;
        } else {
            return getClass().getSimpleName().compareTo(yours.getClass().getSimpleName());
        }
    }

    public String getRecommendedFrequency() {
        return "00:10:00";
    }

    public boolean matchEvent(JSONObject eventData) {
        // default: false
        return false;
    }

    public Collection<Class<? extends JSONObject>> getEventClasses() {
        // default: empty list
        return new ArrayList<Class<? extends JSONObject>>();
    }

    protected void setMatchedObject(JSONObject obj) {
        _matchedObject = obj;
    }
    
    public JSONObject getMatchedObject() {
        return _matchedObject;
    }

}

