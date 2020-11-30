package biliruben.games;

import java.util.Arrays;

/**
 * Given a two-dimensional array, this reduces the arrays into row-echelon
 * form. In some cases, not all rows can be properly reduced. Those rows can
 * either be left at the bottom of the array or pruned entirely
 * @author trey.kirk
 *
 */
public class MatrixReducer {
    
    private static boolean _enableDebug;
    private double[][] _augmentedArray;
    
    /*
     * cut & paste template
     * {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
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
        {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0}, //8
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1}, //9
        {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0}, //10
        {0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0}, //11
        {1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1}, //12
        {0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0}, //13
        {0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0}, //14
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}  //15
    };
    
    public static final double[][] SMALL_HEX_COEFFICIANT_BASE = {
        {1, 0, 1, 0, 0, 1},
        {0, 1, 0, 1, 0, 0},
        {0, 1, 1, 0, 1, 0},
        {0, 0, 0, 1, 0, 1}
        
    };
    
    public static final double[][] INNER_HEX_COEFFICIANT_BASE = {
      //{A, B, C, D, E, F, G},
        {1, 1, 0, 0, 0, 0, 0},
        {0, 0, 1, 1, 1, 0, 0},
        {0, 0, 0, 0, 0, 1, 1},
        {0, 0, 1, 0, 0, 1, 0},
        {1, 0, 0, 1, 0, 0, 1},
        {0, 1, 1, 0, 1, 0, 0},
        {1, 0, 1, 0, 0, 0, 0},
        {0, 1, 0, 1, 0, 1, 0},
        {0, 0, 0, 0, 1, 0, 1}
    };

    // Current solution values are irrelevant. we only want to see our matrix
    // get reduced
    public static final double[][] INNER_HEX_SOLUTION = {
        {100},
        {200},
        {300},
        {400},
        {500},
        {600},
        {700},
        {800},
        {900}
    };

    public static final double[][] SMALL_HEX_SOLUTION = {
        {1},
        {2},
        {3},
        {4}
    };

    public static final double[][] SOLUTION_BASE = {
        {38}, //0
        {38}, //1
        {38}, //2
        {38}, //3
        {38}, //4
        {38}, //5
        {38}, //6
        {38}, //7
        {38}, //8
        {38}, //9
        {38}, //10
        {38}, //11
        {38}, //12
        {38}, //13
        {38}, //14
        {190} //15
    };

    
    // Whenever we get a row we can't reduce properly, we increment this
    // mod value to shift the target columns over to the right.
    private int _columnMod = 0;

    public MatrixReducer(double[][] augmentedArray) {
        this._augmentedArray = augmentedArray;
    }
    
    public static void main(String[] args) {
        // test method
        double[][] augmentedArray = constructAugmentedArrays(SMALL_HEX_COEFFICIANT_BASE, SMALL_HEX_SOLUTION);
        MatrixReducer mr = new MatrixReducer(augmentedArray);
        mr.reduceArray(false);
        printAugmentedArray(augmentedArray);
    }
    
    private static double[][] constructSmallAugmentedArrays() {
        return constructAugmentedArrays(INNER_HEX_COEFFICIANT_BASE, SMALL_HEX_SOLUTION);
    }
    
    private static double[][] constructLargeAugmentedArrays() {
        return constructAugmentedArrays(COEFFICIANT_BASE, SOLUTION_BASE);
    }
    
    /*
     * Builds our double array (aka a matrix) based on our defined constants
     */
    public static double[][] constructAugmentedArrays(double[][] coefficiantArrays, double[][] solutionArrays) {
        double[][] augmentedArrays = new double[coefficiantArrays.length][coefficiantArrays[0].length + 1];
        for (int i = 0; i < coefficiantArrays.length; i++) {
            // iterating the rows; copy the row from coefficient base
            // and add one more element from solution
            System.arraycopy(coefficiantArrays[i], 0, augmentedArrays[i], 0, coefficiantArrays[i].length);
            augmentedArrays[i][augmentedArrays[i].length - 1] = solutionArrays[i][0];
        }
        return augmentedArrays;
    }
    
