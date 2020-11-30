package biliruben.games.simulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionLegend;

/**
 * Inputs:
 * Amount
 * Type
 * Horses (total)
 * Races (total)
 * 
 * Output:
 * Net profits
 * 
 * The simulation is given R races, how much would one win (or lose) if they made the same bet on any given horse (ignoring
 * odds of winning). The 'Type' will be predefined here with a static payout, leaning on the conservative side of payouts. I
 * suppose a properties file could further define types of bets and payouts for further analysis.
 * @author trey.kirk
 *
 */
public class BettingSim {

    private static final String OPT_WAGER = "wager";
    private static final String OPT_RACES = "races";
    private static final String OPT_HORSES = "horses";
    private static final String OPT_AMOUNT = "amount";
    private static final String OPT_VERBOSE = "verbose";
    private static GetOpts _opts;
    
    private int _amount;
    private Wager _wager;
    private boolean _verbose;

    public BettingSim(int amount, Wager wager) {
        this._amount = amount;
        this._wager = wager;
    }
    
    public double simulate(int horses, int races) {
        // Start the payment the total cost to bet
        double payment = _amount * races * _wager.getWagingFactor() * -1;
        System.out.println("Races: " + races);
        System.out.println("Wager: $" + this._amount + " " + _wager);
        System.out.println("Initial cost: " + payment);
        // race loop
        int winCounter = 0;
        for (int i = 0; i < races; i++) {
            Integer[] interests = getInterests(horses);
            Integer[] results = getResults(horses);
            if (this._verbose) {
                System.out.println("Results: " + Arrays.toString(results));
                System.out.println("Interests: " + Arrays.toString(interests));
            }
            if (this._wager.isWin(results, interests)) {
                // add to payment
                payment += _amount * _wager.getPayout();
                winCounter++;
                if (_verbose) System.out.println("After win: " + payment);
            }
        }
        System.out.println(winCounter + " wins");
        
        return payment;
    }
    
    public void setVerbose(boolean verbose) {
        this._verbose = verbose;
    }
    
    private Integer[] getRandomSequence(int total) {
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 1; i <= total; i++) {
            list.add(i);
        }
        Collections.shuffle(list);
        return list.toArray(new Integer[total]);
    }
    
    private Integer[] getResults(int horses) {
        // randomly determine the results of the race for all horses
        return getRandomSequence(horses);
    }
    
    private Integer[] getInterests(int horses) {
        // randomly determine the horses being bet on, in win which order, for a single race
        // first, just a random sequence of all horses
        Integer[] allHorses = getRandomSequence(horses);
        // then a sub-array based on the number wagered
        Integer[] interests = Arrays.copyOfRange(allHorses, 0, _wager.getInterests());
        return interests;
    }

    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        init (args);
        
        int races = Integer.valueOf(_opts.getStr(OPT_RACES));
        int horses = Integer.valueOf(_opts.getStr(OPT_HORSES));
        int bet = Integer.valueOf(_opts.getStr(OPT_AMOUNT));
        boolean verbose = Boolean.valueOf(_opts.getStr(OPT_VERBOSE));
        String wageType = _opts.getStr(OPT_WAGER);
        Class<Wager> wagerClass = (Class<Wager>) Class.forName(Wager.class.getName() + "$" + wageType);
        Wager wager = wagerClass.newInstance();
        
        BettingSim sim = new BettingSim(bet, wager);
        sim.setVerbose(verbose);
        double result = sim.simulate(horses, races);
        System.out.println("Net: " + result);


    }
    
    private static void init(String[] args) {
        _opts = new GetOpts(BettingSim.class);
        OptionLegend legend = new OptionLegend(OPT_AMOUNT);
        legend.setRequired(true);
        legend.setDescription("Base amount to bet");
        _opts.addLegend(legend);
        
        legend = new OptionLegend(OPT_HORSES);
        legend.setRequired(true);
        legend.setDescription("Total horses in each race");
        _opts.addLegend(legend);
        
        legend = new OptionLegend(OPT_RACES);
        legend.setRequired(false);
        legend.setDefaultValue("10");
        legend.setDescription("Total races to wager on");
        _opts.addLegend(legend);
        
        legend = new OptionLegend(OPT_WAGER);
        legend.setRequired(true);
        legend.setDescription("Type of bet to make");
        _opts.addLegend(legend);
        
        legend = new OptionLegend(OPT_VERBOSE);
        legend.setFlag(true);
        _opts.addLegend(legend);

        _opts.parseOpts(args);
    }

}
