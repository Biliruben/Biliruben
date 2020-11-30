package biliruben.games.ninjawarz.object;

import java.lang.reflect.Constructor;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.api.Event;

public class Condition {

    public enum TimerCondition {
        DaiymoAvailable(DaiymoAvailableTimer.class),
        GenbuAvailable(GenbuAvailableTimer.class),
        GirlAvailable(GirlAvailableTimer.class),
        HordeAvailable(HordeAvailableTimer.class),
        Leveled(LeveledTimer.class),
        MagicAvaialble(MagicAvailableTimer.class),
        MechaGenbuAvailable(MechaGenbuAvailableTimer.class),
        RecruitAvailable(RecruitAvailableTimer.class),
        SmallGirlAvailable(SmallGirlAvailableTimer.class),
        Timer(GenericTimer.class),
        TournamentAvailable(TournamentAvailableTimer.class);

        private Class<? extends Timer> _clazz;

        private TimerCondition (Class<? extends Timer> clazz) {
            _clazz = clazz;
        }

        public Timer getInstance(NinjaBot bot) {
            try {
                Constructor<? extends Timer> c = _clazz.getConstructor();
                Timer t = c.newInstance();
                return t;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

    }
  
    public enum EventCondition {

        OnError(ErrorEvent.class),
        OnLeveled(LeveledEvent.class),
        OnLoss(LossEvent.class);
        
        private Class<? extends Event> _clazz;

        private EventCondition (Class<? extends Event> clazz) {
            _clazz = clazz;
        }

        public Event getInstance(NinjaBot bot) {
            try {
                Constructor<? extends Event> c = _clazz.getConstructor();
                Event t = c.newInstance();
                return t;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

    }
    
}
