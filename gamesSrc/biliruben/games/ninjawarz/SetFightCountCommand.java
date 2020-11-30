package biliruben.games.ninjawarz;

public class SetFightCountCommand extends NinjaCommand {

    public SetFightCountCommand(NinjaBot bot) {
        super(bot);
    }

    @Override
    public void execute() {
        if (_arguments == null || _arguments.length < 1) {
            _bot.showFightCount();
        } else {
            String count = _arguments[0];
            _bot.setFightCount(Integer.valueOf(count));
        }
    }

}
