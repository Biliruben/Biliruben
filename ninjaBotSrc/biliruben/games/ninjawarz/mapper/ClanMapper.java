package biliruben.games.ninjawarz.mapper;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import biliruben.games.ninjawarz.KongregateNinjaBot;
import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotApp;
import biliruben.games.ninjawarz.NinjaBotException;
import biliruben.games.ninjawarz.command.NinjaCommand;
import biliruben.games.ninjawarz.command.QueueCommand;
import biliruben.games.ninjawarz.object.Clan;

import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionLegend;

public class ClanMapper {

    private static final String OP_START_CID = "startAt";
    private static String _propFile;
    private static Boolean _isKongregate;
    private static Long _cid;
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

        legend = new OptionLegend(OP_START_CID);
        legend.setRequired(false);
        legend.setDescription("Cland ID to start scanning at");
        legend.setDefaultValue("1000");
        opts.addLegend(legend);

        opts.parseOpts(args);

        /*----------- parse options ---------*/
        _propFile = opts.getStr(NinjaBotApp.OPT_PROP_FILE);
        _isKongregate = Boolean.valueOf(opts.getStr(NinjaBotApp.OPT_KONGREGATE_FLAG));
        _cid = Long.valueOf(opts.getStr(OP_START_CID));
    }

    public void scan (NinjaBot bot) {

        String filename = "clanScan_" + new Date().getTime() + ".csv";
        FileWriter writer = null;
        try {
            writer = new FileWriter(filename);
        } catch (IOException e1) {
            bot.logError(e1);
            return;
        }
        while (!bot.isTermnated()) {
            try {
                Clan clan = bot.getClan(_cid);
                if (clan != null) {
                    String line = clan.getCid() + "," + clan.getName() + "," + clan.getLevel() + "\n";
                    writer.write(line);
                    bot.getOutput().print(line);
                    writer.flush();
                }
            } catch (IOException e) {
                bot.logError(e);
            }
            _cid++;
        }

        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            bot.logError(e);
        }

    }


    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        init(args);
        final NinjaBot bot;
        if (_isKongregate) {
            bot = new KongregateNinjaBot(_propFile);
        } else {
            bot = new NinjaBot(_propFile);
        }
        final ClanMapper mapper = new ClanMapper();
        //setupNinjaCommands(bot); // no commands
        NinjaCommand scan = new NinjaCommand(bot, "scan", "Scans the clan IDs") {

            @Override
            public void execute() throws NinjaBotException {
                NinjaCommand queue = new QueueCommand(bot, "queue");
                String[] scanArgs = {"scanInline"};
                queue.setArguments(scanArgs);
                bot.dispatchCommand(queue);
            }

            @Override
            public NinjaCommand getCopy() {
                return this; // copy away
            }
        };
        
        NinjaCommand scanInline = new NinjaCommand(bot, "scanInline", ""){

            @Override
            public void execute() throws NinjaBotException {
                mapper.scan(bot);
            }

            @Override
            public NinjaCommand getCopy() {
                return this;
            }
        };
        bot.addNinjaCommand(scan);
        bot.addNinjaCommand(scanInline);
        bot.start();
    }

}
