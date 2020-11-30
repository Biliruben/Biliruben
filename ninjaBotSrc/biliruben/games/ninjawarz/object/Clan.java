package biliruben.games.ninjawarz.object;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.Util;

// TODO: Via boss fights, it's clear that the JSON reported for an opponent after a battle is Clan-like, but not exactly
// a Clan.  I might consider a sub-implementation for and Opponent type
@JsonIgnoreProperties(ignoreUnknown=true)
public class Clan extends JSONObject implements Comparable<Clan> {

    public static final String COL_CID = "cid";
    public static final String COL_NAME = "name";
    public static final String COL_LEVEL = "level";
    public static final String[] SORT_COLUMNS = {COL_CID, COL_LEVEL, COL_NAME};

    public static class CidComparator implements Comparator<Clan> {

        @Override
        public int compare(Clan mine, Clan yours) {
            if (mine != null && yours == null) {
                return 1;
            } else if (mine == null && yours != null) {
                return -1;
            } else if (mine == null) {
                return 0;
            }
            long myLevel = mine.getCid();
            long yourLevel = yours.getCid();

            return new Long(myLevel).compareTo(new Long(yourLevel));
        }

    }

    public static class NameComparator implements Comparator<Clan> {

        @Override
        public int compare(Clan mine, Clan yours) {
            if (mine != null && yours == null) {
                return 1;
            } else if (mine == null && yours != null) {
                return -1;
            } else if (mine == null) {
                return 0;
            }

            // null checks out of the way, about that name
            String myName = mine.getName();
            String yourName = yours.getName();
            if (myName == null && yourName != null) {
                return -1;
            } else if (myName != null && yourName == null) {
                return 1;
            } else if (myName == null) {
                return 0;
            }

            // so tired of null checks
            return myName.compareTo(yourName);
        }

    }
    private Object[] achievements;
    private Clan[] allies;
    private long ally_count;
    private Object[] ally_requests;
    private String avatar;
    private String background;
    private String bonus_gold_per_hour;
    private String bonus_gold_earned;
    private String bonus_karma_per_hour;
    private String bonus_karma_earned;
    private long cid;
    private long cloud;
    private long currency_provider;
    private long daimyo_gift;
    private boolean defeated_genbu;
    private boolean defeated_girl;
    private boolean defeated_mechagenbu;
    private boolean defeated_small_girl;
    private Object[] errors;
    private long exp;
    private long exp_modifier;
    private long exp_to_level;
    private long facebook_id;
    private String faction;
    private Object[] flags;
    private String formed;
    private long gained_exp;
    private long gained_gift_currency;
    private long gained_gold;
    private long gained_karma;
    private long gained_level;
    private long gift_currency;
    private long gold;
    private long gold_modifier;
    private long gold_modifiers;
    private long goodness;
    private long has_publish_actions_permission;
    private long horde_in;
    private Item[] inventory;
    private String internal_name;
    private int [] item_drops;
    private long karma;
    private String last_active;
    private String last_auto_heal;
    private String last_bonus;
    private long level;
    private String leveled_at;
    private long likes_app;
    private Magic magic;
    private String magic_equipped;
    private String magic_equipped_at;
    private String name;
    private long needs_assistance;
    private Ninja[] ninjas;
    private long ninjacount;
    private long num_ninjas;
    private long npc;
    private long purchased_gold;
    private long purchased_karma;
    private double random;
    private long relic_slots;
    private Item[] relics;

    private long seconds_of_bonus_exp;
    private long seconds_of_magic;
    private long source;
    private String third_party_id;
    private long total_exp_level;
    private long total_exp_to_level;
    private long total_max_health_before;
    private long total_health_before;
    private long tournament_in;
    private long tutorial;
    private Object[][] rounds;
    private boolean epic;
    private Object[] entities;

    @Override
    public String getId() {
        return String.valueOf(cid);
    }
    
    public Object[] getAchievements() {
        return achievements;
    }
    public Clan[] getAllies() {
        return allies;
    }
    public long getAlly_count() {
        return ally_count;
    }
    public Object[] getAlly_requests() {
        return ally_requests;
    }

