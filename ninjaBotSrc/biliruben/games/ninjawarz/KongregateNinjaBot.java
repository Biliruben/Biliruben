package biliruben.games.ninjawarz;

import java.io.IOException;

/**
 * The Kongregate implementation of NinjaBot
 * @author trey.kirk
 *
 */
public class KongregateNinjaBot extends NinjaBot {

    public static final String BASE_URL = "http://kongregate.ninjawarz.brokenbulbstudios.com";
    public static final String NINJA_BOT_FLAVOR = "Kongregate";
    public static final String REFERER = "http://kongregate.ninjacdn.brokenbulbstudios.com/swf/game.swf?880";

    public KongregateNinjaBot(String propertyFile) throws IOException {
        super(propertyFile);
    }
    
    @Override
    public String getReferer() {
        return REFERER;
    }
    

    /**
     * Gets the base Ninja Warz URL
     * @return
     */
    @Override
    public String getBaseUrl() {
        String baseUrl = getConfiguration().getString(Configuration.PREF_BASE_URL, BASE_URL);
        return baseUrl;
    }

    @Override
    protected String getNinjaBotFlavor() {
        return NINJA_BOT_FLAVOR;
    }
}
