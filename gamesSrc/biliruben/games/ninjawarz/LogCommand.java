package biliruben.games.ninjawarz;

public class LogCommand extends NinjaCommand {

    public LogCommand(NinjaBot bot) {
        super(bot);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void execute() {
        String enableValue = "true"; 
        if (_arguments != null && _arguments.length > 0) {
            enableValue = _arguments[0];
        }
        Boolean doLogging = Boolean.valueOf(enableValue);
        _bot.setLogging(doLogging);
    }
}
