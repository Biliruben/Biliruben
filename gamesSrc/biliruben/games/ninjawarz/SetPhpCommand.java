package biliruben.games.ninjawarz;


public class SetPhpCommand extends NinjaCommand {

    public SetPhpCommand(NinjaBot bot) {
        super(bot);
    }

    @Override
    public void execute() {
        if (_arguments == null || _arguments.length == 0) {
            _bot.showPhpSession();
        } else {
            _bot.setPhpSession(_arguments[0]);
        }
    }

}
