package biliruben.games.ninjawarz.object;

public class SmallGirlAvailableTimer extends BossTrigger {

    public SmallGirlAvailableTimer() {
        super(BossName.SmallGirl);
    }

    @Override
    public String getRecommendedFrequency() {
        return "24:00:00";
    }
}
