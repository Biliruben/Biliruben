package biliruben.games.ninjawarz.object;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Boss extends JSONObject {
    
    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class Entity extends JSONObject {
        
        @JsonIgnoreProperties(ignoreUnknown=true)
        public static class Ability extends JSONObject {
            private String type;
            private String name;
            private long min_damage;
            private long max_damage;
            private boolean magical;
            private boolean active;
            private Modifiers modifiers;
            private boolean stack;

            public String getType() {
                return type;
            }
            public void setType(String type) {
                this.type = type;
            }
            public String getName() {
                return name;
            }
            public void setName(String name) {
                this.name = name;
            }
            public long getMin_damage() {
                return min_damage;
            }
            public void setMin_damage(long min_damage) {
                this.min_damage = min_damage;
            }
            public long getMax_damage() {
                return max_damage;
            }
            public void setMax_damage(long max_damage) {
                this.max_damage = max_damage;
            }
            public boolean isMagical() {
                return magical;
            }
            public void setMagical(boolean magical) {
                this.magical = magical;
            }
            public boolean isActive() {
                return active;
            }
            public void setActive(boolean active) {
                this.active = active;
            }
            public Modifiers getModifiers() {
                return modifiers;
            }
            public void setModifiers(Modifiers modifiers) {
                this.modifiers = modifiers;
            }
            public boolean isStack() {
                return stack;
            }
            public void setStack(boolean stack) {
                this.stack = stack;
            }
            
            public String getTypeDisplay() {
                if ("st".equals(type)) {
                    return "Standard";
                }
                if ("mt".equals(type)) {
                    return "MT";
                }
                if ("aoe".equals(type)) {
                    return "Area of Effect";
                }
                if ("heal".equals(type)) {
                    return "Heal";
                }
                if ("self_buff".equals(type)) {
                    return "Self Buff";
                }
                return type;
            }
            @Override
            public String toString() {
                StringBuffer buff = new StringBuffer();
                buff.append(name + ": max - " + max_damage + ", min - " + min_damage + ", type - "+ getTypeDisplay());
                return buff.toString();
            }
            
        }
        
        @JsonIgnoreProperties(ignoreUnknown=true)
        public static class HealthTrigger extends JSONObject {
            private long health;
            private long ability;
            private boolean cont;
            
            public long getHealth() {
                return health;
            }
            public void setHealth(long health) {
                this.health = health;
            }
            public long getAbility() {
                return ability;
            }
            public void setAbility(long ability) {
                this.ability = ability;
            }
            public boolean isContinue() {
                return cont;
            }
            public void setContinue(boolean cont) {
                this.cont = cont;
            }
            
            @Override
            public String toString() {
                StringBuffer buff = new StringBuffer();
                buff.append(health + " --> " + ability);
                return buff.toString();
            }
            
        }
        
        private String name;
        private double mitigation;
        private double armor;
        private double avoidance;
        private double agility;
        private long max_health;
        private long modified_max_health;
        private long health;
        private long power;
        private long modified_power;
        private long magic;
        private long modified_magic;
        private String attack_mode;
        private int[] attack_order;
        private Ability[] abilities;
        private Object[] buffs;
        private HealthTrigger[] health_triggers;
        
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public double getMitigation() {
            return mitigation;
        }
        
        public void setMitigation(double mitigation) {
            this.mitigation = mitigation;
        }
        public double getArmor() {
            return armor;
        }
        public void setArmor(double armor) {
            this.armor = armor;
        }
        public double getAvoidance() {
            return avoidance;
        }
        public void setAvoidance(double avoidance) {
            this.avoidance = avoidance;
        }
        public long getMax_health() {
            return max_health;
        }
        public void setMax_health(long max_health) {
            this.max_health = max_health;
        }
        public long getModified_max_health() {
            return modified_max_health;
        }
        public void setModified_max_health(long modified_max_health) {
            this.modified_max_health = modified_max_health;
        }
        public long getHealth() {
            return health;
        }
        public void setHealth(long health) {
            this.health = health;
        }
        public long getPower() {
            return power;
        }
        public void setPower(long power) {
            this.power = power;
        }
        public long getModified_power() {
            return modified_power;
        }
        public void setModified_power(long modified_power) {
            this.modified_power = modified_power;
        }
        public long getMagic() {
            return magic;
        }
        public void setMagic(long magic) {
            this.magic = magic;
        }
        public long getModified_magic() {
            return modified_magic;
        }
        public void setModified_magic(long modified_magic) {
            this.modified_magic = modified_magic;
        }
        public String getAttack_mode() {
            return attack_mode;
        }
        public void setAttack_mode(String attack_mode) {
            this.attack_mode = attack_mode;
        }
        public int[] getAttack_order() {
            return attack_order;
        }
        public void setAttack_order(int[] attack_order) {
            this.attack_order = attack_order;
        }
        public Ability[] getAbilities() {
            return abilities;
        }
        public void setAbilities(Ability[] abilities) {
            this.abilities = abilities;
        }
        public double getAgility() {
            return agility;
        }
        public void setAgility(double agility) {
            this.agility = agility;
        }
        public Object[] getBuffs() {
            return buffs;
        }
        public void setBuffs(Object[] buffs) {
            this.buffs = buffs;
        }
        public HealthTrigger[] getHealth_triggers() {
            return health_triggers;
        }
        public void setHealth_triggers(HealthTrigger[] health_triggers) {
            this.health_triggers = health_triggers;
        }
    }
    
    private String name;
    private String internal_name;
    private String avatar;
    private int level;
    private int[] achievements;
    private int[] item_drops;
    private Entity[] entities;
    private String fancy_name;
    private long gold;
    private long exp;
    private String background;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getInternal_name() {
        return internal_name;
    }
    public void setInternal_name(String internal_name) {
        this.internal_name = internal_name;
    }
    public String getAvatar() {
        return avatar;
    }
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }
    public int[] getAchievements() {
        return achievements;
    }
    public void setAchievements(int[] achievements) {
        this.achievements = achievements;
    }
    public int[] getItem_drops() {
        return item_drops;
    }
    public void setItem_drops(int[] item_drops) {
        this.item_drops = item_drops;
    }
    public Entity[] getEntities() {
        return entities;
    }
    public void setEntities(Entity[] entities) {
        this.entities = entities;
    }
    public String getFancy_name() {
        return fancy_name;
    }
    public void setFancy_name(String fancy_name) {
        this.fancy_name = fancy_name;
    }
    public long getGold() {
        return gold;
    }
    public void setGold(long gold) {
        this.gold = gold;
    }
    public long getExp() {
        return exp;
    }
    public void setExp(long exp) {
        this.exp = exp;
    }
    public String getBackground() {
        return background;
    }
    public void setBackground(String background) {
        this.background = background;
    }

}
