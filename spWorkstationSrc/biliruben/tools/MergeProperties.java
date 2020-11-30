package biliruben.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionLegend;

import biliruben.util.Util;

/**
 * Utility to merge a supplied properties file with a target file using predefined rulesets
 * Required:
 * - user's properties - the properties the user is providing
 * - target properties - the base properties to be merged to; this is the target file!
 * 
 * Optional:
 * - Ruleset: MERG-ADD, MERGE-ONLY or MERGE-ADD
 * @author trey.kirk
 *
 */
public class MergeProperties {

    private static final String OPT_RULE = "rule";
    private static final String OPT_TARGET = "target";
    private static final String OPT_USER = "user";
    private static GetOpts _opts;

    public static void main(String[] args) throws IOException {
        init(args);
        String rule = _opts.getStr(OPT_RULE);
        RuleSet rs = RuleSet.fromName(rule);
        MergeProperties merger = new MergeProperties(rs);
        merger.merge(_opts.getStr(OPT_USER), _opts.getStr(OPT_TARGET));
    }

    private static void init(String[] args) {
        _opts = new GetOpts(MergeProperties.class);

        // User file
        OptionLegend legend = new OptionLegend(OPT_USER);
        legend.setRequired(true);
        legend.setDescription("User properties file to import");
        _opts.addLegend(legend);

        // target file
        legend = new OptionLegend(OPT_TARGET);
        legend.setRequired(true);
        legend.setDescription("Target properties file to merge/add to");
        _opts.addLegend(legend);

        // rules
        legend = new OptionLegend(OPT_RULE);
        legend.setRequired(false);
        legend.setDescription("Ruleset to use");
        legend.setAllowedValues(new String[] {RuleSet.MERGE_ADD.getName(),
                RuleSet.MERGE_ONLY.getName(), RuleSet.ADD_ONLY.getName()});
        legend.setDefaultValue(RuleSet.MERGE_ADD.getName());
        _opts.addLegend(legend);

        String usageTail = "\n\tMERGE-ADD: Default ruleset: properties from the user are merged when they are found\n" + 
                "\t\tin the target properties or added when they are not.\n" + 
                "\tMERGE-ONLY: Properties from the user are merged only when they are found in the\n" + 
                "\t\ttarget properties file. None are added\n" + 
                "\tADD-ONLY: Properties from the user are only added when not found in the target file\n";

        _opts.setDescriptionTail(usageTail);
        _opts.parseOpts(args);
    }

    public enum RuleSet {
        MERGE_ADD("mergeAdd"),
        MERGE_ONLY("merge"),
        ADD_ONLY("add");

        private String _name;

        RuleSet(String name) {
            this._name = name;
        }

        public String getName() {
            return this._name;
        }

        public static RuleSet fromName(String name) {
            for (RuleSet rs : RuleSet.values()) {
                if (rs.getName().equalsIgnoreCase(name)) {
                    return rs;
                }
            }

            return null;
        }
    }

    private RuleSet _ruleSet;

    public MergeProperties(RuleSet rules) {
        if (rules != null) {
            this._ruleSet = rules;
        } else {
            this._ruleSet = RuleSet.MERGE_ADD;
        }
    }

    public void merge(String userFilename, String targetFilename) throws IOException {
        if (Util.isEmpty(userFilename)) {
            throw new IllegalArgumentException("User file may not be null");
        } else if (Util.isEmpty(targetFilename)) {
            throw new IllegalArgumentException("Target file may not be null");
        }

        File userFile = new File(userFilename);
        if (!userFile.exists()) {
            throw new FileNotFoundException(userFile + " cannot be found.");
        }

        File targetFile = new File(targetFilename);
        if (!targetFile.exists()) {
            throw new FileNotFoundException(targetFilename + " cannot be found.");
        }

        switch (_ruleSet) {
            case ADD_ONLY: add(userFile, targetFile); break;
            case MERGE_ADD: mergeAdd(userFile, targetFile); break;
            case MERGE_ONLY: merge(userFile, targetFile); break;
        }
    }

    private Properties getProperties(File fromFile) throws IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(fromFile));
        return props;
    }

    private void saveProperties(Properties propertiesToSave, File toFile) throws IOException {
        StringBuilder commentsBuffer = new StringBuilder();
        commentsBuffer.append("Created by " + MergeProperties.class.getSimpleName()).append("\n");
        commentsBuffer.append(new Date());
        propertiesToSave.store(new FileOutputStream(toFile), commentsBuffer.toString());
    }

    private void merge(File userFile, File targetFile) throws IOException {
        // iterates through userFile properties and replaces only those
        // found in targetFile
        Properties userProps = getProperties(userFile);
        Properties targetProps = getProperties(targetFile);
        for (Enumeration<String> e = (Enumeration<String>) userProps.propertyNames(); e.hasMoreElements();) {
            String property = e.nextElement();
            String value = userProps.getProperty(property);
            if (targetProps.containsKey(property)) {
                targetProps.setProperty(property, value);
            }
        }
        saveProperties(targetProps, targetFile);
    }

    private void mergeAdd(File userFile, File targetFile) throws FileNotFoundException, IOException {
        // calls merge, then add
        merge(userFile, targetFile);
        add(userFile, targetFile);
    }

    private void add(File userFile, File targetFile) throws IOException {
        // iterates through userFile and adds only those not found
        // in targetFile
        Properties userProps = getProperties(userFile);
        Properties targetProps = getProperties(targetFile);
        for (Enumeration<String> e = (Enumeration<String>) userProps.propertyNames(); e.hasMoreElements();) {
            String property = e.nextElement();
            String value = userProps.getProperty(property);
            if (!targetProps.containsKey(property)) {
                targetProps.setProperty(property, value);
            }
        }
        saveProperties(targetProps, targetFile);
    }
}
