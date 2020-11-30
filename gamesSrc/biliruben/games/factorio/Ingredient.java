package biliruben.games.factorio;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Ingredient {
    
    private int _amount;
    private String _name;
    /**
     * @return the _name
     */
    public String getName() {
        return _name;
    }
    /**
     * @param _name the _name to set
     */
    public void setName(String _name) {
        this._name = _name;
    }
    /**
     * @return the amount
     */
    public int getAmount() {
        return _amount;
    }
    /**
     * @param amount the amount to set
     */
    public void setAmount(int amount) {
        _amount = amount;
    }
    
    
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        buff.append(getName()).append("(").append(getAmount()).append(")");
        return buff.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        
        if (obj instanceof Ingredient) {
            Ingredient ingObj = (Ingredient)obj;
            return this.getName().equals(ingObj.getName()) && this.getAmount() == ingObj.getAmount();
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        int hash = this.getName() != null ? this.getName().hashCode() : 0;
        hash += this.getAmount() * 3;
        return hash;
    }
}