    public int getAgility() {
        int agility = 0;
        if (ninjas != null && ninjas.length > 0) {
            agility = ninjas[0].getAgility();
        }
        return agility;
    }

    public int getCritRate() {
        int critRate = 0;
        if (ninjas != null && ninjas.length > 0) {
            critRate = ninjas[0].getCrit_rate();
        }
        return critRate;
    }

    public int getArmor() {
        int armor = 0;
        if (ninjas != null && ninjas.length > 0) {
            armor = ninjas[0].getArmor();
        }
        return armor;
    }

    public String getAvatar() {
        return avatar;
    }
    public String getBonus_gold_per_hour() {
        return bonus_gold_per_hour;
    }
    public String getBonus_karma_per_hour() {
        return bonus_karma_per_hour;
    }
    public long getCid() {
        return this.cid;
    }
    public long getCloud() {
        return cloud;
    }
    public long getCurrency_provider() {
        return currency_provider;
    }
    public long getDaimyo_gift() {
        return daimyo_gift;
    }

    public double getDamageRating() {
        /*
         * speed + weapon = DPS
power = DPS+
armor = opp.DPS-
agility = chance to miss
crit = chance to DamageX
health = how much absorption of opp dmg

Damage rating per ninja: (Power / 100) * weapon Dmg
- We can replace weapon dmg with DPS to incorporate speed
- crit rate modifies damage rating by 1+critRate * dmgRating

         */

        double powerRating = 0.0;

        if (ninjas != null) {
            for (Ninja ninja : ninjas) {
                Map weaponAttr = ninja.getWeapon().getAttributes();
                if (weaponAttr != null) {
                    Object dps = weaponAttr.get("DPS");
                    if (dps != null) {
                        double weaponDamage = (Double)dps;
                        double rating = weaponDamage * (ninja.getModified_power() / 100.0);
                        double critRate = 1.0 + (Double.valueOf(getCritRate()) / 100.0);
                        powerRating += (rating * critRate);
                    }
                }
            }
        }

        //powerRating = Double.valueOf(f.format(powerRating * 10));
        powerRating = Util.roundDouble(powerRating);
        return powerRating;
    }

    public Object[] getErrors() {
        return errors;
    }
    public long getExp() {
        return exp;
    }
    public long getExp_modifier() {
        return exp_modifier;
    }
    public long getExp_to_level() {
        return exp_to_level;
    }
    public long getFacebook_id() {
        return facebook_id;
    }
    public String getFaction() {
        return faction;
    }
    public Object[] getFlags() {
        return flags;
    }
    public String getFormed() {
        return formed;
    }
    public long getGained_exp() {
        return gained_exp;
    }
    public long getGained_gift_currency() {
        return gained_gift_currency;
    }
    public long getGained_gold() {
        return gained_gold;
    }
    public long getGained_karma() {
        return gained_karma;
    }
    public long getGained_level() {
        return gained_level;
    }
    public long getGift_currency() {
        return gift_currency;
    }
    public long getGold() {
        return gold;
    }
    public long getGold_modifier() {
        return gold_modifier;
    }
    public long getGold_modifiers() {
        return gold_modifiers;
    }

    public long getMaxHealth() {
        long maxHealth = 0;
        for (Ninja ninja : getNinjas()) {
            maxHealth += ninja.getModified_max_health();
        }
        return maxHealth;
    }

    public long getHealth() {
        long health = 0;
        for (Ninja ninja : getNinjas()) {
            health += ninja.getHealth();
        }
        return health;
    }

    public int getTotalPower() {
        int power = 0;
        for (Ninja ninja : getNinjas()) {
            power += ninja.getModified_power();
        }
        return power;
    }

    public long getHas_publish_actions_permission() {
        return has_publish_actions_permission;
    }
    public long getHorde_in() {
        return horde_in;
    }
    public Item[] getInventory() {
        if (inventory == null) {
            inventory = new Item[0]; // use this enough, i hate checking for null
        }
        return inventory;
    }
    
