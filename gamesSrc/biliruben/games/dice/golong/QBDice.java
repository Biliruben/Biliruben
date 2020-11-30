package biliruben.games.dice.golong;

public class QBDice extends Dice {

    private static final String[] FACES = {GoLong.PENALTY, GoLong.DOWN, GoLong.DOWN, GoLong.DOWN, GoLong.DOWN, GoLong.DOWN, GoLong.DOWN, GoLong.DOWN, GoLong.DOWN, GoLong.DOWN, GoLong.SACKED, GoLong.LOSS_OF_POSSESSION};
    // "cheater" constants! If you change the faces order, change these also!!
    private static int PENALTY_FACE = 0;
    private static int SACKED_FACE = 10;
    private static int LOSS_FACE = 11;

    public QBDice() {
        super (FACES);
    }
    
    public boolean isPenalty() {
        return PENALTY_FACE == getFaceInt();
    }
    
    public boolean isSacked() {
        return SACKED_FACE == getFaceInt();
    }
    
    public boolean isLossOfPossesion() {
        return LOSS_FACE == getFaceInt();
    }
    
    public boolean isDown() {
        int faceInt = getFaceInt();
        return faceInt != PENALTY_FACE && faceInt != SACKED_FACE && faceInt != LOSS_FACE;
    }
}
