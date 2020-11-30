package biliruben.games.ninjawarz;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import biliruben.games.ninjawarz.api.ClanService;
import biliruben.games.ninjawarz.api.CommandGroup;
import biliruben.games.ninjawarz.api.ConnectionManager;
import biliruben.games.ninjawarz.api.DetailPrinter;
import biliruben.games.ninjawarz.api.DetailPrinter.ColumnConfig;
import biliruben.games.ninjawarz.api.Event;
import biliruben.games.ninjawarz.api.ItemAggregator;
import biliruben.games.ninjawarz.api.JSONParser;
import biliruben.games.ninjawarz.api.NinjaRunnable;
import biliruben.games.ninjawarz.api.Trigger;
import biliruben.games.ninjawarz.api.TriggerEvaluator;
import biliruben.games.ninjawarz.api.TriggerService;
import biliruben.games.ninjawarz.command.AddFighterCommand;
import biliruben.games.ninjawarz.command.BatchCommand;
import biliruben.games.ninjawarz.command.HelpCommand;
import biliruben.games.ninjawarz.command.LoopCommand;
import biliruben.games.ninjawarz.command.NinjaCommand;
import biliruben.games.ninjawarz.command.PropertiesCommands;
import biliruben.games.ninjawarz.command.QueueCommand;
import biliruben.games.ninjawarz.command.SetPhpCommand;
import biliruben.games.ninjawarz.command.ShutdownCommand;
import biliruben.games.ninjawarz.object.AssistResult;
import biliruben.games.ninjawarz.object.Battle;
import biliruben.games.ninjawarz.object.Boss;
import biliruben.games.ninjawarz.object.BossCollection;
import biliruben.games.ninjawarz.object.Clan;
import biliruben.games.ninjawarz.object.DaimyoVisit;
import biliruben.games.ninjawarz.object.HospitalResult;
import biliruben.games.ninjawarz.object.Item;
import biliruben.games.ninjawarz.object.Magic;
import biliruben.games.ninjawarz.object.News;
import biliruben.games.ninjawarz.object.Ninja;
import biliruben.games.ninjawarz.object.Recruit;
import biliruben.games.ninjawarz.object.Result;
import biliruben.games.ninjawarz.object.SessionStats;
import biliruben.games.ninjawarz.object.Spoils;
import biliruben.games.ninjawarz.object.Stage;
import biliruben.games.ninjawarz.object.Timer;
import biliruben.games.ninjawarz.object.Tournament;
import biliruben.games.ninjawarz.object.Tournament.TournamentType;
import biliruben.io.HTTPConnection;
import biliruben.io.NullOutputStream;
import biliruben.io.OutputConsole;
import biliruben.threads.ThreadRunner;
import biliruben.threads.ThreadRunner.TRRunnable;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

/**
 * Bot used to perform repetitive fighting tasks for the Kongregate flash game Ninja Warz
 * @author trey.kirk
 *
 */
public class NinjaBot {

    public static final int DEFAULT_DELAY = 10;
    public static final int DEFAULT_FIGHTS = 5;
    public static final int DEFAULT_LEVEL_DELTA = 0;
    private static final boolean DEFAULT_ENABLE_LOGGING = false;
    public static final String DEFAULT_NINJA_BOT_LOG = "NinjaBot.log";
    public static final String LINE_SEP = "-------------------------------------------------------------------------------------------------";
    public static final String NINJABOT_SETTINGS_DIRECTORY = System.getProperty("user.home") + File.separator + ".ninjabot";
    public static final String PREFERENCES_FILE = "ninja.preferences";
    public static final String VERSION = "2.0";
    private static final String COMMAND_NOT_FOUND = ": command not found!";
    private static final String MULTIPLE_COMMANDS_FOUND = "Multiple commands found: ";

    private static NinjaBot _singleton;
    private NinjaCommand _addFighterCD;
    private OutputConsole _console;
    private Set<NinjaCommand> _commands;
    private PrintStream _log;
    private NinjaCommand _loopCommandCD;
    private PrintStream _out;
    private String _phpSession;
    private String _propertyFile = PREFERENCES_FILE;
    private Configuration _config;
    private ThreadRunner _runner;
    private SessionStats _stats;
    private PrintStream _stdout;
    private boolean _terminate = false;
    public final String BASE_URL = "http://ninjawarz.brokenbulbstudios.com";
    public final String NINJA_BOT_FLAVOR = "Facebook";
    public final String REFERER = "http://ninjawarz.brokenbulbstudios.com/?fb_source=bookmark_apps&ref=bookmarks&count=0&fb_bmpos=2_0";
    private JSONParser _jsonParser;
    private boolean _interactive;
    private ConnectionManager _connectionManager;
    private List<Timer> _timers;
    private List<Event> _events;
    private TriggerEvaluator _evaluator;
    private boolean _initialized = false;
    private ClanService _clanService;
    private TriggerService _triggerService;

    public static NinjaBot getInstance() {
        return _singleton;
    }
    /**
     * Default constructor
     * @throws IOException
     */
    public NinjaBot() throws IOException {
        this(null);
    }

    /**
     * Typical constructor
     * @param propertyFile Property file containing the bot configuration
     * @throws IOException
     */
    public NinjaBot(String propertyFile) throws IOException {
        _singleton = this; // because we do this, we should crack down on constructing now
        _interactive = true; // by default
        _connectionManager = new ConnectionManager(this);
        if (propertyFile != null) {
            _propertyFile = propertyFile;
        }
        _commands = new TreeSet<NinjaCommand>();
        readProperties();
        setupOutputStreams();
        Boolean doLogging = _config.getFlag(Configuration.PREF_ENABLE_LOGGING);
        addInternalCommands();
        setLogging(doLogging);
        setupFighterPool();
        _clanService = new ClanService(this);
        _timers = new ArrayList<Timer>();
        _events = new ArrayList<Event>();
        _triggerService = new TriggerService(this);
        _evaluator = new TriggerEvaluator(this);
        _jsonParser = new JSONParser(this);
        _stats = new SessionStats();
        _evaluator.start();
        _initialized = true;
    }

    /**
     * Adds a fight against a clan to the action queue
     * @param clanId The clan id to fight
     * @throws IOException 
     */
    public void addFighter(String clanId) throws IOException {
        addFighter(clanId, getFightCount());
    }

    private int getFightCount() {
        int count = _config.getInteger(Configuration.PREF_FIGHT_COUNT, DEFAULT_FIGHTS);
        return count;
    }
    /**
     * Adds a fight against a clan to the action queue
     * @param clanId The clan id to fight
     * @param iterations The number of fights to do
     * @throws IOException 
     */
    public void addFighter(String clanId, int iterations) throws IOException {
        addFighter(clanId, iterations, true);
    }

    public void addFighter(String clanId, int iterations, boolean useExclusions) throws IOException {
        Clan clan = getClan(clanId);
        if (_clanService.isExcludedClanFight(clan)) {
            getOutput().println(clan.getName() + ":" + clan.getCid() + " is on the exclusion list.");
        } else {
            getOutput().println("Adding opponent: " + clanId + ", " + iterations + " fights.");
            FightingRunnable ninjaRunnable = new FightingRunnable(this, clanId);
            ninjaRunnable.setFights(iterations);
            ninjaRunnable.setDelay(getFightDelay());
            _runner.add(ninjaRunnable);
        }
    }

    private int getFightDelay() {
        int delay = _config.getInteger(Configuration.PREF_FIGHT_DELAY, DEFAULT_DELAY);
        return delay;
    }
    
    public void getGift() throws IOException {
        HTTPConnection giftConn = _connectionManager.getGiftConnection();
        ByteOutputStream bos = new ByteOutputStream();
        giftConn.setOutputStream(bos);
        giftConn.connect();
        String result = bos.toString();
        getOutput().println(result);
    }
    /*
     * these are special commands that I need to reference internally to better handle their operations.  These
     * aren't added to the command set since the command scanner doesn't need to look them up.  Therefore we don't
     * have to care what their named.  I.e. this won't conflict with the existing 'fight' and 'loop' commands
     * 
     * Some commands are added here because it's assumed every app would want these basic commands.  Otherwise
     * each app is expected to add their intended commands
     */
    private void addInternalCommands() {
        NinjaCommand cmd = new AddFighterCommand(this, "fight");
        // I hold on to a fight command so I can call it when the user calls the fightAll method
        _addFighterCD = cmd;

        cmd = new LoopCommand(this, "loop");
        // I hold on to a loop command so I can call it again after each iteration, thus the loop
        _loopCommandCD = cmd;
        addNinjaCommand(cmd);

        // We add the help commands to the set
        cmd = new HelpCommand(this, "help");
        addNinjaCommand(cmd);

        cmd = new HelpCommand(this, "?");
        cmd.setHidden(true);
        addNinjaCommand(cmd);

        // always need an exit command
        cmd = new ShutdownCommand(this, "exit");
        this.addNinjaCommand(cmd);

        /// queue command so apps can use the monitor
        cmd = new QueueCommand(this, "queueCommand");
        this.addNinjaCommand(cmd);

        // the all important phpsessId
        cmd = new SetPhpCommand(this, "phpSession");
        addNinjaCommand(cmd);

        cmd = new BatchCommand(this, "batch");
        addNinjaCommand(cmd);

        PropertiesCommands propCommands = new PropertiesCommands(this);
        addNinjaCommands(propCommands);

    }

    public void addProperty(String key, String value) {
        if (_config == null) {
            try {
                readProperties();
            } catch (IOException e) {
                logError(e);
                return;
            }
        }
        _config.setProperty(key, value);
    }

    /**
     * Adds a {@link NinjaCommand} to the command set
     * @param command
     */
    public void addNinjaCommand(NinjaCommand command) {
        if (command != null) {
            if (_commands == null) {
                _commands = new TreeSet<NinjaCommand>();
            }
            _commands.add(command);
        }
    }

    public void addNinjaCommands(CommandGroup commandGroup) {
        for (NinjaCommand command : commandGroup.getCommands()) {
            addNinjaCommand(command);
        }
    }

    public void addTrigger(Trigger trigger) {
        getOutput().println("Adding trigger: " + trigger);
        if (trigger instanceof Timer) {
            _timers.add((Timer)trigger);
        } 
        if (trigger instanceof Event) {
            _events.add((Event)trigger);
        }
    }

