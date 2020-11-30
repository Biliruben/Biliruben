package biliruben.games.ninjawarz.object;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Attributes extends JSONObject {
    
    private int min_level;
    private double speed;
    private int clan_speed_pct;
    private double damage;
    private int clan_health;
    private int clan_health_pct;
    private int clan_agility;
    private int clan_armor;
    private int clan_power;
    private int clan_power_pct;
    private int health;
    private int health_pct;
    private int agility;
    private int armor;
    private int power;
    private int power_pct;
    private int unsellable;
    private double base_speed;
    private double base_damage;
    private double clan_crit;
    private double gold_mod;
    private double gold_bonus;
    private double exp_mod;
    private double crit;
    private String sur_title_f;
    private String sur_title_m;
    private String title_f;
    private String title_m;
    
    public int getMin_level() {
        return min_level;
    }
    public void setMin_level(int min_level) {
        this.min_level = min_level;
    }
    public double getSpeed() {
        return speed;
    }
    public void setSpeed(double speed) {
        this.speed = speed;
    }
    public double getDamage() {
        return damage;
    }
    public void setDamage(double damage) {
        this.damage = damage;
    }
    public int getClan_speed_pct() {
        return clan_speed_pct;
    }
    public void setClan_speed_pct(int clan_speed_pct) {
        this.clan_speed_pct = clan_speed_pct;
    }
    public int getClan_health() {
        return clan_health;
    }
    public void setClan_health(int clan_health) {
        this.clan_health = clan_health;
    }
    public int getClan_health_pct() {
        return clan_health_pct;
    }
    public void setClan_health_pct(int clan_health_pct) {
        this.clan_health_pct = clan_health_pct;
    }
    public int getClan_agility() {
        return clan_agility;
    }
    public void setClan_agility(int clan_agility) {
        this.clan_agility = clan_agility;
    }
    public int getClan_armor() {
        return clan_armor;
    }
    public void setClan_armor(int clan_armor) {
        this.clan_armor = clan_armor;
    }
    public int getClan_power() {
        return clan_power;
    }
    public void setClan_power(int clan_power) {
        this.clan_power = clan_power;
    }
    public int getClan_power_pct() {
        return clan_power_pct;
    }
    public void setClan_power_pct(int clan_power_pct) {
        this.clan_power_pct = clan_power_pct;
    }
    public int getUnsellable() {
        return unsellable;
    }
    public void setUnsellable(int unsellable) {
        this.unsellable = unsellable;
    }
    public double getBase_speed() {
        return base_speed;
    }
    public void setBase_speed(double base_speed) {
        this.base_speed = base_speed;
    }
    public double getBase_damage() {
        return base_damage;
    }
    public void setBase_damage(double base_damage) {
        this.base_damage = base_damage;
    }
    public double getClan_crit() {
        return clan_crit;
    }
    public void setClan_crit(double clan_crit) {
        this.clan_crit = clan_crit;
    }
    public double getGold_mod() {
        return gold_mod;
    }
    public void setGold_mod(double gold_mod) {
        this.gold_mod = gold_mod;
    }
    public double getGold_bonus() {
        return gold_bonus;
    }
    public void setGold_bonus(double gold_bonus) {
        this.gold_bonus = gold_bonus;
    }
    public double getExp_mod() {
        return exp_mod;
    }
    public void setExp_mod(double exp_mod) {
        this.exp_mod = exp_mod;
    }
    public double getCrit() {
        return crit;
    }
    public void setCrit(double crit) {
        this.crit = crit;
    }
    public String getSur_title_f() {
        return sur_title_f;
    }
    public void setSur_title_f(String sur_title_f) {
        this.sur_title_f = sur_title_f;
    }
    public String getSur_title_m() {
        return sur_title_m;
    }
    public void setSur_title_m(String sur_title_m) {
        this.sur_title_m = sur_title_m;
    }
    public int getHealth() {
        return health;
    }
    public void setHealth(int health) {
        this.health = health;
    }
    public int getHealth_pct() {
        return health_pct;
    }
    public void setHealth_pct(int health_pct) {
        this.health_pct = health_pct;
    }
    public int getAgility() {
        return agility;
    }
    public void setAgility(int agility) {
        this.agility = agility;
    }
    public int getArmor() {
        return armor;
    }
    public void setArmor(int armor) {
        this.armor = armor;
    }
    public int getPower() {
        return power;
    }
    public void setPower(int power) {
        this.power = power;
    }
    public int getPower_pct() {
        return power_pct;
    }
    public void setPower_pct(int power_pct) {
        this.power_pct = power_pct;
    }
    public String getTitle_f() {
        return title_f;
    }
    public void setTitle_f(String title_f) {
        this.title_f = title_f;
    }
    public String getTitle_m() {
        return title_m;
    }
    public void setTitle_m(String title_m) {
        this.title_m = title_m;
    }

}
