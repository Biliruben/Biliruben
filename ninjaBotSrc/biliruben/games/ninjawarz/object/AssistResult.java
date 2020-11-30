package biliruben.games.ninjawarz.object;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class AssistResult extends Result {

    private Spoils spoils;
    private boolean leveled;
    private boolean next_level;
    public Spoils getSpoils() {
        return spoils;
    }
    public void setSpoils(Spoils spoils) {
        this.spoils = spoils;
    }
    public boolean isLeveled() {
        return leveled;
    }
    public void setLeveled(boolean leveled) {
        this.leveled = leveled;
    }
    public boolean isNext_level() {
        return next_level;
    }
    public void setNext_level(boolean next_level) {
        this.next_level = next_level;
    }
    
}
