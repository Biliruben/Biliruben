package biliruben.games;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class AutoDefeatingRolls {

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        int[] dice = {20,20,20,6};
        int bonus = 7;
        int limit = 100000;
        int resultSum = 0;
        Map<Integer, Integer> stats = new HashMap<Integer, Integer>();
        Random r = new Random(System.currentTimeMillis());
        File f = new File("c:/temp/rolls.txt");
        FileWriter fw = new FileWriter(f);
        BufferedWriter buff = new BufferedWriter(fw);
        StringBuilder builder = new StringBuilder();
        for (int d = 0; d < dice.length; d++) {
            builder.append("d").append(dice[d]).append(",");
        }
        builder.append("result\n");
        buff.write(builder.toString());
        for (int i = 0; i < limit; i++) {
            builder = new StringBuilder();
            int result = 0;
            for (int die = 0; die < dice.length; die++) {
                int dieMax = dice[die];
                int roll = r.nextInt(dieMax) + 1;
                builder.append(roll).append(",");
                result += roll;
            }
            result += bonus;
            builder.append(result).append("\n");
            buff.write(builder.toString());
            resultSum += result;
            Integer key = new Integer(result);
            Integer stat = stats.get(key);
            if (stat == null) {
                stat = 0;
            }
            stat++;
            stats.put(key, stat);
        }
        buff.flush();
        buff.close();
        
        int average = resultSum / limit;
        System.out.println(limit + " rolls:");
        System.out.println("Average: " + average);
        System.out.println("Result  ::  Occurrence");
        Set<Integer> keys = new TreeSet<Integer>(stats.keySet());
        for (Integer key : keys) {
            System.out.println(key + " :: " + stats.get(key));
        }

    }

}
