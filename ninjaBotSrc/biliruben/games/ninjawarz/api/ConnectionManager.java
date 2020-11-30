package biliruben.games.ninjawarz.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import sun.security.action.GetBooleanAction;

import biliruben.games.ninjawarz.NinjaBot;
import biliruben.games.ninjawarz.NinjaBotException;
import biliruben.games.ninjawarz.object.Clan;
import biliruben.games.ninjawarz.object.Tournament.TournamentType;
import biliruben.io.HTTPConnection;

public class ConnectionManager {

    private static final String ASSIST_URL = "//ajax/assist_ally";
    private static final String BUY_WEAPON_URL = "//ajax/purchase/weapon";
    private static final String CREATE_TOURNAMENT_URL = "/ajax/create_tournament";
    private static final String DAIMYO_URL = "/ajax/daimyo_gift";
    private static final String CLOUD_URL = "/ajax/golden_cloud";
    private static final String EQUIP_URL = "//ajax/equip_weapon";
    private static final String UNEQUIP_URL = "//ajax/unequip_weapon";
    private static final String FIGHT_URL = "//ajax/fight";
    private static final String HEAL_URL = "/ajax/hospital";
    private static final String HIM_URL = "/ajax/him";
    private static final String LIST_RECRUIT_URL = "//ajax/recruitable_ninjas";
    private static final String ME_URL = "/ajax/me";
    private static final String NEWS_URL = "/ajax/get_flat_news";
    private static final String PHPSESSID = "PHPSESSID=";
    private static final String RECRUIT_NINJA_URL = "//ajax/recruit_ninja";
    private static final String SELL_URL = "//ajax/sell_item";
    private static final String TOURNEY_PRIZES_URL = "/ajax/get_tournament_prizes";
    private static final String WEAPON_SHOP_URL = "/ajax/shop_inventory/weapon";
    private static final String TOURNEY_FIGHT_URL = "//ajax/tournament";
    private static final String STAGE_URL = "//ajax/stage";
    private static final String DISMISS_NINJA_URL = "//ajax/dismiss_ninja";
    private static final String MAGIC_INV_URL = "//ajax/shop_inventory/magic";
    private static final String MAGIC_PURCHASE_URL = "//ajax/purchase/magic";
    private static final String GIFT_URL = "/gift/ajaxySelect";

