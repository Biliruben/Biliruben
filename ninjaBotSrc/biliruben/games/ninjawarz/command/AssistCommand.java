package biliruben.games.ninjawarz.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;
import biliruben.games.ninjawarz.object.Clan;

public class AssistCommand extends NinjaCommand {

    public AssistCommand(NinjaBot bot, String commandName) {
        super(bot, commandName, "Assists an ally");
        setExtendedHelp("This command takes one optional argument that is either an ally's clan ID or a clan's name.  When a clan is provided, " +
                "you will assist the requested ally.  If no argument is provided, NinjaBot will traverse through your preferred ally list and assist " +
                "them.  After they're helped, if you still have available allies to assist, it will continue with your lowest level allies.");
    }

    @Override
    public void execute() throws NinjaBotException {
        try {
            if (_arguments.length < 1) {
                List<Clan> allies = new ArrayList<Clan>();
                // get the preferred allies first
                allies.addAll(getBot().getPreferredAllies());
                
                // get the bottom 25 (aka first 25 elements) of the ally list and assist them
                allies.addAll(Arrays.asList(getBot().getAllies(Clan.COL_LEVEL)));
                int maxAllies = 25;
                int maxErrors = 100;
                if (allies != null && allies.size() > 0) {
                    for (int i = 0; i < allies.size() && i < maxAllies && i < maxErrors; i++) {
                        Clan ally = allies.get(i);
                        if (ally.getNeeds_assistance() > 0) {
                            if (!getBot().assistClan(ally)) {
                                maxAllies++;
                                maxErrors++;
                                if (i > maxErrors) {
                                    getBot().getOutput().println("Aborting due to excessive failes");
                                }
                            }
                        } else {
                            // this one needs no help, try another
                            getBot().getOutput().println(ally.getName() + " doesn't need assistance right now");
                            maxAllies++;
                        }
                    }
                } else {
                    getBot().getOutput().println("You have no friends!");
                }
                
            } else {

                Clan clan = getBot().getClan(_arguments[0]);
                if (clan == null) {
                    getBot().getOutput().println("That clan does not exist!");
                    return;
                } 
                getBot().assistClan(clan);
            }
        } catch (IOException  e) {
            getBot().logError(e);
        }

    }

}
