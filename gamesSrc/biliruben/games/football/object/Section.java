package biliruben.games.football.object;

// TODO: getArticle(int 1-based pos)
// TODO: toString()
public class Section extends AbstractRuleElement<Article> {
    
    private int _number;

    public Section(int num, String title) {
        super(title);
        this._number = num;
    }
    
    public int getNumber() {
        return this._number;
    }
    
    //Section 2. The Field
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        buff.append("\nSection ").append(_number).append(". ").append(getTitle()).append("\n");
        return buff.toString();
    }
    
    public Article getArticle(int articleNumber) {
        for (Article article : getEntries()) {
            if (article.getNumber() == articleNumber) {
                return article;
            }
        }
        return null;
    }

    @Override
    public Section copy() {
        Section copy = new Section(_number, getTitle());
        return copy;
    }
    
    @Override
    public boolean equals(Object obj) {
        boolean matches = super.equals(obj);
        if (matches) {
            Section you = (Section)obj;
            matches = you.getNumber() == _number;
        }
        return matches;
    }
    
    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash += _number * 7;
        return hash;
    }
}
