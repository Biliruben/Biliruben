package biliruben.games.rpg;

public class AbilitiesFormatter {

    public static void main (String[] args) {
        // Stupid simple args: 6 integers for Str, Dex, Con, Int, Wis, & Cha
        
        if (args == null || args.length != 6) {
            System.out.print("Usage: <cmd> Str Dex Con Int Wis Cha\nUse ability scores, not bonuses.\n");
            return;
        }

        String[] labels = new String[] {"Str", "Dex", "Con", "Int", "Wis", "Cha"};
        for (int i = 0; i < 6; i++) {
            String label = labels[i];
            int score = Integer.valueOf(args[i]);
            int bonus = (score - 10) / 2;
            String modifier = "";
            if (bonus >= 0) {
                modifier = "+";
            }
            System.out.println(label + ": " + score + " (" + modifier + bonus + ")");
        }
    }

}