    /**
     * Assists the specified clan
     * @param clan
     * @throws IOException
     */
    public boolean assistClan(Clan clan) throws IOException {
        if (_clanService.isExcludedClanAssist(clan)) {
            getOutput().println(clan.getName() + ":" + clan.getCid() + " is on the assist exclusion list");
            return false;
        }
        HTTPConnection assistConnection = _connectionManager.getAssistConnection(clan.getCid());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        assistConnection.setOutputStream(bos);
        assistConnection.connect();
        String resultJson = bos.toString();
        AssistResult result = _jsonParser.parseJSON(resultJson, AssistResult.class);
        Spoils spoils = result.getSpoils();
        _stats.addSpoils(spoils);
        if ("success".equals(result.getResult())) {
            getOutput().println("You successfully assisted " + clan.getName() + ": " + spoils.getGold() + " gold and " + spoils.getExp() + " exp.");
            return true;
        } else {
            getOutput().println(result.getResult() + ": " + result.getError());
            return false;
        }
    }

    /**
     * Purchases the specified weapon from the weapon shop
     * @param itemId
     * @throws IOException
     * @throws NinjaBotException
     */
    public void buyWeapon(int itemId) throws IOException, NinjaBotException {
        HTTPConnection buyWeaponConnection = _connectionManager.getBuyWeaponConnection(itemId);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        buyWeaponConnection.setOutputStream(bos);
        buyWeaponConnection.connect();
        String jsonResponse = bos.toString();
        Result result = _jsonParser.parseJSON(jsonResponse, Result.class);
        if ("success".equals(result.getResult())) {
            getOutput().println(LINE_SEP + "\nSuccess! You bought a: ");
            showItem(itemId);
        } else if (result.getError() != null) {
            getOutput().println("Error: " + result.getError());
        } else {
            getOutput().println("There was a problem with your transaction: " + jsonResponse);
        }
    }

    public void buyMagic(int sid) throws IOException {
        HTTPConnection buyMagicConnection = _connectionManager.getMagicPurchaseConnection(String.valueOf(sid));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        buyMagicConnection.setOutputStream(bos);
        buyMagicConnection.connect();
        String jsonResponse = bos.toString();
        Result result = _jsonParser.parseJSON(jsonResponse, Result.class);
        if (result.getError() != null) {
            getOutput().println(result.getError());
        } else {
            getOutput().println(result.getResult());
        }
    }

    /**
     * Emptys the action queue of all pending runnables.  Runnables currently running are not affected
     */
    public void clearQueue() {
        getOutput().println("...clearing queue");
        _runner.clear();
        showStatus();
    }

    /**
     * Executes the provided command which will typically result in a call back to the bot
     * @param ninjaCommand
     * @return
     */
    public boolean dispatchCommand(NinjaCommand ninjaCommand) {
        boolean error = false;
        if (ninjaCommand != null) {
            try {
                ninjaCommand.execute();
            }catch (NinjaBotException e) {
                getOutput().println(e.getMessage()); // error in the command
                error = true;
            } catch (Throwable e) {
                // ...whine about it
                getOutput().println("Could not execute command: " + ninjaCommand.getName());
                logError(e);
                e.printStackTrace(getOutput());
                error = true;
            }
        }        
        return error;
    }

    /**
     * Equips the specified ninja with the specified item from inventory
     * @param l
     * @param itemId
     * @throws IOException
     * @throws NinjaBotException
     */
    public void equipNinja(long l, int itemId) throws IOException, NinjaBotException {
        HTTPConnection equipConnection = _connectionManager.getEquipConnection(l, itemId);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        equipConnection.setOutputStream(bos);
        equipConnection.connect();
        String resultJson = bos.toString();
        Result result = _jsonParser.parseJSON(resultJson, Result.class);
        getOutput().println(result.getResult());
        if (result.getError() != null) {
            getOutput().println(result.getError());
        }
    }

    public void unequipNinja(long nid) throws NinjaBotException, IOException {
        HTTPConnection UnequipConnection = _connectionManager.getUnequipConnection(nid);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UnequipConnection.setOutputStream(bos);
        UnequipConnection.connect();
        String resultJson = bos.toString();
        Result result = _jsonParser.parseJSON(resultJson, Result.class);
        getOutput().println(result.getResult());
        if (result.getError() != null) {
            getOutput().println(result.getError());
        }
    }

    /**
     * Fights all clans that are separated by levelDelta
     * @param levelDelta
     * @throws IOException
     */
    public void fightAllOpponents(int levelDelta) throws IOException {
        getOutput().println("Fighting all opponents " + levelDelta + " levels");
        List<Clan> opponents = getOpponents(levelDelta);
        for (Clan opponent : opponents) {
            String oppId = String.valueOf(opponent.getCid());
            addFighter(oppId);
        }
    }
    
    /**
     * Performs a fight against the target cid and returns the Battle result.  This is NOT the usual fight method as it
     * will not put the fight in the Action Queue nor will it run the result Battle through reportBattle.  In addition, it
     * will not auto-heal after the fight result.  This method is intended to be used for commands requiring a more atomic
     * execution of a fight and impose its own control over the Battle data.
     * @param cid
     * @throws IOException 
     */
    public Battle fight(String cid) throws IOException {
        HTTPConnection fightConnection = getConnectionManager().getFightConnection(cid, getOutput());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        fightConnection.setOutputStream(bos);
        fightConnection.connect();
        String battleJSON = bos.toString();
        Battle battle = null;
        try {
            battle = reportBattle(getOutput(), battleJSON);
        } catch (Exception e) {
            logError(e);
        }
        return battle;

    }

    /**
     * Finds an ally by name.  We only do this for allies since allies are the only
     * stable list of clan ids that map to names we have.  A future enhancement may be
     * to keep a running tally of cid to name for any clan that we look at
     * @param allyName
     * @return
     * @throws IOException
     */
    public Clan findAllyByName(String allyName) throws IOException {
        Clan clan = getClan();
        Clan[] allies = clan.getAllies();
        for (Clan ally : allies) {
            if (allyName.equalsIgnoreCase(ally.getName())) {
                return getClan(ally.getCid()); // Ally clan is sparse.  Give daddy the real goods
            }
        }
        // no findy
        return null;
    }

    /**
     * Searches for the specified NinjaCommand
     * @param command
     * @return
     */
    private List<NinjaCommand> findCommands(String command) {
        return findCommands(command, false);
    }

    /**
     * Searches for the specified NinjaCommand
     * @param command The name of the command
     * @param exactMatch When true, the command name specified must be an exact match.  False will allow for a 
     * 'starts with' match and return all commands that match
     * @return
     */
    public List<NinjaCommand> findCommands(String command, boolean exactMatch) {
        List<NinjaCommand> foundCommands = new ArrayList<NinjaCommand>();
        for (NinjaCommand testCommand : _commands) {
            if (testCommand.getName().equals(command)) {
                // exact match, no further looking
                foundCommands.clear(); // remove anyting we've already found
                foundCommands.add(testCommand);
                break;
            }
            if (testCommand.matchesName(command) && !exactMatch) {
                foundCommands.add(testCommand);
            }
        }
        return foundCommands;
    }



    /**
     * Gets the base Ninja Warz URL
     * @return
     */
    public String getBaseUrl() {
        String baseUrl = _config.getString(Configuration.PREF_BASE_URL, BASE_URL);
        return baseUrl;
    }

    /**
     * Returns the My Clan
     * @return
     * @throws IOException
     */
    public Clan getClan() throws IOException {
        HTTPConnection connection = _connectionManager.getProfileConnection();
        ByteOutputStream bos = new ByteOutputStream();
        connection.setOutputStream(bos);
        connection.connect();
        // We get a Clan json
        String clanJSON = bos.toString();
        Clan clan = _jsonParser.parseJSON(clanJSON, Clan.class);
        return clan;
    }

    public Clan getClan(long cid) throws IOException {
        return getClan(String.valueOf(cid));
    }

    /**
     * Returns the clan specified by the Clan ID
     * @param cid
     * @return
     * @throws IOException
     */
    public Clan getClan(String cid) throws IOException {
        HTTPConnection connection = _connectionManager.getProfileConnection(cid);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        connection.setOutputStream(bos);
        connection.connect();
        String clanJson = bos.toString();
        Clan clan = null;
        if ("false".equalsIgnoreCase(clanJson)) {
            // getOutput().println(cid + ": Does not exists!");
            // find a different place to complain
        } else {
            clan = _jsonParser.parseJSON(clanJson, Clan.class);
        }
        if (clan == null) {
            // try by ally name
            clan = findAllyByName(cid);
        }
        return clan;
    }

    public Set<NinjaCommand> getCommands() {
        return new TreeSet<NinjaCommand>(_commands);
    }

    public ConnectionManager getConnectionManager() {
        return _connectionManager;
    }


    public Tournament getNewTournament(int size, TournamentType type) throws IOException {
        HTTPConnection tournamentConnection = _connectionManager.getCreateTournamentConnection(size, type);
        ByteOutputStream bos = new ByteOutputStream();
        tournamentConnection.setOutputStream(bos);
        tournamentConnection.connect();
        String json = bos.toString();
        Tournament tourney = _jsonParser.parseJSON(json, Tournament.class);
        return tourney;
    }

    public List<Item> getTournamentPrizes() throws IOException {
        HTTPConnection connection = _connectionManager.getTournamentPrizesConnection();
        ByteOutputStream bos = new ByteOutputStream();
        connection.setOutputStream(bos);
        connection.connect();
        String json = bos.toString();
        List<Item> items = _jsonParser.parseJSONList(json, Item.class);
        return items;
    }

    public List<Timer> getTimers() {
        return _timers;
    }

    public Battle fightTournament(String tid) throws IOException {
        HTTPConnection connection = _connectionManager.getTournamentFightConnection(tid);
        ByteOutputStream bos = new ByteOutputStream();
        connection.setOutputStream(bos);
        connection.connect();
        String json = bos.toString();
        Battle battle = _jsonParser.parseJSON(json, Battle.class);
        _stats.addBattle(battle);
        return battle;
    }

