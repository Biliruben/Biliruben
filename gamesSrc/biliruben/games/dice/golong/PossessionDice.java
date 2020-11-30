package biliruben.games.dice.golong;

public class PossessionDice extends Dice {
    
    static final int OFFENSE_INDEX = 0;
    static final int DEFENSE_INDEX = 1;
    
    public PossessionDice() {
        super(new String[]{GoLong.OFFENSE, GoLong.DEFENSE});
    }
    
    public boolean isOffense() {
        return getFaceInt() == OFFENSE_INDEX;
    }
    
    public boolean isDefense() {
        return getFaceInt() == DEFENSE_INDEX;
    }
}
