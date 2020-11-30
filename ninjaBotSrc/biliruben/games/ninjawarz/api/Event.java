package biliruben.games.ninjawarz.api;

import java.util.Collection;

import biliruben.games.ninjawarz.command.NinjaCommand;
import biliruben.games.ninjawarz.object.JSONObject;


public interface Event extends Trigger {

    public boolean matchEvent(JSONObject eventData);
    
    public Collection<Class<? extends JSONObject>> getEventClasses();
    
    public void setCommands(NinjaCommand[] commands);
    
    public NinjaCommand[] getCommands();
    
    public long getId();
        
    public void setId(long id);
}
