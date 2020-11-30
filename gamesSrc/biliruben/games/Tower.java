package biliruben.games;

import java.util.Stack;

/**
 * Ye old "Tower of Hanoi" exercise. I never took CompSci and as a result, never had to do this. So
 * I fixed that.
 * 
 * This is an exercise in recursion. The premise is one "tower" of platters, consisting of incrementally smaller
 * platters from the base to the top, must be moved from one peg to another. The restrictions are:
 * - Only one platter can be moved at a time. If there is a platter sitting on top of it, that lower platter
 *   cannot be moved.
 * - Platters may only be moved to empty pegs or on top of a larger platter.
 * 
 * The recursion pattern demonstrated is:
 * - movePlatter is the recursion method that is given:
 *   - what platter to move
 *   - what peg to move it from
 *   - what peg to move it to
 *   - and what the spare peg is
 * - in the movePlatter method, if the platter is not the smallest platter, we assume there is a smaller platter sitting on
 *   top of it. So we must move the platter above to the spare peg first, then move the target platter, then move the above platter
 *   back off of the spare and onto the recently moved platter. However, if it is instead the smallest platter, we just move it
 *   to the target peg (since that platter is known to be able to move to any peg and not violate the constraints).
 *   
 *   In effect, the recursion behaves like so:
 *   - Initial call: "Move platter 6 from peg 1 to peg 3"
 *     - "Recursive call: First move platter 5 from peg 1 to the spare peg 2"
 *     - "Recursive call: before that can happen, move platter 4 from peg 1 to spare peg 3"
 *     - and so on until the inner most call: "move platter 1 from peg 1 to spare peg 2". The calling recursion method
 *       was the one to move platter 2 to peg 3, which it does. It then moves platter 1 from the spare to peg 3.
 *       This returns back to the method where Platter 3 was being moved to target peg 2, which it now can do. it then
 *       sends a new recursive call to move platter 2 from  spare peg 3 back on top of target peg 2
 *       
 *    And so goes the recursive iteration: In order to move platter X to target T, we must first move platter X-1 to spare S
 * @author trey.kirk
 *
 */

public class Tower {
    
    private static Stack<Integer> _peg1;
    private static Stack<Integer> _peg2;
    private static Stack<Integer> _peg3;
    private static int _towerSize;
    private static boolean _debug;
    private static int _ops = 0;
    
    

    public static void main(String[] args) {
        // arg[0] is the size of the tower
        _debug = true;
        _towerSize = 6;
        if (args.length > 0) {
            _towerSize = Integer.valueOf(args[0]);
        }
        
        // arg[1] is true/false, driving the debug output
        if (args.length > 1) {
            _debug = Boolean.valueOf(args[1]);
        }
        _peg1 = new Stack<Integer>();
        _peg2 = new Stack<Integer>();
        _peg3 = new Stack<Integer>();
        
        // populate the first peg
        for (int i = _towerSize; i > 0; i--) {
            _peg1.push(i);
        }
        
        // Initiate by requesting Platter 6 to be moved from _peg1 to _peg3 leveraging _peg2 as a spare
        if (_debug) {
            System.out.print("Before move, ");
            reportPegs();
        }

        movePlatter(_towerSize, _peg1, _peg3, _peg2);
        System.out.println();

        if (_debug) {
            System.out.print("After move, ");
            reportPegs();
        }
        System.out.println("Total operations: " + _ops);
    }
    
    private static void reportPegs() {
        reportPeg("Peg1", _peg1);
        reportPeg("Peg2", _peg2);
        reportPeg("Peg3", _peg3);
        
    }
    
    private static void reportPeg(String msg, Stack<Integer> peg) {
        System.out.println(msg + " :: " + peg);
    }
    
    private static void movePlatter(Integer platter, Stack<Integer> src, Stack<Integer> target, Stack<Integer> spare) {
        _ops++;
        if (_debug) {
            System.out.println("---------------");
            //reportPegs();
            System.out.println("Moving " + platter + " from src: " + src + " to target: " + target + " using spare: " + spare);
        }
        // is it platter #1?
        // Yes - Move it, return
        // else - move platter - 1 to spare, move platter, move spare back
        if (platter == 1) {
            actualMovePlatter(platter, src, target);
        } else {
            movePlatter (platter - 1, src, spare, target);
            actualMovePlatter(platter, src, target);
            movePlatter(platter - 1, spare, target, src);
        }
        
    }
    
    private static void actualMovePlatter(Integer platter, Stack<Integer> src, Stack<Integer> target) {
        target.push(src.pop());
        if (_debug) {
            System.out.println("Moved  " + platter + " to " + target);
        }
    }

}
