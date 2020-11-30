package biliruben.games.dice.golong;

public class BigPlayDice extends Dice {
    
    static final int TOUCHDOWN_FACE_INDEX = 0;
    
    public BigPlayDice() {
        super (new String[] {GoLong.TOUCHDOWN, "-10", "0", "0", "0", "5", "6", "6", "7", "10", "20", "30"});
    }
    
    public boolean isTouchdown() {
        return TOUCHDOWN_FACE_INDEX == getFaceInt();
    }
    
    public int getDistance() {
        if (isTouchdown()) {
            return 0;
        } else {
            return Integer.valueOf(getFace());
        }
    }

}
