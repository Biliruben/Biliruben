package biliruben.games.lcr;

import java.util.HashMap;
import java.util.Map;

import biliruben.games.lcr.Die.Side;

public class LCRGame {
    
    private Player[] _players;
    private Die _die;
    private int _currentPlayer;
    
    private static final int PLAYERS = 4;
    private static final int INIT_TOKENS = 10;
    private static final int DIE_ROLL_LIMIT = 5;
    
    private static final String[] NAMES = {
        "Billy",
        "Tes",
        "Albert",
        "Sally",
        "Bill",
        "Jessica",
        "Abe",
        "X"
    };

    public LCRGame(int players, int startingTokens) {
        initPlayers(players, startingTokens);
        _die = new Die();
    }
    
    public String getStandings() {
        StringBuilder buff = new StringBuilder();
        for (int i = 0; i < _players.length; i++) {
            buff.append(_players[i]).append("\n");
        }
        buff.append("-------------\n");
        return buff.toString();

    }
    
    private void initPlayers(int players, int startingTokens) {
        _players = new Player[players];
        for (int i = 0; i < players; i++) {
            Player p = new Player(NAMES[i], startingTokens);
            _players[i] = p;
        }
        _currentPlayer = 0;
    }
    
    public Side[] rollNextPlayer() {
        Side[] results = rollDice();
        _currentPlayer++;
        if (_currentPlayer >= _players.length) {
            _currentPlayer = 0;
        }
        return results;
    }
    
    public Side[] rollDice() {
        Player p = _players[_currentPlayer];
        int rolls = DIE_ROLL_LIMIT;
        if (p.getTokens() < rolls) {
            rolls = p.getTokens();
        }
        Side[] results = new Side[rolls];
        for (int roll = 0; roll < rolls; roll++) {
            Side result = _die.roll();
            results[roll] = result;
            giveToken(result);
        }
        return results;
    }
    
    private void giveToken(Side forSide) {
        if (forSide == Side.D) {
            return;
        }
        Player fp = _players[_currentPlayer];
        Player targetPlayer = null;
        if (forSide == Side.L) {
            int nextPlayer = _currentPlayer - 1;
            if (nextPlayer < 0) {
                nextPlayer = _players.length - 1;
            }
            targetPlayer = _players[nextPlayer];
        } else if (forSide == Side.R) {
            int nextPlayer = _currentPlayer + 1;
            if (nextPlayer >= _players.length) {
                nextPlayer = 0;
            }
            targetPlayer = _players[nextPlayer];
        }
        if (targetPlayer != null) {
            targetPlayer.addTokens(1);
        }
        fp.addTokens(-1);
    }
    
    public Player getWinner() {
        Player winner = null;
        for (int i = 0; i < _players.length; i++) {
            if (_players[i].getTokens() > 0) {
                if (winner == null) {
                    winner = _players[i];
                } else {
                    // more than one with tokens; no winner
                    return null;
                }
            }
        }
        return winner;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        
        Map<String, Integer> wins = new HashMap<String, Integer>();
        int games = 5000000;
        for (int i = 0; i < games; i++) {
            LCRGame g = new LCRGame(PLAYERS, INIT_TOKENS);
            while (g.getWinner() == null) {
                //System.out.println("Player: " + g.getCurrentPlayer());
                Side[] results = g.rollNextPlayer();
                /*
                for (Side side : results) {
                    System.out.print(side + " ");
                }
                System.out.print("\n");
                System.out.println(g.getStandings());
                */
            }
            String winner = g.getWinner().getName();
            //System.out.println(winner);
            Integer winTotal = wins.get(winner);
            if (winTotal == null) {
                winTotal = 1;
            } else {
                winTotal += 1;
            }
            wins.put(winner, winTotal);
        }
        System.out.println(wins);
    }

    public Player getCurrentPlayer() {
        return _players[_currentPlayer];
    }

}
