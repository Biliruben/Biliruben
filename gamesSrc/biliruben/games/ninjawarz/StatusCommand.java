package biliruben.games.ninjawarz;

public class StatusCommand extends NinjaCommand {

    public StatusCommand(NinjaBot bot) {
        super(bot);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void execute() {
        _bot.showStatus();
    }

}
