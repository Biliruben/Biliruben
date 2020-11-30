package biliruben.games;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * Calculates a List of 12-element arrays representing
 * a valid "ring" around the hexagonal "38 Puzzle"
 * @author trey.kirk
 *
 */
public class RingFinder {
    
    private List<int[]> _chains;
    private int _count;
    private Integer[] _validTokens;
    
    public RingFinder(int[] tokenSet) {
        _validTokens = new Integer[tokenSet.length];
        for (int i = 0; i < tokenSet.length; i++) {
            _validTokens[i] = tokenSet[i];
        }
    }
    
    public RingFinder(Integer[] tokenSet) {
        _validTokens = new Integer[tokenSet.length];
        System.arraycopy(tokenSet, 0, _validTokens, 0, tokenSet.length);
    }
    
    public List<int[]> getRings() {
        // Creates a statistic set around all sets that add up to 38
        Puzzle38SolverUtil.SetStatistics stats = Puzzle38SolverUtil.sumUp(Arrays.asList(_validTokens), 38);

        // Create a stack of all sets containing three tokens
        // Do to transpose sets into identity sets yet. that comes later
        Stack<List<Integer>> stack = new Stack<List<Integer>>();

        for (List<Integer> row : stats.getRowStats().get(3)) {
            stack.add(row);
        }
        // debug. It just so happens a ring containing
        // 19, 11, 18 is the solution ring
        // stack.push(Arrays.asList(new Integer[]{9,11,18}));
        _count = 0;
        createChain(stack);
        
        /*
        if (_chains != null) {
            for (int[] chain : _chains) {
                System.out.println(Arrays.toString(chain));
            }
        }
        */
        //System.out.println(_count + " iterations");
        return _chains;
    }
    
    
    /*
     * StartingDepth: it's expected that there are multiple chains due to rotation / reflection.
     * Callers wanting complete sets should take returned chains and pop the first element from the
     * stack and process again, guaranteeing all chains are found.
     */
    private void createChain(Stack<List<Integer>> currentStack) {
        // entry method that calls to recursion method
        while (currentStack.size() > 5) {
            List<Integer> row = currentStack.peek();
            // test all iterations of
            //[abc] [acb] & [bac]
            // Wait, what about the other three: [cab], [cba], & [bca]? Those are reflections
            // of the first three. We don't test the reflections.
            for (int i = 0; i < 3; i++) {
                _count++;
                switch(i) {
                case 0:
                    // initial state; nothing happens; here for documentation purposes only
                    break;
                case 1:
                    // transppose b & c
                    transposeValues(row, 1, 2);
                    break;
                case 2:
                    // transpose a & b
                    transposeValues(row, 0, 2);
                    // transpose c & b
                    transposeValues(row, 1, 2);
                }
                getNextLink(true, currentStack, null);
            }
            // and pop it out - this avoids testing rotations
            currentStack.pop();
        }
    }
    
    private boolean valueContainedInSet(int value, Stack<List<Integer>> inSet) {
        Iterator<List<Integer>> it = inSet.iterator();
        while (it.hasNext()) {
            List<Integer> testRow = it.next();
            if (testRow.contains(value)) {
                return true;
            }
        }
        return false;
    }
    
    private void storeCompleteChain(Stack<List<Integer>> chain) {
        if (_chains == null) {
            // We store it as a chain of integers
            _chains = new ArrayList<int[]>();
        }
        
        int[] newChain = new int[12]; // it's a 12 element array
        int i = 0;
        for (List<Integer> row : chain) {
            if (i == 0) {
                // very first token always is put in.
                newChain[i] = row.get(0);
            }
            i++;
            // In all cases, the middle token is always put in
            newChain[i] = row.get(1);
            i++;
            if (i <= 11) {
                // the very last token is a repeat of the first; its' not allowed
                newChain[i] = row.get(2);
            }
        }
        _chains.add(newChain);
    }
    
