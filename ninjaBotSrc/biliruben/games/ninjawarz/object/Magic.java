package biliruben.games.ninjawarz.object;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/*
 * 
{"name":"Gust of Wind",
"description":"A damaging whirlwind of debris.",
"min_level":6,
"damage":32,
"duration":28800,
"gift_currency_value":10,
"karma_value":5,
"new_item":0,
"sprite":11,
"sid":1,
"effect":"windgust"}
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Magic extends JSONObject {

    private String name;
    private String description;
    private int min_level;
    private int damage;
    private int duration;
    private int gift_currency_value;
    private int karma_value;
    private int new_item;
    private int sprite;
    private int sid;
    private String effect;
    
    @Override
    public String getId() {
        return String.valueOf(sid);
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public int getMin_level() {
        return min_level;
    }
    public void setMin_level(int min_level) {
        this.min_level = min_level;
    }
    public int getDamage() {
        return damage;
    }
    public void setDamage(int damage) {
        this.damage = damage;
    }
    public int getDuration() {
        return duration;
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }
    public int getGift_currency_value() {
        return gift_currency_value;
    }
    public void setGift_currency_value(int gift_currency_value) {
        this.gift_currency_value = gift_currency_value;
    }
    public int getKarma_value() {
        return karma_value;
    }
    public void setKarma_value(int karma_value) {
        this.karma_value = karma_value;
    }
    public int getNew_item() {
        return new_item;
    }
    public void setNew_item(int new_item) {
        this.new_item = new_item;
    }
    public int getSprite() {
        return sprite;
    }
    public void setSprite(int sprite) {
        this.sprite = sprite;
    }
    public int getSid() {
        return sid;
    }
    public void setSid(int sid) {
        this.sid = sid;
    }
    public String getEffect() {
        return effect;
    }
    public void setEffect(String effect) {
        this.effect = effect;
    }
    
    
}