    public Item[] getEquippedItems() {
        List<Item> equipped = new ArrayList<Item>();
        if (relics != null) {
            for (Item relic : relics) {
                equipped.add(relic);
            }
        }
        if (ninjas != null) {
            for (Ninja ninja : ninjas) {
                if (ninja.getWeapon() != null && !Item.FISTS.equals(ninja.getWeapon().getName())) {
                    equipped.add(ninja.getWeapon());
                }
            }
        }
        return equipped.toArray(new Item[equipped.size()]);
    }
    
    public long getKarma() {
        return karma;
    }
    public String getLast_auto_heal() {
        return last_auto_heal;
    }
    public String getLast_bonus() {
        return last_bonus;
    }
    public long getLevel() {
        return level;
    }
    public String getLeveled_at() {
        return leveled_at;
    }
    public long getLikes_app() {
        return likes_app;
    }
    public Magic getMagic() {
        return magic;
    }
    public String getMagic_equipped() {
        return magic_equipped;
    }
    public String getMagic_equipped_at() {
        return magic_equipped_at;
    }
    public String getName() {
        return name;
    }
    public long getNeeds_assistance() {
        return needs_assistance;
    }
    public Ninja[] getNinjas() {
        if (ninjas == null) {
            ninjas = new Ninja[0];
        }
        return ninjas;
    }
    public long getNpc() {
        return npc;
    }
    public long getPurchased_gold() {
        return purchased_gold;
    }
    public long getPurchased_karma() {
        return purchased_karma;
    }
    public long getRelic_slots() {
        return relic_slots;
    }
    public Item[] getRelics() {
        return relics;
    }
    public long getSeconds_of_bonus_exp() {
        return seconds_of_bonus_exp;
    }
    public long getSeconds_of_magic() {
        return seconds_of_magic;
    }
    public long getSource() {
        return source;
    }
    public String getThird_party_id() {
        return third_party_id;
    }
    public long getTotal_exp_level() {
        return total_exp_level;
    }
    public long getTotal_exp_to_level() {
        return total_exp_to_level;
    }

    public double getToughnessRating() {
        /*
        speed + weapon = DPS
                power = DPS+
                armor = opp.DPS-
                agility = chance to miss
                crit = chance to DamageX
                health = how much absorption of opp dmg

                Damage rating per ninja: (Power / 100) * weapon Dmg
                - We can replace weapon dmg with DPS to incorporate speed
                - crit rate modifies damage rating by 1+critRate * dmgRating

                Health indicates toughness
                - armor effectively increases hit poins via (50% / armor%) * maxHP
                - agility increases hit points  by HPRating * 1+agility
         */
        double toughness = 0.0;

        if (ninjas != null) {
            double armor = (Double.valueOf(getArmor()) / 100.0);
            double actualArmor = armor > 50 ? 50 : armor;
            double armorRating = 1 + actualArmor / .50;
            double agility = 1 + Double.valueOf(getAgility()) / 100.0;
            for (Ninja ninja : ninjas) {
                double toughnessRating = ninja.getModified_max_health() * agility * armorRating;
                toughness += toughnessRating;
            }
        }
        toughness = Util.roundDouble(toughness / 10);
        return toughness;

    }

