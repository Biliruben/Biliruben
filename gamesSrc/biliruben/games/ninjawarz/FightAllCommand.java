package biliruben.games.ninjawarz;

public class FightAllCommand extends NinjaCommand {

    public FightAllCommand(NinjaBot bot) {
        super(bot);
    }

    @Override
    public void execute() {
        int levelDelta = NinjaBot.DEFAULT_LEVEL_DELTA;
        if (_arguments.length > 0) {
            levelDelta = Integer.valueOf(_arguments[0]);
        }
        System.out.println("Fighting all opponents " + levelDelta + " levels");
        _bot.fightAllOpponents(levelDelta);
    }

}
