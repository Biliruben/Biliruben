package biliruben.games.trains;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class TrainChanceCfg {
    
    public static final int COMMON_CHANCE_INDEX = 0;
    public static final int RARE_CHANCE_INDEX = 1;
    public static final int EPIC_CHANCE_INDEX = 2;
    public static final int LEGENDARY_CHANCE_INDEX = 3;
    
    List<Integer> crateChances;

    public TrainChanceCfg () {
        this.crateChances = new ArrayList<Integer>(LEGENDARY_CHANCE_INDEX + 1);
    }
    
    public TrainChanceCfg (Integer... chances) {
        this();
        this.crateChances = Arrays.asList(chances);
        
    }

    public void setCommonChance (int commonChance) {
        this.crateChances.set (COMMON_CHANCE_INDEX, commonChance);
    }

    public void setRareChance (int rareChance) {
        this.crateChances.set (RARE_CHANCE_INDEX, rareChance);
    }
    
    public void setEpicChance (int epicChance) {
        this.crateChances.set (EPIC_CHANCE_INDEX, epicChance);
    }
    
    public void setLegendaryChance (int legendaryChance) {
        this.crateChances.set (LEGENDARY_CHANCE_INDEX, legendaryChance);
    }
    
    public int getCommonChance () {
        return this.crateChances.get(COMMON_CHANCE_INDEX);
    }
    
    public int getRareChance () {
        return this.crateChances.get(RARE_CHANCE_INDEX);
    }
    
    public int getEpicChance() {
        return this.crateChances.get(EPIC_CHANCE_INDEX);
    }
    
    public int getLegenedaryChance() {
        return this.crateChances.get(LEGENDARY_CHANCE_INDEX);
    }
    
    public int getTrain () {
        Random r = new Random();
        int roll = r.nextInt(100);
        int selectedTrain = -1;
        int rollingSum = 0;
        for (int i = 0; i < LEGENDARY_CHANCE_INDEX + 1 && selectedTrain < 0; i++) {
            rollingSum += this.crateChances.get(i);
            if (rollingSum >= roll) {
                selectedTrain = i;
            }
        }
        return selectedTrain;
    }
}
