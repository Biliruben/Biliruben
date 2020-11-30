package biliruben.games.ninjawarz.object;


import biliruben.games.ninjawarz.NinjaBotException;

public class GenericTimer extends AbstractTrigger implements Timer {

    private static final String KEY_TIMER = "timer";

    @Override
    protected boolean internalEvaluate() throws NinjaBotException {
        // By their very nature, Triggers are timers.  So if this method is being called, we've 'wokenUp' and thus must return true
        return true;
    }

    /*
    @Override
    public void setTriggerData(Map<String, Object> data) {
        super.setTriggerData(data);
        // set wakeup time  from data
        _frequency = Long.valueOf(String.valueOf(data.get(KEY_TIMER)));
    }

    @Override
    public Map<String, Object> createTriggerData(Scanner fromInput) {
        Map<String, Object> data = new HashMap<String, Object>();
        try { 
            getBot().getOutput().print("How long to time? (50 = 50 seconds, 5:00 = 5 minutes, 5:00:00 = 5 hours) > ");
            String time = fromInput.nextLine();
            long rawTime = Util.timeStringToLong(time) * 1000;
            data.put(KEY_TIMER, rawTime);
        } catch (Exception e) {
            getBot().getOutput().println(e.getMessage());
        }
        return data;
    }
    */

    @Override
    public String getRecommendedFrequency() {
        return "1:00:00";
    }

}
