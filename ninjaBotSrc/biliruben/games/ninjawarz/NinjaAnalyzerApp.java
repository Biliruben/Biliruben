package biliruben.games.ninjawarz;

import java.io.IOException;

import biliruben.games.ninjawarz.analyze.BattleAnalyzerCommands;
import biliruben.games.ninjawarz.api.CommandGroup;

import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionLegend;

public class NinjaAnalyzerApp {

    private static final String OPT_PROPERTIES = "ninjaProperties";
    private static final String OPT_KONG_FLAG = "kongregate";
    private static GetOpts _opts;

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        init(args);
        boolean kong = Boolean.valueOf(_opts.getStr(OPT_KONG_FLAG));
        String props = _opts.getStr(OPT_PROPERTIES);
        NinjaBot bot = null;
        if (kong) {
            bot = new KongregateNinjaBot(props);
        } else {
            bot = new NinjaBot(props);
        }
        CommandGroup commands = getCommands(bot);
        bot.addNinjaCommands(commands);
        bot.pauseTriggers(); // no triggers, plz
        bot.start();
    }
    
    private static CommandGroup getCommands(NinjaBot bot) {
        BattleAnalyzerCommands group = new BattleAnalyzerCommands(bot);
        return group;
    }
    
    private static void init(String[] args) {
        _opts = new GetOpts(NinjaAnalyzerApp.class);
        
        OptionLegend legend = new OptionLegend(OPT_PROPERTIES);
        legend.setRequired(false);
        _opts.addLegend(legend);
        
        legend = new OptionLegend(OPT_KONG_FLAG);
        legend.setFlag(true);
        _opts.addLegend(legend);
        
        _opts.parseOpts(args);
    }

}
