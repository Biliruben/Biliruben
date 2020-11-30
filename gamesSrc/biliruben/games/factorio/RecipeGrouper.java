package biliruben.games.factorio;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionLegend;

/**
 * Given a JSON of factorio data, pull the elements from the 'recipes' array and group them
 * together based on common target ingredients, from least common to most common. This will
 * help decide in what order to mfg each item.
 * @author trey.kirk
 *
 */

public class RecipeGrouper {
    
    private static class EntrySetComparator implements Comparator<Entry<String, List<Recipe>>> {
        
        private int _reverse;

        private EntrySetComparator(boolean reverse) {
            _reverse = reverse ? 1 : -1;
        }

        @Override
        public int compare(Entry<String, List<Recipe>> entry1, Entry<String, List<Recipe>> entry2) {
            if (entry1 == null && entry2 != null) {
                return -1 * this._reverse;
            } else if (entry1 != null && entry2 == null) {
                return 1 * this._reverse;
            } else if (entry1 == null && entry2 == null) {
                return 0;
            }
            // else neither are null
            List<Recipe> value1 = entry1.getValue();
            List<Recipe> value2 = entry2.getValue();
            Integer e1Size = value1 != null ? value1.size() : 0;
            Integer e2Size = value2 != null ? value2.size() : 0;

            if (e1Size.equals(e2Size)) {
                return ((String)entry1.getKey()).compareTo((String)entry2.getKey()) * this._reverse;
            } else {
                return e1Size.compareTo(e2Size) * this._reverse;
            }
        }
    }

    private static final String[] MAIN_BUS_INGREDIENTS = {
            "iron-plate",
            "copper-plate",
            "steel-plate",
            "advanced-circuit",
            "electronic-circuit",
            "battery",
            "plastic-bar",
            "lubricant",
            "stone",
            "stone-brick",
            "electric-engine-unit",
            "engine-unit",
            "iron-stick"
    };

    // The things we want to build
    private static final String[] INCLUDED_RECIPES = {
            "construction-robot",
            "logistic-robot",
            "accumulator",
            "electric-engine-unit",
            "locomotive",
            "pump",
            "boiler",
            "landfill",
            "rail",
            "electric-furnace",
            "gate",
            "oil-refinery",
            "stone-wall",
            "express-loader",
            "express-splitter",
            "express-transport-belt",
            "express-underground-belt",
            "assembling-machine-3",
            "beacon",
            "logistic-chest-active-provider",
            "logistic-chest-buffer",
            "logistic-chest-passive-provider",
            "logistic-chest-requester",
            "logistic-chest-storage",
            "roboport",
            "solar-panel-equipment",
            "stack-filter-inserter",
            "stack-inserter",
            "substation",
            "arithmetic-combinator",
            "big-electric-pole",
            "constant-combinator",
            "decider-combinator",
            "green-wire",
            "heat-exchanger",
            "heat-pipe",
            "medium-electric-pole",
            "power-switch",
            "red-wire",
            "solar-panel",
            "steam-turbine",
            "cargo-wagon",
            "chemical-plant",
            "empty-barrel",
            "engine-unit",
            "fluid-wagon",
            "pumpjack",
            "steel-chest",
            "storage-tank",
            "train-stop",
            "electric-mining-drill",
            "fast-inserter",
            "iron-gear-wheel",
            "lab",
            "long-handed-inserter",
            "offshore-pump",
            "pipe",
            "pipe-to-ground",
            "radar",
            "rail-chain-signal",
            "rail-signal",
            "repair-pack",
            "small-lamp",
            "steam-engine"
    };
    
    private static final String JSON_SRC_DIR = "C:\\GITRoot\\factoriocalc\\data";
    private static final String DEFAULT_JSON_FILE = "vanilla-0.16.51.json";

    private static GetOpts _opts;
    
    private String _jsonFile;
    private List<Recipe> _recipes;

    private String[] _mainBusIngredients = MAIN_BUS_INGREDIENTS;

    private ArrayList<String> _orderedRecipeBaseComponents;

    private String[] _includedRecipes;

    private boolean _reverseSort;

    public RecipeGrouper() throws JsonProcessingException, IOException {
        this(DEFAULT_JSON_FILE);
    }
    
    public RecipeGrouper(String fileName) throws JsonProcessingException, IOException {
        this(JSON_SRC_DIR, fileName);
    }

    public RecipeGrouper(String jsonSrcDir, String fileName) throws JsonProcessingException, IOException {
        if (jsonSrcDir != null) {
            this._jsonFile = jsonSrcDir + File.separator + fileName;
        } else {
            this._jsonFile = fileName;
        }
        readJsonData();
        _includedRecipes = Arrays.copyOf(INCLUDED_RECIPES, INCLUDED_RECIPES.length);
        Arrays.sort(_includedRecipes);
        Arrays.sort(_mainBusIngredients);
        setOrdering(true);
    }
    
    public void setOrdering(boolean reverse) {
        this._reverseSort = reverse;
    }
    
    private void readJsonData() throws JsonProcessingException, IOException {
        ObjectMapper mappy = new ObjectMapper();
        JsonNode jsonNode = mappy.readTree(new File(_jsonFile));
        JsonNode recipeNodes = jsonNode != null ? jsonNode.get("recipes") : null ;
        _recipes = new ArrayList<Recipe>();
        if (recipeNodes != null) {
            for (JsonNode recipeNode : recipeNodes) {
                Recipe r = mappy.readValue(recipeNode, Recipe.class);
                if (r != null) { // would it?
                    _recipes.add(r);
                }
            }
        }
    }
    
