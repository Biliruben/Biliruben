package biliruben.games.ninjawarz.object;

import java.util.Comparator;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Ninja extends JSONObject {
    
    private static final int NINJA_LEVEL_5_KARMA = 1;
    private static final int NINJA_LEVEL_10_KARMA = 1;
    private static final int NINJA_LEVEL_15_KARMA = 1;
    private static final int NINJA_LEVEL_20_KARMA = 1;
    private static final int NINJA_LEVEL_25_KARMA = 1;
    private static final int NINJA_LEVEL_30_KARMA = 1;
    private static final int NINJA_LEVEL_35_KARMA = 1;
    private static final int NINJA_LEVEL_40_KARMA = 1;
    private static final int NINJA_LEVEL_45_KARMA = 1;
    private static final int NINJA_LEVEL_50_KARMA = 1;
    private static final int NINJA_LEVEL_55_KARMA = 1;
    private static final int NINJA_LEVEL_60_KARMA = 1;
    
    public static class NinjaComparator implements Comparator<Ninja> {

        @Override
        public int compare(Ninja mine, Ninja yours) {
            // tired of null checks, assum their non-null
            
            return ((Long)mine.getNid()).compareTo(yours.getNid());
        }
        
    }
    
    public static class NinjaBirthdate {
        private String pretty;
        private String for_mysql;
        public String getPretty() {
            return pretty;
        }
        public void setPretty(String pretty) {
            this.pretty = pretty;
        }
        public String getFor_mysql() {
            return for_mysql;
        }
        public void setFor_mysql(String for_mysql) {
            this.for_mysql = for_mysql;
        }
        
        @Override
        public String toString() {
            return getPretty();
        }
    }
    
    private long nid;
    private String name;
    private int level;
    private Item weapon;
    private int max_health;
    private int power;
    private int initial_health;
    private int initial_power;
    private int gender;
    private String blood_type;
    private NinjaBirthdate birthdate;
    private int value;
    private boolean elite;
    private int modified_max_health;
    private int modified_power;
    private int crit_rate;
    private String initial_name;
    private int armor;
    private int penetration;
    private int agility;
    private int health;
    private int superior;
    private String rnid;
    private String price;
    
    @Override
    public String getId() {
        return String.valueOf(nid);
    }
    
    public long getNid() {
        return nid;
    }
    public void setNid(long nid) {
        this.nid = nid;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }
    public Item getWeapon() {
        return weapon;
    }
    public void setWeapon(Item weapon) {
        this.weapon = weapon;
    }
    public int getMax_health() {
        return max_health;
    }
    public void setMax_health(int max_health) {
        this.max_health = max_health;
    }
    public int getPower() {
        return power;
    }
    public void setPower(int power) {
        this.power = power;
    }
    public int getInitial_health() {
        return initial_health;
    }
    public void setInitial_health(int initial_health) {
        this.initial_health = initial_health;
    }
    public int getInitial_power() {
        return initial_power;
    }
    public void setInitial_power(int initial_power) {
        this.initial_power = initial_power;
    }
    public int getGender() {
        return gender;
    }
    public void setGender(int gender) {
        this.gender = gender;
    }
    public String getBlood_type() {
        return blood_type;
    }
    public void setBlood_type(String blood_type) {
        this.blood_type = blood_type;
    }
    public NinjaBirthdate getBirthdate() {
        return birthdate;
    }
    public void setBirthdate(NinjaBirthdate birthdate) {
        this.birthdate = birthdate;
    }
    public int getValue() {
        return value;
    }
    public void setValue(int value) {
        this.value = value;
    }
    public boolean getElite() {
        return elite;
    }
    public void setElite(Object elite) {
        if (elite instanceof Boolean) {
            this.elite = (Boolean)elite;
        } else if (elite instanceof Integer) {
            int numElite = (Integer)elite;
            this.elite = numElite == 0 ? false : true;
        }
        
    }
    
    
    
    public int getModified_max_health() {
        return modified_max_health;
    }
    public void setModified_max_health(int modified_max_health) {
        this.modified_max_health = modified_max_health;
    }
    public int getModified_power() {
        return modified_power;
    }
    public void setModified_power(int modified_power) {
        this.modified_power = modified_power;
    }
    public int getCrit_rate() {
        return crit_rate;
    }
    public void setCrit_rate(int crit_rate) {
        this.crit_rate = crit_rate;
    }
    public String getInitial_name() {
        return initial_name;
    }
    public void setInitial_name(String initial_name) {
        this.initial_name = initial_name;
    }
    public int getArmor() {
        return armor;
    }
    public void setArmor(int armor) {
        this.armor = armor;
    }
    public int getPenetration() {
        return penetration;
    }
    public void setPenetration(int penetration) {
        this.penetration = penetration;
    }
    public int getAgility() {
        return agility;
    }
    public void setAgility(int agility) {
        this.agility = agility;
    }
    public int getHealth() {
        return health;
    }
    public void setHealth(int health) {
        this.health = health;
    }
    public long getKarmaValue() {
        long runningKarmaValue = 0;
        for (int i = 1; i < 13 && level >= 5 * (i - 1); i++) {
            int min = 5 * (i - 1) + 1;
            int max = 5 * i + 1;
            // min 21, lvl 24, max 25
            // 3 lvls worth
            int test = level > max ? max : level; // loop condition handles the min boundry
            runningKarmaValue += (test - min) * i;
        }
        
        // and check the weapon
        if (weapon != null) {
            runningKarmaValue += weapon.getKarma_value();
        }
        return runningKarmaValue;
    }
    public String getGenderStr() {
        if (gender != 0) {
            return "Male";
        } else {
            return "Female";
        }
    }
    public int getSuperior() {
        if (superior == 0) {
            int tot = initial_health + initial_power;
            if (tot > 90) {
                superior = 1;
            }
        }
        return superior;
    }
    public void setSuperior(int superior) {
        this.superior = superior;
    }
    public String getRnid() {
        return rnid;
    }
    public void setRnid(String rnid) {
        this.rnid = rnid;
    }
    public String getPrice() {
        return price;
    }
    public void setPrice(String price) {
        this.price = price;
    }
}
