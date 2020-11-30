package biliruben.games.ninjawarz.command;

import java.io.IOException;
import java.util.Arrays;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;

/**
 * Get:
 * - item
 * - clan
 * - queue
 * @author trey.kirk
 *
 */
public class GetCommand extends NinjaCommand {
    public static final String GET_ITEM = "item";
    public static final String GET_CLAN = "clan";
    public static final String GET_QUEUE = "queue";
    public static final String GET_NEWS = "news";
    public static final String GET_NINJA = "ninja";
    private static final String[] GET_ALL = {GET_ITEM, GET_CLAN, GET_QUEUE, GET_NEWS, GET_NINJA};

    public GetCommand(NinjaBot bot, String commandName) {
        super(bot, commandName, "Gets somthing and prints it to the screen");
    }

    @Override
    public void execute() throws NinjaBotException {
        if (_arguments.length == 0) {
            throw new NinjaBotException("Get takes at least one argument: " + Arrays.toString(GET_ALL));
        }

        String getWhat = _arguments[0].toLowerCase().trim();
        if (GET_ITEM.toLowerCase().startsWith(getWhat)) {
            // get item
            if (_arguments.length < 2) {
                throw new NinjaBotException("ItemID must be supplied!");
            }
            try {
                getBot().showItem(Integer.valueOf(_arguments[1]));
            } catch (NumberFormatException e) {
                throw new NinjaBotException("A valid integer must be supplied!");
            } catch (IOException e) {
                throw new NinjaBotException(e);
            }
        } else if (GET_CLAN.toLowerCase().startsWith(getWhat)) {
            // get clan
            try {
                if (_arguments.length < 2) {
                    getBot().printMe();
                } else {
                    String clan = _arguments[1];
                    getBot().showOpponent(clan);
                }

            } catch (NumberFormatException e) {
                throw new NinjaBotException("A valid integer must be supplied!");
            } catch (IOException e) {
                throw new NinjaBotException(e);
            }
        } else if (GET_QUEUE.toLowerCase().startsWith(getWhat)) {
            // get queue
            getBot().showQueue();
        } else if (GET_NEWS.toLowerCase().startsWith(getWhat)) {
            getBot().showNews();
        } else if (GET_NINJA.toLowerCase().startsWith(getWhat)) {
            if (_arguments.length < 2) {
                throw new NinjaBotException("A Ninja ID must be supplied");
            } else {
                String nid = _arguments[1];
                try {
                    getBot().showNinja(Long.valueOf(nid));
                } catch (NumberFormatException e) {
                    throw new NinjaBotException("A valid integer must be supplied!");
                } catch (IOException e) {
                    throw new NinjaBotException(e);
                }
            }
        } else {
            throw new NinjaBotException(getWhat + " is not a valid argument fo the " + getName() + " command: " + Arrays.toString(GET_ALL));
        }
    }

}
