package biliruben.games.ninjawarz;

import java.io.IOException;

import biliruben.api.Command;

import com.biliruben.util.csv.CSVSource;
import com.biliruben.util.csv.CSVSource.CSVType;
import com.biliruben.util.csv.CSVSourceImpl;

public abstract class NinjaCommand implements Command {

    protected NinjaBot _bot;
    protected String[] _arguments;

    public NinjaCommand(NinjaBot bot) {
        _bot = bot;
    }

    public void setArguments(String[] arguments) {
        _arguments = arguments;
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

}
