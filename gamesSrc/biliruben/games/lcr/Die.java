package biliruben.games.lcr;

import java.util.Random;

public class Die {
    
    public enum Side {
        L,
        R,
        C,
        D;
    }
    
    private static final Side[] SIDES = new Side[]{
        Side.L,
        Side.C,
        Side.R,
        Side.D,
        Side.D,
        Side.D
    };
    private Random _rand;
    
    public Die() {
        this._rand = new Random();
    }
    
    public Side roll() {
        int roll = _rand.nextInt(SIDES.length);
        return SIDES[roll];
    }

}
