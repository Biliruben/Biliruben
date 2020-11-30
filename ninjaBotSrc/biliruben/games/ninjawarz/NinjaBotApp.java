package biliruben.games.ninjawarz;

import java.io.IOException;

import biliruben.games.ninjawarz.command.CoreCommandGroup;
import biliruben.games.ninjawarz.command.ExclusionPreferencesCommands;
import biliruben.games.ninjawarz.command.TriggerCommands;

import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionLegend;

/**
 * The application.
 * @author trey.kirk
 * TODO: I'm creating the commands here, why aren't I parsing the the commands also?
 */
public class NinjaBotApp {

    private static Boolean _isKongregate;
    private static String _propFile;


    public static final String OPT_KONGREGATE_FLAG = "kongregate";
    public static final String OPT_PROP_FILE = "ninjaProperties";

    static void init(String[] args) {
        GetOpts opts = new GetOpts(NinjaBot.class);
    
        OptionLegend legend = new OptionLegend(NinjaBotApp.OPT_PROP_FILE);
        legend.setRequired(false);
        legend.setDescription("Properties file to read from and write to.  Filename only!");
        opts.addLegend(legend);
    
        legend = new OptionLegend(NinjaBotApp.OPT_KONGREGATE_FLAG);
        legend.setFlag(true);
        legend.setDescription("When enabled, the Kongregate version of NinjaBot is invoked");
        opts.addLegend(legend);
    
        opts.parseOpts(args);
    
        /*----------- parse options ---------*/
        _propFile = opts.getStr(NinjaBotApp.OPT_PROP_FILE);
        _isKongregate = Boolean.valueOf(opts.getStr(NinjaBotApp.OPT_KONGREGATE_FLAG));
    }
    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        init(args);
        NinjaBot bot;
        if (_isKongregate) {
            bot = new KongregateNinjaBot(_propFile);
        } else {
            bot = new NinjaBot(_propFile);
        }
        setupNinjaCommands(bot);
        /*
        Trigger zombie = new HordeAvailableTrigger();
        NinjaCommand zombieCommand = bot.processCommand("fightz", null);
        zombie.setCommands(new NinjaCommand[]{zombieCommand});
        bot.addTrigger(zombie);
        */
        
        bot.start();
    }
    
    private static void setupNinjaCommands(NinjaBot bot) {
        // NinjaBot creates its own internal set of commands. Why wouldn't we have those in there?
        // Because we want NinjaBot to be rather abstract allowing implementers to create their own "flavor"
        // of ninja bot.  I.e. this application uses NinjaBot as a direct client and thus has a rich
        // set of commands.  However, an app like the ClanMapper is interested in a very small
        // set of commands and wants to keep its command set diffeerent from this command
        // set.

        CoreCommandGroup commandGroup = new CoreCommandGroup(bot);
        bot.addNinjaCommands(commandGroup);
        
        TriggerCommands triggerGroup = new TriggerCommands(bot);
        bot.addNinjaCommands(triggerGroup);
        
        ExclusionPreferencesCommands exclusionGroup = new ExclusionPreferencesCommands(bot);
        bot.addNinjaCommands(exclusionGroup);
    }


}
