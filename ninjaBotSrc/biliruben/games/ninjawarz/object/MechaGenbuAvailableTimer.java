package biliruben.games.ninjawarz.object;

public class MechaGenbuAvailableTimer extends BossTrigger {

    public MechaGenbuAvailableTimer() {
        super(BossName.MechaGenbu);
    }

    @Override
    public String getRecommendedFrequency() {
        return "24:00:00";
    }
}
