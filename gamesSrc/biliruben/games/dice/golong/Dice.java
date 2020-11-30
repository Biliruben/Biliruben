package biliruben.games.dice.golong;

import java.util.Arrays;
import java.util.Random;

public class Dice implements Comparable<Dice>{

    private String[] _faces;
    private Random _random;
    private int _lastRollIndex;
    
    public Dice (String[] values) {
        _faces = Arrays.copyOf(values, values.length);
        _random = new Random();
    }
    
    // Chainable method (Dice.rollDie().getRollValue())
    public Dice rollDie() {
        _lastRollIndex = _random.nextInt(_faces.length);
        return this;
    }
    
    public String getFace() {
        return _faces[_lastRollIndex];
    }
    
    public int getFaceInt() {
        return _lastRollIndex;
    }

    @Override
    public int compareTo(Dice other) {
        // Just compare the _lastRoll
        int ret = 0;
        if (_lastRollIndex > other._lastRollIndex) {
            ret++;
        } else if (_lastRollIndex < other._lastRollIndex) {
            ret--;
        }
        return ret;
    }
    

}
