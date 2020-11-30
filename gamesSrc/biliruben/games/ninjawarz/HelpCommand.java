package biliruben.games.ninjawarz;

public class HelpCommand extends NinjaCommand {

    public HelpCommand(NinjaBot bot) {
        super(bot);
    }

    @Override
    public void execute() {
        _bot.showHelp();
    }

}
