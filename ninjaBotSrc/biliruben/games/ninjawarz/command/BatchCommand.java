package biliruben.games.ninjawarz.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import biliruben.files.DosFileNameFilter;
import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;

public class BatchCommand extends NinjaCommand {

    public static final String BATCH_COMMAND_FILE_EXTENSION = ".nbb";

    private static class BatchCommandExecutorCommand extends NinjaCommand {

        private List<NinjaCommand> _commands;
        private boolean _dirty = true;

        public BatchCommandExecutorCommand(NinjaBot bot, String commandName) {
            super(bot, commandName, "User created batch command");
            _commands = new ArrayList<NinjaCommand>();
        }

        @Override
        public NinjaCommand getCopy() {
            // we don't use the standard constructor, so just give it what we are
            BatchCommandExecutorCommand proxy = new BatchCommandExecutorCommand(getBot(), getName());
            proxy.setCommands(_commands);
            return proxy;
        }
        private void setCommands(List<NinjaCommand> commands) {
            _commands = commands;
            _dirty = true;
        }

        private boolean isDirty() {
            return _dirty;
        }

        private void setDirty(boolean isDirty) {
            _dirty = isDirty;
        }

        @Override
        public void execute() throws NinjaBotException {
            getBot().getOutput().println("Exeucting batch command: " + getName());
            boolean error = false;
            for (NinjaCommand command : _commands) {

                getBot().getOutput().print(command.getName() + ": ");
                error = getBot().dispatchCommand(command);
                if (error) {
                    getBot().getOutput().println("An error was encountered executing batch command.  Terminating batch!");
                    break;
                }
            }
        }

        @Override
        public String toString() {
            StringBuffer buff = new StringBuffer();
            buff.append(getName()).append("; ");
            for (NinjaCommand command : _commands) {
                buff.append(command.getName() + Arrays.toString(command._arguments)).append("; ");
            }
            return buff.toString();
        }

        private String writeCommands() {
            // write out the commands as they're inputted
            StringBuffer buff = new StringBuffer();
            for (NinjaCommand command : _commands) {
                buff.append(command.getName());
                if (command._arguments.length > 0) {
                    for (String arg : command._arguments) {
                        buff.append(" ").append(arg);
                    }
                }
                buff.append("\n");
            }
            buff.append("end\n");
            return buff.toString();
        }
    }

    public BatchCommand(NinjaBot bot, String commandName) {
        super(bot, commandName, "Creates a batch command to run several commands in sequence");
        setExtendedHelp("Invoking this command will start the Batch Command wizard.  It requires one argument, which is the batch command name.  " +
                "Alternatively, two arguments may be passed with 'delete' being the first argument.  For example, 'batch farm' will create a batch command " + 
                "named 'farm'.  'batch delete farm' will instead delete the batch command named 'farm'");
    }

    private BatchCommandExecutorCommand validateCommand(String commandName) throws NinjaBotException {
        List<NinjaCommand> commands = getBot().findCommands(commandName, true);
        if (commands.size() > 0){
            NinjaCommand foundCommand = commands.get(0);
            if (foundCommand instanceof BatchCommandExecutorCommand) {
                return (BatchCommandExecutorCommand) foundCommand;
            } else {
                throw new NinjaBotException(commandName + " is already defined!");
            }
        }
        return null;
    }

    private void displayCommand(BatchCommandExecutorCommand command) {
        getBot().getOutput().println(command.writeCommands());
    }

    private static List<NinjaCommand> scanForNinjaCommands(InputStream fromStream, NinjaBot bot, String commandName, boolean validate) {
        // in this case, we're the receiver of our own command
        String secondaryPrompt = "(batch:" +  commandName +")$ ";
        Scanner secondaryScanner = new Scanner(fromStream);
        String lineIn = "";
        List<NinjaCommand> commands = new ArrayList<NinjaCommand>();
        boolean invalid = false;
        while (!"end".equals(lineIn)) {
            // don't show the prompt if we're reading form an alternate stream (i.e. from file)
            if (fromStream == System.in) {
                bot.getOutput().print(secondaryPrompt);
            }
            lineIn = secondaryScanner.nextLine();
            if (lineIn != null && !"".equals(lineIn.trim())) {
                lineIn = lineIn.split("#")[0].trim();
                if ("".equals(lineIn)) {
                    continue; // trim the comments and skip lines that are only comments
                }
                String[] commandTokens = lineIn.split(" ", 2);
                String command = commandTokens[0];
                String arguments = null;
                if (commandTokens.length > 1) {
                    arguments = commandTokens[1];
                }
                //processCommand(command, arguments);
                if (!"end".equals(command)) {
                    NinjaCommand ninjaCommand = bot.processCommand(command, arguments);
                    if (ninjaCommand != null) {
                        commands.add(ninjaCommand);
                    } else {
                        if (validate) {
                            invalid = true;
                        }
                    }
                }
            }
        }
        if (invalid) {
            commands.clear();
        }
        return commands;
    }

