package biliruben.games.ninjawarz;

public class AddFighterCommand extends NinjaCommand {

    public AddFighterCommand(NinjaBot bot) {
        super(bot);
    }

    @Override
    public void execute() {
        if (_arguments == null || _arguments.length < 1) {
            System.out.println("Need fighter id!");
        } else {
            if (_arguments.length > 1) {
                int iterations = Integer.valueOf(_arguments[1]);
                _bot.addFighter(_arguments[0], iterations);
            } else {
                _bot.addFighter(_arguments[0]);
            }
        }
    }

}
