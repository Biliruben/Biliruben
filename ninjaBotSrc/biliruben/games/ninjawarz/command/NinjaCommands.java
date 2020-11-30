package biliruben.games.ninjawarz.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;
import biliruben.games.ninjawarz.api.AbstractNinjaBotGroup;
import biliruben.games.ninjawarz.object.Item;
import biliruben.games.ninjawarz.object.Ninja;

public class NinjaCommands extends AbstractNinjaBotGroup {

    public static class EquipCommand extends NinjaCommand {

        public EquipCommand(NinjaBot bot, String commandName) {
            super(bot, commandName, "Equips a ninja with the item id specified");
        }

        @Override
        public void execute() throws NinjaBotException {
            if (_arguments.length < 1) {
                throw new NinjaBotException("An ItemID must be supplied. Optionally, a NinjaID may be specified too");
            }
            try {

                int iid = Integer.valueOf(_arguments[0]);
                if (_arguments.length > 1) {
                    int nid = Integer.valueOf(_arguments[1]);

                    getBot().equipNinja(nid, iid);

                } else {
                    // equip all ninjas with as many of the item as we can
                    // start with the lowest level ninja
                    Ninja[] ninjas = getBot().getClan().getNinjas();
                    Arrays.sort(ninjas, new Ninja.NinjaComparator());
                    for (int i = ninjas.length - 1; i >= 0; i--) { // go the opposite direction of the sort
                        Item nextEquip = getBot().getItem(iid);
                        if (nextEquip != null) {
                            getBot().equipNinja(ninjas[i].getNid(), nextEquip.getIid());
                        } else {
                            // we've run out of items, time to stop
                            getBot().getOutput().println("I ran out of inventory!");
                            break;
                        }
                    }
                }


            } catch (NumberFormatException e) {
                throw new NinjaBotException("Integer values must be provided for both the NinjaID and the ItemID");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                getBot().logError(e);
            }

        }

    }


    public static class UnequipNinjaCommand extends NinjaCommand {

        public UnequipNinjaCommand(NinjaBot bot, String commandName) {
            super(bot, commandName, "Unequips a ninja");
        }

        @Override
        public void execute() throws NinjaBotException {
            try {
                if (_arguments.length < 1) {
                    // mass uneqquip
                    for (Ninja ninja : getBot().getClan().getNinjas()) {
                        getBot().unequipNinja(ninja.getNid());
                    }
                } else {
                    long nid = Long.valueOf(_arguments[0]);
                    getBot().unequipNinja(nid);
                }
            } catch (IOException e) {
                getBot().logError(e);
            } catch (NumberFormatException e) {
                throw new NinjaBotException("An integer value must be provided");
            }
        }

    }
    
    public static class RecruitNinjaCommand extends NinjaCommand {
        public RecruitNinjaCommand(NinjaBot bot, String commandName) {
            super(bot, commandName, "Recruits a ninja recruit");
        }
        
        @Override
        public void execute() throws NinjaBotException {
            if (_arguments.length < 1) {
                throw new NinjaBotException("A recruit ID must be specified");
            }
            String rnid = _arguments[0];
            try {
                getBot().recruitNinja(rnid);
            } catch (IOException e) {
                getBot().logError(e);
            }
        }
    }
    
    public static class DismissNinjaCommand extends NinjaCommand {
        public DismissNinjaCommand(NinjaBot bot, String commandName) {
            super(bot, commandName, "Dismisses a ninja");
        }
        
        @Override
        public void execute() throws NinjaBotException {
            if (_arguments.length < 1) {
                throw new NinjaBotException("A ninja ID must be specified");
            }
            String nid = _arguments[0];
            try {
                getBot().dismissNinja(Long.valueOf(nid));
            } catch (IOException e) {
                getBot().logError(e);
            } catch (NumberFormatException e) {
                throw new NinjaBotException("An integer value must be provided");
            }
        }
    }

    public NinjaCommands(NinjaBot bot) {
        super(bot);
    }

    @Override
    public Collection<NinjaCommand> getCommands() {
        List<NinjaCommand> commands = new ArrayList<NinjaCommand>();
        NinjaCommand cmd = new EquipCommand(getBot(), "equipNinja");
        commands.add(cmd);

        cmd = new UnequipNinjaCommand(getBot(), "unequipNinja");
        commands.add(cmd);
        
        cmd = new RecruitNinjaCommand(getBot(), "recruitNinja");
        commands.add(cmd);
        
        cmd = new DismissNinjaCommand(getBot(), "dismissNinja");
        commands.add(cmd);
        
        return commands;
    }

}