    /**
     * Given the item ID, returns the {@link Item} for that ID from inventory
     * @param itemId
     * @return
     * @throws IOException
     */
    public Item getItem(int itemId) throws IOException {
        Item foundItem = null;
        Clan me = getClan();
        if (me != null) {
            Item[] items = me.getInventory();
            if (items != null) {
                for (Item item : items) {
                    if (item.getIid() == itemId) {
                        foundItem = item;
                        break;
                    }
                }
            }
        }
        if (foundItem == null) {
            Item[] equipped = me.getEquippedItems();
            if (equipped != null) {
                for (Item item : equipped) {
                    if (item.getIid() == itemId) {
                        foundItem = item;
                        break;
                    }
                }
            }
        }
        return foundItem;
    }

    /**
     * Returns the OutputStream used for logging.  When Logging is disabled, this is
     * a {@link NullOutputStream}
     * @return
     */
    public PrintStream getLoggingStream() {
        return _log;
    }

    public List<Magic> getMagic() throws IOException {
        HTTPConnection magicConnection = _connectionManager.getMagicInventoryConnection();
        ByteOutputStream bos = new ByteOutputStream();
        magicConnection.setOutputStream(bos);
        magicConnection.connect();
        List<Magic> magicInv = _jsonParser.parseJSONList(bos.toString(), Magic.class);
        return magicInv;
    }

    public Magic getMagic(int sid) throws IOException {
        List<Magic> magics = getMagic();
        for (Magic magic : magics) {
            if (magic.getSid() == sid) {
                return magic;
            }
        }
        return null;
    }

    /**
     * Fetches the latest {@link News}
     * @return
     * @throws IOException
     */
    public String getNews() throws IOException {
        ByteArrayOutputStream newsOutput = new ByteArrayOutputStream();
        HTTPConnection newsConnection = _connectionManager.getNewsConnection(newsOutput);
        newsConnection.connect();
        String jsonNews = newsOutput.toString();
        StringBuffer buff = new StringBuffer();
        List<News> news = _jsonParser.parseJSONList(jsonNews, News.class);
        for (News newsLine : news) {
            buff.append(newsLine.getClearText() + "\n");
        }
        /*
        getOutput().println(rawNews);
        newsOutput = new ByteArrayOutputStream();
        NewsParser.parseNews(rawNews, newsOutput);
        return newsOutput.toString();
         */
        return buff.toString();


    }

    /**
     * Returns a List of {@link Item} available for purchase in the Weapon Shop
     * @param levelOnly When false, all weapons are listed without regard to the level they are available at.  Note, this
     * does not circumvent the level restriction and thus will not allow one to purchase inappropriately leveled weapons.
     * @return
     * @throws IOException
     */
    public List<Item> getWeaponShop(boolean levelOnly) throws IOException {
        HTTPConnection wsConnection = _connectionManager.getWeaponShopConnection();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        wsConnection.setOutputStream(bos);
        wsConnection.connect();
        String wsJson = bos.toString();
        List<Item> weaponShopInventory = _jsonParser.parseJSONList(wsJson, Item.class);
        List<Item> filteredWeapons = new ArrayList<Item>();
        if (levelOnly) {
            Clan me = getClan();
            long testLevel = me.getLevel();
            for (Item weapon : weaponShopInventory) {
                if (weapon.getMinLevel() <= testLevel) {
                    filteredWeapons.add(weapon);
                }
            }
        } else {
            filteredWeapons.addAll(weaponShopInventory);
        }
        Collections.sort(filteredWeapons, new Item.ItemDpsComparator());
        return filteredWeapons;
    }


    /**
     * Returns a String denoting this implementation of NinjaBot. The default is Facebook and a second
     * implementation for Kongregate has been created
     * @return
     * @see KongregateNinjaBot
     */
    protected String getNinjaBotFlavor() {
        return NINJA_BOT_FLAVOR;
    }

    /*
     * Returns a list of Clans representing the opponents 'levelDelta' levels away from you
     */
    private List<Clan> getOpponents(int levelDelta) throws IOException {
        String opponentResponse = null;
        List<Clan> opponents = null;
        HTTPConnection oppConnection = _connectionManager.getListOpponent(levelDelta);
        ByteOutputStream bytes = new ByteOutputStream();
        oppConnection.setOutputStream(bytes);
        oppConnection.connect();
        opponentResponse = bytes.toString();
        if (opponentResponse != null && !"".equals(opponentResponse.trim())) {
            // list of Clan
            opponents =  _jsonParser.parseJSONList(opponentResponse, Clan.class);
        }
        return opponents;
    }

    /**
     * Returns the OutputStream appropriate for your thread.  When the interactive console thread is the caller,
     * STDOUT is returned.  Otherwise it's assumed the thread is our action queue and thus the {@link OutputConsole}
     * OutputStream is returned instead
     * @return
     */
    public PrintStream getOutput() {
        // TODO: Be a little smarter with threads and maybe build a referencable map.  As soon as I decide I need
        // a third thread for some reason, this becomes unwieldy.
        Thread t = Thread.currentThread();
        PrintStream out;
        if ("main".equals(t.getName())) {
            out = _stdout;
        } else {
            out = _out;
        }
        return out;
    }

    /**
     * Returns the session ID
     * @return
     * @throws NinjaBotException
     */
    public String getPhpSession() throws NinjaBotException {
        if (_phpSession == null) {
            throw new NinjaBotException("PHP Session ID has not been set yet!");
        } else {
            return _phpSession;
        }
    }

    public Configuration getConfiguration() {
        return _config;
    }


    /**
     * Returns a list of {@link Ninja} that are available recruits
     * @return
     * @throws IOException
     */
    public List<Recruit> getRecruits() throws IOException {
        HTTPConnection listRecruitConnection = _connectionManager.getRecruitListConnection();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        listRecruitConnection.setOutputStream(bos);
        listRecruitConnection.connect();
        String json = bos.toString();
        List<Recruit> recruitList = _jsonParser.parseJSONList(json, Recruit.class);
        return recruitList;
    }

    /**
     * Gets the referer property used in each connection.  Implementations should override this to 
     * return appropriate referers lest the server may get wary of you.  There is no garauntee that
     * the server cares about this value or that the server would no be wary for other reasons anyways 
     * @return
     */
    public String getReferer() {
        return REFERER;
    }


    /**
     * Returns the {@link SessionStats} object which tracks our progress
     * @return
     */
    public SessionStats getSessionStats() {
        return _stats;
    }

    public Stage getStage() throws IOException {
        HTTPConnection stageConnection = _connectionManager.getStageConnection();
        ByteOutputStream bos = new ByteOutputStream();
        stageConnection.setOutputStream(bos);
        stageConnection.connect();
        String json = bos.toString();
        Stage stage = _jsonParser.parseJSON(json, Stage.class);
        return stage;

    }

