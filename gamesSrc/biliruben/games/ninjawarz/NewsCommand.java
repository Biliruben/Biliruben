package biliruben.games.ninjawarz;

public class NewsCommand extends NinjaCommand {

    public NewsCommand(NinjaBot bot) {
        super(bot);
    }

    @Override
    public void execute() {
        _bot.showNews();
    }

}
