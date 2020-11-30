package biliruben.games.ninjawarz.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.api.CommandGroup;


public class CoreCommandGroup implements CommandGroup {

    private NinjaBot _bot;
    
    public CoreCommandGroup (NinjaBot bot) {
        _bot = bot;
    }
    
    @Override
    public Collection<NinjaCommand> getCommands() {
        List<NinjaCommand> commands = new ArrayList<NinjaCommand>();
        NinjaBot bot = _bot;
        
        NinjaCommand cmd = new StartFightingCommand(_bot, "resume");
        commands.add(cmd);

        //cmd = new GiftCommand(bot, "gift");
        //commands.add(cmd); -- not ready yet
        
        cmd = new DeleteCommand(bot, "delete");
        commands.add(cmd);
        
        cmd = new AssistCommand(bot, "assistAlly");
        commands.add(cmd);
        
        cmd = new EnterTournamentCommand(bot, "enterTournament");
        commands.add(cmd);

        cmd = new CompareClanCommand(bot, "compareClan");
        commands.add(cmd);

        cmd = new BuyWeaponCommand(bot, "buyWeapon");
        commands.add(cmd);

        cmd = new FireSaleCommand(bot, "fireSale");
        commands.add(cmd);

        cmd = new HealCommand(bot, "heal");
        commands.add(cmd);

        cmd = new ShowStatsCommand(bot, "stats");
        commands.add(cmd);

        cmd = new VisitDaimyoCommand(bot, "visitDaimyo");
        commands.add(cmd);

        cmd = new GoldenCloudCommand(bot, "goldenCloud");
        commands.add(cmd);
        
        cmd = new FightZombie(bot, "fightZombies");
        commands.add(cmd);
        
        cmd = new FightGenbu(bot, "fightGenbu");
        commands.add(cmd);
        
        cmd = new FightMechagenbu(bot, "fightMechaGenbu");
        commands.add(cmd);
        
        cmd = new FightSmallGirl(bot, "fightSmallGirl");
        commands.add(cmd);
        
        cmd = new FightGirl(bot, "fightGirl");
        commands.add(cmd);

        cmd = new SellAllCommand(bot, "sellAll");
        commands.add(cmd);

        cmd = new StopFightingCommand(bot, "pause");
        commands.add(cmd);

        cmd = new ShutdownCommand(bot, "quit");
        cmd.setHidden(true);
        commands.add(cmd);

        cmd = new ShutdownCommand(bot, "shutdown");
        cmd.setHidden(true);
        commands.add(cmd);

        cmd = new AddFighterCommand(bot, "fight");
        commands.add(cmd);

        cmd = new ListCommand(bot, "list");
        commands.add(cmd);

        cmd = new FightAllCommand(bot, "fightAll");
        commands.add(cmd);

        cmd = new StatusCommand(bot, "qStatus");
        cmd.setHidden(true);
        commands.add(cmd);
        
        cmd = new StatusCommand(bot, "status");
        commands.add(cmd);

        cmd = new LogCommand(bot, "log");
        commands.add(cmd);

        cmd = new ClearFightersCommand(bot, "clearQueue");
        commands.add(cmd);

        cmd = new SellItemCommand(bot, "sell");
        commands.add(cmd);

        cmd = new GetCommand(bot, "get");
        commands.add(cmd);
        
        cmd = new NewsCommand(bot, "news");
        cmd.setHidden(true);
        commands.add(cmd);

        cmd = new BuyMagic(bot, "buyMagic");
        commands.add(cmd);
        
        cmd = new BossStatus(bot, "bosses");
        commands.add(cmd);
        
        /* core command dependancies
        CommandGroup cmdGroup = new ExclusionPreferencesCommands(bot);
        commands.addAll(cmdGroup.getCommands());
        */
        
        CommandGroup cmdGroup = new NinjaCommands(bot);
        commands.addAll(cmdGroup.getCommands());
        
        /* has a dependancy for the core commands being added first
        cmdGroup = new TriggerCommands(bot);
        commands.addAll(cmdGroup.getCommands());
        */
        
        return commands;
    }

}
