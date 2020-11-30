package biliruben.games.ninjawarz;

public class StopFightingCommand extends NinjaCommand {

    public StopFightingCommand(NinjaBot bot) {
        super(bot);
    }

    @Override
    public void execute() {
        _bot.pause();
    }

}
