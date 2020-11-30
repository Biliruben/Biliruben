package biliruben.games.ninjawarz.object;

import java.util.Comparator;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import biliruben.games.ninjawarz.Util;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Item extends JSONObject implements Comparable<Item> {

    public static final int TYPE_WEAPON = 1;
    public static final int TYPE_RELIC = 2;
    private static final double SPEED_STANDARD_ONE_SECOND = 100;
    public static final double SELL_VALUE_MOD = 0.5;
    public static final String FISTS = "fists";
    
    public static class ItemDpsComparator implements Comparator<Item> {

        @Override
        public int compare(Item mine, Item yours) {
            if (mine != null && yours == null) {
                return 1;
            } else if (mine == null && yours != null) {
                return -1;
            } else if (mine == null) {
                return 0;
            }
            
            Map myAttributes = mine.getAttributes();
            Map yourAttributes = yours.getAttributes();
            Double myDps = 0.0;
            Double yourDps = 0.0;
            if (myAttributes != null) {
                myDps =(Double)myAttributes.get("DPS");
            }
            if (yourAttributes != null) {
                yourDps = (Double)yourAttributes.get("DPS");
            }
            
            return myDps.compareTo(yourDps);
        }
        
    }
    
    private String sprite;
    private int unique;
    private int iid;
    private int rarity;
    private int is_plural;
    private int type;
    private int standanimation;
    private String description;
    private String name;
    private long value;
    private int karma_value;
    private Map attributes;
    //private Attributes attributes;
    private int slot;
    private int runanimation;
    private int sort_order;
    
    @Override
    public String getId() {
        return String.valueOf(iid);
    }
    
    public int getUnique() {
        return unique;
    }
    public void setUnique(int unique) {
        this.unique = unique;
    }
    public int getIid() {
        return iid;
    }
    public void setIid(int iid) {
        this.iid = iid;
    }
    public int getRarity() {
        return rarity;
    }
    public void setRarity(int rarity) {
        this.rarity = rarity;
    }
    public int getIs_plural() {
        return is_plural;
    }
    public void setIs_plural(int is_plural) {
        this.is_plural = is_plural;
    }
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public int getStandanimation() {
        return standanimation;
    }
    public void setStandanimation(int standanimcation) {
        this.standanimation = standanimcation;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getMinLevel() {
        int minLevel = 0;
        if (attributes != null) {
            String strMinLevel = (String)attributes.get("min_level");
            if (strMinLevel != null && !"".equals(strMinLevel.trim())) {
                minLevel = Integer.valueOf(strMinLevel);
            }
        }
        return minLevel;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public long getValue() {
        return value;
    }
    public void setValue(long value) {
        this.value = value;
    }
    
    public int getSellValue() {
        int sellValue = 0;
        if (karma_value > 0) {
            sellValue = (int)(karma_value * SELL_VALUE_MOD);
        } else {
            sellValue = (int)(value * SELL_VALUE_MOD);
        }
        return sellValue;
    }
    
    public int getKarma_value() {
        return karma_value;
    }
    public void setKarma_value(int karma_value) {
        this.karma_value = karma_value;
    }
    public Map getAttributes() {
        return attributes;
    }
    public void setAttributes(Object attributes) {
        if (attributes instanceof Map) {
            this.attributes = (Map)attributes;
            if (getType() == TYPE_WEAPON) {
                Object damObj = this.attributes.get("damage");
                Object speObj = this.attributes.get("speed");
                if (damObj != null && speObj != null) {
                    double damage = Double.valueOf(damObj.toString());
                    double speed = Double.valueOf(speObj.toString());
                    double dps = (SPEED_STANDARD_ONE_SECOND / speed) * damage;
                    dps = Util.roundDouble(dps);
                    this.attributes.put("DPS", dps);
                }
            }
        } else {
            // don't care!
        }
    }

    public int getSlot() {
        return slot;
    }
    public void setSlot(int slot) {
        this.slot = slot;
    }
    public int getRunanimation() {
        return runanimation;
    }
    public void setRunanimation(int runanimation) {
        this.runanimation = runanimation;
    }
    public String getSprite() {
        return sprite;
    }
    public void setSprite(String sprite) {
        this.sprite = sprite;
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        hashCode += getName().hashCode() * 3;
        hashCode += getIid() * 7;
        return hashCode;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Item)) {
            return false;
        } else {
            Item yours = (Item)obj;
            if (getName().equals(yours.getName()) && getIid() == yours.getIid()) {
                return true;
            }
        }
        return false;
    }
    @Override
    public int compareTo(Item o) {
        if (o == null) {
            return 1;
        }
        if (o.getType() != this.getType()) {
            return ((Integer)this.getType()).compareTo(o.getType());
        }
        return ((Integer)getIid()).compareTo(o.getIid());
        
    }
    public int getSort_order() {
        return sort_order;
    }
    public void setSort_order(int sort_order) {
        this.sort_order = sort_order;
    }
    
    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(getIid()).append(" : ").append(getName()).append(" ").append(getAttributes());
        return buff.toString();
    }
}
