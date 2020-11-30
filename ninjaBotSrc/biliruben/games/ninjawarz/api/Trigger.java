package biliruben.games.ninjawarz.api;

import java.util.Map;
import java.util.Scanner;

import biliruben.games.ninjawarz.command.NinjaCommand;
import biliruben.games.ninjawarz.object.JSONObject;

public interface Trigger {

    public static final int  EXECUTION_ONCE = 1;
    public static final int  EXECUTION_INIFINITE = -1;

    public abstract void setCommands(NinjaCommand[] commands);

    public abstract NinjaCommand[] getCommands();
    
    public abstract void setCommandStrings(String[] commands);
    
    public abstract String[] getCommandStrings();

    public abstract long getExpiration();

    public abstract void setExpiration(long expiration);

    public abstract boolean isExpired();

    public abstract boolean isExpires();

    public abstract void setExpires(boolean expires);

    public abstract boolean isReady();

    public abstract String toPropertyString();

    public abstract void setId(long id);

    public abstract long getId();

    public abstract Map<String, Object> getTriggerData();

    public abstract void setTriggerData(Map<String, Object> data);
    
    public abstract void setExecutions(int executions);
    
    public abstract int getExecutions();

    public abstract Map<String, Object> createTriggerData(Scanner fromInput);

    public abstract JSONObject getMatchedObject();


}