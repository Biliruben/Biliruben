package biliruben.games.ninjawarz.command;

import java.io.IOException;
import java.util.Arrays;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;

public class ListCommand extends NinjaCommand {
    public static final String LIST_INVENTORY = "items";
    public static final String LIST_OPP = "opponents";
    public static final String LIST_ALLIES = "allies";
    public static final String LIST_WEAPON_SHOP = "weaponShop";
    public static final String LIST_RECRUITS = "recruits";
    public static final String LIST_EXCLUDE_FIGHT = "excludedFights";
    public static final String LIST_EXCLUDE_ASSIST = "excludedAssists";
    public static final String LIST_PREFERRED_ASSIST = "preferredAssists";
    public static final String LIST_TRIGGER = "triggers";
    public static final String LIST_MAGIC = "magic";
    private static final String[] LIST_ALL = {LIST_ALLIES, 
        LIST_INVENTORY, 
        LIST_OPP, 
        LIST_WEAPON_SHOP, 
        LIST_RECRUITS,
        LIST_EXCLUDE_FIGHT,
        LIST_EXCLUDE_ASSIST,
        LIST_PREFERRED_ASSIST,
        LIST_MAGIC,
        LIST_TRIGGER};

    public ListCommand(NinjaBot bot, String commandName) {
        super(bot, commandName, "Lists various objects");
    }

    @Override
    public void execute() throws NinjaBotException {

        int levelDelta = NinjaBot.DEFAULT_LEVEL_DELTA;
        if (_arguments.length == 0) {
            throw new NinjaBotException("List takes at least one argument: " + Arrays.toString(LIST_ALL));
        }
        String listWhat = _arguments[0].toLowerCase().trim();
        if (LIST_ALLIES.toLowerCase().startsWith(listWhat)) {
            try {
                if (_arguments.length < 2) {
                    getBot().showAllies(null);
                } else {
                    String sortColumn = _arguments[1];
                    getBot().showAllies(sortColumn);
                }
            } catch (IOException e) {
                throw new NinjaBotException(e);
            }
        } else if (LIST_INVENTORY.toLowerCase().startsWith(listWhat)) {
            try {
                getBot().showInventory();
            } catch (IOException e) {
                getBot().logError(e);
                throw new NinjaBotException(e);
            }
        } else if (LIST_OPP.toLowerCase().startsWith(listWhat)) {
            if (_arguments.length > 1) {
                try {
                    levelDelta = Integer.valueOf(_arguments[1]);
                } catch (NumberFormatException e) {
                    throw new NinjaBotException("Only integer values can be specified: " + _arguments[0]);
                }
            }
            try {
                getBot().showOpponents(levelDelta);
            } catch (IOException e) {
                getBot().logError(e);
            }
        } else if (LIST_WEAPON_SHOP.toLowerCase().startsWith(listWhat)) {
            try {
                getBot().showWeaponShop();
            } catch (IOException e) {
                getBot().logError(e);
            }
        } else if (LIST_RECRUITS.toLowerCase().startsWith(listWhat)) {
            try {
                getBot().showRecruits();
            } catch (IOException e) {
                getBot().logError(e);
            }
        } else if (LIST_EXCLUDE_FIGHT.toLowerCase().startsWith(listWhat)) {
            try {
                getBot().showExcludeFight();
            } catch (IOException e) {
                getBot().logError(e);
            }
        } else if (LIST_EXCLUDE_ASSIST.toLowerCase().startsWith(listWhat)) {
            try {
                getBot().showExcludeAssist();
            } catch (IOException e) {
                getBot().logError(e);
            }
        } else if (LIST_PREFERRED_ASSIST.toLowerCase().startsWith(listWhat)) {
            try {
                getBot().showPreferredAssist();
            } catch (IOException e) {
                getBot().logError(e);
            }
        } else if (LIST_TRIGGER.toLowerCase().startsWith(listWhat)) {
            getBot().showTriggers();
        } else if (LIST_MAGIC.toLowerCase().startsWith(listWhat)) {
            getBot().showMagic();
        } else {
            throw new NinjaBotException(listWhat + " is not a valid argument for the " + getName() + " command: " + Arrays.toString(LIST_ALL));
        }


    }

}