    public long getTournament_in() {
        return tournament_in;
    }
    public long getTutorial() {
        return tutorial;
    }
    public boolean isDefeated_genbu() {
        return defeated_genbu;
    }
    public boolean isDefeated_girl() {
        return defeated_girl;
    }
    public boolean isDefeated_mechagenbu() {
        return defeated_mechagenbu;
    }
    public boolean isDefeated_small_girl() {
        return defeated_small_girl;
    }
    public void setAchievements(Object[] achievements) {
        this.achievements = achievements;
    }
    public void setAllies(Clan[] allies) {
        this.allies = allies;
    }
    public void setAlly_count(long ally_count) {
        this.ally_count = ally_count;
    }
    public void setAlly_requests(Object[] ally_requests) {
        this.ally_requests = ally_requests;
    }
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    public void setBonus_gold_per_hour(String bonus_gold_per_hour) {
        this.bonus_gold_per_hour = bonus_gold_per_hour;
    }
    public void setBonus_karma_per_hour(String bonus_karma_per_hour) {
        this.bonus_karma_per_hour = bonus_karma_per_hour;
    }
    public void setCid(long cid) {
        this.cid = cid;
    }
    public void setCloud(long cloud) {
        this.cloud = cloud;
    }
    public void setCurrency_provider(long currency_provider) {
        this.currency_provider = currency_provider;
    }
    public void setDaimyo_gift(long daimyo_gift) {
        this.daimyo_gift = daimyo_gift;
    }
    public void setDefeated_genbu(boolean defeated_genbu) {
        this.defeated_genbu = defeated_genbu;
    }
    public void setDefeated_girl(boolean defeated_girl) {
        this.defeated_girl = defeated_girl;
    }
    public void setDefeated_mechagenbu(boolean defeated_mechagenbu) {
        this.defeated_mechagenbu = defeated_mechagenbu;
    }
    public void setDefeated_small_girl(boolean defeated_small_girl) {
        this.defeated_small_girl = defeated_small_girl;
    }
    public void setErrors(Object[] errors) {
        this.errors = errors;
    }
    public void setExp(long exp) {
        this.exp = exp;
    }
    public void setExp_modifier(long exp_modifier) {
        this.exp_modifier = exp_modifier;
    }
    public void setExp_to_level(long exp_to_level) {
        this.exp_to_level = exp_to_level;
    }
    public void setFacebook_id(long facebook_id) {
        this.facebook_id = facebook_id;
    }
    public void setFaction(String faction) {
        this.faction = faction;
    }
    public void setFlags(Object[] flags) {
        this.flags = flags;
    }
    public void setFormed(String formed) {
        this.formed = formed;
    }
    public void setGained_exp(long gained_exp) {
        this.gained_exp = gained_exp;
    }
    public void setGained_gift_currency(long gained_gift_currency) {
        this.gained_gift_currency = gained_gift_currency;
    }
    public void setGained_gold(long gained_gold) {
        this.gained_gold = gained_gold;
    }
    public void setGained_karma(long gained_karma) {
        this.gained_karma = gained_karma;
    }
    public void setGained_level(long gained_level) {
        this.gained_level = gained_level;
    }

    public void setGift_currency(long giftCurrency) {
        this.gift_currency = giftCurrency;
    }

