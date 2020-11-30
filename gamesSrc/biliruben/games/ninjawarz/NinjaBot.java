package biliruben.games.ninjawarz;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import biliruben.files.HTTPConnection;
import biliruben.io.NullOutputStream;

import com.biliruben.tools.threads.ThreadRunner;
import com.biliruben.util.csv.CSVSource;
import com.biliruben.util.csv.CSVSource.CSVType;
import com.biliruben.util.csv.CSVSourceImpl;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

/**
 * Bot used to perform repetitive fighting tasks for the Kongregate flash game Ninja Warz
 * TODO: (v1.0 requirements)
 * - Create an HTTP proxy listener that will intercept http requests and pull out cookie and phpsessid values
 * - Encapsulate the JSON parsing and genericize... look for 3rd party implementations
 * - Take more advantage of the JSON information returned.  See if we can report information like:
 *      How much damage was sustained
 *      Our oponents' karma values
 * - Enhance logging
 * - Looping commands
 * - Add a welcome message in the console so it's not so stark
 * - Create a news aggregator, apart from logger.  Needs a command
 * - Create a font command
 * - Starting to come up with preferences that could use some persistence
 * - Create a Heal command with an output parameter -- heal from the runnable goes to the monitor while
 *      heal from stdin go to stdout
 * - Read Heal JSON response and return success, money spent
 * - Create an event scheduler so commands can be scheduled.  Quartz?
 * - Create a Daimyo command
 * - Sniff out what it takes to inventory Weapons, Artifacts
 * - Refactor NinjaRunnable and pull out specifics and make it more abstract.  It may need to be renamed to reflect its
 *      pure fighting nature so that future runnables can likewise be appropriately catagorized
 * - Refactor the OutputConsole to the api package.  It's not tied to NinjaBot
 * - Commands: drop the aliases and just rename them to what I'm more often using
 * - Listen to the news and alert on key events (like leveling)
 * 
 * @author trey.kirk
 *
 */
public class NinjaBot {

    private static final String NINJA_BOT_LOG = "NinjaBot.log";

    private static class CommandDescription implements Comparable<CommandDescription> {
        private List<String> _aliases;
        private Class<? extends NinjaCommand> _commandClass;
        private String _commandDescription;
        private String _commandName;

        CommandDescription(String name, Class<? extends NinjaCommand> clazz, String description) {
            _commandName = name;
            _commandClass = clazz;
            _commandDescription = description;
            _aliases = new ArrayList<String>();
        }

        void addAlias(String alias) {
            _aliases.add(alias);
        }

        List<String> getAliases() {
            return _aliases;
        }

        Class<? extends NinjaCommand> getCommand() {
            return _commandClass;
        }

        String getDescription() {
            return _commandDescription;
        }

        String getName() {
            return _commandName;
        }

        boolean matchesName (String partial) {
            boolean matched = false;
            String tempPartial = partial.toLowerCase();
            String tempActual = _commandName.toLowerCase();
            matched = tempActual.startsWith(tempPartial);

            if (!matched && _aliases.size() > 0) {
                Iterator<String> aliasIt = _aliases.iterator();
                while (aliasIt.hasNext() && !matched) {
                    String alias = aliasIt.next().toLowerCase();
                    matched = alias.startsWith(partial);
                }
            }
            return matched;
        }

        @Override
        public int compareTo(CommandDescription o) {
            if (o == null) {
                return 1;
            } else {
                return this.getName().compareTo(o.getName());
            }
        }
        
        @Override
        public String toString() {
            StringBuffer str = new StringBuffer();
            str.append(_commandName);
            if (_aliases != null && _aliases.size() > 0) {
                str.append(" (");
                for (String alias : _aliases) {
                    str.append(alias).append(" ");
                }
                str.append(")");
            }
            str.append(": ").append(_commandDescription);
            return str.toString();
        }

    }

