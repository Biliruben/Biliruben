package biliruben.games.ninjawarz;

import java.text.DecimalFormat;

import biliruben.games.ninjawarz.command.DeleteCommand;
import biliruben.games.ninjawarz.command.NinjaCommand;

/**
 * Bag of helpful methods
 * @author trey.kirk
 *
 */
public class Util {
    
    /**
     * Returns a string representing the specified char repeated a number of times
     * @param repeat
     * @param c
     * @return
     */
    public static String repeatChar(int repeat, char c) {
        char[] charArray = new char[repeat];
        for (int i = 0; i < repeat; i++) {
            charArray[i] = c;
        }
        return new String(charArray);
    }
    
    public static String descape(String toDescape) {
        if (toDescape == null) {
            return null;
        }

        while (toDescape.contains("\\'")) {
            toDescape = toDescape.replaceAll("\\\\'", "'");
        }
        return toDescape;
    }
    
    public static String commaNumber(String number) {
        if (number == null) {
            return null;
        }
        
        if (number.matches("^\\d*$")) {
            // it's a number.  Is a double?
            if (number.contains(".")) {
                double dubble = Double.valueOf(number);
                return commaNumber(dubble);
            } else {
                long lawng = Long.valueOf(number);
                return commaNumber(lawng);
            }
        } else {
            // not a number, send back what you gave me
            return number;
        }
    }
    
    public static String commaNumber(double dubble) {
        
        DecimalFormat f = new DecimalFormat("#,###,###.##");
        String commaed = f.format(dubble);
        return commaed;
    }

    public static String commaNumber(long number) {
        //11027331
        //11,027,331
        DecimalFormat f = new DecimalFormat("#,###,###");
        String commaed = f.format(number);
        return commaed;
    }
    
    // for testing
    public static void main (String[] args) {
        long tehNum = 12345678901L;
        String tehNumStr = commaNumber(tehNum);
        System.out.println(tehNumStr);
    }
    
    /**
     * Given a duration in seconds, returns a String value representing the duration
     * in human readable time.  The granularity is as litle as 1 minute and tops out in 
     * days.  I.E. it will not report '2 minutes 32 seconds' nor will it report '1 month 2 days'.  It
     * will instead use '2 minutes' (seconds are dropped entirely) or '33 days' (assuming the month noted above
     * is a 31 day month)
     * @param duration
     * @return
     */
    public static String getDurationString(long duration) {
        long second = 1000;
        long minute = 60 * second;
        long hour = 60 * minute;
        long day = 24 * hour;
        
        //long duration = new Date().getTime() - _created.getTime();
        
        long days = duration / day;
        duration = duration % day;
        
        long hours = duration / hour; 
        duration = duration % hour;
        
        long minutes = duration / minute;
        // who cares after minutes?
        StringBuffer buff = new StringBuffer();
        if (days > 0) {
            buff.append(days + " day").append(days > 1 ? "s " : " ");
        }
        if (hours > 0) {
            buff.append(hours + " hour").append(hours > 1 ? "s " : " ");
        }
        if (minutes > 0) {
            buff.append(minutes + " minute").append(minutes > 1 ? "s " : " ");
        }
        if (days + hours + minutes == 0) {
            buff.append("Not very long");
        }
        return buff.toString();

    }

    public static double roundDouble(double dubble) {
        // rounds a double to two decimal places
        // given 1234.567899
        // return 1234.57
        double modifier = Math.pow(10, 2);
        double shiftedValue = dubble * modifier;
        return Math.round(shiftedValue) / modifier;

    }

    public static long timeStringToLong(String time) {
        if (time == null) {
            return 0;
        }
        String[] tokens = time.split(":");
        if (tokens.length > 4) {
            throw new IllegalArgumentException("Only 3 colons are permitted to represent dd:hh:mm:ss");
        }
        int pos = 0;
        long timeValue = 0;
        for (int i = tokens.length - 1; i >= 0; i--) { // work backwards
            long digits = Long.valueOf(tokens[i]);
            if (pos > 0) {
                // at least minutes, xply by 60
                digits = digits * 60;
            }
            
            if (pos > 1) {
                // at least hours, xply by 60 again
                digits = digits * 60;
            }
            
            if (pos > 2) {
                // days! xply by 24
                digits = digits * 24;
            }
            timeValue += digits;
            pos++;
        }
        return timeValue;
    }
    
    public static DeleteCommand findOrCreateDeleteCommand(NinjaBot bot) {
        DeleteCommand command = null;
        for (NinjaCommand ninjaCommand : bot.getCommands()) {
            if (ninjaCommand instanceof DeleteCommand) {
                command = (DeleteCommand)ninjaCommand;
                break;
            }
        }
        // add one if not found
        if (command == null) {
            bot.logError(new NinjaBotException("Delete command not added yet!  Adding one but this may cause undesired results."));
            command = new DeleteCommand(bot, DeleteCommand.DEFAULT_NAME);
            bot.addNinjaCommand(command);
        }
        return command;
    }

}
 