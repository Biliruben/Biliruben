package biliruben.games.dice.golong;

public class KickoffDice extends AbstractKickingDie {
    

    public KickoffDice() {
        super(GoLong.TOUCHBACK, GoLong.TOUCHDOWN);
    }
    
    public boolean isTouchdown() {
        return GOAL_POST_INDEX == getFaceInt();
    }
    
    public boolean isTouchback() {
        return BLOCK_INDEX == getFaceInt();
    }
}