    public static final String BASE_URL = "http://kongregate.ninjawarz.brokenbulbstudios.com";
    public static final int DEFAULT_DELAY = 10;
    public static final int DEFAULT_FIGHTS = 5;
    public static final int DEFAULT_LEVEL_DELTA = -4;
    public static final String FIGHT_URL = BASE_URL + "//ajax/fight";
    public static final String HEAL_URL = BASE_URL + "/ajax/hospital";
    public static final String NEWS_URL = BASE_URL + "/ajax/get_flat_news";
    public static final String OPP_LIST_URL = BASE_URL + "//ajax/get_opponents";
    public static final boolean REQUIRE_COOKIE = false;
    public static final String VERSION = "0.1";
    public static void buildCommonRequestProperties(HTTPConnection connection, NinjaBot bot) throws NinjaBotException {
        connection.setRequestProperty("Host", "kongregate.ninjawarz.brokenbulbstudios.com");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:14.0) Gecko/20100101 Firefox/14.0.1");
        connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        connection.setRequestProperty("Accept-Language", "en-us,en;q=0.5");
        //connection.setRequestProperty("Accept-Encoding", "gzip, deflate"); // we don't care / want compression
        connection.setRequestProperty("DNT", "1");
        connection.setRequestProperty("Connection", "keep-alive");
        String cookie = bot.getCookie();
        if (cookie != null) {
            connection.setRequestProperty("Cookie", cookie);
        }

