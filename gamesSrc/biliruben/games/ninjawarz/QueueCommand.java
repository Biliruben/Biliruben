package biliruben.games.ninjawarz;

public class QueueCommand extends NinjaCommand {

    public QueueCommand(NinjaBot bot) {
        super(bot);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void execute() {
        _bot.queueCommand(_arguments);
    }

}