    /**
     * Prints out the status of the action queue
     * @return
     */
    public String getStatus() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Status:\n");
        if (_runner.isActivated()) {
            buffer.append("Queue Running!\n");
        } else {
            buffer.append("Queue Paused\n");
        }
        int queued = _runner.getWorkload();
        buffer.append(queued).append(" in queue\n");
        buffer.append("\n");
        if (_evaluator.isPaused()) {
            buffer.append("Triggers Paused\n");
        } else {
            buffer.append("Triggers Running!\n");
        }
        int timers = _timers.size();
        int events = _events.size();
        buffer.append(timers + " timers\n");
        buffer.append(events + " events\n");
        return buffer.toString();
    }

    /**
     * Returns a List of {@link Item} available for purchase in the Weapon Shop.  This return only weapons
     * that are available to purchase for your level.
     * @return
     * @throws IOException
     */
    public List<Item> getWeaponShop() throws IOException {
        return getWeaponShop(true);
    }

    /**
     * Constructs the welcome message shown in the {@link OutputConsole} during startup
     * @return
     */
    protected String getWelcomeMessage() {
        StringBuffer buff = new StringBuffer();
        buff.append(LINE_SEP + "\n");
        buff.append("NinjaBot ").append(VERSION).append(" (").append(getNinjaBotFlavor()).append(")\n");
        buff.append(new Date().toString()).append("\n");
        buff.append("\n").append(getStatus()).append("\n");
        buff.append("Ready!\n");
        return buff.toString();
    }

    /**
     * Visits the hospital
     * @throws IOException
     */
    public void heal() throws IOException {
        getOutput().println("Healing...");
        getOutput().flush(); // can get a little out of order
        HTTPConnection healConnection = _connectionManager.getHealConnection(getOutput());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        healConnection.setOutputStream(bos);
        healConnection.connect();
        String healJSON = bos.toString();
        reportHeal(getOutput(), healJSON);
    }

    /**
     * Used by applications to determine if the bot is running
     * @return
     */
    public boolean isTermnated() {
        return _terminate;
    }

    public boolean isReady() {
        // will return true if the phpsession id has been set
        // and the clan can be fetched
        try {
            return !_terminate && _phpSession != null && getClan() != null;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            return false;
        }
    }

    /**
     * Our general logging method.  This will put the error message to the current OutputStream and send the
     * entire stack trace to the logging stream.  Note: if you want stack traces, enable logging
     * @param e
     * @see #getOutput()
     * @see #setLogging(boolean)
     * @see #getLoggingStream()
     */
    public void logError(Throwable e) {

        if (_config.getFlag(Configuration.PREF_ENABLE_LOGGING)) {
            e.printStackTrace(getOutput());
        } else {
            getOutput().println("ERROR: " + e.getMessage());
        }
        e.printStackTrace(getLoggingStream());
    }

    /**
     * The Loop action:  Given an array of command tokens, a command will be constructed from those tokens with
     * the first token being the command name and all subsequent tokens used as the arguments.  The appropriate
     * {@link NinjaCommand} will be parsed from the tokens and enqueued into the action queue.  Following that,
     * another loop command for the same command tokens will be enqueued ensuring the command is always being iterated.
     * This looping has no conditional control for stopping.  So stopping the loop means you have to clear the action
     * queue.
     * @param commandTokens
     * @see #clearQueue()
     */
    public void loopCommand(String[] commandTokens) {
        String command = commandTokens[0];
        // TODO: reusing a lot of code from the queueCommand method.  simplify this someday.
        // we support a shortcut command for adding a fighter, but we're not going to do it for queueing
        List<NinjaCommand> foundCommands = findCommands(command);
        if (foundCommands.size() == 0) {
            getOutput().println(command + COMMAND_NOT_FOUND);
        } else if (foundCommands.size() > 1) {
            getOutput().println(MULTIPLE_COMMANDS_FOUND);
            for (NinjaCommand foundCommand : foundCommands) {
                getOutput().println("\t" + foundCommand.getName());
            }
        } else {
            StringBuffer commandString = new StringBuffer();
            if (commandTokens.length > 1) {
                for (int i = 1; i < commandTokens.length; i++) {
                    String token = commandTokens[i];
                    if (token.contains("\\s")) {
                        commandString.append("\"").append(token).append("\"");
                    } else {
                        commandString.append(token);
                    }
                    commandString.append(" ");
                }
            }
            // and toss in the queued command to said queue
            getOutput().println("Queueing command: " + command + " " + commandString.toString());
            CommandRunnable runnable = new CommandRunnable(this, command, commandString.toString());
            _runner.add(runnable);
            // then queue the loop command
            String[] loopCommandTokens = new String[commandTokens.length + 1];
            loopCommandTokens[0] = _loopCommandCD.getName();
            for (int i = 0; i < commandTokens.length; i++) {
                loopCommandTokens[i + 1] = commandTokens[i];
            }
            queueCommand(loopCommandTokens);
        }
    }

    /**
     * Sends the pause command to the action queue runner
     * @see ThreadRunner#pause()
     */
    public void pauseQueue() {
        // This pauses the runner
        getOutput().println("Pausing Action Queue...");
        _runner.pause();
    }

    public void pauseTriggers() {
        // this pauses the Trigger evaluator
        getOutput().println("Pausing Trigger Evaluator...");
        _evaluator.pause();
    }

    /**
     * Prints out a detailed summary of the given Clan
     * @param clan
     */
    public void printClan(Clan clan) {
        PrintStream out = getOutput();
        out.println(clan.getCid() + " : " + clan.getName());
        String format = "%-25s";
        // level
        out.printf(format, "Level:");
        out.println(clan.getLevel());
        // exp
        out.printf(format, "Experience:");
        out.println(Util.commaNumber(clan.getExp()));
        out.printf(format, "Exp to level:");
        out.println(Util.commaNumber(clan.getExp_to_level()));
        out.printf(format, "Total Karma Value:");
        out.println(Util.commaNumber(clan.getKarmaValue()));
        out.printf(format, "Total Karma Purchased:");
        out.println(Util.commaNumber(clan.getPurchased_karma()));
        out.println();

        // karma
        out.printf(format, "Karma:");
        out.println(Util.commaNumber(clan.getKarma()));

        // gold
        out.printf(format, "Gold:");
        out.println(Util.commaNumber(clan.getGold()));
        out.println();

        // show daiymo visit -- only if it's us
        long nextDaiymo = clan.getDaimyo_gift() * 1000;

        out.printf(format, "Next Daiymo Visit: ");
        String nextDaiymoStr = null;
        if (nextDaiymo == 0) {
            nextDaiymoStr = "NOW!";
        } else {
            nextDaiymoStr = Util.getDurationString(nextDaiymo);
        }
        out.println(nextDaiymoStr + "\n");

        // health of health max
        out.printf(format, "Health:");
        out.println(Util.commaNumber(clan.getHealth()) + " / " + Util.commaNumber(clan.getMaxHealth()));

        // all ninja power
        out.printf(format, "Total Power:");
        out.println(Util.commaNumber(clan.getTotalPower()));

        // agility
        out.printf(format, "Agility:");
        out.println(clan.getAgility());

        // armor
        out.printf(format, "Armor:");
        out.println(clan.getArmor());

        // crit rate
        out.printf(format, "Crit rate:");
        out.println(clan.getCritRate());

        out.println();

        // damage rate
        out.printf(format, "Damage Rating:");
        out.println(Util.commaNumber(clan.getDamageRating()));

        // toughness
        out.printf(format, "Toughness Rating:");
        out.println(Util.commaNumber(clan.getToughnessRating()));
        out.println();

        // relics
        Magic magic = clan.getMagic();
        if (magic != null && magic.getName() != null) {
            out.printf(format, "Magic Equipped:");
            out.println(magic.getName() + ": " + magic.getDamage() + " damage");
            out.println();
        }

        Item[] relics = clan.getRelics();
        if (relics != null && relics.length > 0) {
            for (int i = 0; i < relics.length; i++) {
                int slot = i + 1;
                out.printf(format, "Relic Slot " + slot + ":");
                out.printf(format, relics[i].getName());
                out.println(relics[i].getAttributes());
            }
            out.println();
        }

        /*
        // ninja summary
        Ninja[] ninjas = clan.getNinjas();
        if (ninjas != null && ninjas.length > 0) {
            out.println("Ninjas:");
            DecimalFormat formater = new DecimalFormat("##.##");

            for (int i = 0; i < ninjas.length; i++) {
                Ninja ninja = ninjas[i];
                out.printf("%-7s", ninja.getNid() + ": ");
                out.printf("%-35s", ninja.getName());
                out.printf("%-10s", "Level " + ninja.getLevel());
                out.printf("%-14s", "Power: " + ninja.getModified_power());
                out.printf("%-14s", "Health: " + ninja.getModified_max_health());
                Double dps = (Double) ninja.getWeapon().getAttributes().get("DPS");

                String baseDamage = formater.format(Double.valueOf(ninja.getWeapon().getAttributes().get("damage").toString()));

                String speed = formater.format(Double.valueOf(ninja.getWeapon().getAttributes().get("speed").toString()));
                String dpsString = "";
                if (dps != null) {
                    dpsString = " (Damage: " + baseDamage + ", Speed: " + speed + ", DPS: " + dps + ")";
                }
                out.println(" w/ " + ninja.getWeapon().getName() + dpsString);
            }
            out.println();
        }
        */
        showNinjas(clan.getNinjas(), true);

    }

    /**
     * Takes the {@link Clan#getDamageRating()} and {@link Clan#getToughnessRating()} values for the current
     * clan and compares them to the provided clan
     * @param clan
     * @throws IOException
     * @see {@link Clan#getDamageRating()}
     * @see {@link Clan#getToughnessRating()}
     */
    public void printClanComparison(Clan clan) throws IOException {
        String col1Format = "%-20s";
        String col2Format = "%-14s";
        //String col3Format = col1Format;

        Clan you = getClan();
        getOutput().println(LINE_SEP);

        getOutput().printf(col1Format, "");
        getOutput().printf(col2Format, "You:");
        getOutput().println(clan.getCid() + " (" + clan.getName() + "): \n");

        getOutput().printf(col1Format, "Damage Rating");
        getOutput().printf(col2Format, Util.commaNumber(you.getDamageRating()));
        getOutput().println(Util.commaNumber(clan.getDamageRating()));

        getOutput().printf(col1Format, "Toughness Rating");
        getOutput().printf(col2Format, Util.commaNumber(you.getToughnessRating()));
        getOutput().println(Util.commaNumber(clan.getToughnessRating()));
    }

    /**
     * Prints the details for the current Clan
     * @throws IOException
     */
    public void printMe() throws IOException {
        printClan(getClan());
    }

    /**
     * Processes the command given and returns the {@link NinjaCommand} responsible for
     * executing the desired action
     * @param command
     * @param arguments
     * @return
     */
    public NinjaCommand processCommand(String command, String arguments) {
        // trim out stuff we don't want
        if (command != null) {
            command = command.trim();
        }

        if (arguments != null) {
            arguments = arguments.trim();
        }
        // shortcut: if they plugged in a single integer, assume 'addFighter int'
        if (command.matches("\\d*")) {
            // all digits
            arguments = command;
            command = _addFighterCD.getName();
        }
        List<NinjaCommand> foundCommands = findCommands(command);
        // found anything?
        if (foundCommands.size() == 0) {
            getOutput().println(command + COMMAND_NOT_FOUND);
        } else if (foundCommands.size() > 1) { // found multiples
            getOutput().println(MULTIPLE_COMMANDS_FOUND);
            for (NinjaCommand foundCommand : foundCommands) {
                getOutput().println("\t" + foundCommand.getName());
            }
        } else {
            // one command to rule them all, and in the darkness bind them!
            NinjaCommand foundCommand = foundCommands.get(0);
            // don't use this one, use a clone.  Why?  The commands we
            // have are considered 'prototypes', or templates.  They model
            // how commands of their type should be constructed.  Consumers
            // of the commands (like NinjaBot) will tweak properties based on
            // arguments provided.  We can't allow tweaks like that to propogate
            // to our prototype commands
            NinjaCommand copy = foundCommand.getCopy();
            copy.parseArguments(arguments);
            return copy;
        }
        return null;
    }

    public void purgeTrigger(Trigger trigger) {
        if (trigger instanceof Event) {
            _events.remove((Event)trigger);
        } else if (trigger instanceof Timer) {
            _timers.remove((Timer)trigger);
        }
    }

    /**
     * Most commands are executed in the main thread, interactive of the user.  Some commands like
     * the fight command and the fightall command are always run in the action queue.  Via the 
     * queueCommand method any command may be executed in the action queue leaving the interactive
     * console for other actions.  Ideally, one may decide to sell off all overhead inventory
     * which may take several minutes.  The {@link #sellAllItem(int)} command is a command that
     * runs in-thread for the console and thus would prevent the user from being able to do anything
     * while it's selling items.  Using the queueCommand, that same sellAll command may instead be
     * enqueued in the action queue, allowing the user to continue to use the console for other means.
     * @param commandTokens
     */
    public void queueCommand(String[] commandTokens) {
        // TODO: we reuse this command parsing logic in different places.  Really need to simplify that more
        String command = commandTokens[0];
        // we support a shortcut for adding a fighter.  don't care to support it for queueing
        List<NinjaCommand> foundCommands = findCommands(command);
        if (foundCommands.size() == 0) {
            getOutput().println(command + COMMAND_NOT_FOUND);
        } else if (foundCommands.size() > 1) {
            getOutput().println(MULTIPLE_COMMANDS_FOUND);
            for (NinjaCommand foundCommand : foundCommands) {
                getOutput().println("\t" + foundCommand.getName());
            }
        } else {
            StringBuffer commandString = new StringBuffer();
            if (commandTokens.length > 1) {
                for (int i = 1; i < commandTokens.length; i++) {
                    String token = commandTokens[i];
                    if (token.contains("\\s")) {
                        commandString.append("\"").append(token).append("\"");
                    } else {
                        commandString.append(token);
                    }
                    commandString.append(" ");
                }
            }
            // and toss in the queued command to said queue
            getOutput().println("Queueing command: " + command + " " + commandString.toString());
            CommandRunnable runnable = new CommandRunnable(this, command, commandString.toString());
            _runner.add(runnable);
        }
    }

    /*
     * Reads in the batch command files from the ninjabot home directory and creates usable
     * commands for those
     */
    private void readBatchCommands() {
        List<NinjaCommand> batchCommands;
        try {
            batchCommands = BatchCommand.readBatchCommands(this);
            for (NinjaCommand batchCommand : batchCommands) {
                addNinjaCommand(batchCommand);
            }
        } catch (FileNotFoundException e) {
            logError(e);
        }

    }

    /*
     * Reads the configuration properties
     * 
     */
    private void readProperties() throws IOException {
        System.out.println("Reading properties from " + _propertyFile);
        _config = Configuration.readProperties(_propertyFile);
        setDefaultProperties();
        String phpSession = _config.getString(Configuration.PREF_PHP_SESSION);
        if (phpSession != null) {
            setPhpSession(phpSession);
        }
    }

    /*
     * Ensures that these properties are explicity defined in the configuration file
     * allowing the user to easily determine relevant property values
     */
    protected void setDefaultProperties() {
        _config.setProperty(Configuration.PREF_ENABLE_LOGGING, _config.getFlag(Configuration.PREF_ENABLE_LOGGING, DEFAULT_ENABLE_LOGGING));
        _config.setProperty(Configuration.PREF_FIGHT_COUNT, _config.getInteger(Configuration.PREF_FIGHT_COUNT, DEFAULT_FIGHTS));
        _config.setProperty(Configuration.PREF_FIGHT_DELAY, _config.getInteger(Configuration.PREF_FIGHT_DELAY, DEFAULT_DELAY));
        _config.setProperty(Configuration.PREF_LOG_FILE_NAME, _config.getString(Configuration.PREF_LOG_FILE_NAME, DEFAULT_NINJA_BOT_LOG));
    }
    public void recruitNinja(String rnid) throws IOException {
        HTTPConnection connection = _connectionManager.getRecruitConnection(rnid);
        ByteOutputStream bos = new ByteOutputStream();
        connection.setOutputStream(bos);
        connection.connect();
        String json = bos.toString();
        Result result = _jsonParser.parseJSON(json, Result.class);
        getOutput().println(result.getResult());
        if (result.getError() != null) {
            getOutput().println(result.getError());
        }
    }

    public void dismissNinja(long nid) throws IOException {
        HTTPConnection connection = _connectionManager.getDissmissConnection(nid);
        ByteOutputStream bos = new ByteOutputStream();
        connection.setOutputStream(bos);
        connection.connect();
        String json = bos.toString();
        Result result = _jsonParser.parseJSON(json, Result.class);
        getOutput().println(result.getResult());
        if (result.getError() != null) {
            getOutput().println(result.getError());
        }
    }

    /**
     * Removes a command from the command set, effectively making that command unusable from the console.
     */
    public boolean removeCommand(NinjaCommand realCommand) {
        // TODO: this was created so we could delete batch commands.  So get on with implementing batch command deletion
        boolean result = _commands.remove(realCommand);
        if (result) {
            getOutput().println("Successfully deleted command: " + realCommand.getName());
        } else {
            getOutput().println("Could not remove command: " + realCommand.getName());
        }
        return result;
    }

    /**
     * Given the JSON output of a fight, a {@link Battle} object is parsed and the significant
     * details of the battle are pushed into the session statistics
     * @param output
     * @param battleJSON
     * @return
     * @throws IOException
     */
    public Battle reportBattle(PrintStream output, String battleJSON) throws IOException {
        // TODO: this method lacks focus.  Should it parse the JSON or log the stats?
        Battle battle = _jsonParser.parseJSON (battleJSON, Battle.class);
        if (battle == null) {
            getOutput().println("No battle to report on!");
            return null;
        }
        if (battle.getError() != null) {
            getOutput().println("Error: " + battle.getError());
            return battle;
        }

        _stats.addBattle(battle);

        try {
            // could add some exec time, let it test
            getClan(); // comes with a Daiymo butt sniffing
        } catch (IOException e) {
            logError(e);
        }

        StringBuffer buff = new StringBuffer();
        if (!"win".equals(battle.getResult())) {
            buff.append("YOU LOST!!\n");
        } else {
            buff.append("You won!\n");
        }
        if (battle.isLeveled()) {
            buff.append("\nYOU LEVELED to " + (battle.getMe().getLevel() + 1) + "!!\n");
        }
        Spoils spoils = battle.getSpoils();
        if (spoils != null) {
            buff.append("Experience: " + spoils.getExp() + "\n");
            buff.append("Gold: " + spoils.getGold() + "\n");
            Item[] items = spoils.getItems();
            if (items != null && items.length > 0) {
                buff.append("Items: ");
                for (Item item : items) {
                    buff.append(item.getName());
                    if (item.getKarma_value() > 0) {
                        buff.append("(!)");
                    }
                    buff.append(", ");
                }
                buff.delete(buff.length() - 2, buff.length()).append("\n");
            }

            if (spoils.getKills() > 0) {
                buff.append("\nKills: " + spoils.getKills()+ "\n");
            }
            if (spoils.isHigh_score()) {
                buff.append("\nA NEW HIGH SCORE!\n");
            }
        } else {
            buff.append("Sorry, no spoils :-(\n");
        }
        output.println(buff.toString());
        /* errors in battle are in the form of, "stop picking on them"... not an error
                if ("error".equals(battle.getResult()) && pauseOnError()) {
                   pause();
                }
         */
        return battle;
    }

    /**
     * Prints the heal results from the heal JSON
     * @param output
     * @param healJSON
     * @throws IOException
     */
    public void reportHeal(PrintStream output, String healJSON) throws IOException {
        // TODO: I feel like the caller of the Heal connection should've parsed the json
        HospitalResult result = _jsonParser.parseJSON(healJSON, HospitalResult.class);
        _stats.addHospital(result);
        if (result != null && !result.isError()) {
            String msg = "You spent " + result.getGold_spent() + " healing.";
            output.println(msg);
        } else if (result != null && result.isError()) {
            String msg = "There was an error healing: " + result.getError();
            output.println(msg);
        } else {
            output.println("No heal results!");
        }
    }

    /**
     * Resumes a paused action queue
     */
    public void resumeQueue() {
        getOutput().println("Resuming Action Queue...");
        _runner.resume();
    }

    public void resumeTriggers() {
        getOutput().println("Resuming Trigger Evaluator...");
        _evaluator.resume();
    }

    /**
     * Given an item id, this will sell all of those items from inventory.  This is how you unload
     * 1,000+ Negi and Fried Shrimp!
     * @param itemId
     * @throws IOException
     */
    public void sellAllItem(int itemId) throws IOException {
        Item[] items = getClan().getInventory();
        for (Item item : items) { // this looping just ensures we sell the right number
            if (item.getIid() == itemId) {
                // sell it!
                sellItem(itemId);
            }
        }
    }

    /**
     * Given an item id, sells one item of that id from inventory
     * @param item
     * @throws IOException
     */
    public void sellItem(int item) throws IOException {
        // sells one item from your inventory
        Item sellIt = getItem(item);
        HTTPConnection connection = _connectionManager.getSellConnection(item);
        ByteOutputStream bos = new ByteOutputStream();
        connection.setOutputStream(bos);
        connection.connect();
        // We get a Result json
        String sellJSON = bos.toString();
        Result result = _jsonParser.parseJSON(sellJSON, Result.class);
        if ("success".equals(result.getResult())) {
            String sellResult = "You sold item " + item + " for " + result.getSold_for();
            if (sellIt != null) {
                if (sellIt.getKarma_value() > 0) {
                    sellResult += " Karma";
                } else {
                    sellResult += " Gold";
                }
            }
            getOutput().println(sellResult);
        } else {
            getOutput().println(result.getResult() + ": " + result.getError());
        }
    }

    /**
     * Sets the iterations for fights done to a target clan.  The default is
     * {@value #DEFAULT_FIGHTS}.  
     * @param count
     */
    public void setFightCount(int count) {
        _config.setProperty(Configuration.PREF_FIGHT_COUNT, count);
    }

    /**
     * Sets the delay used to rest the ninjas in between fights.  The default is
     * {@value #DEFAULT_DELAY}.  Note: in development, it was observed that 9 seconds
     * was the minimum possible value to use that would allow the ninjas to start the
     * next fight.  However, 9 seconds also seemed to be right on the bubble as sometimes
     * the server would report back that the ninjas still needed rest.  In practice, 10 seconds
     * is the quickest reliable delay.
     * @param delay
     */
    public void setFightDelay(int delay) {
        getOutput().println("Setting delay: " + delay);
        if (delay <= 0) {
            getOutput().println("Delay value too low!");
            return;
        }
        _config.setProperty(Configuration.PREF_FIGHT_DELAY, String.valueOf(delay));
    }

    /**
     * When set to true, the bot will scan for standard input from
     * the user.  When set to false, it's assume the application class
     * will be directing NinjaBot
     * @param interactive
     */
    public void setInteractive(boolean interactive) {
        _interactive = interactive;

    }

    /**
     * When set to true, logging is enabled.  A log file will be created in the system temp directory
     * named {@value #DEFAULT_NINJA_BOT_LOG}
     * @param doLogging
     */
    public void setLogging(boolean doLogging) {
        if (doLogging) {
            String logFileName = _config.getString(Configuration.PREF_LOG_FILE_NAME, DEFAULT_NINJA_BOT_LOG);
            File logFile = new File(System.getProperty("java.io.tmpdir") + File.separator + logFileName);
            try {
                _log = new PrintStream(new FileOutputStream(logFile, true));
                getOutput().println("Logging is enabled: " + logFile.getAbsolutePath());
            } catch (FileNotFoundException e) {
                e.printStackTrace(getOutput());
            }
        } else {
            _log = new PrintStream(new NullOutputStream());
            getOutput().println("Logging is disabled");
        }
        _config.setProperty(Configuration.PREF_ENABLE_LOGGING, doLogging);
    }

    /**
     * Sets the NinjaCommand collection to be used
     * @param commands
     * @see #addNinjaCommand(NinjaCommand)
     */
    public void setNinjaCommands(Collection<NinjaCommand> commands) {
        if (commands != null) {
            _commands = new TreeSet<NinjaCommand>(commands);
        }
    }

    /**
     * Sets the session id
     * @param phpSessionId
     */
    public void setPhpSession(String phpSessionId) {
        _phpSession = phpSessionId;
        String msg = "Setting phpSession: " + phpSessionId + "\n";
        PrintStream output = getOutput();
        if (output != null) {
            output.println(msg);
        } else {
            // we do this check because setPhpSession could be called very early and possibly
            // before our output streams are setup
            System.out.println(msg);
        }
        _config.setProperty(Configuration.PREF_PHP_SESSION, _phpSession);

        if (_initialized) {
            Clan me = null;
            try {
                me = getClan();
            } catch (IOException e) {
                getOutput().println("There was an error fetching your clan: " + e.getMessage());
                logError(e);
            }
            if (me != null) {
                printClan(me);
            }
        }

    }

    /*
     * Creates the thread runner to run the action queue
     */
    private void setupFighterPool() {
        _runner = new ThreadRunner(1);
        _runner.setReportIncrement(0);
    }

    /*
     * Sets up the output streams
     */
    private void setupOutputStreams() throws IOException {
        _console = new OutputConsole("NinjaBot Monitor (" + getNinjaBotFlavor() + ")");
        _out = new PrintStream(_console.getOutput());
        _stdout = System.out;
    }

    public Clan[] getAllies(String sortColumn) throws NinjaBotException, IOException {
        Clan clan = getClan();
        Clan[] allies = clan.getAllies();
        if (allies != null && allies.length > 0) {
            if (sortColumn == null || Clan.COL_LEVEL.equals(sortColumn)) {
                // use the default sort
                Arrays.sort(allies);
            } else if (Clan.COL_NAME.equals(sortColumn)) {
                // use the name comparator
                Arrays.sort(allies, new Clan.NameComparator());
            } else if (Clan.COL_CID.equals(sortColumn)) {
                // use the level comparator
                Arrays.sort(allies, new Clan.CidComparator());
            } else {
                // your sort column sucks
                throw new NinjaBotException(sortColumn + " is not a vallid sort for clans.  Currently " + Arrays.toString(Clan.SORT_COLUMNS) + " are supported");
            }    
        }
        return allies;
    }

    /**
     * Lists all allies
     * @param sortColumn
     * @throws IOException
     * @throws NinjaBotException
     */
    public void showAllies(String sortColumn) throws IOException, NinjaBotException {

        Clan[] allies = getAllies(sortColumn);
        if (allies != null && allies.length > 0) {
            getOutput().println(LINE_SEP);
            for (Clan ally : allies) {
                getOutput().printf("%-10s", ally.getCid());
                String assistanceNeeded = ally.getNeeds_assistance() > 0 ? "*" : "";
                getOutput().printf("%-20s", assistanceNeeded + ally.getName());
                getOutput().println("Lvl " + ally.getLevel());
            }
        } else {
            getOutput().println("Looks like you have no friends.  Are you a terrible person?");
        }
    }

    public void showBosses() throws IOException {
        Stage stage = getStage();
        BossCollection bosses = stage.getBosses();

        int COL_NAME_LEN = 12;
        int COL_HEALTH_LEN = 7;
        int COL_ARMOR_LEN = 7;
        int COL_ATTACK_MODE_LEN = 25;
        int COL_ABILITIES_LEN = 65;
        int COL_HEALTH_TRIGGERS_LEN = 17;
        int COL_SPOILS_LEN = 36;

        getOutput().printf("%-" + COL_NAME_LEN + "s", "Name");
        getOutput().printf("%-" + COL_HEALTH_LEN + "s", "Health");
        getOutput().printf("%-" + COL_ARMOR_LEN + "s", "Armor");
        getOutput().printf("%-" + COL_ATTACK_MODE_LEN + "s", "Attack Mode");
        getOutput().printf("%-" + COL_ABILITIES_LEN + "s", "Abilities");
        getOutput().printf("%-" + COL_HEALTH_TRIGGERS_LEN + "s", "Health Triggers");
        getOutput().printf("%-" + COL_SPOILS_LEN + "s", "Spoils");
        getOutput().println();
        getOutput().printf("%-" + COL_NAME_LEN + "s", Util.repeatChar(COL_NAME_LEN - 1, '-'));
        getOutput().printf("%-" + COL_HEALTH_LEN + "s", Util.repeatChar(COL_HEALTH_LEN - 1, '-'));
        getOutput().printf("%-" + COL_ARMOR_LEN + "s", Util.repeatChar(COL_ARMOR_LEN - 1, '-'));
        getOutput().printf("%-" + COL_ATTACK_MODE_LEN + "s", Util.repeatChar(COL_ATTACK_MODE_LEN - 1, '-'));
        getOutput().printf("%-" + COL_ABILITIES_LEN + "s", Util.repeatChar(COL_ABILITIES_LEN - 1, '-'));
        getOutput().printf("%-" + COL_HEALTH_TRIGGERS_LEN + "s", Util.repeatChar(COL_HEALTH_TRIGGERS_LEN - 1, '-'));
        getOutput().printf("%-" + COL_SPOILS_LEN + "s", Util.repeatChar(COL_SPOILS_LEN - 1, '-'));
        getOutput().println();
        // headers are done, print items
        if (bosses == null || bosses.isEmpty()) {
            getOutput().println("(No bosses are avaialble)");
            return;
        }
        for (Boss boss : bosses) {
            for (Boss.Entity entity : boss.getEntities()) {
                boolean first = true;
                String atkMode = entity.getAttack_mode();
                if ("ordered".equals(atkMode)) {
                    atkMode += " " + Arrays.toString(entity.getAttack_order());
                }
                for (int i = 0; i < entity.getAbilities().length; i++) {
                    if (first) {
                        getOutput().printf("%-" + COL_NAME_LEN + "s", boss.getInternal_name());
                        getOutput().printf("%-" + COL_HEALTH_LEN + "s", entity.getHealth());
                        getOutput().printf("%-" + COL_ARMOR_LEN + "s", entity.getArmor());
                        getOutput().printf("%-" + COL_ATTACK_MODE_LEN + "s", atkMode);
                        getOutput().printf("%-" + COL_ABILITIES_LEN + "s", "[" + i + "] " + entity.getAbilities()[i]);
                        getOutput().printf("%-" + COL_HEALTH_TRIGGERS_LEN + "s", Arrays.toString(entity.getHealth_triggers()));
                        getOutput().printf("%-" + COL_SPOILS_LEN + "s", boss.getGold() + " Gold, " + boss.getExp() + " XP, " + boss.getItem_drops()[0] + " Item ID");
                        getOutput().println();
                    } else {
                        getOutput().printf("%-" + COL_NAME_LEN + "s", " ");
                        getOutput().printf("%-" + COL_HEALTH_LEN + "s", " ");
                        getOutput().printf("%-" + COL_ARMOR_LEN + "s", " ");
                        getOutput().printf("%-" + COL_ATTACK_MODE_LEN + "s", " ");
                        getOutput().printf("%-" + COL_ABILITIES_LEN + "s", "[" + i + "] " + entity.getAbilities()[i]);
                        getOutput().printf("%-" + COL_HEALTH_TRIGGERS_LEN + "s", " ");
                        getOutput().printf("%-" + COL_SPOILS_LEN + "s", " ");
                        getOutput().println();
                    }
                    first = false;
                }
            }
            getOutput().printf("%-" + COL_NAME_LEN + "s", Util.repeatChar(COL_NAME_LEN - 1, '-'));
            getOutput().printf("%-" + COL_HEALTH_LEN + "s", Util.repeatChar(COL_HEALTH_LEN - 1, '-'));
            getOutput().printf("%-" + COL_ARMOR_LEN + "s", Util.repeatChar(COL_ARMOR_LEN - 1, '-'));
            getOutput().printf("%-" + COL_ATTACK_MODE_LEN + "s", Util.repeatChar(COL_ATTACK_MODE_LEN - 1, '-'));
            getOutput().printf("%-" + COL_ABILITIES_LEN + "s", Util.repeatChar(COL_ABILITIES_LEN - 1, '-'));
            getOutput().printf("%-" + COL_HEALTH_TRIGGERS_LEN + "s", Util.repeatChar(COL_HEALTH_TRIGGERS_LEN - 1, '-'));
            getOutput().printf("%-" + COL_SPOILS_LEN + "s", Util.repeatChar(COL_SPOILS_LEN - 1, '-'));
            getOutput().println();
        }

    }

    /**
     * Prints the fight iteration value
     */
    public void showFightCount() {
        getOutput().println("Current fight count: " + getFightCount());
    }

    /**
     * Prints the command line usage for the console
     */
    public void showHelp() {
        getOutput().println();
        for (NinjaCommand command : _commands) {
            getOutput().printf("%-20s", command.getName());
            getOutput().println(command.getDescription());
            //_stdout.printf(command.getName() + "%s20", command.getDescription() + "\n");
            //_stdout.println(command.getName() + ":\t" + command.getDescription());
            /*
            for (String alias : command.getAliases()) {
                _stdout.println("Alias: " + alias);
            }
             */
        }
    }

    /**
     * Prints the usage statement for the given command
     * @param command
     */
    public void showHelp(String command) {
        List<NinjaCommand> foundCommands = findCommands(command);
        if (foundCommands.size() == 0) {
            getOutput().println(command + COMMAND_NOT_FOUND);
        } else if (foundCommands.size() > 1) {
            getOutput().println(MULTIPLE_COMMANDS_FOUND + foundCommands);
        } else {
            NinjaCommand ninjaCommand = foundCommands.get(0);
            getOutput().println(ninjaCommand.getName() + ": " + ninjaCommand.getDescription());
        }
    }

    /**
     * Prints the current inventory
     * @throws IOException
     */
    public void showInventory() throws IOException {
        Clan clan = getClan();
        Item[] items = clan.getInventory();
        Arrays.sort(items);
        showItems(Arrays.asList(items));
    }

    /**
     * Prints the details of a specific item from inventory
     * TODO: if the item isn't in inventory, check the equipped items
     * @param itemId
     * @throws IOException
     */
    public void showItem(int itemId) throws IOException {
        // get item via id
        Clan me = getClan();
        if (me == null) {
            getOutput().println("Sorry, I don't know about you yet...");
            return;
        }

        Item foundItem = getItem(itemId);

        if (foundItem == null) {
            getOutput().println("You don't have one of those!");
            return;
        }
        String type = null;
        switch (foundItem.getType()) {
        case Item.TYPE_WEAPON: type = "Weapon"; break;
        case Item.TYPE_RELIC: type = "Relic"; break;
        default: type = "UNKNOWN: " + foundItem.getType();
        }
        getOutput().println();
        String valueString = String.valueOf(foundItem.getValue() > 0 ? foundItem.getSellValue() + " Gold" : foundItem.getSellValue() + " Karma");
        getOutput().println(foundItem.getName() + "  (" + type + ")  " + valueString);
        Map attrs = foundItem.getAttributes();
        getOutput().println(attrs);
        // dps is under attributes :-.


        // show item stats all pretty like: Name, DPS, Value (gold / karma), 
        // debug, show 'type', might help me group weapons to relics
    }

    /**
     * Prints the given collection of items
     * @param items
     */
    protected void showItems(List<Item> items) {
        String id = "id";
        String count = "cnt";
        String name = "name";
        String value = "value";
        String attributes = "attributes";
        String[] fields = {id, count, name, value, attributes};
        Map<String, Integer> columns = new HashMap<String, Integer>();
        columns.put(id, 5); columns.put(count, 6);
        columns.put(name, 27); columns.put(value, 15);
        columns.put(attributes, 20);
        
        ColumnConfig standardConfig = new ColumnConfig(columns, fields);
        standardConfig.addPropertyMap(id, "iid");
        
        DetailPrinter printer = new DetailPrinter(getOutput(), standardConfig);
        
        ColumnConfig lineConfig = null; // this is a bit dynamic
        
        ItemAggregator aggregator = new ItemAggregator(items);
        boolean printSep = false;
        for (Item item : aggregator.getUniqueItems()) {
            lineConfig = new ColumnConfig(standardConfig);
            
            if (!printSep && item.getType() == Item.TYPE_RELIC) {
                printSep = true;
                getOutput().println(LINE_SEP);
            }
            
            Map<String, String> dataLine = new HashMap<String, String>();
            String markedName = item.getName();
            if (item.getKarma_value() > 0) {
                // shift the count field
                lineConfig.setColumn(count, lineConfig.getLength(count) - 2);
                lineConfig.setColumn(name, lineConfig.getLength(name) + 2);
                markedName = "* " + markedName;
            }
            dataLine.put(name, markedName);
            dataLine.put(id, String.valueOf(item.getIid()));
            dataLine.put(count, String.valueOf(aggregator.getCount(item)));
            
            String costString = null;
            if (item.getKarma_value() > 0) {
                costString = item.getSellValue() +  " Karma";
            } else {
                costString = item.getSellValue() + " Gold";
            }
            dataLine.put(value, costString);
            dataLine.put(attributes, item.getAttributes().toString());

            printer.printLn(dataLine, lineConfig);
        }
    }

    public void showMagic() {
        showMagic(true);
    }

    public void showMagic(boolean forLevel) {
        // sid
        // Name
        // dmg
        // duration
        // karma cost

        try {
            String[] fields = {"sid", "name", "damage", "duration", "karma"};
            Map<String, Integer> columns = new HashMap<String, Integer>();
            columns.put("sid", 5);
            columns.put("name", 30);
            columns.put("damage", 7);
            columns.put("duration", 9);
            columns.put("karma", 4);

            ColumnConfig config = new DetailPrinter.ColumnConfig(columns, fields);
            config.addPropertyMap("karma", "karma_value");
            DetailPrinter printer = new DetailPrinter(getOutput(), config);
            long level = getClan().getLevel();
            for (Magic magic: getMagic()) {
                if (forLevel && magic.getMin_level() <= level || !forLevel) {
                    printer.printLn(magic);
                }
            }

        } catch (IOException e) {
            logError(e);
        }
    }

    /**
     * Prints the news
     */
    public void showNews() {
        try {
            String news = getNews();
            getOutput().print(news);
        } catch (IOException e) {
            logError(e);
        }
    }

    public void showNinja(long nid) throws IOException {
        Clan me = getClan();
        for (Ninja ninja : me.getNinjas()) {
            if (nid == ninja.getNid()) {
                showNinja(ninja);
                return;
            }
        }
    }

    public void showNinja(Ninja ninja) {
        int COL_VALUE_LEN = 15;
        getOutput().printf("%-" + COL_VALUE_LEN + "s", "nid:");
        getOutput().println(ninja.getNid());

        getOutput().printf("%-" + COL_VALUE_LEN + "s", "name:");
        getOutput().println(ninja.getName());

        getOutput().printf("%-" + COL_VALUE_LEN + "s", "Max Health:");
        getOutput().println(ninja.getModified_max_health());

        getOutput().printf("%-" + COL_VALUE_LEN + "s", "Max Power:");
        getOutput().println(ninja.getModified_power());

        getOutput().printf("%-" + COL_VALUE_LEN + "s", "Weapon:");
        getOutput().println(ninja.getWeapon());

        getOutput().printf("%-" + COL_VALUE_LEN + "s", "Gender:");
        getOutput().println(ninja.getGenderStr());

        getOutput().printf("%-" + COL_VALUE_LEN + "s", "Birthdate:");
        getOutput().println(ninja.getBirthdate());

        getOutput().printf("%-" + COL_VALUE_LEN + "s", "Blood Type:");
        getOutput().println(ninja.getBlood_type());

    }
    
    public void showNinjas(boolean showHeader) throws IOException {
        Ninja[] ninjas = getClan().getNinjas();
        showNinjas(ninjas, showHeader);
    }
    
    public void showNinjas(Ninja[] ninjas, boolean showHeader) {
        String nid = "nid";
        String name = "name";
        String level = "lvl";
        String power = "currPower";
        String health = "currHealth";
        String orig_power = "initPow";
        String orig_health = "initHealth";
        String weapon = "weapon";
        String[] fields = {nid, name, level, orig_power, orig_health, power, health, weapon};
        Map<String, Integer> columns = new HashMap<String, Integer>();
        
        columns.put(nid, 11);
        columns.put(name, 38);
        columns.put(level, 5);
        columns.put(power, 14);
        columns.put(health, 16);
        columns.put(orig_power, 12);
        columns.put(orig_health, 12);
        columns.put(weapon, 50);
        
        ColumnConfig standardConfig = new ColumnConfig(columns, fields);
        DetailPrinter printer = new DetailPrinter(getOutput(), standardConfig);
        printer.setPrintHeader(showHeader);
        
        for (Ninja ninja : ninjas) {
            Map<String, String> data = new HashMap<String, String>();
            ColumnConfig lineConfig = new ColumnConfig(standardConfig);
            data.put(nid, String.valueOf(ninja.getNid()));
            
            String superiorName = ninja.getName();
            if (ninja.getSuperior() > 0) {
                superiorName = "* " + superiorName;
                lineConfig.setColumn(name, lineConfig.getLength(name) + 2);
                lineConfig.setColumn(nid, lineConfig.getLength(nid) - 2);
            }
            data.put(name, superiorName);
            data.put(level, String.valueOf(ninja.getLevel()));
            data.put(power, String.valueOf(ninja.getPower()));
            data.put(health, String.valueOf(ninja.getHealth()));
            data.put(orig_power, String.valueOf(ninja.getInitial_power()));
            data.put(orig_health, String.valueOf(ninja.getInitial_health()));
            Item item = ninja.getWeapon();
            String weaponString = item.getIid() + " : " + item.getName() + " DPS: " + item.getAttributes().get("DPS"); 
            data.put(weapon, weaponString);
            printer.printLn(data, lineConfig);
        }
    }

    /**
     * Prints the clan details
     * TODO: change to 'showClan'; should match our other *Clan methods
     * @param cid
     * @throws IOException
     */
    public void showOpponent(String cid) throws IOException {
        Clan foundClan = null;
        if (cid.matches("^\\d*$")) {
            // digits
            foundClan = getClan(cid);
        }
        if (foundClan == null) { // try anyways
            Clan ally = findAllyByName(cid);
            if (ally != null) {
                String allyId = String.valueOf(ally.getCid());
                foundClan = getClan(allyId);
            }
        }
        if (foundClan == null || foundClan.getCid() == 0) {
            getOutput().println("Could not find a clan by that id: " + cid);
        } else {
            printClan(foundClan);
        }
    }

    /**
     * Lists the opponents available to fight in the fighting balloon
     * @param levelDelta
     * @throws IOException
     */
    public void showOpponents(int levelDelta) throws IOException {
        getOutput().println("Listing all opponents " + levelDelta + " levels");
        List<Clan> opponents = getOpponents(levelDelta);
        for (Clan opp : opponents) {
            String opponentLine = opp.getCid() + ": " + opp.getName() + " -- " + opp.getNum_ninjas() + " ninjas";
            getOutput().println(opponentLine);
        }
    }

    /**
     * Prints the current session ID
     */
    public void showPhpSession() {
        getOutput().println("PHP Session ID: " + _phpSession);
    }

    /**
     * Displays the current commands queued in the action queue
     */
    public void showQueue() {
        List<TRRunnable> runnables = _runner.getWorkItems();
        if (runnables == null || runnables.size() == 0) {
            getOutput().println("The action queue is empty.");
        }
        for (TRRunnable runnable : runnables) {
            NinjaRunnable nRunnable = (NinjaRunnable)runnable;
            getOutput().println(nRunnable.getDescription());
        }
    }

    /**
     * Prints the current availble Ninja recruits
     * @throws IOException
     */
    public void showRecruits() throws IOException {
        // TODO: refactor to use the DetailPrinter
        getOutput().println("Listing all new recruits");
        List<Recruit> recruits = getRecruits();
        // nid, gender, name, power, health, blood type, birth day
        String col1Format = "%-39s";
        String col2Format = "%-10s";
        String col3Format = "%-25s";
        String col4Format = "%-8s";
        String col5Format = "%-8s";
        String col6Format = "%-6s";
        PrintStream out = getOutput();
        out.printf(col1Format, "rnid");
        out.printf(col2Format, "Gender");
        out.printf(col3Format, "Name");
        out.printf(col4Format, "Power");
        out.printf(col5Format, "Health");
        out.printf(col6Format, "Blood");
        out.println("Birth Date");
        out.printf(col1Format, "-------------------------------------");
        out.printf(col2Format, "------");
        out.printf(col3Format, "----");
        out.printf(col4Format, "-----");
        out.printf(col5Format, "------");
        out.printf(col6Format, "-----");
        out.println("----------");
        for (Ninja recruit : recruits) {
            String superior = recruit.getSuperior() == 0 ? "* " : "  ";
            out.printf(col1Format, recruit.getRnid());
            out.printf(col2Format, superior + recruit.getGenderStr());
            out.printf(col3Format, recruit.getName());
            out.printf(col4Format, recruit.getPower());
            out.printf(col5Format, recruit.getHealth());
            out.printf(col6Format, recruit.getBlood_type());
            out.println(recruit.getBirthdate().getPretty());
        }

    }

    /**
     * Prints the current statistics
     * @throws IOException
     */
    public void showStats() throws IOException {
        getOutput().println(LINE_SEP);
        _stats.printStats(getOutput());
        getOutput().println(LINE_SEP);
        printMe();
    }

    /**
     * Prints the action queue status
     */
    public void showStatus() {
        getOutput().println(getStatus());
    }

    /**
     * Prints the next time the Daiymo may be visited
     * @throws IOException 
     */
    public void showVisitDaiymo() throws IOException {
        long nextVisit = getClan().getDaimyo_gift();
        Date nextDate = new Date(nextVisit);
        getOutput().println("Next Daiymo visit: " + nextDate);
    }

    /**
     * Lists the inventory in the weapon shop
     * @throws IOException
     */
    public void showWeaponShop() throws IOException {
        List<Item> weaponShop = getWeaponShop();
        showItems(weaponShop);
    }

    /**
     * Shuts down the bot
     */
    public void shutdown() {
        // print the stats on the way out
        try {
            showStats();
        } catch (Throwable t) {
            logError(t);
        } finally {
            _terminate = true;
        }
    }

    /**
     * Starts the bot
     */
    public void start() {
        try {
            _out.println(getWelcomeMessage());
            readBatchCommands();
            startCommandScanner();
        } finally {
            try {
                // get our trigger properties
                _triggerService.updateProperties();
                _config.writeProperties( _propertyFile);
                BatchCommand.writeBatchCommands(_commands);
            } catch (Throwable e) {
                System.err.println("Error saving properties!");
                e.printStackTrace();
            }
            _runner.shutDown(true);
            _console.exitConsole();
            _evaluator.shutdown();
        }
    }

    /*
     * Starts the command scanner
     */
    private void startCommandScanner() {
        // TODO: I think command interpretation needs to be taken out of the bot and encapsulated
        // in its own class.  But that's a lot of work and this works fine as-is... :-^
        Scanner scanner = new Scanner(System.in);
        boolean setOutput = false;
        PrintStream originalStdOut = _stdout;
        while (!_terminate && _interactive) {
            getOutput().print("> ");
            getOutput().flush(); // so's we can see the prompt
            String commandLine = scanner.nextLine();
            // See if the user asked to redirect
            String[] redirectTokens = commandLine.split(">");
            if (redirectTokens.length > 1) {
                // the second element is the redirect file
                String outputFileName = redirectTokens[1].replace('\\', '/').trim();
                try {
                    FileOutputStream fos = new FileOutputStream(outputFileName);
                    _stdout = new PrintStream(fos);
                    /*
                    FileOutputStream fos = new FileOutputStream(outputFileName);
                    _stdout =  new PrintWriter(fos, true);
                     */
                    setOutput = true;
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    getOutput().println("Can't redirect to " + outputFileName);
                    logError(e);
                }
            }
            commandLine = redirectTokens[0];
            // a command is a string followed by 0 or more strings
            if (commandLine != null && !"".equals(commandLine.trim())) {
                String[] commandTokens = commandLine.split(" ", 2);
                String command = commandTokens[0];
                String arguments = null;
                if (commandTokens.length > 1) {
                    arguments = commandTokens[1];
                }
                NinjaCommand ninjaCommand = processCommand(command, arguments);
                dispatchCommand(ninjaCommand);
            }
            if (setOutput) {
                // put it back
                _stdout.flush();
                _stdout.close();
                _stdout = originalStdOut;
            }
        }
    }

    /**
     * Action used to visit the Daimyo
     * @throws IOException
     */
    public void visitDaimyo() throws IOException {
        HTTPConnection connection = _connectionManager.getDaimyoConnection();
        ByteOutputStream bos = new ByteOutputStream();
        connection.setOutputStream(bos);
        connection.connect();
        String visitJSON = bos.toString();
        DaimyoVisit result = _jsonParser.parseJSON(visitJSON, DaimyoVisit.class);
        Spoils spoils = result.getSpoils();
        _stats.addSpoils(spoils);
        if ("success".equals(result.getResult())) {
            getOutput().println("The Daimyo bestowed " + spoils.getGold() + " gold and " + spoils.getKarma() + " karma!");
        } else {
            getOutput().println("The Daimyo kindly asks that you die in a fire.");
        }
        long waitMillis = result.getWait() * 1000;
        long nextDate = new Date().getTime() + waitMillis;
        Date date = new Date(nextDate);
        getOutput().println("Please visit the Daimyo after " + date);
    }
    
    public void visitGoldenCloud() throws IOException {
        HTTPConnection connection = _connectionManager.getCloudConnection();
        ByteOutputStream bos = new ByteOutputStream();
        connection.setOutputStream(bos);
        connection.connect();
        String visitJSON = bos.toString();
        DaimyoVisit result = _jsonParser.parseJSON(visitJSON, DaimyoVisit.class);
        Spoils spoils = result.getSpoils();
        _stats.addSpoils(spoils);
        if ("success".equals(result.getResult())) {
            getOutput().println("The Golden Cloud bestowed " + spoils.getGold() + " gold!");
        } else {
            getOutput().println("The Golden Cloud kindly asks that you die in a fire.");
        }
        long waitMillis = result.getWait() * 1000;
        long nextDate = new Date().getTime() + waitMillis;
        Date date = new Date(nextDate);
        getOutput().println("Please visit the Golden Cloud after " + date);
    }

    public void addExcludedFighter(String cid) throws IOException {
        _clanService.addFightExclusion(cid);
        getOutput().println(cid + " added to fighter exclusion list");
    }

    public void addExcludedAssist(String cid) throws IOException {
        _clanService.addAssistExclusion(cid);
        getOutput().println(cid + " added to assist exclusion list");
    }

    public void addPreferredAssist(String cid) throws IOException {
        _clanService.addAssistPrefer(cid);
        getOutput().println(cid + " added to assist preferred list");
    }

    public void removeExcludedFighter(String cid) throws IOException {
        if (_clanService.removeFightExclusion(cid)) {
            getOutput().println(cid + " removed from fighter exclusion list");
        } else {
            getOutput().println(cid + " was not found");
        }
    }

    public void removeExcludedAssist(String cid) throws IOException {
        if (_clanService.removeAssistExclusion(cid)) {
            getOutput().println(cid + " removed from assist exclusion list");
        } else {
            getOutput().println(cid + " was not found");
        }
    }

    public void removePreferredAssist(String cid) throws IOException {
        if (_clanService.removeAssistPrefer(cid)) {
            getOutput().println(cid + " removed from assist preferred list");
        } else {
            getOutput().println(cid + " was not found");
        }
    }

    public void showExcludeFight() throws IOException {
        List<Clan> excluded = _clanService.getExcludedFights();
        _clanService.printClanList(excluded);
    }

    public void showExcludeAssist() throws IOException {
        List<Clan> excluded = _clanService.getExludedAssist();
        _clanService.printClanList(excluded);
    }

    public void showPreferredAssist() throws IOException {
        List<Clan> preferred = _clanService.getPreferredAssist();
        _clanService.printClanList(preferred);
    }

    public List<Clan> getPreferredAllies() throws IOException {
        return _clanService.getPreferredAssist();
    }

    public List<Event> getEvents() {
        return _events;
    }

    public void addEvent(Event event) {
        _events.add(event);
    }

    public void removeEvent(Event event) {
        _events.remove(event);
    }
    public void showTriggers() {
        List<Trigger> triggers = new ArrayList<Trigger>();
        triggers.addAll(getTimers());
        triggers.addAll(getEvents());
        _triggerService.printTriggers(triggers);
    }
    public Trigger getTrigger(long id) {
        Trigger getTrigger = null;
        List<Trigger> triggers = new ArrayList<Trigger>(getTimers());
        triggers.addAll(getEvents());
        for (Trigger trigger : triggers) {
            if (trigger.getId() == id) {
                getTrigger = trigger;
                break;
            }
        }

        return getTrigger;
    }

    public void removeTrigger(Trigger deleteTrigger) {
        boolean removed = getTimers().remove(deleteTrigger);
        if (!removed) {
            removed = getEvents().remove(deleteTrigger);
        }
        if (removed) {
            getOutput().println("Removed trigger: " + deleteTrigger);
        } else {
            getOutput().println(deleteTrigger + " was not present!");
        }
    }
    public void removeProperty(String property) {
        _config.removeProperty(property);
    }
    public List<Trigger> getTriggers() {
        List<Trigger> triggers = new ArrayList<Trigger>();
        triggers.addAll(getEvents());
        triggers.addAll(getTimers());
        return triggers;
    }
    public TriggerEvaluator getTriggerEvaluator() {
        return _evaluator;
    }

}