        connection.setRequestProperty("Referer", "http://kongregate.ninjacdn.brokenbulbstudios.com/swf/game.swf?880");
        connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
    }
    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        NinjaBot bot = new NinjaBot();
        //System.setOut(bot.getOutput());
        bot.start();
    }
    private CommandDescription _addFighterCD;
    private Set<CommandDescription> _commands;

    private OutputConsole _console;


    private String _cookie;
    private int _delay = NinjaBot.DEFAULT_DELAY;
    private int _fightCount;
    private OutputStream _log;
    private PrintStream _out;
    private boolean _pauseOnError;
    private String _phpSession;

    private ThreadRunner _runner;


    private PrintStream _stdout;


    private boolean _terminate = false;

    public NinjaBot() throws IOException {
        _fightCount = NinjaBot.DEFAULT_FIGHTS;
        setupOutputStream();
        setLogging(false);
        setupFighterPool();
        setupCommandDescriptions();
    }

    public void addFighter(String fighterId) {
        addFighter(fighterId, _fightCount);
    }

    public void addFighter(String fighterId, int iterations) {
        _stdout.println("Adding opponent: " + fighterId + ", " + iterations + " fights.");
        FightingRunnable ninjaRunnable = new FightingRunnable(this, fighterId);
        ninjaRunnable.setFights(iterations);
        ninjaRunnable.setDelay(_delay);
        _runner.add(ninjaRunnable);

    }

    public void clearQueue() {
        _stdout.println("...clearing queue");
        _runner.clear();
        showStatus();
    }

    public void fightAllOpponents(int levelDelta) {
        List<String[]> opponents = getOpponents(levelDelta);
        for (String [] opponent : opponents) {
            addFighter(opponent[0]);
        }
    }

    private List<CommandDescription> findCommands(String command) {
        List<CommandDescription> foundCommands = new ArrayList<CommandDescription>();
        for (CommandDescription testCommand : _commands) {
            if (testCommand.getName().equals(command)) {
                // exact match, no further looking
                foundCommands.clear(); // remove anyting we've already found
                foundCommands.add(testCommand);
                break;
            }
            if (testCommand.matchesName(command)) {
                foundCommands.add(testCommand);
            }
        }
        return foundCommands;
    }

    public String getCookie() throws NinjaBotException {
        if (_cookie == null && REQUIRE_COOKIE) {
            throw new NinjaBotException("Cookie has not been set yet!");
        } else {
            return _cookie;
        }
    }

    public HTTPConnection getFightConnection(String fighterId) throws MalformedURLException {

        URL url = new URL(NinjaBot.FIGHT_URL);
        HTTPConnection fightConnection = new HTTPConnection(url, getLoggingStream());
        try {
            String token = "PHPSESSID=" + getPhpSession() + "&opponent=" + fighterId;
            fightConnection.setContentToken(token);
            NinjaBot.buildCommonRequestProperties(fightConnection, this);
            fightConnection.setRequestProperty("Content-length", String.valueOf(token.length()));

        } catch (NinjaBotException e) {
            // problem, send it to the log and the bot
            e.printStackTrace(System.err);
            e.printStackTrace(new PrintStream(getLoggingStream()));
            // and shuter down
            if (pauseOnError()) {
                pause();
            }
        }
        return fightConnection;
    }

    public HTTPConnection getHealConnection() throws MalformedURLException {

        URL url = new URL(NinjaBot.HEAL_URL);
        HTTPConnection healConnection = new HTTPConnection(url, getLoggingStream());
        try {
            String token = "PHPSESSID=" + getPhpSession();
            healConnection.setContentToken(token);
            //buildCommonRequestProperties(healConnection);
            healConnection.setRequestProperty("Content-length", String.valueOf(token.length()));
        } catch (NinjaBotException e) {
            // problem, send it to the log and the bot
            e.printStackTrace(System.err);
            e.printStackTrace(new PrintStream(getLoggingStream()));
            // and shuter down
            if (pauseOnError()) {
                pause();
            }
        }
        return healConnection;
    }

    public OutputStream getLoggingStream() {
        return _log;
    }

    public String getNews() throws IOException {
        ByteArrayOutputStream newsOutput = new ByteArrayOutputStream();
        HTTPConnection newsConnection = getNewsConnection(newsOutput);
        newsConnection.connect();
        String rawNews = newsOutput.toString();
        newsOutput = new ByteArrayOutputStream();
        NewsParser.parseNews(rawNews, newsOutput);
        return newsOutput.toString();
    }

    public HTTPConnection getNewsConnection(ByteArrayOutputStream bos) throws MalformedURLException {
        /*
        if (_newsConnection != null) {
            return _newsConnection;
        }*/


        URL url = new URL(NinjaBot.NEWS_URL);
        HTTPConnection _newsConnection = new HTTPConnection(url, bos);
        try {
            String token = "PHPSESSID=" + getPhpSession();
            _newsConnection.setContentToken(token);
            NinjaBot.buildCommonRequestProperties(_newsConnection, this);
            _newsConnection.setRequestProperty("Content-length", String.valueOf(token.length()));
        } catch (NinjaBotException e) {
            // problem, send it to the log and the bot
            e.printStackTrace(System.err);
            e.printStackTrace(new PrintStream(getLoggingStream()));
            if (pauseOnError()) {
                pause();
            }
        }
        return _newsConnection;
    }

    private List<String[]> getOpponents(int levelDelta) {
        String opponentResponse = null;
        List<String[]> opponents = null;
        try {        
            URL url;
            try {
                url = new URL(NinjaBot.OPP_LIST_URL + "/" + levelDelta + "/0/50");
            } catch (MalformedURLException e) {
                throw new NinjaBotException(e);
            }
            ByteOutputStream bytes = new ByteOutputStream();
            HTTPConnection oppConnection = new HTTPConnection(url, bytes);

            String token = "PHPSESSID=" + getPhpSession();
            oppConnection.setContentToken(token);
            NinjaBot.buildCommonRequestProperties(oppConnection, this);
            try {
                oppConnection.connect();
                opponentResponse = bytes.toString();
            } catch (IOException e) {
                throw new NinjaBotException(e);
            }
        } catch (NinjaBotException e) {
            e.printStackTrace(System.err);
        }
        if (opponentResponse != null && !"".equals(opponentResponse.trim())) {
            opponents = parseOpponentListJSON(opponentResponse);
        }
        return opponents;
    }

    public PrintStream getOutput() {
        return _out;
    }

    public String getPhpSession() throws NinjaBotException {
        if (_phpSession == null) {
            throw new NinjaBotException("PHP Session ID has not been set yet!");
        } else {
            return _phpSession;
        }
    }

    public String getStatus() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Status:\n");
        if (_runner.isActivated()) {
            buffer.append("Running!\n");
        } else {
            buffer.append("Paused\n");
        }
        int queued = _runner.getWorkload();
        buffer.append(queued).append(" in queue");
        return buffer.toString();
    }

    protected PrintStream getStdOut() {
        return _stdout;
    }

    protected String getWelcomeMessage() {
        StringBuffer buff = new StringBuffer();
        buff.append("NinjaBot ").append(VERSION).append("\n");
        buff.append("\n").append(getStatus()).append("\n");
        buff.append("Ready!\n");
        return buff.toString();
    }

    private List<String[]> parseOpponentListJSON(String opponentJSON) {
        // strip the wrapping [ ... ]
        List<String[]> opponentList = new ArrayList<String[]>();
        opponentJSON = opponentJSON.substring(1, opponentJSON.length() - 1);
        // tokenize vs. },
        String[] arrayTokens = opponentJSON.split("},");
        for (String dataLine : arrayTokens) {
            String[] values = new String[3];
            // strip leading {
            if (dataLine.startsWith("{")) {
                dataLine = dataLine.substring(1);
            }
            if (dataLine.endsWith("}")) {
                dataLine = dataLine.substring(0, dataLine.length() - 1);
            }
            // now it's "key","value":"key2","value2"
            CSVSource kvPairsCsv = new CSVSourceImpl(dataLine, CSVType.WithOutHeader);
            try {
                String[] kvPairs = kvPairsCsv.getNextLine();
                // for each kvPair, get: user id, user name, number of ninja

                for (String kvPair : kvPairs) {
                    // surrounding quotes were stripped,put them back
                    kvPair = "\"" + kvPair + "\"";
                    // parse vs :
                    CSVSource valuesCsv = new CSVSourceImpl(kvPair, CSVType.WithOutHeader);
                    valuesCsv.setDelim(':');
                    String[] keyValue = valuesCsv.getNextLine();
                    if (keyValue != null && keyValue.length == 2) {
                        if ("cid".equals(keyValue[0])) {
                            values[0] = keyValue[1];
                        } else if ("name".equals(keyValue[0])) {
                            values[1] = keyValue[1];
                        } else if ("num_ninjas".equals(keyValue[0])) {
                            values[2] = keyValue[1];
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace(getOutput());
            }
            opponentList.add(values);
        }
        return opponentList;
    }

    public void pause() {
        // This pauses the runner
        getOutput().println("Pausing...");
        _runner.pause();
    }

    public boolean pauseOnError() {
        return _pauseOnError;
    }

    public void processCommand(String command, String arguments) {
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
        List<CommandDescription> foundCommands = findCommands(command);
        // found anything?
        if (foundCommands.size() == 0) {
            _stdout.println(command + ": command not found!");
        } else if (foundCommands.size() > 1) { // found multiples
            _stdout.println("Multiple commands found: ");
            for (CommandDescription foundCommand : foundCommands) {
                _stdout.println("\t" + foundCommand.getName());
            }
        } else {
            // one command rule them all, and in the darkness bind them!
            CommandDescription foundCommand = foundCommands.get(0);
            try {
                NinjaCommand realCommand = foundCommand.getCommand().getConstructor(NinjaBot.class).newInstance(this);
                realCommand.parseArguments(arguments);
                realCommand.execute();
            } catch (Throwable e) {
                // So many possible problems arise when using reflection to run our commands, but they're all handled the same way
                // ...whine about it
                _stdout.println("Could not execute command: " + command + ": " + e.getMessage());
                e.printStackTrace(new PrintStream(getLoggingStream()));
            }
        }
    }

    public void queueCommand(String[] commandTokens) {
        String command = commandTokens[0];
        // we support a shortcut for adding a fighter.  don't care to support it for queueing
        List<CommandDescription> foundCommands = findCommands(command);
        if (foundCommands.size() == 0) {
            _stdout.println(command + ": command not found!");
        } else if (foundCommands.size() > 1) {
            _stdout.println("Multiple commands found: ");
            for (CommandDescription foundCommand : foundCommands) {
                _stdout.println("\t" + foundCommand.getName());
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
            _stdout.println("Queueing command: " + command + " " + commandString.toString());
            CommandRunnable runnable = new CommandRunnable(this, command, commandString.toString());
            _runner.add(runnable);
        }
    }

    public void resume() {
        getOutput().println("Resuming...");
        _runner.resume();
    }

    public void setCookie(String cookie) {
        _cookie = cookie;
    }

    public void setFightCount(int count) {
        _fightCount = count;
    }

    public void setFightDelay(int delay) {
        _stdout.println("Setting delay: " + delay);
        if (delay <= 0) {
            _stdout.println("Delay value too low!");
        } else {
            _delay = delay;
        }
    }

    public void setLogging(boolean doLogging) {
        if (doLogging) {
            File logFile = new File(System.getProperty("java.io.tmpdir") + File.separator + NINJA_BOT_LOG);
            try {
                _log = new FileOutputStream(logFile);
                _stdout.println("Logging is enabled");
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace(_out);
            }
        } else {
            _log = new NullOutputStream();
            _stdout.println("Logging is disabled");
        }
    }

    public void setPauseOnError(boolean pauseOnError) {
        getStdOut().println("Setting pauseOnError to " + pauseOnError);
        _pauseOnError = pauseOnError;
    }

    public void setPhpSession(String phpSessionId) {
        _phpSession = phpSessionId;
    }


    private void setupCommandDescriptions() {
        _commands = new TreeSet<CommandDescription>();

        CommandDescription desc = new CommandDescription("resume", StartFightingCommand.class, "Resumes fighting and queue processing");
        _commands.add(desc);

        desc = new CommandDescription("pause", StopFightingCommand.class, "Stops fighting and stops processing the queue");
        _commands.add(desc);

        desc = new CommandDescription("cookie", SetCookieCommand.class, "Sets the session cookie");
        _commands.add(desc);

        desc = new CommandDescription("phpSession", SetPhpCommand.class, "Sets the phpSessionId");
        _commands.add(desc);

        desc = new CommandDescription("exit", ShutdownCommand.class, "Shutdown the bot");
        desc.addAlias("shutdown");
        desc.addAlias("quit");
        _commands.add(desc);

        desc = new CommandDescription("fight", AddFighterCommand.class, "Adds the next fighter into the queue");
        _addFighterCD = desc;
        _commands.add(desc);

        desc = new CommandDescription("help", HelpCommand.class, "Displays usability help");
        desc.addAlias("?");
        _commands.add(desc);

        desc = new CommandDescription("setFightCount", SetFightCountCommand.class, "Sets the number of fights per fighter id");
        desc.addAlias("count");
        _commands.add(desc);

        desc = new CommandDescription("listOpponents", ListOpponentsCommand.class, "Lists opponents");
        _commands.add(desc);

        desc = new CommandDescription("fightAll", FightAllCommand.class, "Fights all of the opponents in a level offset by yours");
        _commands.add(desc);

        desc = new CommandDescription("status", StatusCommand.class, "Shows the current status");
        _commands.add(desc);

        desc = new CommandDescription("news", NewsCommand.class, "Shows latest news feed");
        _commands.add(desc);

        desc = new CommandDescription("log", LogCommand.class, "Enables or disables logging extended JSON output");
        _commands.add(desc);

        desc = new CommandDescription("clearQueue", ClearFightersCommand.class, "Clears the queue");
        _commands.add(desc);

        desc = new CommandDescription("setFightDelay", SetDelayCommand.class, "Sets the delay the ninja waits until starting the next fight");
        _commands.add(desc);

        desc = new CommandDescription("setPauseOnError", PauseOnErrorCommand.class, "When true, the bot will pause automatically when an error is encountered");
        _commands.add(desc);
        
        desc = new CommandDescription("queueCommand", QueueCommand.class, "Enqueues a command");
        _commands.add(desc);
    }

    private void setupFighterPool() {
        _runner = new ThreadRunner(1);
        _runner.setReportIncrement(0);
    }

    private void setupOutputStream() throws IOException {
        _console = new OutputConsole("NinjaBot Monitor");
        _out = new PrintStream(_console.getOutput());
        _stdout = System.out;
    }

    public void showDelay() {
        _stdout.println("Current delay: " + _delay);
    }

    public void showFightCount() {
        _stdout.println("Current fight count: " + _fightCount);
    }

    public void showNews() {
        try {
            String news = getNews();
            _stdout.print(news);
        } catch (IOException e) {
            e.printStackTrace(new PrintStream(getLoggingStream()));
        }
    }

    public void showOpponents(int levelDelta) {
        List<String[]> opponents = getOpponents(levelDelta);
        for (String[] opp : opponents) {
            String opponentLine = Arrays.toString(opp);
            _stdout.println(opponentLine);
        }
    }

    public void showPhpSession() {
        getStdOut().println("PHP Session ID: " + _phpSession);
    }

    public void showStatus() {
        _stdout.println(getStatus());
    }

    public void showHelp() {
        _stdout.println();
        for (CommandDescription command : _commands) {
            _stdout.printf("%-20s", command.getName());
            _stdout.println(command.getDescription());
            //_stdout.printf(command.getName() + "%s20", command.getDescription() + "\n");
            //_stdout.println(command.getName() + ":\t" + command.getDescription());
            /*
            for (String alias : command.getAliases()) {
                _stdout.println("Alias: " + alias);
            }
             */
        }
    }

    public void shutdown() {
        _terminate = true;
    }

    public void start() {
        try {
            _out.println(getWelcomeMessage());
            startCommandScanner();
        } finally {
            _runner.shutDown(true);
            _console.exitConsole();
        }
    }

    private void startCommandScanner() {
        Scanner scanner = new Scanner(System.in);
        while (!_terminate) {
            _stdout.print("> ");
            String commandLine = scanner.nextLine();
            // a command is a string followed by 0 or more strings
            if (commandLine != null && !"".equals(commandLine.trim())) {
                String[] commandTokens = commandLine.split(" ", 2);
                String command = commandTokens[0];
                String arguments = null;
                if (commandTokens.length > 1) {
                    arguments = commandTokens[1];
                }
                processCommand(command, arguments);
            }
        }
    }
}
