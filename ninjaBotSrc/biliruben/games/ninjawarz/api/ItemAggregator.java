package biliruben.games.ninjawarz.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import biliruben.games.ninjawarz.object.Item;

/**
 * Functor used to create a unique set of items and aggregate their values
 * @author trey.kirk
 *
 */
public class ItemAggregator {
    private Map<Item, Integer> _itemMap;
    private List<Item> _items; // use this to mantain the order

    private ItemAggregator() {
        _itemMap = new HashMap<Item, Integer>();
        _items = new LinkedList<Item>();
    }
    
    public ItemAggregator(Collection<Item> items) {
        this();
        aggregateItems(items);
    }
    
    public ItemAggregator(Item[] items) {
        this();
        aggregateItems(Arrays.asList(items));
    }
    
    private void aggregateItems(Collection<Item> items) {
        for (Item item : items) {
            Integer currentValue = _itemMap.get(item);
            if (currentValue == null) {
                currentValue = 0;
                _items.add(item);
            }
            currentValue++;
            _itemMap.put(item, currentValue);  // dealing with primitives, so re-put
        }
    }
    
    public int getCount (Item forItem) {
        return _itemMap.get(forItem);
    }
    
    public Collection<Item> getUniqueItems() {
        // order it
        return new ArrayList<Item>(_items);
    }
}
