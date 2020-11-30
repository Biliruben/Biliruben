package biliruben.games.dice.golong;

import java.util.List;

public class DiceCup {
    
    protected static final int DEFAULT_DOWN_DICE = 3;
    
    private QBDice _qbDice;
    private BigPlayDice _bigPlayDice;
    private PenaltyDice _penaltyDice;
    private List<DownDice> _downDice;
    
    public DiceCup() {
        this(DEFAULT_DOWN_DICE);
    }
    
    public DiceCup(int downDice) {
        _qbDice = new QBDice();
        _bigPlayDice = new BigPlayDice();
        _penaltyDice = new PenaltyDice();
        for (int i = 0; i < downDice; i++) {
            _downDice.add(new DownDice());
        }
    }
    
    public void rollAllDice() {
        _qbDice.rollDie();
        if (_bigPlayDice != null) {
            _bigPlayDice.rollDie();
        }
        for (DownDice downDie : _downDice) {
            if (downDie != null) {
                downDie.rollDie();
            }
        }
        _penaltyDice.rollDie();
    }
    
    public List<DownDice> getDownDice() {
        return _downDice;
    }
    
    public QBDice getQBDie() {
        return _qbDice;
    }
    
    public BigPlayDice getBigPlayDie() {
        return _bigPlayDice;
    }
    
    public DownDice playDownDie(int downDieIndex) {
        DownDice downDie = _downDice.get(downDieIndex);
        _downDice.remove(downDieIndex);
        return downDie;
    }
    
    public BigPlayDice playBigPlayDie() {
        BigPlayDice bigPlayDice = _bigPlayDice;
        _bigPlayDice = null;
        return bigPlayDice;
    }
    
    
    public boolean isSacked() {
        return _qbDice.isSacked();
    }
    
    public int getSackDistance() {
        return _penaltyDice.getDistance();
    }
    
    public boolean isTurnover() {
        if (_qbDice.isLossOfPossesion()) {
            PossessionDice p = new PossessionDice();
            p.rollDie();
            // true of the defense recovered; false otherwise
            return p.isDefense();
        } else {
            // wasn't a loss of possession
            return false;
        }
    }
    
    public boolean isPenalty() {
        return _qbDice.isPenalty();
    }
}
