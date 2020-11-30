package biliruben.games.dice.golong;

public class DownDice extends Dice {

    public DownDice() {
        super (new String[]{"-2", "-1", "0", "0", "0", "1", "1", "1", "2", "2", "3"});
    }
    
    public int getDistance() {
        return Integer.valueOf(getFace());
    }
}
