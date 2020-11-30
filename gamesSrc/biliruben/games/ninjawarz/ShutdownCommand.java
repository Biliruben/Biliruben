package biliruben.games.ninjawarz;

public class ShutdownCommand extends NinjaCommand {

    public ShutdownCommand(NinjaBot bot) {
        super(bot);
    }

    @Override
    public void execute() {
        _bot.shutdown();
    }

}
