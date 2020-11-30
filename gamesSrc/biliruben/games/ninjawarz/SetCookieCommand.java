package biliruben.games.ninjawarz;

public class SetCookieCommand extends NinjaCommand {

    public SetCookieCommand(NinjaBot bot) {
        super(bot);
    }

    @Override
    public void execute() {
        _bot.setCookie(_arguments[0]);
    }

}
