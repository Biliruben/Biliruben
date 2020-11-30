package biliruben.games.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoxSimulator {
    
    private Map<Integer, Integer> _boxes;

    public BoxSimulator(int boxes) {
        init(boxes);
    }
    
    private void init(int boxes) {
        List<Integer> dollars = new ArrayList<Integer>();
        for (int i = 0; i < boxes; i++) {
            dollars.add(i);
        }
        Collections.shuffle(dollars);
        _boxes = new HashMap<Integer, Integer>();
        for (int i = 0; i < boxes; i++) {
            Integer dollar = dollars.get(i);
            // if the dollar matches the box, swap it
            // with the last dollar
            //
            // update: this little swapout reduces the chances of success by about ~7%
            if (dollar == i && false) {
                Integer lastDollar = dollars.get(dollars.size() - 1);
                dollars.add(dollars.size() - 1, dollar);
                dollar = lastDollar;
            }
            _boxes.put(i, dollar);
        }
    }
    
    public boolean findDollar(int targetDollar, int guesses) {
        int nextGuess = targetDollar;
        do {
            int discoveredDollar = _boxes.get(nextGuess);
            if (discoveredDollar == targetDollar) {
                break;
            }
            guesses--;
            nextGuess = discoveredDollar;
        } while (guesses > 0);
        return guesses > 0; // any guesses left? We found it!
    }

}
