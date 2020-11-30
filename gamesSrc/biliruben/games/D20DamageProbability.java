package biliruben.games;

import java.util.Random;

public class D20DamageProbability {
    
    private static int _samples;
    private static int _AC;
    private static int _atkBonus;
    private static double _avgDamage;
    private static int _attacks;

    /**
     * Given a bonus to attack, target AC, and average damage, provide the normalized damage per attack
     */
    public D20DamageProbability() {
        // TODO Auto-generated constructor stub
    }

    public static void main(String[] args) {
        Random r = new Random();
        try {
            _samples = Integer.valueOf(args[0]);
            _AC = Integer.valueOf(args[1]);
            _atkBonus = Integer.valueOf(args[2]);
            _avgDamage = Double.valueOf(args[3]);
            if (args.length > 4) {
                _attacks = Integer.valueOf(args[4]);
            } else {
                _attacks = 1;
            }
        } catch (Throwable t) {
            t.printStackTrace(System.out);
            System.out.println("parameters: Sample AC AtkBonus AvgDamage");
            return;
        }
        
        int successes = 0;
        for (int i = 0; i < _samples; i++) {
            // Each sample is a 'round' of attacks
            for (int atk = 0; atk < _attacks; atk++) {
                int roll = r.nextInt(20) + 1 + _atkBonus;
                if (roll >= _AC) {
                    successes++;
                }
            }
        }
        
        double avgAtkDmg = (successes * _avgDamage) / _samples;
        System.out.println("Successes: " + successes);
        System.out.println("Avg Damage: " + avgAtkDmg);
        
        // different probability test: given a roll of 2d6, how much do I improve the average roll if
        // i can reroll it once per roll and choose the highest result?
        
        /*
        int samples = 1000;
        double totalDamage = 0.0;
        double totalBetterDamage = 0.0;
        for (int i = 0; i < samples; i++) {
            int d1 = r.nextInt(6) + 1;
            int d2 = r.nextInt(6) + 1;
            int firstRoll = d1+d2;
            
            d1 = r.nextInt(6) + 1;
            d2 = r.nextInt(6) + 1;
            int secondRoll = d1+d2;
            
            totalDamage += firstRoll;
            totalBetterDamage += firstRoll > secondRoll ? firstRoll : secondRoll;
        }
        
        System.out.println("totalDamage: " + totalDamage);
        System.out.println("totalBetterDamage: " + totalBetterDamage);
        
        double averageRegularDamage = totalDamage / (double)samples;
        double averageBetterDamage = totalBetterDamage / (double)samples;
        
        System.out.println("averageRegularDamage: " + averageRegularDamage);
        System.out.println("averageBetterDamage: " + averageBetterDamage);
        */

    }

}
