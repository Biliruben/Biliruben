package biliruben.games.ninjawarz.command;

import java.io.IOException;
import java.lang.reflect.Constructor;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;

import com.biliruben.util.csv.CSVSource;
import com.biliruben.util.csv.CSVSource.CSVType;
import com.biliruben.util.csv.CSVSourceImpl;

@JsonIgnoreProperties({"bot", "prototype"})
public abstract class NinjaCommand implements Comparable<NinjaCommand> {

    //TODO: Remove the dependancy for a NinjaBot.  Execute() should ensure one's provided
    private NinjaBot _bot;
    protected String[] _arguments;
    private String _name;
    private String _description;
    private boolean _hidden = false;
    private String _extendedHelp;

    protected NinjaCommand(NinjaBot bot, String commandName, String commandDescription) {
        _bot = bot;
        _name = commandName;
        _description = commandDescription;
    }
    
    public NinjaCommand getCopy() {
        try {
            Constructor<? extends NinjaCommand> construct = this.getClass().getConstructor(NinjaBot.class, String.class);
            NinjaCommand neu = construct.newInstance(_bot, this.getName());
            return neu;
        } catch (Throwable e) {
            _bot.logError(e);
        }
        return null;
    }
    
    public String getExtendedHelp() {
        if (_extendedHelp == null || "".equals(_extendedHelp.trim())) {
            return getDescription();
        } else {
            return _extendedHelp;
        }
    }
    
    public void setExtendedHelp(String extendedHelp) {
        _extendedHelp = extendedHelp;
    }

    protected NinjaBot getBot() {
        return _bot;
    }
    
    public String getName() {
        return _name;
    }
    
    public String getDescription() {
        return _description;
    }

    public String[] getArguments() {
        return _arguments;
    }
    
    public void setArguments(String[] arguments) {
        _arguments = arguments;
    }
    
    public void setHidden(boolean isHidden) {
        _hidden = isHidden;
    }
    
    public boolean isHidden() {
        return _hidden;
    }

    public abstract void execute() throws NinjaBotException;

    public boolean matchesName (String partial) {
        boolean matched = false;
        String tempPartial = partial.toLowerCase();
        String tempActual = _name.toLowerCase();
        matched = tempActual.startsWith(tempPartial);

        return matched;
    }

    public void parseArguments(String argumentsString) {
        if (argumentsString == null || "".equals(argumentsString.trim())) {
            // no arguments
            setArguments(new String[0]);
        } else {
            CSVSource csv = new CSVSourceImpl(argumentsString, CSVType.WithOutHeader);
            csv.setDelim(' ');
            String[] tokens = new String[0];
            try {
                tokens = csv.getNextLine();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            setArguments(tokens);
        }
    }
    
    @Override
    public int compareTo(NinjaCommand yours) {
        if (yours == null) {
            return 1;
        }
        
        return getName().compareTo(yours.getName());
    }

    @Override
    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append(_name);
        str.append(": ").append(_description);
        return str.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        NinjaCommand yours = (NinjaCommand)obj;
        return yours.getName().equals(yours.getName());
    }
    
    @Override
    public int hashCode() {
        String desc = _description == null ? "" : _description;
        int hash = (_name.hashCode() * 1) + (_description.hashCode() * 7);
        return hash;
    }

}
