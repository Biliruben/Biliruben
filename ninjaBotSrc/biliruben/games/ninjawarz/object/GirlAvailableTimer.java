package biliruben.games.ninjawarz.object;

public class GirlAvailableTimer extends BossTrigger {

    public GirlAvailableTimer() {
        super(BossName.Girl);
    }
    
    @Override
    public String getRecommendedFrequency() {
        return "24:00:00";
    }
}