    public void setGold(long gold) {
        this.gold = gold;
    }
    public void setGold_modifier(long gold_modifier) {
        this.gold_modifier = gold_modifier;
    }
    public void setGold_modifiers(long gold_modifiers) {
        this.gold_modifiers = gold_modifiers;
    }
    public void setHas_publish_actions_permission(long has_publish_actions_permission) {
        this.has_publish_actions_permission = has_publish_actions_permission;
    }
    public void setHorde_in(long horde_in) {
        this.horde_in = horde_in;
    }
    public void setInventory(Item[] inventory) {
        this.inventory = inventory;
    }
    public void setKarma(long karma) {
        this.karma = karma;
    }
    public void setLast_auto_heal(String last_auto_heal) {
        this.last_auto_heal = last_auto_heal;
    }
    public void setLast_bonus(String last_bonus) {
        this.last_bonus = last_bonus;
    }
    public void setLevel(long level) {
        this.level = level;
    }
    public void setLeveled_at(String leveled_at) {
        this.leveled_at = leveled_at;
    }
    public void setLikes_app(long likes_app) {
        this.likes_app = likes_app;
    }
    public void setMagic(Magic magic) {
        this.magic = magic;
    }
    public void setMagic_equipped(String magic_equipped) {
        this.magic_equipped = magic_equipped;
    }
    public void setMagic_equipped_at(String magic_equipped_at) {
        this.magic_equipped_at = magic_equipped_at;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setNeeds_assistance(long needs_assistance) {
        this.needs_assistance = needs_assistance;
    }
    public void setNinjas(Ninja[] ninjas) {
        this.ninjas = ninjas;
    }
    public void setNpc(long npc) {
        this.npc = npc;
    }
    public void setPurchased_gold(long purchased_gold) {
        this.purchased_gold = purchased_gold;
    }
    public void setPurchased_karma(long purchased_karma) {
        this.purchased_karma = purchased_karma;
    }
    public void setRelic_slots(long relic_slots) {
        this.relic_slots = relic_slots;
    }
    public void setRelics(Item[] relics) {
        this.relics = relics;
    }
    public void setSeconds_of_bonus_exp(long seconds_of_bonus_exp) {
        this.seconds_of_bonus_exp = seconds_of_bonus_exp;
    }
    public void setSeconds_of_magic(long seconds_of_magic) {
        this.seconds_of_magic = seconds_of_magic;
    }
    public void setSource(long source) {
        this.source = source;
    }
    public void setThird_party_id(String third_party_id) {
        this.third_party_id = third_party_id;
    }
    public void setTotal_exp_level(long total_exp_level) {
        this.total_exp_level = total_exp_level;
    }
    public void setTotal_exp_to_level(long total_exp_to_level) {
        this.total_exp_to_level = total_exp_to_level;
    }
    public void setTournament_in(long tournament_in) {
        this.tournament_in = tournament_in;
    }
    public void setTutorial(long tutorial) {
        this.tutorial = tutorial;
    }
    public String getBonus_karma_earned() {
        return bonus_karma_earned;
    }
    public void setBonus_karma_earned(String bonus_karma_earned) {
        this.bonus_karma_earned = bonus_karma_earned;
    }
    public String getBonus_gold_earned() {
        return bonus_gold_earned;
    }
    public void setBonus_gold_earned(String bonus_gold_earned) {
        this.bonus_gold_earned = bonus_gold_earned;
    }
    public long getNinjacount() {
        return ninjacount;
    }
    public void setNinjacount(long ninjacount) {
        this.ninjacount = ninjacount;
    }
    public long getTotal_max_health_before() {
        return total_max_health_before;
    }
    public void setTotal_max_health_before(long total_max_health_before) {
        this.total_max_health_before = total_max_health_before;
    }
    public long getTotal_health_before() {
        return total_health_before;
    }
    public void setTotal_health_before(long total_health_before) {
        this.total_health_before = total_health_before;
    }
    @Override
    public int compareTo(Clan o) {
        if (o == null) {
            return 1;
        }
        int ret = ((Long)getLevel()).compareTo(o.getLevel());
        if (ret == 0) {
            ret = ((Long)getCid()).compareTo(o.getCid());
        }
        return ret;
    }
    public long getKarmaValue() {
        long karmaValue = karma;
        if (relics != null) {
            for (Item relic : relics) {
                karmaValue += relic.getKarma_value();
            }
        }

        if (ninjas != null) {
            for (Ninja ninja : ninjas) {
                karmaValue += ninja.getKarmaValue();
            }
        }

        return karmaValue;
    }
    public long getNum_ninjas() {
        return num_ninjas;
    }
    public void setNum_ninjas(long num_ninjas) {
        this.num_ninjas = num_ninjas;
    }
    public String getLast_active() {
        return last_active;
    }
    public void setLast_active(String last_active) {
        this.last_active = last_active;
    }
    public long getGoodness() {
        return goodness;
    }
    public void setGoodness(long goodness) {
        this.goodness = goodness;
    }
    public double getRandom() {
        return random;
    }
    public void setRandom(double random) {
        this.random = random;
    }

    public String getBackground() {
        return background;
    }
    public void setBackground(String background) {
        this.background = background;
    }
    public Object[] getEntities() {
        return entities;
    }
    public void setEntities(Object[] entities) {
        this.entities = entities;
    }
    public boolean isEpic() {
        return epic;
    }
    public void setEpic(boolean epic) {
        this.epic = epic;
    }
    public String getInternal_name() {
        return internal_name;
    }
    public void setInternal_name(String internal_name) {
        this.internal_name = internal_name;
    }
    public int[] getItem_drops() {
        return item_drops;
    }
    public void setItem_drops(int[] item_drops) {
        this.item_drops = item_drops;
    }
    public Object[][] getRounds() {
        return rounds;
    }
    public void setRounds(Object[][] rounds) {
        this.rounds = rounds;
    }


}
