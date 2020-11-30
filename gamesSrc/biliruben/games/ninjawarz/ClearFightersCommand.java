package biliruben.games.ninjawarz;

public class ClearFightersCommand extends NinjaCommand {

    public ClearFightersCommand(NinjaBot bot) {
        super(bot);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void execute() {
        _bot.clearQueue();
    }

}
