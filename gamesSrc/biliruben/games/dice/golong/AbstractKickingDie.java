package biliruben.games.dice.golong;

public abstract class AbstractKickingDie extends Dice {

    static final int GOAL_POST_INDEX = 0;
    static final int BLOCK_INDEX = 1; 
    
    AbstractKickingDie(String goalPostFace, String blockedFace) {
        super(new String[] {goalPostFace, blockedFace, "10", "20", "30", "45", "60", "55", "35", "40", "25", "50"});
    }
    
    public int getDistance() {
        int faceIndex = getFaceInt();
        if (faceIndex > 1) {
            return Integer.valueOf(getFace());
        } else {
            // It was either a block, touchdown, or touchback. This is zero
            return 0;
        }
    }
    
}
