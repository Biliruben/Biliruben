package biliruben.games.cards.object;

public class UnoCArd extends Card {

    public enum Color {
        Green,
        Blue,
        Yellow,
        Red,
        Black;
    }
    
    public enum Rank {
        Zero(0),
        One(1),
        Two(2),
        Three(3),
        Four(4),
        Five(5),
        Six(6),
        Seven(7),
        Eight(8),
        Nine(9);
        
        private int _value;

        Rank (int value) {
            _value = value;
        }
        
        public int getValue() {
            return _value;
        }
    }
    
    
    
    @Override
    public int compareTo(Card yours) {
        // TODO Auto-generated method stub
        return 0;
    }

}
