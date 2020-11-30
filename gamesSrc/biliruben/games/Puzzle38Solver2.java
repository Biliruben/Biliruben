package biliruben.games;

import java.util.Arrays;
import java.util.List;


/**
 * Solves the "38 Puzzle" by using a known set of "ring" token sets to create
 * a more descriptive augmented 2 dimensional array. We then plug the array into
 * the MatrixReducer and confirm a solution is possible
 */
public class Puzzle38Solver2 {
    
    public static final double[] DBL_VALID_TOKENS = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19};
    public static final Integer[] INT_VALID_TOKENS = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19};

    /*
     * Step 1: find all valid rings of the "38 - Puzzle"
     * Step 2: model a system of equations injecting each ring as a guess for the constant values
     * Step 3: make her open the box
     * Step 4: solve the array system and validate the results
     * 
     * The matrix template. In the solutions column, the notation is:
     *    "38 - [two elements of the ring array]"
     *    
        A,B,C,D,E,F,G, Solutions
        1,1,0,0,0,0,0, 38 - [11 + 3]
        0,0,1,1,1,0,0, 38 - [10 + 4]
        0,0,0,0,0,1,1, 38 - [9 + 5]
        0,0,1,0,0,1,0, 38 - [11 + 7]
        1,0,0,1,0,0,1, 38 - [0 + 6]
        0,1,0,0,1,0,0, 38 - [1 + 5]
        1,0,1,0,0,0,0, 38 - [9 + 1]
        0,1,0,1,0,1,0, 38 - [2 + 8]
        0,0,0,0,1,0,1, 38 - [3 + 7]
     */
    
    // Build the coefficient array. Happily, it's static
    private static final double[][] COEFFICIENTS_ARRAY = {
        {1, 1, 0, 0, 0, 0, 0},
        {0, 0, 1, 1, 1, 0, 0}, // 5
        {0, 0, 0, 0, 0, 1, 1},
        {0, 0, 1, 0, 0, 1, 0},
        {1, 0, 0, 1, 0, 0, 1}, // 5
        {0, 1, 0, 0, 1, 0, 0},
        {1, 0, 1, 0, 0, 0, 0},
        {0, 1, 0, 1, 0, 1, 0}, // 5
        {0, 0, 0, 0, 1, 0, 1}
    };

    // Build the coefficient array. Happily, it's static
    private static final double[][] MOD_COEFFICIENTS_ARRAY = {
        {1, 1, 0,  0, 0, 0},
        {0, 0, 1,  1, 0, 0}, // 5
        {0, 0, 0,  0, 1, 1},
        {0, 0, 1,  0, 1, 0},
        {1, 0, 0,  0, 0, 1}, // 5
        {0, 1, 0,  1, 0, 0},
        {1, 0, 1,  0, 0, 0},
        {0, 1, 0,  0, 1, 0}, // 5
        {0, 0, 0,  1, 0, 1}
    };

    private static final double[][] SMALL_RING_COEFFICIENTS_ARRAY = {
        {1, 1, 0, 0, 0, 0},
        {0, 1, 1, 0, 0, 0},
        {0, 0, 1, 1, 0, 0},
        {0, 0, 0, 1, 1, 0},
        {0, 0, 0, 0, 1, 1},
        {1, 0, 0, 0, 0, 1}
    };
    
    /*
     * The only reason this is double is because MatrixReducer was long-ago
     * coded that way. In the end, it's unnecessary
     */
    private static double[][] getSolutionsArray(int[] ring) {
        double[][] solutionsArray = new double[9][1];
        int mod = 0;
/*
        for (int row = 0; row < solutionsArray.length; row++) {
            switch (row) {
            case 0: mod = ring[11] + ring[3]; break;
            case 1: mod = ring[10] + ring[4]; break;
            case 2: mod = ring[9] + ring[5]; break;
            case 3: mod = ring[11] + ring[7]; break;
            case 4: mod = ring[0] + ring[6]; break;
            case 5: mod = ring[1] + ring[5]; break;
            case 6: mod = ring[9] + ring[1]; break;
            case 7: mod = ring[2] + ring[8]; break;
            case 8: mod = ring[3] + ring[7]; break;
            }
            solutionsArray[row][0] = 38 - mod;
        }
        */
        for (int row = 0; row < solutionsArray.length; row++) {
            switch (row) {
            case 0: mod = ring[11] + ring[3]; break;
            case 1: mod = ring[10] + ring[4] + 5; break;
            case 2: mod = ring[9] + ring[5]; break;
            case 3: mod = ring[11] + ring[7]; break;
            case 4: mod = ring[0] + ring[6] + 5; break;
            case 5: mod = ring[1] + ring[5]; break;
            case 6: mod = ring[9] + ring[1]; break;
            case 7: mod = ring[2] + ring[8] + 5; break;
            case 8: mod = ring[3] + ring[7]; break;
            }
            solutionsArray[row][0] = 38 - mod;
        }

    /*
        for (int row = 0; row < solutionsArray.length; row++) {
            switch (row) {
            case 0: mod = ring[11] + ring[3]; break;
            case 1: mod = ring[1] + ring[5]; break;
            case 2: mod = ring[3] + ring[7]; break;
            case 3: mod = ring[5] + ring[9]; break;
            case 4: mod = ring[7] + ring[11]; break;
            case 5: mod = ring[9] + ring[1]; break;
            }
            solutionsArray[row][0] = 38 - mod;
        }
    */
        return solutionsArray;
    }
    
    public static void main(String[] args) {
        RingFinder rf = new RingFinder(INT_VALID_TOKENS);
        List<int[]> rings = rf.getRings();
        
        for (int[] ring : rings) {
            System.out.println("Ring: " + Arrays.toString(ring));
            double[][] augmentedArray = MatrixReducer.constructAugmentedArrays(MOD_COEFFICIENTS_ARRAY, getSolutionsArray(ring));
            System.out.println("Initial array: ");
            for (int row = 0; row < augmentedArray.length; row++) {
                System.out.println(Arrays.toString(augmentedArray[row]));
            }
            System.out.println("Reduced:");
            MatrixReducer mr = new MatrixReducer(augmentedArray);
            mr.reduceArray(true);
            for (int row = 0; row < augmentedArray.length; row++) {
                System.out.println(Arrays.toString(augmentedArray[row]));
            }
            System.out.println("-----------------------------------------------");
        }

        
    }
    

}
