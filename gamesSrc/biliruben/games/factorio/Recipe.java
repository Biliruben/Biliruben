package biliruben.games.factorio;

import java.util.Arrays;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Recipe {
    
    private String _name;
    private Ingredient[] _ingredients;
    /**
     * @return the ingredients
     */
    public Ingredient[] getIngredients() {
        return _ingredients;
    }
    /**
     * @param ingredients the ingredients to set
     */
    public void setIngredients(Ingredient[] ingredients) {
        _ingredients = ingredients;
    }
    /**
     * @return the name
     */
    public String getName() {
        return _name;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        _name = name;
    }
    
    
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        buff.append(getName()).append(" :: ").append(Arrays.toString(getIngredients()));
        return buff.toString();
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) { return false; }
        if (obj instanceof Recipe) {
            Recipe recipeObj = (Recipe)obj;
            boolean equals = this.getName() != null ? this.getName().equals(recipeObj.getName()) : recipeObj.getName() != null;
            // still equal? test ingredients
            if (equals) {
                return Arrays.equals(this.getIngredients(), recipeObj.getIngredients());
            } else {
                // false
                return equals;
            }
        } else {
            // no sub classes right now, so definitely false
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        int hash = getName() != null ? getName().hashCode() : 0;
        hash += getIngredients() != null ? Arrays.hashCode(getIngredients()) * 3 : 0;
        return hash;
    }
}
