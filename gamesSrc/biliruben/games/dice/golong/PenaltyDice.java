package biliruben.games.dice.golong;

public class PenaltyDice extends Dice {

    
    static final int OFFSETTING_INDEX = 0;
    static final int PI_INDEX = 1;
    
    public PenaltyDice() {
        super(new String[] {GoLong.OFFSETTING, GoLong.PASS_INTERFERANCE, "5", "5", "5", "5", "5", "10", "10", "10", "15", "15"});
    }
    
    public boolean isOffsetting() {
        return OFFSETTING_INDEX  == getFaceInt();
    }
    
    public boolean isPassInterferance() {
        return PI_INDEX == getFaceInt();
    }
    
    public int getDistance() {
        if (getFaceInt() > PI_INDEX) {
            return Integer.valueOf(getFace());
        } else {
            return 0;
        }
    }
}
