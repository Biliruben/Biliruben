package biliruben.games.ninjawarz;

public class ListOpponentsCommand extends NinjaCommand {

    public ListOpponentsCommand(NinjaBot bot) {
        super(bot);
    }

    @Override
    public void execute() {
        
        
        int levelDelta = NinjaBot.DEFAULT_LEVEL_DELTA;
        if (_arguments.length > 0 && !"".equals(_arguments[0].trim())) {
            levelDelta = Integer.valueOf(_arguments[0]);
        }
        System.out.println("Listing all opponents " + levelDelta + " levels");
        _bot.showOpponents(levelDelta);
    }

}
