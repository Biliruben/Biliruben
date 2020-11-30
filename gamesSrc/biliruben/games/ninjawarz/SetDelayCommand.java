package biliruben.games.ninjawarz;

public class SetDelayCommand extends NinjaCommand {

    public SetDelayCommand(NinjaBot bot) {
        super(bot);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void execute() {
        if (_arguments == null || _arguments.length < 1) {
            _bot.showDelay();
        } else {
            int delay = Integer.valueOf(_arguments[0]);
            _bot.setFightDelay(delay);
        }
    }

}