    public static void buildCommonRequestProperties(HTTPConnection connection, NinjaBot bot) throws NinjaBotException {
        //connection.setRequestProperty("Host", "kongregate.ninjawarz.brokenbulbstudios.com");
        //connection.setRequestProperty("Host", "ninjawarz.brokenbulbstudios.com");
        connection.setRequestProperty("Referer", bot.getReferer());
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:14.0) Gecko/20100101 Firefox/14.0.1");
        connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        connection.setRequestProperty("Accept-Language", "en-us,en;q=0.5");
        //connection.setRequestProperty("Accept-Encoding", "gzip, deflate"); // we don't care / want compression
        connection.setRequestProperty("DNT", "1");
        connection.setRequestProperty("Connection", "keep-alive");
        /*
        String cookie = bot.getCookie();
        if (cookie != null) {
            connection.setRequestProperty("Cookie", cookie);
        }
         */
        connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
    }
    private NinjaBot _bot;


    public final String OPP_LIST_URL = "//ajax/get_opponents";

    public ConnectionManager(NinjaBot bot) {
        this._bot = bot;
    }

    /**
     * Gets the HTTPConnection for assisting allies
     * @param cid
     * @return
     * @throws MalformedURLException
     */
    public HTTPConnection getAssistConnection(long cid) throws MalformedURLException {
        HTTPConnection assistConnection = null;
        try {
            URL url = new URL(getAssistUrl() + "?" + PHPSESSID + _bot.getPhpSession());
            assistConnection = new HTTPConnection(url, _bot.getOutput());
            Random r = new Random();
            int type = r.nextInt(5);
            buildCommonRequestProperties(assistConnection, _bot);
            //String token = "type=" + type + "&cid=" + l + "&grade=A+&text=";
            assistConnection.setContentToken("type=" + type);
            assistConnection.setContentToken("cid=" + cid);
            // TODO: allow a user specified grade
            assistConnection.setContentToken("grade=A+");
            assistConnection.setContentToken("text=");
            //assistConnection.setRequestProperty("Content-length", String.valueOf(token.length()));
        } catch (NinjaBotException e) {
            _bot.logError(e);
        }
        return assistConnection;
    }

    /**
     * Gets the URL used to assist an ally
     * @return
     */
    public String getAssistUrl() {
        return _bot.getBaseUrl() + ConnectionManager.ASSIST_URL;
    }



    /**
     * Gets the HTTPConnection to buy a weapon
     * @param itemId
     * @return
     * @throws MalformedURLException
     * @throws NinjaBotException
     */
    public HTTPConnection getBuyWeaponConnection(int itemId) throws MalformedURLException, NinjaBotException {
        URL url = new URL(getBuyWeaponURL() + "?" + PHPSESSID  + _bot.getPhpSession());
        HTTPConnection buyWeaponConnection = new HTTPConnection(url);
        try {
            String token = String.valueOf("iid=" + itemId);
            buyWeaponConnection.setContentToken(token);
            buildCommonRequestProperties(buyWeaponConnection, _bot);
        } catch (NinjaBotException e) {
            _bot.logError(e);
        }
        return buyWeaponConnection;
    }


    /**
     * Returns the URL used to buy a weapon
     * @return
     * @throws MalformedURLException
     */
    public String getBuyWeaponURL() throws MalformedURLException {
        return _bot.getBaseUrl() + BUY_WEAPON_URL;
    }
    
    public String getCloudUrl() {
        return _bot.getBaseUrl() + CLOUD_URL;
    }

    public HTTPConnection getCloudConnection() throws MalformedURLException {
        URL url = new URL(getCloudUrl());
        HTTPConnection cloudConnection = new HTTPConnection(url);
        try {
            String token = PHPSESSID + _bot.getPhpSession();
            cloudConnection.setContentToken(token);
            buildCommonRequestProperties(cloudConnection, _bot);
        } catch (NinjaBotException e) {
            _bot.logError(e);
        }
        return cloudConnection;
    }

    public HTTPConnection getCreateTournamentConnection(int size, TournamentType type) throws MalformedURLException {
        HTTPConnection connection = null;
        try {
            URL url = new URL(getCreateTournamentUrl() + "?" + PHPSESSID + _bot.getPhpSession());
            connection = new HTTPConnection(url);

            buildCommonRequestProperties(connection, _bot);
        } catch (NinjaBotException e) {
            _bot.logError(e);
        }
        connection.setContentToken("size=" + String.valueOf(size));
        connection.setContentToken("type=" + type.getType());
        return connection;
    }

    public String getCreateTournamentUrl() {
        return _bot.getBaseUrl() + CREATE_TOURNAMENT_URL;
    }

    /**
     * Gets the HTTPConnection to poke the Daimyo
     * @return
     * @throws MalformedURLException
     */
    public HTTPConnection getDaimyoConnection() throws MalformedURLException {
        URL url = new URL(getDaimyoUrl());
        HTTPConnection daimyoConnection = new HTTPConnection(url);
        try {
            String token = PHPSESSID + _bot.getPhpSession();
            daimyoConnection.setContentToken(token);
            buildCommonRequestProperties(daimyoConnection, _bot);
        } catch (NinjaBotException e) {
            _bot.logError(e);
        }
        return daimyoConnection;
    }

    /**
     * Returns the URL used to visit the Daimyo
     * @return
     */
    private String getDaimyoUrl() {
        return _bot.getBaseUrl() + DAIMYO_URL;
    }

    /**
     * Returns the equipping connection which is used to arm a Ninja with a particular item from inventory
     * @param l
     * @param itemId
     * @return
     * @throws MalformedURLException
     * @throws NinjaBotException
     */
    public HTTPConnection getEquipConnection(long l, int itemId) throws MalformedURLException, NinjaBotException {
        URL url = new URL(getEquipUrl() + "?" + PHPSESSID + _bot.getPhpSession());
        HTTPConnection equipConnection = new HTTPConnection(url);
        try {
            String token = "nid=" + l + "&iid=" + itemId;
            equipConnection.setContentToken(token);
            buildCommonRequestProperties(equipConnection, _bot);
            equipConnection.setRequestProperty("Content-length", String.valueOf(token.length()));
            //equipConnection.setRequestProperty("Referer", "http://kongregate.ninjacdn.brokenbulbstudios.com/swf/dojo/dojo.swf?880");
        } catch (NinjaBotException e) {
            // problem, send it to the log and the bot
            _bot.logError(e);
        }
        return equipConnection;
    }

    /**
     * Returns the equip ninja url
     */
    public String getEquipUrl() {
        return _bot.getBaseUrl() + EQUIP_URL;
    }

    public HTTPConnection getUnequipConnection(long nid) throws MalformedURLException, NinjaBotException {
        URL url = new URL(getUnequipUrl() + "?" + PHPSESSID + _bot.getPhpSession());
        HTTPConnection unequipConnection = new HTTPConnection(url);
        try {
            String token = "nid=" + nid;
            unequipConnection.setContentToken(token);
            buildCommonRequestProperties(unequipConnection, _bot);
            unequipConnection.setRequestProperty("Content-length", String.valueOf(token.length()));
            //equipConnection.setRequestProperty("Referer", "http://kongregate.ninjacdn.brokenbulbstudios.com/swf/dojo/dojo.swf?880");
        } catch (NinjaBotException e) {
            // problem, send it to the log and the bot
            _bot.logError(e);
        }
        return unequipConnection;
    }
    
    public String getUnequipUrl() {
        return _bot.getBaseUrl() + UNEQUIP_URL;
    }
    /**
     * Returns the connection used for fighting another clan
     * @param clanId
     * @return
     * @throws MalformedURLException
     */
    public HTTPConnection getFightConnection(String clanId, OutputStream fightStream) throws MalformedURLException {
        URL url = new URL(getFightUrl());
        HTTPConnection fightConnection = new HTTPConnection(url, fightStream);
        try {
            String token = PHPSESSID + _bot.getPhpSession() + "&opponent=" + clanId;
            fightConnection.setContentToken(token);
            buildCommonRequestProperties(fightConnection, _bot);
            fightConnection.setRequestProperty("Content-length", String.valueOf(token.length()));

        } catch (NinjaBotException e) {
            // problem, send it to the log and the bot
            _bot.logError(e);
        }
        return fightConnection;
    }

    /**
     * Returns the URL used for fighting another clan
     * @return
     */
    private String getFightUrl() {
        return _bot.getBaseUrl() + FIGHT_URL;
    }

    /**
     * Returns the connection used to heal
     * @return
     * @throws MalformedURLException
     */
    public HTTPConnection getHealConnection(OutputStream healStream) throws MalformedURLException {

        URL url = new URL(getHealUrl());
        HTTPConnection healConnection = new HTTPConnection(url, healStream);
        try {
            String token = PHPSESSID + _bot.getPhpSession();
            healConnection.setContentToken(token);
            //buildCommonRequestProperties(healConnection);
            healConnection.setRequestProperty("Content-length", String.valueOf(token.length()));
        } catch (NinjaBotException e) {
            // problem, send it to the log and the bot
            _bot.logError(e);
        }
        return healConnection;
    }

    /**
     * The URL used to visit the hospital
     * @return
     */
    private String getHealUrl() {
        return _bot.getBaseUrl() + HEAL_URL;
    }

    /**
     * Returns the URL used to fetch a {@link Clan}
     * @return
     */
    public String getHimUrl() {
        return _bot.getBaseUrl() + HIM_URL;
    }

    public HTTPConnection getListOpponent(int levelDelta) throws MalformedURLException {
        URL url = null;
        //url = new URL(getListOppUrl() + "/" + levelDelta + "/0/50");
        url = new URL(getListOppUrl() + "/" + levelDelta);
        HTTPConnection oppConnection = new HTTPConnection(url);
        try {
            String token = PHPSESSID + _bot.getPhpSession();
            oppConnection.setContentToken(token);

            buildCommonRequestProperties(oppConnection, _bot);
        } catch (NinjaBotException e) {
            _bot.logError(e);
        }
        return oppConnection;
    }

    /**
     * Gets the URL used to list opponents in the balloon
     * @return
     */
    private String getListOppUrl() {
        return _bot.getBaseUrl() + OPP_LIST_URL;
    }

    public HTTPConnection getRecruitConnection(String rnid) throws MalformedURLException {
        URL url = null;
        try {
            url = new URL(getRecruitUrl() + "?" + PHPSESSID + _bot.getPhpSession());
        } catch (NinjaBotException e1) {
            _bot.logError(e1);
        }
        HTTPConnection recruitConnection = new HTTPConnection(url);
        try {
            //String token = "rnid=" + rnid.replace("-", "%2D");
            String token = "rnid=" + rnid;
            recruitConnection.setContentToken(token);
            buildCommonRequestProperties(recruitConnection, _bot);
            recruitConnection.setRequestProperty("Content-length", String.valueOf(token.length()));
            //equipConnection.setRequestProperty("Referer", "http://kongregate.ninjacdn.brokenbulbstudios.com/swf/dojo/dojo.swf?880");
        } catch (NinjaBotException e) {
            // problem, send it to the log and the bot
            _bot.logError(e);
        }
        return recruitConnection;
    }
    
    public String getRecruitUrl() {
        return _bot.getBaseUrl() + RECRUIT_NINJA_URL;
    }

    public HTTPConnection getDissmissConnection(long nid) throws MalformedURLException {
        URL url = null;
        try {
            url = new URL(getDismissUrl() + "?" + PHPSESSID + _bot.getPhpSession());
        } catch (NinjaBotException e1) {
            _bot.logError(e1);
        }
        HTTPConnection dismissConnection = new HTTPConnection(url);
        try {
            //String token = "rnid=" + rnid.replace("-", "%2D");
            String token = "nid=" + nid;
            dismissConnection.setContentToken(token);
            buildCommonRequestProperties(dismissConnection, _bot);
            dismissConnection.setRequestProperty("Content-length", String.valueOf(token.length()));
            //equipConnection.setRequestProperty("Referer", "http://kongregate.ninjacdn.brokenbulbstudios.com/swf/dojo/dojo.swf?880");
        } catch (NinjaBotException e) {
            // problem, send it to the log and the bot
            _bot.logError(e);
        }
        return dismissConnection;
    }

    public String getDismissUrl() {
        return _bot.getBaseUrl() + DISMISS_NINJA_URL;
    }
    

    /**
     * Gets the Recruit URL
     * @return
     */
    protected String getListRecruitUrl() {
        return _bot.getBaseUrl() + LIST_RECRUIT_URL;
    }
    
    private String getMagicInventoryUrl() {
        return _bot.getBaseUrl() + MAGIC_INV_URL;
    }

    public HTTPConnection getMagicInventoryConnection() throws MalformedURLException {
        URL url = null;
        HTTPConnection magicInvConnection = null;
        try {
            url = new URL(getMagicInventoryUrl() + "?" + PHPSESSID + _bot.getPhpSession());
            magicInvConnection = new HTTPConnection(url);
            buildCommonRequestProperties(magicInvConnection, _bot);
        } catch (NinjaBotException e) {
            _bot.logError(e);
        }
        return magicInvConnection;
    }

    
    private String getMagicPurchaseUrl() {
        return _bot.getBaseUrl() + MAGIC_PURCHASE_URL;
    }
    
    public HTTPConnection getMagicPurchaseConnection(String sid) throws MalformedURLException {
        URL url = null;
        HTTPConnection magicPurchaseConnection = null;
        try {
            url = new URL(getMagicPurchaseUrl() + "?" + PHPSESSID + _bot.getPhpSession());
            magicPurchaseConnection = new HTTPConnection(url);
            magicPurchaseConnection.setContentToken("sid=" + sid);
            magicPurchaseConnection.setContentToken("karma=1");
            buildCommonRequestProperties(magicPurchaseConnection, _bot);
        } catch (NinjaBotException e) {
            _bot.logError(e);
        }
        return magicPurchaseConnection;
    }
    
    protected String getGiftUrl() {
        return _bot.getBaseUrl() + GIFT_URL;
    }
    
    public HTTPConnection getGiftConnection() throws MalformedURLException {
        URL url;
        HTTPConnection giftConnection = null;
        try {
            url = new URL (getGiftUrl() + "?" + PHPSESSID + _bot.getPhpSession());
            giftConnection = new HTTPConnection(url);
            buildCommonRequestProperties(giftConnection, _bot);
        } catch (NinjaBotException e) {
            _bot.logError(e);
        }
        return giftConnection;
    }
    
    /**
     * Returns the URL to fetch your own {@link Clan}
     * @return
     */
    private String getMeUrl() {
        return _bot.getBaseUrl() + ME_URL;
    }
    
    /**
     * Returns the connection used to read the News
     * @param bos
     * @return
     * @throws MalformedURLException
     */
    public HTTPConnection getNewsConnection(ByteArrayOutputStream bos) throws MalformedURLException {
        URL url = new URL(getNewsUrl());
        HTTPConnection _newsConnection = new HTTPConnection(url, bos);
        try {
            String token = PHPSESSID + _bot.getPhpSession();
            _newsConnection.setContentToken(token);
            buildCommonRequestProperties(_newsConnection, _bot);
            _newsConnection.setRequestProperty("Content-length", String.valueOf(token.length()));
        } catch (NinjaBotException e) {
            // problem, send it to the log and the bot
            _bot.logError(e);
        }
        return _newsConnection;
    }

    /**
     * Returns the URL to read the News
     * @return
     */
    private String getNewsUrl() {
        return _bot.getBaseUrl() + NEWS_URL;
    }

    /**
     * Returns the connection used to view ones profile, which is essentially a {@link Clan}
     * @return
     * @throws MalformedURLException
     */
    public HTTPConnection getProfileConnection() throws MalformedURLException {
        return getProfileConnection(null);
    }

    /**
     * Returns the HHTP connection used to fetch a {@link Clan}
     * @param cid
     * @return
     * @throws MalformedURLException
     */
    public HTTPConnection getProfileConnection(String cid) throws MalformedURLException {
        URL url = null;
        HTTPConnection meConnection = null;
        try {

            if (cid == null) {
                url = new URL(getMeUrl());
            } else {
                url = new URL(getHimUrl() + "?PHPSESSID=" + _bot.getPhpSession());
            }

            meConnection = new HTTPConnection(url);
            if (cid != null) {
                meConnection.setContentToken("cid=" + cid);
            } else {
                String token = PHPSESSID + _bot.getPhpSession();
                meConnection.setContentToken(token);
            }

            buildCommonRequestProperties(meConnection, _bot);
        } catch (NinjaBotException e) {
            // problem, send it to the log and the bot
            _bot.logError(e);
        }
        return meConnection;
    }

    /**
     * Returns the connection to lists new recruits
     * @return
     * @throws MalformedURLException
     */
    public HTTPConnection getRecruitListConnection() throws MalformedURLException {
        URL url = null;
        HTTPConnection listRecruitConnection = null;
        try {
            url = new URL(getListRecruitUrl() + "?PHPSESSID=" + _bot.getPhpSession());
            listRecruitConnection = new HTTPConnection(url);
            buildCommonRequestProperties(listRecruitConnection, _bot);
        } catch (NinjaBotException e) {
            // problem, send it to the log and the bot
            _bot.logError(e);
        }
        return listRecruitConnection;

    }

    /**
     * Returns the connection used to sell an item from inventory
     * @param itemId
     * @return
     * @throws MalformedURLException
     */
    public HTTPConnection getSellConnection(int itemId) throws MalformedURLException {
        URL url = new URL(getSellUrl());
        HTTPConnection sellItemConnection = new HTTPConnection(url);
        try {
            String token = PHPSESSID + _bot.getPhpSession() + "&iid=" + itemId;
            sellItemConnection.setContentToken(token);
            //token = "iid=" + itemId;
            //sellItemConnection.setContentToken(token);
            buildCommonRequestProperties(sellItemConnection, _bot);
        } catch (NinjaBotException e) {
            _bot.logError(e);
        }
        return sellItemConnection;
    }

    /**
     * Sell item URL
     * @return
     */
    private String getSellUrl() {
        return _bot.getBaseUrl() + SELL_URL;
    }
    
    private String getStageUrl() {
        return _bot.getBaseUrl() + STAGE_URL;
    }
    
    public HTTPConnection getStageConnection() throws IOException {
        HTTPConnection connection = null;
        URL connectionUrl;
        try {
            connectionUrl = new URL(getStageUrl() + "?" + PHPSESSID + _bot.getPhpSession());
            connection = new HTTPConnection(connectionUrl);
            buildCommonRequestProperties(connection, _bot);
        } catch (NinjaBotException e) {
            _bot.logError(e);
        }
        return connection;
    }

    public HTTPConnection getTournamentPrizesConnection() throws IOException {
        HTTPConnection connection = null;
        URL connectionUrl;
        try {
            connectionUrl = new URL(getTournamentPrizesUrl() + "?" + PHPSESSID + _bot.getPhpSession());

            connection = new HTTPConnection(connectionUrl);
            buildCommonRequestProperties(connection, _bot);
        } catch (NinjaBotException e) {
            _bot.logError(e);
        }
        return connection;
    }

    public String getTournamentPrizesUrl() {
        return _bot.getBaseUrl() + TOURNEY_PRIZES_URL;
    }

    public HTTPConnection getTournamentFightConnection(String tid) throws MalformedURLException {
        URL url = null;
        HTTPConnection tourneyFightConnection = null;
        try {
            url = new URL(getTournamentFightUrl() + "?" + PHPSESSID + _bot.getPhpSession());
            tourneyFightConnection = new HTTPConnection(url);
            String token = "tid=" + tid;
            tourneyFightConnection.setContentToken(token);
            buildCommonRequestProperties(tourneyFightConnection, _bot);
        } catch (NinjaBotException e) {
            _bot.logError(e);
        }
        return tourneyFightConnection;
    }

    public String getTournamentFightUrl() {
        return _bot.getBaseUrl() + TOURNEY_FIGHT_URL;
    }
    /**
     * The weapon shop connection
     * @return
     * @throws MalformedURLException
     */
    public HTTPConnection getWeaponShopConnection() throws MalformedURLException {
        URL url = null;
        HTTPConnection wsConnection = null;
        try {
            url = new URL(getWeaponShopUrl() + "?PHPSESSID=" + _bot.getPhpSession());
            wsConnection = new HTTPConnection(url);
            String token = PHPSESSID + _bot.getPhpSession();
            wsConnection.setContentToken(token);
            buildCommonRequestProperties(wsConnection, _bot);
        } catch (NinjaBotException e) {
            // problem, send it to the log and the bot
            _bot.logError(e);
        }
        return wsConnection;
    }

    /**
     * The weapon shop url
     * @return
     * @throws MalformedURLException
     */
    public URL getWeaponShopUrl() throws MalformedURLException {
        return new URL(_bot.getBaseUrl() + WEAPON_SHOP_URL);
    }

}
