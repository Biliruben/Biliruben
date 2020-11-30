package biliruben.games;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionLegend;

import Jama.Matrix;
import biliruben.threads.ThreadRunner;
import biliruben.threads.ThreadRunner.TRRunnable;


public class Puzzle38Solver {

    /*
     * Iteration 1: Use "Cramer's Rule". Implementation halted as early implementations
     *      clearly indicate a determinant of 0 for our working matrix.
     * 
     * Iteration 2: Convert augmented matrix (of 16 original formulas) into row-echelon
     *      form (row-echelon has "leading 1" rows with each row's leading 1 being
     *      incremented one row to the right as the row above. Once in row-echelon
     *      is achieved, substitute remaining unknowns (we expect 7).
     *      
     * Iteration 3: Take the row-echelon formatted matrix and augment it with the
     *      missing rows using guessing values. After the guessed values are
     *      inserted we'll further reduce it to a reduced row echelon using the
     *      following ruleset to validate guesses:
     *       - all guessed sets will adhere to these rules
     *       - the solution must be consistent (no row reduces to 1 = 0)
     *       - the solution values must fall into the set of VALID_TOKENS
     *       - no solution value will repeat a previous solution value
     */

    /*
     * Our Base 16 Formulas: the 15 that all add up to 38 and the 16th of
     * all elements summed up
     * 
     * This is a copy & paste template that makes it easier to
     *    augment the below value
     * {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
     * 
     * After iteration 2 was complete, rows 8, 9, 10, & 15 cannot be
     *    reduced to row-echelon. They're removed from the base set in
     *    preparation for iteration 3
     */
    public static final double[][] COEFFICIANT_BASE = {
        // 0  1  2  3  4  5  6  7  8  9  10 11 12 13 14 15 16 17 18
        // A  B  C  D  E  F  G  H  I  J  K  L  M  N  O  P  Q  R  S
        {1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, //0
        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, //1
        {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0}, //2
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0}, //3
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1}, //4
        {1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, //5
        {0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0}, //6
        {0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0}, //7
        //{0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0}, //8
        //{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1}, //9
        //{0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0}, //10
        {0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0}, //11
        {1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1}, //12
        {0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0}, //13
        {0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0}, //14
        //{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}  //15
    };

    /*
     * The reduced row echelon of the guessed values coefficients. We
     * prefer to guess columns 0, 2, 7, 9, 11, 16, 18
     */
    public static final double[][] GUESSING_BASE = {
        // 0  1  2  3  4  5  6  7  8  9  10 11 12 13 14 15 16 17 18
        // A  B  C  D  E  F  G  H  I  J  K  L  M  N  O  P  Q  R  S
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
    };
    /*
     * We've resigned the fact that we're just gonna have to brute force
     * some of this. Manual analysis suggests we're gonna have seven
     * unknowables. So here we define the tokens we'd prefer to guess
     * because we know they have some limitations. For example, tokens
     * on the point of the hexagon are known to be three or higher. Further
     * the center token is known to be 11 or less. These rules will allow
     * us to use the most efficient guessing.
     */
    public static final double[] PREFERRED_GUESSES = {
        1, 3, 8, 10, 12, 17, 19
    };

    /*
     * This actually doesn't have to be a double-array, just felt right
     */
    public static final double[][] SOLUTION_BASE = {
        {38}, //0
        {38}, //1
        {38}, //2
        {38}, //3
        {38}, //4
        {38}, //5
        {38}, //6
        {38}, //7
        //{38}, //8
        //{38}, //9
        //{38}, //10
        {38}, //11
        {38}, //12
        {38}, //13
        {38} //14
        //{190} //15
    };

