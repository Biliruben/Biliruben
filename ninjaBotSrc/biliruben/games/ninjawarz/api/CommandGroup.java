package biliruben.games.ninjawarz.api;

import java.util.Collection;
import java.util.Set;

import biliruben.games.ninjawarz.command.NinjaCommand;

public interface CommandGroup {
    
    public Collection<NinjaCommand> getCommands();

}
