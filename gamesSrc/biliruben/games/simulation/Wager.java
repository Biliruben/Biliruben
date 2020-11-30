package biliruben.games.simulation;

import java.util.Arrays;

public abstract class Wager {
    
    private int _interests;
    private boolean _boxed;
    private double _payout;

    Wager(int interests, boolean boxed, double payout) {
        this._interests =  interests;
        this._boxed = boxed;
        this._payout = payout;
    }
    
    public double getPayout() {
        return _payout;
    }
    
    public double getWagingFactor() {
        // unboxed, it's the number of interests
        // boxed, it's the factorial of interests
        if (!_boxed) {
            return 1;
        } else {
            double val = 1.0;
            for (int i = 0; i < _interests; i++) {
                val = (_interests - i) * val;
            }
            return val;
        }
    }
    
    public int getInterests() {
        return this._interests;
    }
    
    public abstract boolean isWin(Integer[] results, Integer[] interests);
    
    private boolean innerIsWin(Integer[] results, Integer[] interests) {
        // if interestes longer than results, fuck off
        if (interests.length > results.length) return false;
        Integer[] intWorking = Arrays.copyOf(interests, interests.length);
        Integer[] resWorking = Arrays.copyOfRange(results, 0, intWorking.length);
        if (this._boxed) {
            Arrays.sort(intWorking);
            Arrays.sort(resWorking);
        }
        boolean win = Arrays.equals(intWorking, resWorking);
        return win;
    }
    
    public static class Win extends Wager {
        
        public Win() {
            super(1, false, 2.0);
        }

        @Override
        public boolean isWin(Integer[] results, Integer[] interests) {
            return results[0] == interests [0];
        }
    }
    
    public static class EXB extends Wager {
        public EXB() {
            super(2, true, 10.0);
        }
        
        @Override
        public boolean isWin(Integer[] results, Integer[] interests) {
            return super.innerIsWin(results, interests);
        }
    }
    
    public static class PEB extends Wager {
        public PEB() {
            super(4, true, 1000);
        }
        
        @Override
        public boolean isWin(Integer[] results, Integer[] interests) {
            return super.innerIsWin(results, interests);
        }
    }
    
    public static class Perfecta extends Wager {
        public Perfecta() {
            super(4, false, 1000);
        }
        
        @Override
        public boolean isWin(Integer[] results, Integer[] interests) {
            return super.innerIsWin(results, interests);
        }
    }
    
    public static class TRB extends Wager {
        public TRB() {
            super(3, true, 20);
        }
        
        @Override
        public boolean isWin(Integer[] results, Integer[] interests) {
            return super.innerIsWin(results, interests);
        }
    }
    
    public static class Trifecta extends Wager {
        public Trifecta() {
            super(3, false, 20);
        }
        
        @Override
        public boolean isWin(Integer[] results, Integer[] interests) {
            return super.innerIsWin(results, interests);
        }
    }
    
    public static class Exacta extends Wager {
        public Exacta() {
            super(2, false, 10.0);
        }

        @Override
        public boolean isWin(Integer[] results, Integer[] interests) {
            return super.innerIsWin(results, interests);
        }
    }
    
    public static class Show extends Wager {
        
        public Show() {
            super(1, false, 1.5);
        }
        
        @Override
        public boolean isWin(Integer[] results, Integer[] interests) {
            int horse = interests[0];
            boolean isWin = results[0] == horse;
            if (!isWin && results.length > 1) {
                isWin = results[1] == horse;
            }
            if (!isWin && results.length > 2) {
                isWin = results[2] == horse;
            }
            return isWin;
        }
    }
    
    public static class Place extends Wager {
        
        public Place() {
            super(1, false, 1.75);
        }
        
        @Override
        public boolean isWin(Integer[] results, Integer[] interests) {
            int horse = interests[0];
            boolean isWin = results[0] == horse;
            if (!isWin && results.length > 1) {
                isWin = results[1] == horse;
            }
            return isWin;
        }
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " (" + getPayout() + ")";
    }
}