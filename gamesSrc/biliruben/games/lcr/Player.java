package biliruben.games.lcr;

public class Player implements Comparable<Player>{
    
    private int _tokens;
    private String _name;
    
    public Player(String name, int tokens) {
        this._name = name;
        this._tokens = tokens;
    }
    
    public int getTokens() {
        return _tokens;
    }
    
    public void addTokens(int tokens) {
        _tokens += tokens;
    }
    
    public String toString() {
        return _name + " " + _tokens + " tokens";
    }
    
    public String getName() {
        return _name;
    }

    @Override
    public int compareTo(Player otherPlayer) {
        if (otherPlayer == null) {
            return 1;
        } else {
            return ((Integer)getTokens()).compareTo(otherPlayer.getTokens());
        }
    }

}
