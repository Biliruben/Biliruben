package biliruben.games.ninjawarz.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;

public class DeleteCommand extends NinjaCommand {

    public static interface Deletable {
        
        public void callDelete(String[] args) throws NinjaBotException;
        
        public String getObjectType();
    }

    public static final String DEFAULT_NAME = "delete";
    
    private List<Deletable> _deletables;
    
    public DeleteCommand(NinjaBot bot, String commandName) {
        super(bot, commandName, "Deletes a configuration object from NinjaBot");
        _deletables = new ArrayList<Deletable>();
    }
    
    public DeleteCommand(NinjaBot bot) {
        this (bot, DEFAULT_NAME);
    }
    
    @Override
    public NinjaCommand getCopy() {
        DeleteCommand deleteCommand = (DeleteCommand)super.getCopy();
        deleteCommand.setDeletables(_deletables);
        return deleteCommand;
    }
    
    private void setDeletables(List<Deletable> deletables) {
        _deletables = deletables;
    }

    public void add(Deletable deletable) {
        _deletables.add(deletable);
    }
    
    public void remove(Deletable deletable) {
        _deletables.remove(deletable);
    }
    
    protected List<Deletable> getDeletables() {
        return _deletables;
    }

    @Override
    public void execute() throws NinjaBotException {

        if (_arguments.length == 0) {
            throw new NinjaBotException(getName() + " what? " + getName() + " requires one of the following arguments: " + Arrays.toString(getDeleteArguments()));
        }

        String deleteType = _arguments[0];
        for (Deletable deletable : getDeletables()) {
            if (deletable.getObjectType().toLowerCase().startsWith(deleteType.toLowerCase())) {
                String[] newArgs = new String[_arguments.length - 1];
                System.arraycopy(_arguments, 1,newArgs, 0, newArgs.length);
                deletable.callDelete(newArgs);
                return;
            }
        }
        // did you get here?  That's bad!
        throw new NinjaBotException (deleteType + " not found!  " + getName() + " requires one of the following arguments: " + Arrays.toString(getDeleteArguments()));
    }
    
    private String[] getDeleteArguments() {
        String[] deleteArguments = new String[_deletables.size()];
        int i = 0;
        for (Deletable deletable : _deletables) {
            deleteArguments[i] = deletable.getObjectType();
            i++;
        }
        Arrays.sort(deleteArguments);
        return deleteArguments;
    }

}
