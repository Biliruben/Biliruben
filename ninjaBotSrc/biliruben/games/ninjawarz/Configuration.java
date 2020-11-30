package biliruben.games.ninjawarz;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.TreeSet;



/**
 * Bag of property handling methods.  Seems I should just port them over to {@link Util}
 * @author trey.kirk
 *
 */
public class Configuration {

    public static final String PREF_ENABLE_LOGGING = "log.enable";
    public static final String PREF_FIGHT_COUNT = "fight.count";
    public static final String PREF_FIGHT_DELAY = "fight.delay";
    public static final String PREF_LOG_FILE_NAME = "log.name";
    public static final String PREF_PHP_SESSION = "phpSession";
    public static final String PREF_BASE_URL = "system.baseUrl";
    
    public static Configuration readProperties(String fileName) throws IOException {
        Properties props = new Properties();
        File prefFile = getFile(fileName);
        if (prefFile.exists() && prefFile.canRead()) {
            props.load(new FileInputStream(prefFile));
        }
        return new Configuration(props);
    }
    
    private static File getFile(String fileName) {
        String filePath = NinjaBot.NINJABOT_SETTINGS_DIRECTORY + File.separator + fileName;
        return new File(filePath);
    }

    
    private Properties _props;

    public Configuration(Properties properties) {
        _props = properties;
    }

    public Map<String, String> getProperties(String forPrefix) {
        if (!forPrefix.endsWith(".")) {
            forPrefix += ".";
        }
        Map<String,String> matchedProps = new HashMap<String, String>();
        for (Object key : _props.keySet()) {
            String keyString = key.toString();
            if (keyString.startsWith(forPrefix)) {
                // strip the prefix
                String subProp = keyString.substring(forPrefix.length());
                matchedProps.put(subProp, _props.getProperty(keyString));
            }
        }
        return matchedProps;
    }

    public void writeProperties(String fileName) throws IOException {
        File prefFile = getFile(fileName);
        File dir = prefFile.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        _props.store(new FileOutputStream(prefFile), null);
    }

    public boolean getFlag(String property) {
        return getFlag(property, false);
    }
    
    public boolean getFlag(String property, boolean defaultValue) {
        String boolString = _props.getProperty(property, String.valueOf(defaultValue));
        return Boolean.valueOf(boolString);
    }
    
    public String getString(String property) {
        return _props.getProperty(property);
    }
    
    public String getString(String property, String defaultValue) {
        return _props.getProperty(property, defaultValue);
    }
    
    public Set<String> getKeys() {
        Set<String> keys = new TreeSet<String>();
        for (Object key : _props.keySet()) {
            keys.add(String.valueOf(key));
        }
        return keys;
    }

    public int getInteger(String property, int defaultValue) {
        int value = 0;
        try {
            value = Integer.valueOf(_props.getProperty(property, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            // don't care, leave as 0
        }
        return value;
    }
    
    public int getInteger(String property) {
        return getInteger(property, 0);
    }
    
    public void setProperty(String property, Object config) {
        _props.setProperty(property, String.valueOf(config));
    }
    
    public void removeProperty(String property) {
        _props.remove(property);
    }
    
    
}
