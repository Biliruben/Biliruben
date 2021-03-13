package biliruben.games.trains;

import java.util.Arrays;

public class CrateChances {
    
    private static int[] getResults (int limit, TrainChanceCfg cfg) {
        int[] totals = {0, 0, 0, 0};
        for (int i = 0; i < limit; i++) {
            int selectedTrain = cfg.getTrain();
            int currentTrainTotal = totals[selectedTrain];
            totals[selectedTrain] = ++currentTrainTotal;
        }

        return totals;
    }

    public static void main(String[] args) {
        // Scenario 1: 200 purchases of level 1 crates
        int limit = Integer.valueOf(args[0]);
        TrainChanceCfg commonCfg = new TrainChanceCfg (75, 15, 8, 2);
        int[] commonTotals = getResults(2 * limit, commonCfg);
        System.out.println ("Common: " + Arrays.toString(commonTotals));

        TrainChanceCfg rareCfg = new TrainChanceCfg(40, 40, 15, 5);
        int[] rareTotals = getResults(limit, rareCfg);
        System.out.println ("Rare: " + Arrays.toString(rareTotals));
    }

}
