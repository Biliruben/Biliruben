package biliruben.games.dice.golong;

public class PuntingDice extends AbstractKickingDie {
    
    public PuntingDice() {
        super(GoLong.TOUCHBACK, GoLong.BLOCKED);
    }
    
    public boolean isTouchback() {
        return GOAL_POST_INDEX == getFaceInt();
    }
    
    public boolean isBlocked() {
        return BLOCK_INDEX == getFaceInt();
    }
}
