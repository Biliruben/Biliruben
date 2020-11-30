package biliruben.games.football.object;


// TODO: roman numeral formatter
// TODO: getEntry(int 1-based pos) / getEntry (String romanNumeral)
// TODO: toString()
public class Article extends AbstractRuleElement<Interpretation> {

    private String _reference;
    // This will get rendered with Roman Numerals, but their element pos identifies them
    private int _number;

    public Article(int number, String title, String reference) {
        super(title);
        this._reference = reference;
        this._number = number;
    }
    
    public int getNumber() {
        return _number;
    }

    public String getReference() {
        return this._reference;
    }
    
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        buff.append("\n").append(getTitle()).append("—ARTICLE ").append(_number).append("\n").append("Approved Ruling ").append(_reference).append("\n");
        return buff.toString();
    }
    
    public Interpretation getInterpretation (String numeral) {
        for (Interpretation interpreptation : getEntries()) {
            if (interpreptation.getNumeral().equalsIgnoreCase(numeral)) {
                return interpreptation;
            }
        }
        return null;
    }

    @Override
    public Article copy() {
        Article copy = new Article(_number, getTitle(), _reference);
        return copy;
    }
    
    @Override
    public boolean equals(Object obj) {
        boolean equals = super.equals(obj);
        if (equals) {
            Article you = (Article)obj;
            equals = you._number == this._number &&
                you._reference.equals(this._reference);
        }

        return equals;
    }
    
    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash += _number * 7;
        hash += _reference.hashCode() * 5;
        return hash;
    }
}
