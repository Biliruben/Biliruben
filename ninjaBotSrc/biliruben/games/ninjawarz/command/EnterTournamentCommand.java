package biliruben.games.ninjawarz.command;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;
import biliruben.games.ninjawarz.object.Battle;
import biliruben.games.ninjawarz.object.Item;
import biliruben.games.ninjawarz.object.Spoils;
import biliruben.games.ninjawarz.object.Tournament;
import biliruben.games.ninjawarz.object.Tournament.TournamentType;

public class EnterTournamentCommand extends NinjaCommand {

    public EnterTournamentCommand(NinjaBot bot, String commandName) {
        super(bot, commandName, "Enters the tournament");
    }

    @Override
    public void execute() throws NinjaBotException {
        // Three step process:
        // This is interactive, so no options needed
        // ah, but let's let them select a tourney ahead of time
        int selection = 0;
        if (_arguments.length > 0) {
            try {
                selection = Integer.valueOf(_arguments[0]);
                if (selection < 1 || selection > 6) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                getBot().getOutput().println(selection + " is not a valid argument.  Only an integer, 1 - 6 may be used");
            }
        }
        try {
            int prizeChoice = selection;
            if (selection == 0) {
                // 1. list the tournament prizes and prompt for selection
                List<Item> prizes = getBot().getTournamentPrizes();
                getBot().getOutput().println("Select a prize: ");
                getBot().getOutput().println("0: Exit tournament");
                for (int i = 0; i < prizes.size(); i++) {
                    int choice = i + 1;
                    Item item = prizes.get(i);
                    getBot().getOutput().println(choice + " " + item.getName() + ": " + item.getAttributes().get("DPS"));
                }
                getBot().getOutput().print("(tourney)$ ");
                Scanner scanner = new Scanner(System.in);
                String choice = scanner.nextLine();

                try {
                    prizeChoice = Integer.valueOf(choice);
                    if (prizeChoice < 0 || prizeChoice > 6) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    getBot().getOutput().println(choice + " was not a valid selection, exiting");
                }
                if (prizeChoice == 0) {
                    return;
                }
            }
            // a choice has been made.  Div by 2 tells us how many rounds.  Mod 2 tells us Open or Same
            int sizeChoice = (prizeChoice + 1) / 2 + 3;
            int typeChoice = prizeChoice % 2;
            TournamentType type = null;
            switch (typeChoice) {
            case 0: type = TournamentType.OpenLevel; break;
            case 1: type = TournamentType.SameLevel; break;
            }
            // 2. create the request tournament
            Tournament tourney = getBot().getNewTournament(sizeChoice, type);
            if (tourney.getError() != null) {
                getBot().getOutput().println("ERROR: " + tourney.getError());
                return; // no tourney for you
            }
            // 3. Fight!
            boolean lost = false;
            int round = 1;
            Spoils spoils = null;
            while (round <= sizeChoice && !lost) {
                Battle battle = getBot().fightTournament(String.valueOf(tourney.getTid()));
                getBot().getOutput().printf("%-10s", "Round " + round + ":");
                getBot().getOutput().printf("%-15s", battle.getOpponent().getCid() + ": " + battle.getOpponent().getName());
                String result = battle.getResult();
                getBot().getOutput().println(":: " + result);
                if ("loss".equals(result)) {
                    lost = true;
                }
                spoils = battle.getSpoils();
                round++;
            }
            if (spoils != null) {
                getBot().getOutput().println("You recieved: " + Arrays.toString(spoils.getItems()) + " and " + spoils.getExp() + " XP");
            }

        } catch (IOException e) {
            getBot().logError(e);
        }
    }

}
