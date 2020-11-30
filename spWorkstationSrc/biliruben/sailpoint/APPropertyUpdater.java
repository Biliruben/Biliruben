package biliruben.sailpoint;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionLegend;

import biliruben.tools.MergeProperties;
import biliruben.tools.MergeProperties.RuleSet;
import biliruben.util.Util;

public class APPropertyUpdater {


    private static GetOpts _opts;

    private static final String OPT_TAG = "tag";
    private static final String OPT_TEMPLATE = "template";
    private static final String OPT_PROPERTIES_FILES = "buildProperties";
    
    public static void main(String[] args) throws IOException {
        init(args);

        String tag = _opts.getStr(OPT_TAG);
        String templateSource = _opts.getStr(OPT_TEMPLATE);
        String propertiesFileName = _opts.getStr(OPT_PROPERTIES_FILES);
        
        File templateFile = null;
        if (!Util.isEmpty(templateSource)) {
            templateFile = new File(templateSource);
        }
        
        File propertiesFile = new File(propertiesFileName);
        
        Properties prop = new Properties();
        prop.load(new FileInputStream(propertiesFile));
        prop.setProperty("db.name", tag);
        prop.setProperty("db.userName", tag);
        prop.setProperty("db.userPassword", tag);
        prop.store(new FileOutputStream(propertiesFile), null);
        
        if (templateFile != null && templateFile.exists()) {
            Properties templateProp = new Properties();
            templateProp.load(new FileInputStream(templateFile));
            MergeProperties merger = new MergeProperties(RuleSet.MERGE_ADD);
            merger.merge(templateSource, propertiesFileName);
        }
    }
    
    private static void init(String[] args) {
        _opts = new GetOpts(APPropertyUpdater.class);
        
        // Need iiq tag
        OptionLegend legend = new OptionLegend(OPT_TAG);
        legend.setRequired(true);
        legend.setDescription("IIQ Tag representing database details");
        _opts.addLegend(legend);
        
        // template source file
        legend = new OptionLegend(OPT_TEMPLATE);
        legend.setRequired(false);
        legend.setDescription("Source template for static values");
        _opts.addLegend(legend);
        
        legend = new OptionLegend(OPT_PROPERTIES_FILES);
        legend.setRequired(false);
        legend.setDefaultValue("build.properties");
        legend.setDescription("Properties file to modify");
        _opts.addLegend(legend);
        
        // what else?
        
        _opts.parseOpts(args);
    }

}
