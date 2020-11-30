package biliruben.games.ninjawarz.object;

import biliruben.games.ninjawarz.NinjaBotException;
import biliruben.games.ninjawarz.api.Trigger;

public interface Timer extends Trigger {

    public static final long FREQ_FAST = 10 * 1000; // 10 seconds
    public static final long FREQ_MED = 5 * 60 * 1000; // 5 minutes
    public static final long FREQ_SLOW = 2 * 60 * 60 * 1000; // 2 hours
    public static final long FREQ_DAILY = 24 * 60 * 60 * 1000; // 24 hours;

    public abstract long getWakeupTime();

    public abstract void setWakeupTime(long wakeupTime);

    public abstract void incrementWakeup();

    public abstract boolean evaluate() throws NinjaBotException;

    public abstract long getFrequency();
    
    public abstract void setFrequency(long frequency);
    
    public String getRecommendedFrequency();
    
}