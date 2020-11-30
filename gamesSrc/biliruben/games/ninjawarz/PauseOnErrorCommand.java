package biliruben.games.ninjawarz;

public class PauseOnErrorCommand extends NinjaCommand {

    public PauseOnErrorCommand(NinjaBot bot) {
        super(bot);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void execute() {
        String pauseOnErrorArg = "true";
        if (_arguments != null && _arguments.length > 0) {
            pauseOnErrorArg = _arguments[0];
        }
        boolean pauseOnError = Boolean.valueOf(pauseOnErrorArg);
        _bot.setPauseOnError(pauseOnError);
    }

}