    private void transposeValues(List<Integer> list, int idx1, int idx2) {
        int hold = list.get(idx1);
        list.set(idx1, list.get(idx2));
        list.set(idx2, hold);
    }
    
    private void getNextLink(boolean first, Stack<List<Integer>> currentStack, Stack<List<Integer>> currentChain) {
        // Begin recursion (input: stack of candidate sets, set to link to, current chain (List of Sets)):
        
        //   - Create a copy of the stack
        Stack<List<Integer>> copy = new Stack<List<Integer>>();
        Iterator<List<Integer>> it = currentStack.iterator(); // I forget if this iterates forwards or backwards with respect to how our stack pops. In theory, it doesn't matter.
        while (it.hasNext()) {
            List<Integer> row = it.next();
            copy.add(new ArrayList<Integer>(row));
            // don't transpose right now, do it in the loop below. otherwise recursive calls to here will
            // re-transpose previous rows making life horrifically long
        }
        
        //   - start loop
        
        while (!copy.isEmpty()) {
            //   - pop stack; get set
            List<Integer> candidateRow = copy.pop();
            
            for (int transposed = 0; transposed < 2; transposed++) {
                _count++;
                //   - if stack is linkable to incoming set (a null incoming set is auto-pass), test
                //     for completeness.
                boolean matches = false;
                if (currentChain == null || currentChain.isEmpty()) {
                    currentChain = currentChain == null ? new Stack<List<Integer>>() : currentChain;
                    // this is good; push-in
                    currentChain.push(candidateRow);
                    matches = true;
                } else {
                    // does the row have a value that matches the last digit for the top row of the current chain?
                    List<Integer> currentRow = currentChain.peek();
                    int lastDigit = currentRow.get(currentRow.size() - 1);

                    int idx = candidateRow.indexOf(lastDigit);

                    // does the candidateRow have the one we're testing have the last digit of the previous row?
                    if (idx != -1) {
                        // it does, put it in front
                        matches = true;
                        if (idx != 0) {
                            transposeValues(candidateRow, 0, idx);
                        }

                        // so this row has the right digit in the front, are the other
                        // two in the current chain set? Yes is the wrong answer
                        // Don't test the first digit, dummy
                        // Dont' do this test when we have 5 knowns, that's a special case
                        for (int i = 1; i < candidateRow.size() && matches && currentChain.size() < 5; i++) {
                            matches = !valueContainedInSet(candidateRow.get(i), currentChain);
                        }
                        

                        if (matches && currentChain.size() == 5) {
                            // not only does the first digit have to match the previous link's last
                            // but the last digit has to match the first link's first
                            // first .get(0) --> the first row List
                            // second .get(0) --> the first digit of that List
                            matches = currentChain.get(0).get(0).equals(candidateRow.get(candidateRow.size() - 1));
                            
                            // one final thing; in case there's duplication of the sets, make sure the middle value
                            // is still unique
                            matches = matches && !valueContainedInSet(candidateRow.get(1), currentChain);
                        }

                        if (matches) {
                            // this is good; push-in
                            currentChain.push(candidateRow);
                        }
                    } else {
                        // it doesn't have a match. We don't need to bother with the transposition loop
                        transposed++;
                    }
                }

                // From here we've matched or we didn't. If we've matched, we've already pushed
                // If the chain is complete, store it and pop the last row 
                // TODO: we can stop at 5 elements since the 6ht is academic
                if (currentChain.size() >= 6) {
                    storeCompleteChain(currentChain);
                    currentChain.pop(); // and get rid of it
                } else if (matches) {
                    // find the next
                    getNextLink(false, currentStack, currentChain);
                    currentChain.pop(); // get rid of it after the search
                }
                // If this is the first entry into getNextLink, we actually want to
                // bail out and not iterate the full stack (avoids duplication)
                if (!first) {
                    transposeValues(candidateRow, 1, 2);
                } else {
                    return; // We found all chains for this top-level row. The outer
                            // must make the next move
                }
            } // end for
        } // end while; stack copy exhausted
    }

}
