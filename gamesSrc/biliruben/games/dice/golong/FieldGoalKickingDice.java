package biliruben.games.dice.golong;

public class FieldGoalKickingDice extends AbstractKickingDie {
    
    public FieldGoalKickingDice() {
        super(GoLong.FIELD_GOAL, GoLong.BLOCKED);
    }
    
    public boolean isFieldGoal() {
        return GOAL_POST_INDEX == getFaceInt();
    }
    
    public boolean isBlocked() {
        return BLOCK_INDEX == getFaceInt();
    }


}
