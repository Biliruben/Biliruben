package biliruben.games.factorio;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;

public class RecipeDiffer {

    private Map<String, Recipe> _newRecipes;
    private Map<String, Recipe> _oldRecipes;

    public RecipeDiffer(String recipeJsonOld, String recipeJsonNew) throws JsonProcessingException, IOException {
        this._oldRecipes = readJsonData(recipeJsonOld);
        this._newRecipes = readJsonData(recipeJsonNew);
    }
    
    private Map<String, Recipe> readJsonData(String jsonFile) throws JsonProcessingException, IOException {
        ObjectMapper mappy = new ObjectMapper();
        JsonNode jsonNode = mappy.readTree(new File(jsonFile));
        JsonNode recipeNodes = jsonNode != null ? jsonNode.get("recipes") : null ;
        Map<String, Recipe> recipes = new HashMap<String, Recipe>();
        if (recipeNodes != null) {
            for (JsonNode recipeNode : recipeNodes) {
                Recipe r = mappy.readValue(recipeNode, Recipe.class);
                if (r != null) { // would it?
                    recipes.put(r.getName(), r);
                }
            }
        }
        return recipes;
    }
    
    public List<String> findDeltas(boolean includeAdded, boolean includeRemoved) {
        // iterate the old set and compare to new; remove from each as we go
        // whatever is left in each map is the added/removed, depending on the map
        // !! Be careful and avoid ConcurrentMod
        // Use copies as to not busticate the original maps
        Map<String, Recipe> newRecipes = new HashMap<String, Recipe>(_newRecipes);
        Map<String, Recipe> oldRecipes = new HashMap<String, Recipe>(_oldRecipes);
        List<String> deltas = new ArrayList<String>();
        Iterator<Entry<String, Recipe>> oldEntryIterator = oldRecipes.entrySet().iterator();
        while (oldEntryIterator.hasNext()) {
            Entry<String, Recipe> oldEntry = oldEntryIterator.next();
            Recipe oldRecipe = oldEntry.getValue();
            String recipeName = oldEntry.getKey();
            Recipe newRecipe = newRecipes.get(recipeName);
            if (newRecipe != null && !oldRecipe.equals(newRecipe)) {
                deltas.add(recipeName);
            }
            oldEntryIterator.remove();
            newRecipes.remove(recipeName);
        }
        
        // Whatever's left in either map is old/new
        if (includeAdded && !newRecipes.isEmpty()) {
            deltas.addAll(newRecipes.keySet());
        }
        
        if (includeRemoved && !oldRecipes.isEmpty()) {
            deltas.addAll(oldRecipes.keySet());
        }
        
        return deltas;
    }
    
    public Map<String, Recipe> getOldRecipes() {
        return this._oldRecipes;
    }
    
    public Map<String, Recipe> getNewRecipes() {
        return this._newRecipes;
    }
    
    public Recipe getOldRecipe(String recipeName) {
        return this._oldRecipes.get(recipeName);
    }
    
    public Recipe getNewRecipe(String recipeName) {
        return this._newRecipes.get(recipeName);
    }


    public static void main(String[] args) throws JsonProcessingException, IOException {
        String newJsons = "C:\\GITRoot\\factoriocalc\\data\\vanilla-0.17.1.json";
        String oldJsons = "C:\\GITRoot\\factoriocalc\\data\\vanilla-0.16.51.json";
        
        RecipeDiffer diffy = new RecipeDiffer(oldJsons, newJsons);
        List<String> diffs = diffy.findDeltas(false, false);
        for (String diff : diffs) {
            System.out.println(diff);
        }
        

    }

}
