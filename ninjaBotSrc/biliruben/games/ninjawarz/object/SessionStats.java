package biliruben.games.ninjawarz.object;

import java.io.PrintStream;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import biliruben.games.ninjawarz.Util;

@JsonIgnoreProperties(ignoreUnknown=true)
public class SessionStats {

    private int _karmaGained;
    private int _goldStolen;
    private int _goldGained;
    private int _fightsWon;
    private int _fightsLost;
    private int _expGained;
    private int _itemsGained;
    private int _karmaValueGained;
    private int _goldValueGained;
    private int _goldSpent;
    private Date _created;
    private int _oppsKilled;
    private int _zombieMaxKills;
    private int _zombieKillls;
    
    public SessionStats() {
        _created = new Date();
        _karmaGained = 0;
        _goldStolen = 0;
        _goldGained = 0;
        _fightsWon = 0;
        _fightsLost = 0;
        _expGained = 0;
        _itemsGained = 0;
        _karmaValueGained = 0;
        _goldValueGained = 0;
        _goldSpent = 0;
        _oppsKilled = 0;
        _zombieMaxKills = 0;
        _zombieKillls = 0;
    }
    
    public void addSpoils(Spoils spoils) {
        if (spoils == null) {
            return;
        }
        Item[] items = spoils.getItems();
        _expGained += spoils.getExp();
        _goldGained += spoils.getGold();
        if (items != null) {
            _itemsGained += items.length;
            processItemsValue(items);
        }
        _karmaGained += spoils.getKarma();
        int kills = spoils.getKills();
        if (_zombieMaxKills < kills) {
            _zombieMaxKills = kills;
        }
        _zombieKillls += kills; 
        
    }

    private void processItemsValue(Item[] items) {
        for (Item item : items) {
            _karmaValueGained += item.getKarma_value() * Item.SELL_VALUE_MOD;
            _goldValueGained += item.getValue() * Item.SELL_VALUE_MOD;
        }
    }
    
    public void addBattle(Battle battle) {
        if (battle == null) {
            return;
        }
        Spoils spoils = battle.getSpoils();
        addSpoils(spoils);
        if ("loss".equals(battle.getResult())) {
            _fightsLost++;
        } else if ("win".equals(battle.getResult())) {
            _fightsWon++;
            if (battle.isKilled()) {
                _oppsKilled++;
            }
        }
        
    }
    
    public Map<String, String> getStatsMap() {
        Map<String, String> map = new TreeMap<String, String>();
        map.put("Karma Gained", String.valueOf(_karmaGained));
        map.put("Karma Value Gained", String.valueOf(_karmaValueGained));
        map.put("Gold Gained", String.valueOf(_goldGained));
        map.put("Gold Value Gained", String.valueOf(_goldValueGained));
        map.put("Gold Stolen", String.valueOf(_goldStolen));
        map.put("Gold Spent", String.valueOf(_goldSpent));
        map.put("Exp Gained", String.valueOf(_expGained));
        map.put("Items Gained", String.valueOf(_itemsGained));
        map.put("Fights Won", String.valueOf(_fightsWon));
        map.put("Fights Lost", String.valueOf(_fightsLost));
        map.put("Opponents Killed", String.valueOf(_oppsKilled));
        map.put("Zombies Killed", String.valueOf(_zombieKillls));
        map.put("Most Zombies Killed", String.valueOf(_zombieMaxKills));
        return map;
    }
    
    public String getSessionDuration() {
        long duration = new Date().getTime() - _created.getTime();
        return Util.getDurationString(duration);
    }
    
    public void printStats(PrintStream print) {
        /*
        getOutput().printf("%-5s", item.getIid());
        getOutput().printf("%-7s", "x" + aggregator.getCount(item));
        */
        print.printf("%-25s", "Session Duration");
        print.println(getSessionDuration() + "\n");
        Map<String, String> stats = getStatsMap();
        for (String key : stats.keySet()) {
            print.printf("%-25s", key);
            print.println(Util.commaNumber(stats.get(key)));
        }
        
    }

    public void addHospital(HospitalResult result) {
        if (result == null) {
            return;
        }
        _goldSpent += result.getGold_spent();
    }
    
}
