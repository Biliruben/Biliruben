package biliruben.games.cards.object;

import java.util.HashSet;
import java.util.Set;

public abstract class Card implements Comparable<Card> {

    private Set<Comparable> _elements;

    public Card (Comparable... elements) {
        _elements = new HashSet<Comparable>();
        for (Comparable element : elements) {
            _elements.add(element);
        }
    }
    public Set<Comparable> getElements() {
        return _elements; // do I need to make it defensive?
    }

    public void setElements(Set<Comparable> elements) {
        _elements = elements;
    }

}
