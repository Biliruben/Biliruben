package biliruben.games;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import biliruben.games.Puzzle38SolverUtil.SetStatistics;

public class Puzzle38SolverUtil {

    private static final Integer[] TOKEN_SET = 
            new Integer[] {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19};

    private static final Integer TEST_VALUE = 2;
    private static final Integer ROW_LENGTH = 3;
    private static final Integer TARGET_VALUE = 38;

    private static int _count;
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        SetStatistics stats = sumUp(new ArrayList<Integer>(Arrays.asList(TOKEN_SET)), TARGET_VALUE);

        Map<Integer, Set<List<Integer>>> rowStatistics = stats.getRowStats();
        Map<Integer, Set<List<Integer>>> valueStatistics = stats.getValueStats();
        int rowLength = ROW_LENGTH;
        int targetToken = TEST_VALUE;
        System.out.println("All " + rowLength + " element rows (" + rowStatistics.get(rowLength).size() + ") : " + rowStatistics.get(rowLength));
        System.out.println("All rows containing " + targetToken + ": " + valueStatistics.get(targetToken));
        Set<List<Integer>> union = getUnionSet(stats, rowLength, targetToken);
        System.out.println("Union of both sets:");
        for (List<Integer> row : union) {
            System.out.println(row);
        }
        Set<List<Integer>> crossSection = getExclusiveCrossSection(union, targetToken);
        System.out.println("Cross section of union for " + targetToken + ": " + crossSection);
    }
    
    private static Set<List<Integer>> getExclusiveCrossSection(Set<List<Integer>> sourceSet, int commonElement) {
        // find all rows where each token is unique except for the common element
        Set<List<Integer>> crossSection = new HashSet<List<Integer>>();
        Set<List<Integer>> candidateSet = null;
        Set<List<Integer>> srcSetCopy = new HashSet<List<Integer>>(sourceSet);
        while (!srcSetCopy.isEmpty()) {
            boolean hasCommon = false;
            boolean hasUnique = true;
            for (List<Integer> row : srcSetCopy) {
                if (crossSection.size() == 0) {
                    // first check is free
                    hasCommon = true;
                } else {
                    for (Integer fromRow : row) {
                        if (fromRow == commonElement) {
                            hasCommon = true;
                        } else {
                            for (List<Integer> crossSectionRow : crossSection) {
                                if (crossSectionRow.contains(fromRow)) {
                                    hasUnique = false;
                                }
                            }
                        }
                    }
                }
                if (hasCommon && hasUnique) {
                    crossSection.add(row);
                }
                hasCommon = false;
                hasUnique = true;
            }
            if (candidateSet == null || crossSection.size() >= candidateSet.size()) {
                candidateSet = crossSection;
            }
            
            // Pop the first row from the source
            if (!crossSection.isEmpty()) {
                srcSetCopy.remove(crossSection.iterator().next());
            }
            crossSection = new HashSet<List<Integer>>();
        }
        return candidateSet;
    }
    
    private static Set<List<Integer>> getUnionSet(SetStatistics stats, int rowSize, int commonValue) {
        Map<Integer, Set<List<Integer>>> rowStatistics = stats.getRowStats();
        Map<Integer, Set<List<Integer>>> valueStatistics = stats.getValueStats();
        Set<List<Integer>> rowsBySize = rowStatistics.get(rowSize);
        Set<List<Integer>> rowsByValue = valueStatistics.get(commonValue);
        Set<List<Integer>> union = findCommonElements(rowsBySize, rowsByValue);
        return union;
    }
    
    private static <K> Set<K> findCommonElements (Set<K> set1, Set<K> set2) {
        Set<K> commonSet = new HashSet<K>();
        // iterate the smaller one, it'll be quicker
        Set<K> sourceSet = null;
        Set<K> targetSet = null;
        if (set1.size() < set2.size()) {
            sourceSet = new HashSet<K>(set1);
            targetSet = new HashSet<K>(set2);
        } else {
            sourceSet = new HashSet<K>(set2);
            targetSet = new HashSet<K>(set1);
        }
        for (K srcToken : sourceSet) {
            boolean found = targetSet.remove(srcToken);
            if (found) {
                commonSet.add(srcToken);
            }
        }
        return commonSet;
    }
    
    private static void catalogRow(SetStatistics stats, List<Integer> row) {
        // catalog the row size
        
        Map<Integer, Set<List<Integer>>> rowStatistics = stats.getRowStats();
        Set<List<Integer>> rowsFromSize = rowStatistics.get(row.size());
        if (rowsFromSize == null) {
            rowsFromSize = new HashSet<List<Integer>>();
            rowStatistics.put(row.size(), rowsFromSize);
        }
        rowsFromSize.add(row);
        
        Map<Integer, Set<List<Integer>>> valueStatistics = stats.getValueStats();
        // and the members of the row
        for (int token : row) {
            Set<List<Integer>> rowsFromValues = valueStatistics.get(token);
            if (rowsFromValues == null) {
                rowsFromValues = new HashSet<List<Integer>>();
                valueStatistics.put(token, rowsFromValues);
            }
            rowsFromValues.add(row);
        }
    }
    
    public static class SetStatistics {
        
        private Map<Integer, Set<List<Integer>>> _valueStatistics;
        private Map<Integer, Set<List<Integer>>> _rowStatistics;

        private SetStatistics(Map<Integer, Set<List<Integer>>> valueStatistics, Map<Integer, Set<List<Integer>>> rowStatistics) {
            this._valueStatistics = valueStatistics;
            this._rowStatistics = rowStatistics;
        }
        
        private SetStatistics() {
            this._valueStatistics = new HashMap<Integer, Set<List<Integer>>>();
            this._rowStatistics = new HashMap<Integer, Set<List<Integer>>>();
        }
        
        private void setValueStats(Map<Integer, Set<List<Integer>>> valueStats) {
            this._valueStatistics = valueStats;
        }
        
        /**
         * A Map of all sets indexed by included values. Eg getValueStats().get(3)
         * will return all sets that include '3'
         * @return
         */
        public Map<Integer, Set<List<Integer>>> getValueStats() {
            return this._valueStatistics;
        }
        
        private void setRowStats(Map<Integer, Set<List<Integer>>> rowStats) {
            this._rowStatistics = rowStats;
        }
        
        /**
         * A Map of all sets indexed by size. Eg getRowStats().get(3) will return
         * all sets that are exactly three elements
         * @return
         */
        public Map<Integer, Set<List<Integer>>> getRowStats() {
            return _rowStatistics;
        }
        
        /**
         * Returns all sets matching the size and containing the indicated value
         * @param rowSize
         * @param value
         * @return
         */
        public Set<List<Integer>> getCrossReference(int rowSize, int value) {
            Set<List<Integer>> rows = _valueStatistics.get(value);
            Set<List<Integer>> crossSet = new HashSet<List<Integer>>();
            for (List<Integer>row : rows) {
                if (row.size() == 3) {
                    crossSet.add(row);
                }
            }
            return crossSet;
        }
    }
    
    public static SetStatistics sumUp(List<Integer> numbers, int target) {
        _count = 0;
        SetStatistics stats = new SetStatistics();
        sumUpRecursive(stats, numbers, target, new ArrayList<Integer>());
        //System.out.println(_count + " iterations to calculate statistics");
        return stats;
    }

    public static void sumUpRecursive(SetStatistics stats, List<Integer> numbers, int target, List<Integer> partial) {
        _count++;
        int s = 0;
        for (int x : partial) {
            s += x;
        }
        if (s == target) {
             //System.out.println("sum("+Arrays.toString(partial.toArray())+")="+target);
             catalogRow(stats, partial);
        }
        if (s >= target) {
             return;
        }
        for(int i=0;i<numbers.size();i++) {
              ArrayList<Integer> remaining = new ArrayList<Integer>();
              int n = numbers.get(i);
              for (int j=i+1; j<numbers.size();j++) {
                  remaining.add(numbers.get(j));
              }
              ArrayList<Integer> partial_rec = new ArrayList<Integer>(partial);
              partial_rec.add(n);
              sumUpRecursive(stats, remaining,target,partial_rec);
        }
     }
}