    // This comes into play when we start guessing values
    public static final double[] VALID_TOKENS = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19};

    private static boolean _enableDebug = false;
    private static int _printEvery = 10000;
    private static int STOP_AFTER = -1;

    private static int _trThreads = 6;
    private static int _trWork = 10000;

    private static final String OPT_THREADS = "t";

    private static final String OPT_REPORT = "r";

    private static final String OPT_DEBUG = "d";

    private static final String OPT_MAX_QUEUE_SIZE = "q";

    private static double[][] finalSolution = null;  //SIEG HEIL!

    private static GetOpts _opts;

    private static Date _startDate;
    private static class ReduceRunner implements TRRunnable {

        double[][] _augmentedArray;

        ReduceRunner(double[][] augmentedArray) {
            _augmentedArray = augmentedArray;
        }

        @Override
        public void run() {
            
            // I love a good for-loop where the block is all in the declaration
            int i;
            //for (i = 0; i < _augmentedArray.length && isLeadingZeros(_augmentedArray[i], i); i++);

            // we passed when i is >= _augmentedArray.length
            /*
            if (i >= _augmentedArray.length) {
                finalSolution = _augmentedArray;
            }
            */

        }

        @Override
        public void pause() {
            // TODO Auto-generated method stub

        }

        @Override
        public void resume() {
            // TODO Auto-generated method stub

        }

        @Override
        public void shutdown() {
            // TODO Auto-generated method stub

        }

    }
    
    private static void init(String[] args) {
        _opts = new GetOpts(Puzzle38Solver.class);
        
        OptionLegend legend = new OptionLegend(OPT_THREADS);
        legend.setDefaultValue("4");
        _opts.addLegend(legend);
        
        legend = new OptionLegend(OPT_MAX_QUEUE_SIZE);
        legend.setDefaultValue("10000");
        _opts.addLegend(legend);
        
        legend = new OptionLegend(OPT_REPORT);
        legend.setDefaultValue("10000");
        _opts.addLegend(legend);
        
        legend = new OptionLegend(OPT_DEBUG);
        legend.setDefaultValue("false");
        legend.setFlag(true);
        _opts.addLegend(legend);
        
        _opts.parseOpts(args);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

        _startDate = new Date();
        init(args);
        // our guessing arrays that we'll tack in later
        // 0  1  2  3  4  5  6  7  8  9  10 11 12 13 14 15 16 17 18
        // A  B  C  D  E  F  G  H  I  J  K  L  M  N  O  P  Q  R  S
        double[] A = {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        double[] C = {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        double[] H = {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        double[] L = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0};
        double[] Q = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0};
        double[] S = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1};
        double[] J = {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        // Map our pointIndexes index with our arrays
        Map<Integer, double[]> indexMap = new HashMap<Integer, double[]>();
        indexMap.put(0, A);
        indexMap.put(1, C);
        indexMap.put(2, H);
        indexMap.put(3, L);
        indexMap.put(4, Q);
        indexMap.put(5, S);
        indexMap.put(6, J);

        // Thread runner. Limiting the workload is ideal
        _trThreads =  Integer.valueOf(_opts.getStr(OPT_THREADS));
        _trWork = Integer.valueOf(_opts.getStr(OPT_MAX_QUEUE_SIZE));
        _printEvery = Integer.valueOf(_opts.getStr(OPT_REPORT));
        _enableDebug = Boolean.valueOf(_opts.getStr(OPT_DEBUG));
        
        ThreadRunner tr = new ThreadRunner(_trThreads, _trWork);
        tr.setReportIncrement(_printEvery);

        // the array that actually tracks our values
        int[] pointsIndexes = {2, 3, 4, 5, 6, 7};

        // Effectively we have seven nested loops, but we don't have to write 7 ugly
        // actual loops. Use a while loop that uses 'A' as the outer most loop. We exit
        // when A reaches index 14.  Why aren't we iterating A through to idx 18?
        //
        // Because of our 7 unknowable guess tokens, 6 of them are the 6 points on the puzzle.
        // These six can rotate just as the entire puzzle can providing a total
        // of six different solutions. Example, consider the set:
        // 3, 7, 15, 9, 4, 13
        //
        // Shifting this array to the right emulates the rotation of the puzzle and 
        // demonstrates the other five possibilities:
        // 7, 15, 9, 4, 13, 3
        // 15, 9, 4, 13, 3, 7
        // etc.
        //
        // To reduce guesses we're going to eliminate patterns we've already guessed in
        // a different rotation. We do this by ensuring A is always the lowest value of
        // our series. If you look at the examples above, shifting the elements until the
        // lowest value is in the first element (3), these "normalized" arrays are then
        // equal.
        //
        // For our 7th token: J is the center token. It doesn't care about the rotation
        // since the center never changes from one rotation to the next. Thus J is the same
        // value for all of the six possible values. Instead, J limits itself to a known
        // property of the center token: it's impossible for it to be greater than 11 as 
        // it is impossible to create three sets of the remaining tokens that add up to 38
        int count = 0;
        while (pointsIndexes[0] < 14 && finalSolution == null) {
            for (int j = 0; j < 11 && finalSolution == null; j++) {
                if (!isUnique(pointsIndexes, j)) {
                    continue;
                }
                // all the work
                int[] guessValues = new int[pointsIndexes.length + 1];
                for (int i = 0; i < pointsIndexes.length; i++) {
                    guessValues[i] = pointsIndexes[i];
                }
                guessValues[guessValues.length - 1] = j;
                double[][] augmentedArray = constructAugmentedArrays(guessValues, indexMap);

                if (_enableDebug) {
                    printAugmentedArray(augmentedArray);
                    System.out.println("----------------------------------------");
                }
                // some printy debug stuff
                if (_printEvery > 0 && count % _printEvery == 0) {
                    printAugmentedArray(augmentedArray);
                    System.out.println("-------------------------------");
                }

                if (count > STOP_AFTER && STOP_AFTER != -1) {
                    System.out.println("Terminated after max tries: " + count);
                    System.out.println(Arrays.toString(pointsIndexes) + " " + j);
                    return;
                }

                count++;

                ReduceRunner r = new ReduceRunner(augmentedArray);
                if (_trThreads > 1) {
                    // setup runner
                    tr.add(r);
                } else {
                    // single threaded? Ok, just run it
                    r.run();
                }
            }
            // iterate the points indexes
            incrementPointsIndex(pointsIndexes, pointsIndexes.length - 1); // this method is awesomely recursive
        }

        while (finalSolution == null && tr.hasWork()) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // don't care
            }
        }

        if (finalSolution != null) {
            tr.shutDown(true);
            printAugmentedArray(finalSolution);
            System.out.println("Solution found after " + count + " tries");
        }

        System.out.println("Count: " + count);
        System.out.println(Arrays.toString(pointsIndexes));

        Date delta = new Date(System.currentTimeMillis() - _startDate.getTime());
        SimpleDateFormat sdf = new SimpleDateFormat("HHH:mm:ss");
        System.out.println("Elapsed time: (" + delta.getTime() + ") " + sdf.format(delta));
        // Code after this comment is old iteration 2
        /*        
        double[][] augmentedArray = constructAugmentedArrays();
        // catalog the originals
        // the reference of the original values
        double[][] copy = constructAugmentedArrays();
        // index the original array objects
        List<double[]> originalList = new ArrayList<double[]>();
        for (int i = 0; i < augmentedArray.length; i++) {
            originalList.add(augmentedArray[i]);
        }

        // row reduction happened here

        //printAugmentedArray(augmentedArray);

        // Final version will not have this to avoid useless dependancies
        // It just happens to print prettier
        Matrix.constructWithCopy(augmentedArray).print(3, 0);

        // Show me the original arrays of the non-reduced rows so I can
        // prune them from iteration 3
        for (int i = reducedRows; i < augmentedArray.length; i++) {
            double[] modifiedRow = augmentedArray[i];
            // find the double[] in the list
            int idx = originalList.indexOf(modifiedRow);
            if (idx > -1) {
                System.out.println(idx + ": " + Arrays.toString(copy[idx]));
            }
        }
         */
    }


    /*
     * Confirms the 'value' is unique from the rest of the array
     */
    private static boolean isUnique(int[] testArray, int value) {
        // since 'isUnique' isn't sorted, we'll have to do it the dumb way
        for (int testIndex = 0; testIndex < testArray.length; testIndex++) {
            if (value == testArray[testIndex]) {
                return false;
            }
        }
        return true;
    }
    /*
     * Increments the points indexes ensuring we don't duplicate
     * values represented by rotating the ring
     */
    private static void incrementPointsIndex(int[] pointsIndexes, int idx) {
        // this is a recursive method. When the value at [idx] is maxed, we decrement
        // idx and recurse back in. After exiting we set [idx] to the lowest avaialable value

        // check 1: can we increment the value at idx. Yes if the next value
        // is unique and within range
        int incr = pointsIndexes[idx];
        do {
            incr++;
        } while (!isUnique(pointsIndexes, incr));
        // this might ultimately be a bad value (like 19), but the next piece resolves that
        pointsIndexes[idx] = incr;

        // if pointsIndexes[idx] has blown the upper limit, then we need to recurse
        if (pointsIndexes[idx] >= VALID_TOKENS[VALID_TOKENS.length - 1]) {
            // in theory we could reach a point where the first element is too high
            // and recursion will cause an array out of bounds exception. However, we
            // know that our top level caller controls that limit
            incrementPointsIndex(pointsIndexes, idx - 1);

            // now that we've incremented the previous value, set ours to the lowest available
            for (int token = 2; token < VALID_TOKENS.length; token++) {
                if (VALID_TOKENS[token] <= VALID_TOKENS[pointsIndexes[0]]) {
                    continue; // nobody is allowed to be smaller than the first element
                }

                if (isUnique(pointsIndexes, token)) {
                    // the lowest available
                    pointsIndexes[idx] = token;
                    return; // all done
                }
            }
        }
    }



    /*
     * Debug print statement
     */
    private static void print(String msg) {
        if (_enableDebug) {
            System.out.println(msg);
        }
    }




    /*
     * Prints the contents of the passed in double-array. This method prints
     * irrespective of the debug conditional.
     */
    private static void printAugmentedArray(double[][] augmentedArray) {
        // No joy in writing this pattern everywhere I want to trace my double array
        for (int i = 0; i < augmentedArray.length; i++) {
            System.out.println(Arrays.toString(augmentedArray[i]));
        }    
    }



    private static double[][] constructAugmentedArrays(int[] guessValues,
            Map<Integer, double[]> indexMap) {
        // get our basic augmented arrays
        double[][] constructedArrays = constructAugmentedArrays();

        // tack on the guessed arrays from indexMap with an extra column from the guessValues
        double[][] augmentedArrays = new double[constructedArrays.length + indexMap.size()][constructedArrays[0].length];
        System.arraycopy(constructedArrays, 0, augmentedArrays, 0, constructedArrays.length);
        int nextIndex = constructedArrays.length;
        for (int i = 0; i < guessValues.length; i++) {
            double[] coefficientArray = indexMap.get(i);
            double[] fullArray = new double[coefficientArray.length + 1];
            System.arraycopy(coefficientArray, 0, fullArray, 0, coefficientArray.length);
            fullArray[fullArray.length - 1] = VALID_TOKENS[guessValues[i]];
            augmentedArrays[nextIndex] = fullArray;
            nextIndex++;
        }
        return augmentedArrays;
    }

    /*
     * Builds our double array (aka a matrix) based on our defined constants
     */
    private static double[][] constructAugmentedArrays() {
        double[][] augmentedArrays = new double[COEFFICIANT_BASE.length][COEFFICIANT_BASE[0].length + 1];
        for (int i = 0; i < COEFFICIANT_BASE.length; i++) {
            // iterating the rows; copy the row from coefficient base
            // and add one more element from solution
            System.arraycopy(COEFFICIANT_BASE[i], 0, augmentedArrays[i], 0, COEFFICIANT_BASE[i].length);
            augmentedArrays[i][augmentedArrays[i].length - 1] = SOLUTION_BASE[i][0];
        }
        return augmentedArrays;
    }
}