    private boolean isIncluded(Recipe recipe) {
        return isIncluded(recipe.getName());
    }
    
    private boolean isIncluded(String recipeName) {
        return Arrays.binarySearch(_includedRecipes, recipeName) < 0;
    }
    
    private Recipe getRecipe(String forName) {
        for (Recipe recipe : this._recipes) {
            if (recipe.getName().equals(forName)) {
                return recipe;
            }
        }
        return null;
    }
    
    private static void addIngredientRecipe(String ingredient, Recipe recipe, Map<String, List<Recipe>> map) {
        List<Recipe> contributingRecipes = map.get(ingredient);
        if (contributingRecipes == null) {
            contributingRecipes = new ArrayList<Recipe>();
            map.put(ingredient, contributingRecipes);
        }
        contributingRecipes.add(recipe);
    }
    
    private Set<String> getBusIngredients(Recipe forRecipe) {
        Ingredient[] ingredients = forRecipe.getIngredients();
        Set<String> ingredientSet = new HashSet<String>();
        for (int i = 0; i < ingredients.length; i++) {
            String ingredientName = ingredients[i].getName();
            if (Arrays.binarySearch(_mainBusIngredients, ingredientName) >= 0) {
                ingredientSet.add(ingredientName);
            } else {
                // not found; Find the recipe for the ingredient and recurse
                Recipe ingredientRecipe = getRecipe(ingredientName);
                if (ingredientRecipe == null) {
                    // No recipe for that!
                    // dunno what to do about that yet, jsut complain
                    System.out.println(forRecipe + ":: No recipe found for: " + ingredientName);
                } else {
                    ingredientSet.addAll(getBusIngredients(ingredientRecipe));
                }
            }
        }
        
        return ingredientSet;
    }
    
    public Map<String, List<Recipe>> groupRecipes() {
        // Build a map of ingredient-recpies for each of the specified bus ingredients
        // I did originally say, "group by least common", but I don't have to be that complicated. Just
        // report the map in order of smallest value-set to largest. To eliminate duplicate values, I could remove
        // dupe values after determining order.
        
        Map<String, List<Recipe>> groupedRecipes = new HashMap<String, List<Recipe>>();
        for (Recipe recipe : this._recipes) {
            // System.out.println(recipe);
            if (!isIncluded(recipe)) {
                Set<String> ingredients = getBusIngredients(recipe);
                for (String ingredient : ingredients) {
                    addIngredientRecipe(ingredient, recipe, groupedRecipes);
                }
            }
        }
        
        // Need to sort the entry-set
        Set<Entry<String, List<Recipe>>> entrySet = groupedRecipes.entrySet();
        SortedSet<Entry<String, List<Recipe>>> sorted = new TreeSet<Entry<String, List<Recipe>>>(new EntrySetComparator(this._reverseSort));
        sorted.addAll(entrySet);
        
        _orderedRecipeBaseComponents = new ArrayList<String>();
        for (Entry<String, List<Recipe>>entry : sorted) {
            _orderedRecipeBaseComponents.add(entry.getKey());
            // get the value list and remove it from each other list in the map
            // this is a lot of work for a minor pay-off; defer for now
        }
        
        return groupedRecipes;
    }
    
    public List<String> getOrderedKeys() {
        return this._orderedRecipeBaseComponents;
    }
    
    public static void main(String[] args) throws JsonProcessingException, IOException {
        init(args);
        boolean reverse = Boolean.valueOf(_opts.getStr("reverse"));
        String jsonFile = _opts.getStr("recipe");
        RecipeGrouper g = null;
        if (jsonFile != null) {
            g = new RecipeGrouper(null, jsonFile);
        }
        g.setOrdering(!reverse);
        Map<String, List<Recipe>> recipes = g.groupRecipes();
        /*
        for (String key : recipes.keySet()) {
            System.out.println(key + "\t:" + recipes.get(key));
        }
        */
        Set<Recipe> usedRecipes = new HashSet<Recipe>();
        // It'd be nice if I could implement a map that naturally orders the way I want it instead of having to leverage a
        // separate List of keys
        List<String> keys = g.getOrderedKeys();
        System.out.println("------------------------------------------------------------------------");
        for (String key : keys) {
            StringBuilder buff = new StringBuilder();
            buff.append(key).append(" :\n");
            List<Recipe> recipeList = recipes.get(key);
            boolean deleteBuff = false;
            for (Recipe recipe : recipeList) {
                if (usedRecipes.add(recipe)) {
                    //buff.append(recipe.getName()).append(" :: ");
                    buff.append("\t\t").append(recipe.getName()).append("\n");
                }
            }
            System.out.println(buff.toString());
        }
    }
    
    
    private static void init(String[] args) {
        _opts = new GetOpts(RecipeGrouper.class);
        
        OptionLegend legend = new OptionLegend("recipe", "Recipe JSON file");
        legend.setRequired(false);
        _opts.addLegend(legend);
        
        legend = new OptionLegend("reverse", "Sorts in reverse order, showing the base materials that are used most first");
        legend.setRequired(false);
        legend.setFlag(true);
        _opts.addLegend(legend);
        
        _opts.parseOpts(args);
    }

}