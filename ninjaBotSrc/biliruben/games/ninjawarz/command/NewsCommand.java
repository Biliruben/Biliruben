package biliruben.games.ninjawarz.command;

import biliruben.games.ninjawarz.NinjaBot;

public class NewsCommand extends NinjaCommand {

    public NewsCommand(NinjaBot bot, String commandName) {
        super(bot, commandName, "Shows latest news feed");
        // TODO Auto-generated constructor stub
    }

    @Override
    public void execute() {
        getBot().showNews();
    }

}
