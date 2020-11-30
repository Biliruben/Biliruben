package biliruben.games.ninjawarz.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;
import biliruben.games.ninjawarz.command.NinjaCommand;
import biliruben.games.ninjawarz.object.JSONObject;
import biliruben.games.ninjawarz.object.Timer;

public class TriggerEvaluator implements Runnable {

    private static final int WAIT_DURATION = 15000;

    public static final long DEFAULT_INCREMENT = 5000;

    private NinjaBot _bot;
    private long _increment = DEFAULT_INCREMENT;

    private boolean _halt;
    private boolean _pause;

    public TriggerEvaluator(NinjaBot bot) {
        _bot = bot;
        _halt = false;
        _pause = false;
    }

    public long getIncrement() {
        return _increment;
    }

    public void setIncrement(long increment) {
        _increment = increment;
    }

    public void shutdown() {
        _halt = true;

        synchronized(this) {
            this.notify();            
        }


    }

    public void start() {
        Thread t = new Thread(this, this.getClass().getSimpleName());
        t.start();
    }

    @Override
    public void run() {
        do {
            // sleep a little since this starts up early in the
            // bot's life
            synchronized (this) {
                try {
                    this.wait(WAIT_DURATION);
                } catch (InterruptedException e) {
                    // don't care
                }
            }
        } while (!_bot.isReady() && !_halt);
        while (!_halt) {
            try {
                if (!_pause) {
                    evaluateTimers();
                }
            } catch (Throwable e1) { // don't let any exception disrupt us
                _bot.logError(e1);
            }
            try {
                Thread.sleep(_increment);
            } catch (InterruptedException e) {
                // don't care
            }
        }
    }

    public void evaluateEvent(JSONObject data) {
        List<Event> purgeList = new ArrayList<Event>();
        if (!_pause) {
            for (Event event : _bot.getEvents()) {
                if (event.isExpired() || event.getExecutions() == 0) {
                    purgeList.add(event);
                } else {
                    for (Class clazz : event.getEventClasses()) {
                        if (clazz.isAssignableFrom(data.getClass())) {
                            if (event.matchEvent(data)) {
                                dispatchCommands(event);
                                if (event.getExecutions() != Trigger.EXECUTION_INIFINITE) {
                                    event.setExecutions(event.getExecutions() - 1);
                                }
                            }
                            break; // it's been tested
                        }
                    }
                }
            }

            for (Event purge : purgeList) {
                _bot.purgeTrigger(purge);
            }
        }
    }

    private void evaluateTimers() throws NinjaBotException {
        List<Timer> purgeList = new ArrayList<Timer>();
        for (Timer trigger : _bot.getTimers()) {
            if (trigger.isExpired() || trigger.getExecutions() == 0) {
                // trigger has expired, don't evaluate and put it on the purge list
                purgeList.add(trigger);
            } else if (trigger.isReady()) {
                boolean matched = trigger.evaluate();
                if (matched) {
                    // triggered!
                    dispatchCommands(trigger);
                    if (trigger.getExecutions() > 0) {
                        trigger.setExecutions(trigger.getExecutions() - 1);
                    }
                }
                trigger.incrementWakeup();
            }
        }
        for (Trigger toPurge : purgeList) {
            _bot.purgeTrigger(toPurge);
        }
    }

    public static final String ARG_ID = "$id$";

    private void dispatchCommands(Trigger trigger) {
        _bot.getOutput().println(new Date().toString() + ": Dispatching trigger: " + trigger);
        for (NinjaCommand command : trigger.getCommands()) {
            // parse the arguments of the command and see if any are '$id$'.  If so, replace that with the matched object's getId()
            String[] args = command.getArguments();
            if (args != null && args.length > 0) {
                for (int i = 0; i < args.length; i++) {
                    String arg = args[i];

                    if (ARG_ID.equals(arg.toLowerCase())) {
                        // matched our 'id' variable.  substitute
                        if (trigger.getMatchedObject() != null) {
                            args[i] = trigger.getMatchedObject().getId();
                        } else {
                            args[i] = "";
                        }
                    }
                }
            }
            _bot.dispatchCommand(command);
        }
    }

    public void pause() {
        _pause = true;
    }

    public void resume() {
        _pause = false;
    }

    public boolean isPaused() {
        return _pause;
    }
}
