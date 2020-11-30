package biliruben.games.ninjawarz.command;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;
import biliruben.games.ninjawarz.object.Clan;
import biliruben.games.ninjawarz.object.Item;
import biliruben.games.ninjawarz.object.Ninja;

public class FireSaleCommand extends NinjaCommand {

    public FireSaleCommand(NinjaBot bot, String commandName) {
        super(bot, commandName, "Sells all duplicate items");
    }
    
    private Set<Item> getNinjaWeapons(Ninja[] ninjas) {
        Set<Item> ninjaWeapons = new HashSet<Item>();
        if (ninjas != null && ninjas.length > 0){
            for (Ninja ninja : ninjas) {
                ninjaWeapons.add(ninja.getWeapon());
            }
        }
        return ninjaWeapons;
    }
    
    public void fireSell() throws IOException {
        Set<Item> foundItems = new HashSet<Item>();
        // create a set of all equipped items and relics
        Clan me = getBot().getClan();
        Item[] relics = me.getRelics();
        foundItems.addAll(Arrays.asList(relics));
        foundItems.addAll(getNinjaWeapons(me.getNinjas()));
        
        // iterate through the inventory set.  for each item:
        for (Item item : me.getInventory()) {
            if (!foundItems.add(item)) {
                // wasn't in the set
                // sell it!
                getBot().sellItem(item.getIid());
            }
        }
    }

    @Override
    public void execute() throws NinjaBotException {
        try {
            fireSell();
        } catch (IOException e){
            throw new NinjaBotException(e);
        }
    }

}
