package biliruben.games.dice.golong;

import java.util.ArrayList;
import java.util.List;

import com.biliruben.util.GetOpts;

public class GoLong {
    public static final String TOUCHDOWN = "Touchdown";
    public static final String OFFSETTING = "Offsetting";
    public static final String TOUCHBACK = "Touchback";
    public static final String PASS_INTERFERANCE = "Pass Inteferance";
    public static final String OFFENSE = "Offense";
    public static final String DEFENSE = "Defense";
    public static final String FIELD_GOAL = "Field Goal";
    public static final String BLOCKED = "Blocked";
    public static final String LOSS_OF_POSSESSION = "Loss of Possession";
    public static final String SACKED = "Sacked";
    public static final String DOWN = "Down";
    public static final String PENALTY = "Penalty";

    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }
    
    
    private static void init(GetOpts opts) {
      
    }
    
    
    private void doRound() {
        
        DiceCup cup = new DiceCup();

        // begin loop
        cup.rollAllDice();
        if (cup.isPenalty()) {
            PenaltyDice penalty = new PenaltyDice();
            penalty.rollDie();
            if (penalty.isOffsetting()) {
                // no op; continue loop
            } //else
            PossessionDice poss = new PossessionDice()s;
            poss.rollDie();
            if (poss.isDefense()) {
                // ask current player to accept/decline
            } else {
                // it's on the offense; ask defenseive player
            }
            
        }
        if (cup.getBigPlayDie().isTouchdown()) {
            if (cup.getQBDie().isLossOfPossesion()) {
                // pick six
            } else {
                // touchdown
            }
        }
        
            // roll dice
        
            // player decides which to play; remove unplayed dice
        
            // does the series continue? continue loop
        // end loop
    }


}