    /**
     * Modifies the original array into row-echelon form
     * @param pruneDependancies
     * @return
     */
    public void reduceArray(boolean pruneDependancies) {
        innerReduceArray(pruneDependancies, _augmentedArray);
    }
    
    /**
     * Reduces a copy of the array and returns the copy
     * @param pruneDependancies
     * @return
     */
    public double[][] reduceArrayCopy(boolean pruneDependancies) {
        double[][] copy = copyArray(_augmentedArray);
        innerReduceArray(pruneDependancies, copy);
        return copy;
    }
    
    /*
     * This could just as easily be a static utility method put
     * somewhere else.
     */
    private double[][] copyArray(double[][] original) {
        double[][] copy = new double[original.length][original[0].length];
        for (int i = 0; i < original.length; i++) {
            System.arraycopy(original[i], 0, copy[i], 0, original[i].length);
        }
        return copy;
    }
    
    /*
     * Reduces the passed in array. This modifies the passed in array
     */
    private void innerReduceArray(boolean pruneDependancies, double[][] augmentedArray) {
        // unused logic. at some point I thought multiple
        // passes might further reduce rows. Left this
        // here for future possible uses
        _columnMod = 0;
        int reducedRows = 0;
        if (_enableDebug) {
            print("Augmented Array:");
            printAugmentedArray(_augmentedArray);
        }
        for (int i = 0; i < _augmentedArray.length && _columnMod + i < _augmentedArray[0].length; i++) {
            boolean reduced = reduceRow(_augmentedArray, i);
            if (!reduced) {
                _columnMod++;
                i--; // keep the row the same and try again
            } else {
                reducedRows++;
            }
        }
        print("Reduced rows: " + reducedRows);
        // relying on the number of rose we've reduced, let's instead
        // ensure that we are truly in row-echelon form

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
     * This is a row-reduction method that reduces the row. Note that it's the row
     * index that's important and not the initial row contents. Given that one
     * of the fundamental reduction methods is row swapping, we cannot guarantee
     * the original row set in the incoming targetRow will be reflected in the resulting
     * return. No row above the targetRow are to be modified
     * 
     * This modifies the provided double-array; fuck defensive copies
     */
    private boolean reduceRow(double[][] augmentedArray, int targetRow) {
        int targetCol = targetRow + _columnMod;
        print("Reducing @ [" + targetRow + ", "+ targetCol + "]: " + Arrays.toString(augmentedArray[targetRow]));

        // use a while loop that continues to pass the row until it has a leading one
        // We assume that our double array has as many or fewer rows than columns. If
        // not we'll allow an ArrayOutOfBoundsException

        // This is our pushed counter. Rows we can't reduce properly get pushed. If we push
        // totalRows - targetRow (i.e. all rows below the original target), this row-column
        // goes un-reduced. We'll return to the same row but an incremented column
        int pushed = 0;
        while (!isLeadingOne(augmentedArray[targetRow], targetCol)) {
            // testing reduced rows is expensive, so toggle this when we make an operation
            // that we know moves it towards reduction
            boolean operated = false;

            // test 1: is there a row below this that has a leading 1? If so, swap it
            if (!isLeadingZeros(augmentedArray[targetRow], targetCol)) {
                for (int i = targetRow + 1; !operated && i < augmentedArray.length; i++) {
                    if (isLeadingZeros(augmentedArray[i], targetCol)) {
                        // good start: this row has all zeros leading it. We can easily
                        // coerce it to a leading 1 if it doesn't already have one
                        print("Swapping with row " + i + ": " + Arrays.toString(augmentedArray[i]));
                        double[] hold = augmentedArray[i];
                        augmentedArray[i] = augmentedArray[targetRow];
                        augmentedArray[targetRow] = hold;
                        operated = true;
                    }
                }
            } else {
                // hey, we're leading zeros, make it a one now
                print("Coercing to one: " + Arrays.toString(augmentedArray[targetRow]));
                double divisor = augmentedArray[targetRow][targetCol];
                for (int i = targetCol; i < augmentedArray[targetRow].length; i++) {
                    augmentedArray[targetRow][i] = augmentedArray[targetRow][i] / divisor;
                }
                operated = true;
            }

            if (operated) continue;

            // test 2: we're still not leading 0s. Iterate the columns for our
            // target row and use another row (one from above is fine) that
            // matches our current leading zeros, whatever that might be until
            // we reach the targetRow column. 
            print("Operation 2");
            for (int col = 0; col < targetRow; col++) {
                if (augmentedArray[targetRow][col] == 0) {
                    continue;
                }
                // Use the row at index 'col' which is by definition of our
                // process a leading one row
                double[] eliminationRow = new double[augmentedArray[0].length];
                System.arraycopy(augmentedArray[col], 0, eliminationRow, 0, eliminationRow.length);

                // our elimination row is known to have a leading 1. So we can assume our
                // deduction factor is simply the value for the target column on targetRow
                double factor = augmentedArray[targetRow][col];
                // Note: I know I could start the iteration at col, but what fun is that
                print("Elimination factor: " + factor);
                print("Target row:       " + Arrays.toString(augmentedArray[targetRow]));
                print("Elimination row:  " + Arrays.toString(eliminationRow));
                for (int i = 0; i < eliminationRow.length; i++) {
                    augmentedArray[targetRow][i] = augmentedArray[targetRow][i] - (eliminationRow[i] * factor);
                }
                print("Post elimination: " + Arrays.toString(augmentedArray[targetRow]));
            }

            // We still may not have what we want. If so, push this row to the last row and process again
            if (!isLeadingZeros(augmentedArray[targetRow], targetCol) && pushed < augmentedArray.length - targetRow - 1) {
                pushDown(augmentedArray, targetRow);
                pushed++;
            }
            if (pushed >= augmentedArray.length - targetRow - 1) {
                // we've pushed too much, we can't do this one
                return false;
            }
            // At this point we are ready to retest
        }

        // we got here? good, we've passed
        return true;
    }

    /*
     * Takes the row at 'targetRow' and holds it to the side. All rows below are
     * shifted up one. 'targetRow' is slid into the last row
     */
    private void pushDown(double[][] augmentedArray, int targetRow) {
        print("Pushing row " + targetRow);
        double[] tmpRow = augmentedArray[targetRow];
        for (int i = targetRow + 1; i < augmentedArray.length; i++) {
            augmentedArray[i - 1] = augmentedArray[i];
        }
        augmentedArray[augmentedArray.length - 1] = tmpRow;
        if (_enableDebug)
            printAugmentedArray(augmentedArray);
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
    
    /**
     * Utility method to test if the target row is a 'leading one'
     * row, which by definition is one that has zeros until the target
     * index, where in which it then has a value of '1'
     * @param row
     * @param leadingIndex
     * @return
     */
    public static boolean isLeadingOne(double[] row, int leadingIndex) {
        return isLeadingZeros(row, leadingIndex) && row[leadingIndex] == 1;
    }

    /**
     * Utility method to test if the target row has zeros leading up
     * until the target index. Returns true iff all columns preceding
     * leadingIndex are zero and value in leadingIndex is non-zero
     * @param row
     * @param leadingIndex
     * @return
     */
    public static boolean isLeadingZeros(double[] row, int leadingIndex) {
        for (int i = 0; i < leadingIndex; i++) {
            if (row[i] != 0) {
                return false;
            }
        }
        // a row "leading 0s" has to have a non-zero value at our target column.
        // While it's a true statement that this row might be a leading 0s, but
        // for a different column.
        return row[leadingIndex] != 0;
    }
}
