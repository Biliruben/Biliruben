package biliruben.games.simulation;

public class DollarBoxSim {

    /**
     * Given 100 people numbered 1-100. Each has a dollar with their number associated.
     * Each dollar is put into random boxes also labeled 1-100. Simulate a guessing pattern
     * in which each person attempts to find their dollar by using a chaining method.
     * @param args
     */
    public static void main(String[] args) {
        // Create a simulation object. This app will execute and summarize the results
        int ITERATIONS = 1000;
        int tally = 0;
        for (int j = 0; j < ITERATIONS; j++) {
            BoxSimulator sim = new BoxSimulator(100);
            boolean allFound = true;
            for (int i = 0; i < 100 && allFound; i++) {
                allFound = allFound && sim.findDollar(i, 50);
            }
            if (allFound) {
                tally++;
            }
        }
        System.out.println("Iterations: " + ITERATIONS + ", found: " + tally);
    }

}
