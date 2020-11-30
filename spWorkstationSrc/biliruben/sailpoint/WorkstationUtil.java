package biliruben.sailpoint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Static utilities for LappyBoo to use
 * @author trey.kirk
 *
 */
public class WorkstationUtil {
    
    private static final int DEFAULT_MANAGER_FACTOR = 10;


    public static void main(String[] args) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
       // arg1 is always the command
       if (args == null || args.length < 1) {
           String msg = "At least one argument must be provided:\n";
           Method[] methods = WorkstationUtil.class.getMethods();
           for (Method m : methods) {
               // all static public methods not named 'main' are commands
               if (!"main".equals(m.getName()) && Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers())) {
                   msg = msg + m.getName() + "\n";
               }
           }
           System.err.println(msg);
           return;
       }
       String cmd = args[0];
       Method m = WorkstationUtil.class.getMethod(cmd, List.class);
       List<String> subArgs = null;
    // arg2+ are any options.  The command method is reponsible for parsing and reporting the options
       if (args.length > 1) {
           subArgs = Arrays.asList(args).subList(1, args.length);
       }
       m.invoke(null, subArgs);
    }
    
    public static void filterDupe(List<String> args) throws IOException {
        if (args == null || args.size() < 1) {
            throw new IllegalArgumentException("You must provide a file name to process");
        }
        String f = args.get(0);
        BufferedReader r = new BufferedReader(new FileReader(f));
        String last = null;
        while (r.ready()) {
            String line = r.readLine();
            if (line != null && !line.equals(last)) {
                System.out.println(line);
                last = line;
            }
        }
    }
    
    /**
     * Given a list of accountIds, render a tree of accounts using a 'factor' representing the number of
     * subordiantes per manager. This value is treated as strictly as possible and does not represent 'real-world'
     * hierarchies where organizations have groups varying in size
     * @param args
     * @throws IOException
     */
    public static void createHierarchy(List<String> args) throws IOException {
        
        // each tier is a map of lists. The key is the manager name while the value
        // is a list of subordinates. Each element of the lists are further maps of
        // list until the lowest level is reached, Maps of null values. Here the keys
        // are the employees
        if (args == null || args.size() < 1) {
            throw new IllegalArgumentException("A filename containing a single column of accountIds must be provided");
        }
        int factor = DEFAULT_MANAGER_FACTOR;
        if (args.size() == 2) {
            factor = Integer.valueOf(args.get(1));
        }
        int count = 0;
        int level = 0;
        int levelLimit = 1;
        BufferedReader reader = openFileForRead(args.get(0));
        Map<Integer, List<String>> tiers = new HashMap<Integer, List<String>>();
        while (reader != null && reader.ready()) {
            String line = reader.readLine();
            List<String> tier = tiers.get(level);
            if (tier == null) {
                tier = new ArrayList<String>();
                tiers.put(level, tier);
            }
            tier.add(line);
            count++;
            if (count >= levelLimit) {
                level++;
                levelLimit = (int)Math.pow(factor, level);
                count = 0;
            }
        }
        int maxLevel = level;
        
        // we have a map of managers and their level, now build parings
        List<String> previousTier = tiers.get(0);
        for (level = 1; level <= maxLevel; level++) {
            List<String> tier = tiers.get(level);
            for (int i = 0; i < previousTier.size(); i++) {
                String mgr = previousTier.get(i);
                for (int j = 0; j < factor && ((factor*i) + j) < tier.size(); j++) {
                    String employee = tier.get((factor*i) + j);
                    System.out.println(mgr + "," + employee);
                }
            }
            previousTier = tier;
        }
    }
    
    private static int calculateLevel(int count, int factor) {
        // level = log(factor)(count)
        // log(factor-base)(of-count) = ln(of-count)/ln(factor-base)
        count++;
        double rawLevel = Math.log(count)/Math.log(factor);
        // Java & base 10 arithmatic with real numbers is quirky. Coerce to a big integer and
        // perform integer operations instead
        
        // Note: I'm reinventing the Math.ciel() operation but incorporating
        // a bit of double truncation. Why? because in our case, numbers like
        // 5.0000000000001 should return 5, not 6
        int rawLevelFloor = (int) Math.floor(rawLevel);
        int rawLevelScaled = (int) (rawLevel * 10000);
        int rawLevelFloorScaled = rawLevelFloor * 10000;
        int delta = rawLevelScaled - rawLevelFloorScaled;
        if (delta > 0) {
            return rawLevelFloor + 1;
        } else {
            return rawLevelFloor + 0;
        }
    }
    
    
    private static BufferedReader openFileForRead(String fileName) throws FileNotFoundException {
        File f = new File(fileName);
        FileReader fr = new FileReader(f);
        BufferedReader bfr = new BufferedReader(fr);
        return bfr;
    }
    
    /**
     * Converts the iiq.properties file to use the iiqTag in the db username, pw, and mysql db name
     * @param sphome
     * @param iiqTag
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    public static void convertIIQProperties(List<String> args) throws FileNotFoundException, IOException {
        if (args == null || args.size() != 2) {
            throw new IllegalArgumentException("Required arguments: 'gitHome', 'iiqTag'");
        }
        // Read in the iiq properties file as a Properties object
        String spHome = args.get(0);
        String iiqTag = args.get(1);
        /* fuck this! callers have to supply the directory
        if (!spHome.endsWith("src")) {
            spHome = spHome.concat("/src");
        }
        */
        
        Properties prop = new Properties();
        prop.load(new FileInputStream(new File(spHome + "/iiq.properties")));
        
        // find the three properties to jackerate and jackerate
        prop.setProperty("dataSource.username", iiqTag);
        prop.setProperty("dataSource.password", iiqTag);
        prop.setProperty("dataSource.url", "jdbc:mysql://localhost/" + iiqTag + "?useServerPrepStmts=true&tinyInt1isBit=true&useUnicode=true&characterEncoding=utf8");
        
        // serialize back to iiq.properties
        prop.store(new FileOutputStream(new File(spHome + "/iiq.properties")), "Created by " + WorkstationUtil.class.getSimpleName() + " " + new Date().toString());
    }

    
    public static void convertDDL(List<String> args) throws IOException {
        if (args == null || args.size() < 2) {
            throw new IllegalArgumentException("Required arguments: 'sphome', 'iiqTag', ['DDL']");
        }
        
        String spHome = args.get(0);
        String iiqTag = args.get(1);
        String ddl = "/WEB-INF/database/create_identityiq_tables.mysql";
        if (args.size() > 2) {
            ddl = args.get(2);
        }

        File f = new File(spHome + ddl);
        FileReader fr = new FileReader(f);
        BufferedReader bfr = new BufferedReader(fr);
        StringWriter writer = new StringWriter();
        String line = null;
        //Pattern pattern = Pattern.compile("(.* )identityiq([\\s\\.;'].*)");
        Pattern pattern = Pattern.compile("identityiq([^P])");
        do {
            line = bfr.readLine();
            if (line != null) {
                /*
                Matcher m = pattern.matcher(line);
                if (m.matches()) {
                    String preamble = m.group(1);
                    String suffix = m.group(2);
                    StringBuffer buff = new StringBuffer();
                    String replaced = buff.append(preamble).append(m.replaceAll(iiqTag)).append(suffix).toString();
                    //String replaced = line != null ? line.replaceAll("identityiq[\\.;\\s]", iiqTag) : "";
                    writer.write(replaced + "\n"); // do I need a newline?
                } else {
                    writer.write(line + "\n");
                }
                */
                Matcher m = pattern.matcher(line);
                StringBuffer buff = new StringBuffer();
                while (m.find()) {
                    m.appendReplacement(buff, iiqTag);
                    if (m.groupCount() > 0) {
                        buff.append(m.group(1));
                    }
                }
                m.appendTail(buff);
                line = buff.toString();
            }
            writer.write(line + "\n");
        } while (bfr.ready() && line != null);
        
        bfr.close();
        FileWriter fw = new FileWriter(f);
        fw.write(writer.toString());
        fw.flush(); fw.close();

    }
}
