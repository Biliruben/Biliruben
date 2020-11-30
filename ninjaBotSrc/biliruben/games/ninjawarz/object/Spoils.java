package biliruben.games.ninjawarz.object;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/*
 * "spoils":{"gold":0,"items":[],"exp":1,"unlocked":[],"achievements":[]},
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Spoils extends JSONObject {
    
    private int gold;
    private Item[] items;
    private int exp;
    private Item[] unlocked;
    private Object[] achievements;
    private boolean leveled;
    private String stolen;
    private boolean high_score;
    private int kills;
    private int karma;
    private String destined;
    private long bid;
    private long ninja;
    
    public int getGold() {
        return gold;
    }
    public void setGold(int gold) {
        this.gold = gold;
    }
    public Item[] getItems() {
        return items;
    }
    public void setItems(Item[] items) {
        this.items = items;
    }
    public int getExp() {
        return exp;
    }
    public void setExp(int exp) {
        this.exp = exp;
    }
    public Item[] getUnlocked() {
        return unlocked;
    }
    public void setUnlocked(Item[] unlocked) {
        this.unlocked = unlocked;
    }
    public Object[] getAchievements() {
        return achievements;
    }
    public void setAchievements(Object[] ahievements) {
        this.achievements = ahievements;
    }
    public String getStolen() {
        return stolen;
    }
    public void setStolen(String stolen) {
        this.stolen = stolen;
    }
    public boolean isHigh_score() {
        return high_score;
    }
    public void setHigh_score(boolean high_score) {
        this.high_score = high_score;
    }
    public int getKills() {
        return kills;
    }
    public void setKills(int kills) {
        this.kills = kills;
    }
    public int getKarma() {
        return karma;
    }
    public void setKarma(int karma) {
        this.karma = karma;
    }
    public String getDestined() {
        return destined;
    }
    public void setDestined(String destined) {
        this.destined = destined;
    }
    public boolean isLeveled() {
        return leveled;
    }
    public void setLeveled(boolean leveled) {
        this.leveled = leveled;
    }
    public long getBid() {
        return bid;
    }
    public void setBid(long bid) {
        this.bid = bid;
    }
    public long getNinja() {
        return ninja;
    }
    public void setNinja(long ninja) {
        this.ninja = ninja;
    }

}