    private void deleteBatchCommand() throws NinjaBotException, IOException {
        if (_arguments.length < 2) {
            throw new NinjaBotException("A batch command must be specified with the delete statement");
        }
        String deleteCommand = _arguments[1];
        List<NinjaCommand> foundCommand = getBot().findCommands(deleteCommand, true);
        if (foundCommand == null || foundCommand.size() == 0) {
            getBot().getOutput().println("Command not found: " + deleteCommand);
        } else {
            // exact match, there can be only one
            NinjaCommand realCommand = foundCommand.get(0);
            if (realCommand instanceof BatchCommandExecutorCommand) {
                if (getBot().removeCommand(realCommand)) {
                    // delete the batch file too
                    String fileName = realCommand.getName() + BATCH_COMMAND_FILE_EXTENSION;
                    String fullFileName = NinjaBot.NINJABOT_SETTINGS_DIRECTORY + File.separator + fileName;
                    File commandFile = new File(fullFileName);
                    if (commandFile.isFile() && !commandFile.isDirectory()) {
                        FileInputStream fis = new FileInputStream(commandFile);
                        fis.close();
                        System.gc();
                        boolean deleted = commandFile.delete();
                        if (!deleted) {
                            throw new NinjaBotException("Couldn't delete the batch file! You may need to do it manually: " + fullFileName + "\nThis is likely due to a bug in Java, not this program.  Seriously, google that shit.");
                        }
                    }
                }
            } else {
                throw new NinjaBotException("You can't delete that command!  Only batch commands may be deleted.");
            }
        }
    }

    @Override
    public void execute() throws NinjaBotException {
        if (_arguments.length < 1) {
            throw new NinjaBotException("A new command name must be supplied");
        }

        String commandName = _arguments[0];
        if ("delete".equalsIgnoreCase(commandName)) {
            try {
                deleteBatchCommand();
            } catch (IOException e) {
                throw new NinjaBotException(e);
            }
            return;
        }
        // first, see if this command is already defined
        BatchCommandExecutorCommand foundCommand = validateCommand(commandName);
        if (foundCommand != null) {
            displayCommand(foundCommand);
            return; // just display it
        }

        List<NinjaCommand> commands = scanForNinjaCommands(System.in, getBot(), commandName, false);

        // user has stopped input, Create the NinjaCommand and give it to the NinjaBot as a new command
        if (commands.size() > 0) {
            BatchCommandExecutorCommand executor = new BatchCommandExecutorCommand(getBot(), commandName);
            executor.setCommands(commands);
            getBot().addNinjaCommand(executor);
            getBot().getOutput().println("Batch command '" + commandName + "' has been successfully created!");
        }
    }

    public static List<BatchCommandExecutorCommand> findBatchCommands(Set<NinjaCommand> commands) {
        List<BatchCommandExecutorCommand> foundCommands = new ArrayList<BatchCommandExecutorCommand>();
        for (NinjaCommand command : commands) {
            if (command instanceof BatchCommandExecutorCommand) {
                foundCommands.add((BatchCommandExecutorCommand)command);
            }
        }
        return foundCommands;
    }

    public static List<NinjaCommand> readBatchCommands(NinjaBot bot) throws FileNotFoundException {
        File fileList = new File(NinjaBot.NINJABOT_SETTINGS_DIRECTORY);
        String[] fileListNames = fileList.list(new DosFileNameFilter("*" + BATCH_COMMAND_FILE_EXTENSION));
        List<NinjaCommand> readCommands = new ArrayList<NinjaCommand>();
        if (fileListNames != null) {
            for (String fileName : fileListNames) {
                String fullFileName = NinjaBot.NINJABOT_SETTINGS_DIRECTORY + File.separator + fileName;
                bot.getOutput().println("Reading batch file: " + fileName);
                String commandName = fileName.split("\\.")[0];
                FileInputStream fileIn = new FileInputStream(new File(fullFileName));
                List<NinjaCommand> commands = scanForNinjaCommands(fileIn, bot, commandName, true);
                if (commands != null && commands.size() > 0) {
                    BatchCommandExecutorCommand batchExecutor = new BatchCommandExecutorCommand(bot, commandName);
                    batchExecutor.setCommands(commands);
                    batchExecutor.setDirty(false); // don't re-write until somebody changes it
                    readCommands.add(batchExecutor);
                }
            }
        }
        return readCommands;
    }

    public static void writeBatchCommands(Set<NinjaCommand> commands) throws IOException {
        List<BatchCommandExecutorCommand> batchCommands = findBatchCommands(commands);
        for (BatchCommandExecutorCommand batchCommand : batchCommands) {
            if (batchCommand.isDirty()) {
                // write it
                File batchFile = new File(NinjaBot.NINJABOT_SETTINGS_DIRECTORY + File.separator + batchCommand.getName() + BATCH_COMMAND_FILE_EXTENSION);
                FileWriter writer = new FileWriter(batchFile);
                writer.write(batchCommand.writeCommands());
                writer.flush();
                writer.close();
            }
        }
    }
}
