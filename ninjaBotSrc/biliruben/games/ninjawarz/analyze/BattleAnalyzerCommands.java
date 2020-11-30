package biliruben.games.ninjawarz.analyze;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;
import biliruben.games.ninjawarz.api.CommandGroup;
import biliruben.games.ninjawarz.api.JSONParser;
import biliruben.games.ninjawarz.command.NinjaCommand;
import biliruben.games.ninjawarz.object.Battle;
import biliruben.games.ninjawarz.object.Clan;
import biliruben.games.ninjawarz.object.Fight;
import biliruben.games.ninjawarz.object.Ninja;

import com.biliruben.util.csv.CSVRecord;

public class BattleAnalyzerCommands implements CommandGroup {

    static void serializeBattle(Battle battle) throws IOException {
        // in a single file, store:
        // csv breakdown of my ninjas
        // csv breakdown of opps ninjas
        // csv breakdown of each battle
        
        // determine filename
        Clan me = battle.getMe();
        Clan opp = battle.getOpponent();
        String fileName = me.getName() + "_vs_" + opp.getName() + "_" + (new Date().getTime()/1000) + ".csv";
        
        // setup the file
        FileWriter writer = new FileWriter(fileName); // using pwd
        
        // CSV my ninjas
        writeNinjaCsv(writer, me);
        writer.flush();
        
        // CSV opp ninjas
        writeNinjaCsv(writer, opp);
        writer.flush();
        
        // CSV the battle data
        writeFightsCsv(writer, battle.getFights());
        writer.flush();
        writer.close();
    }
    
    private static void writeFightsCsv(FileWriter writer, Fight[] fights) throws IOException {
        String[] outputFields = {"Good Guy", "Bad Guy", "actor", "atk damage", "atk type"};
        CSVRecord record = new CSVRecord(outputFields);
        record.setIncludeFieldNames(true);
        record.setDelimiter(",");
        for (Fight fight : fights) {
            Map<String, String> baseLine = new HashMap<String, String>();
            baseLine.put("Good Guy", String.valueOf(fight.getMy_ninja()));
            baseLine.put("Bad Guy", String.valueOf(fight.getOpp_ninja()));
            addCombats(record, baseLine, fight);
        }
        Iterator<String> csvIt = record.iterator();
        while (csvIt.hasNext()) {
            writer.write(csvIt.next());
        }
    }
    
    private static void addCombats(CSVRecord record, Map<String, String> baseLine, Fight fight) {
        int[][] combats = fight.getCombat();
        for (int[] combat : combats) {
            Map<String, String> line = new HashMap<String, String>(baseLine);
            // combat is a 4 element int[]: [actor, damage, opp health, type]
            line.put("actor", String.valueOf(combat[0]));
            line.put("atk damage", String.valueOf(combat[1]));
            line.put("atk type", String.valueOf(combat[3]));
            record.addLine(line);
        }
    }
    
    private static void writeNinjaCsv(FileWriter writer, Clan clan) throws IOException {
        String[] fields = {"nid", "level", "index", "speed", "damage", "max health", "power", "crit rate", "armor", "agility"};
        CSVRecord record = new CSVRecord(fields);
        record.setDelimiter(",");
        record.setIncludeFieldNames(true);
        int idx = 0;
        for (Ninja ninja : clan.getNinjas()) {
            Map<String, String> line = new HashMap<String, String>();
            line.put("nid", String.valueOf(ninja.getNid()));
            line.put("level", String.valueOf(ninja.getLevel()));
            line.put("index", String.valueOf(idx));
            line.put("speed", String.valueOf(ninja.getWeapon().getAttributes().get("speed")));
            line.put("damage", String.valueOf(ninja.getWeapon().getAttributes().get("damage")));
            line.put("max health", String.valueOf(ninja.getModified_max_health()));
            line.put("power", String.valueOf(ninja.getModified_power()));
            line.put("crit rate", String.valueOf(ninja.getCrit_rate()));
            line.put("armor", String.valueOf(ninja.getArmor()));
            line.put("agility", String.valueOf(ninja.getAgility()));
            record.addLine(line);
            idx++;
        }
        
        Iterator<String> csvIt = record.iterator();
        while (csvIt.hasNext()) {
            writer.write(csvIt.next());
        }
        writer.write("\n"); // line sep    
    }
    
    private NinjaBot _bot;

    public BattleAnalyzerCommands(NinjaBot bot) {
        _bot = bot;
    }
    
    public static class WhoAmI extends NinjaCommand {
        public WhoAmI(NinjaBot bot, String commandName) {
            super(bot, commandName, "Shows your clan information");
        }

        @Override
        public void execute() throws NinjaBotException {
            try {
                getBot().printMe();
            } catch (IOException e) {
                getBot().logError(e);
            }
        }
    }
    
    public static class ImportJsonCommand extends NinjaCommand {

        public ImportJsonCommand(NinjaBot bot, String commandName) {
            super(bot, commandName, "Imports a JSON file for output conversion");
        }

        @Override
        public void execute() throws NinjaBotException {
            // Imports a Battle JSON, creates the json Battle object, serializes new CSV(s)
            try {
                FileReader reader = new FileReader(getArguments()[0]);
                StringBuilder buff = new StringBuilder();
                char c = 0;
                while (reader.ready() && c != -1) {
                    c = (char) reader.read();
                    if (c != -1) {
                        buff.append(c);
                    }
                }
                String battleJson = buff.toString();
                JSONParser parser = new JSONParser(getBot());
                Battle battle = parser.parseJSON(battleJson, Battle.class);
                serializeBattle(battle);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
    }
    
    public static class AnalyzeBattleCommand extends NinjaCommand {
        public AnalyzeBattleCommand(NinjaBot bot, String commandName) {
            super(bot, commandName, "Executes a battle and provides Battle output");
        }

        @Override
        public void execute() throws NinjaBotException {
            // Executes the Battle, uses the return object to serialize new CSV(s)
            String fid = getArguments()[0];
            try {
                Battle battle = getBot().fight(fid);
                serializeBattle(battle);
            } catch (IOException e) {
                getBot().logError(e);
            }
            
        }
        
    }
    
    @Override
    public Collection<NinjaCommand> getCommands() {
        List<NinjaCommand> commands = new ArrayList<NinjaCommand>();
        commands.add(new ImportJsonCommand(_bot, "import"));
        commands.add(new AnalyzeBattleCommand(_bot, "battle"));
        commands.add(new WhoAmI(_bot, "whoAmI"));
        return commands;
    }
    
}
