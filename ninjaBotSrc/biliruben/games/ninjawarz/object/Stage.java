package biliruben.games.ninjawarz.object;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Stage extends JSONObject {

    private Clan[] playing_friends;
    private BossCollection bosses; 
    private Clan[] other;
    
    public Clan[] getPlaying_friends() {
        return playing_friends;
    }
    public void setPlaying_friends(Clan[] playing_friends) {
        this.playing_friends = playing_friends;
    }
    public BossCollection getBosses() {
        return bosses;
    }
    
    public void setBosses(BossCollection bosses) {
        this.bosses = (BossCollection) bosses;
    }

    public Clan[] getOther() {
        return other;
    }
    public void setOther(Clan[] other) {
        this.other = other;
    }

}
