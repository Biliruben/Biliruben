package biliruben.games.ninjawarz;

public class StartFightingCommand extends NinjaCommand {

    public StartFightingCommand(NinjaBot bot) {
        super(bot);
    }

    @Override
    public void execute() {
        _bot.resume();
    }

}
