package biliruben.games.ninjawarz.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.Configuration;
import biliruben.games.ninjawarz.object.Clan;

public class ClanService {
    
    public static final String FIGHT_EXCLUSION_KEY_PREFIX = "fight.exclude.";
    public static final String ASSIST_EXCLUSION_KEY_PREFIX = "assist.exclude.";
    public static final String ASSIST_PREFER_KEY_PREFIX = "assist.prefer.";

    private NinjaBot _bot;

    public ClanService(NinjaBot bot) {
        _bot = bot;
    }
    
    private boolean isMatchedByKey(Clan clan, String propertyPrefix) {
        boolean matched = false;
        if (!propertyPrefix.endsWith(".")) {
            propertyPrefix += ".";
        }
        if (clan != null) {
            String name = clan.getName();
            if (name != null) {
                matched = _bot.getConfiguration().getFlag(propertyPrefix + name);
            }
            if (!matched) {
                // try by ID also
                long cid = clan.getCid();
                matched = _bot.getConfiguration().getFlag(propertyPrefix + cid);
            }
        }
        return matched;
    }
    
    public boolean isExcludedClanFight(Clan clan) {
        return isMatchedByKey(clan, FIGHT_EXCLUSION_KEY_PREFIX);
    }
    
    public boolean isExcludedClanAssist(Clan clan) {
        return isMatchedByKey(clan, ASSIST_EXCLUSION_KEY_PREFIX);
    }
    
    public boolean isPreferredClanAssist(Clan clan) {
        return isMatchedByKey(clan, ASSIST_PREFER_KEY_PREFIX);
    }
    
    public void addFightExclusion(String clanNameOrId) throws IOException {
        Clan clan = _bot.getClan(clanNameOrId);
        if (clan != null) {
            _bot.getConfiguration().setProperty(FIGHT_EXCLUSION_KEY_PREFIX + clan.getCid(), "true");
        }
    }
    
    public boolean removeFightExclusion(String clanNameOrId) throws IOException {
        Clan clan = _bot.getClan(clanNameOrId);
        if (clan != null) {
            _bot.getConfiguration().removeProperty(FIGHT_EXCLUSION_KEY_PREFIX + clan.getCid());
            return true;
        }
        return false;
    }
    
    public void addAssistExclusion(String clanNameOrId) throws IOException {
        Clan clan = _bot.getClan(clanNameOrId);
        if (clan != null) {
            _bot.getConfiguration().setProperty(ASSIST_EXCLUSION_KEY_PREFIX + clan.getCid(), "true");
        }
    }
    
    public boolean removeAssistExclusion(String clanNameOrId) throws IOException {
        Clan clan = _bot.getClan(clanNameOrId);
        if (clan != null) {
            _bot.getConfiguration().removeProperty(ASSIST_EXCLUSION_KEY_PREFIX + clan.getCid());
            return true;
        }
        return false;
    }
    
    public void addAssistPrefer(String clanNameOrId) throws IOException {
        Clan clan = _bot.getClan(clanNameOrId);
        if (clan != null) {
            _bot.getConfiguration().setProperty(ASSIST_PREFER_KEY_PREFIX + clan.getCid(), "true");
        }
    }
    
    public boolean removeAssistPrefer(String clanNameOrId) throws IOException {
        Clan clan = _bot.getClan(clanNameOrId);
        if (clan != null) {
            _bot.getConfiguration().removeProperty(ASSIST_PREFER_KEY_PREFIX + clanNameOrId);
            return true;
        }
        return false;
    }

    public void printClanList(Collection<Clan> clans) {
        int COL_ID_LEN = 10;
        int COL_NAME_LEN = 20;
        _bot.getOutput().printf("%-" + COL_ID_LEN + "s", "CID");
        _bot.getOutput().printf("%-" + COL_NAME_LEN + "s", "Name");
        _bot.getOutput().println();
        _bot.getOutput().println(_bot.LINE_SEP);
        if (clans == null || clans.size() == 0) {
            _bot.getOutput().println("No clans found");
        } else {
            for (Clan clan : clans) {
                _bot.getOutput().printf("%-" + COL_ID_LEN + "s", clan.getCid());
                _bot.getOutput().printf("%-" + COL_NAME_LEN + "s", clan.getName());
                _bot.getOutput().println();
            }
        }
    }
    
    private List<Clan> getMatchClansInProperties(String propertyPrefix) throws IOException {
        List<Clan> clans = new ArrayList<Clan>();
        Map<String, String> excludeProps = _bot.getConfiguration().getProperties(propertyPrefix);
        for (String key : excludeProps.keySet()) {
            String cid = key;
            Clan clan = _bot.getClan(cid);
            if (!isExcludedClanAssist(clan)) {
                clans.add(clan);
            }
        }
        return clans;
    }
    
    public List<Clan> getExcludedFights() throws IOException {
        return getMatchClansInProperties(FIGHT_EXCLUSION_KEY_PREFIX);
    }
    
    public List<Clan> getExludedAssist() throws IOException {
        return getMatchClansInProperties(ASSIST_EXCLUSION_KEY_PREFIX);
    }
    
    public List<Clan> getPreferredAssist() throws IOException {
        return getMatchClansInProperties(ASSIST_PREFER_KEY_PREFIX);
    }
    

}
