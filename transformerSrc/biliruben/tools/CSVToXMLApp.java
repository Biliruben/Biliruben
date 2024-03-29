package biliruben.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionLegend;
import com.biliruben.util.csv.CSVSource;
import com.biliruben.util.csv.CSVSource.CSVType;
import com.biliruben.util.csv.CSVSourceImpl;

import biliruben.transformer.DataProcessor;
import biliruben.transformer.handler.DocumentHandler;

public class CSVToXMLApp {

    private static final String OPT_CSV_FILE = "csvFile";
    private static final String OPT_OUT_FILE = "outFile";
    private static final String OPT_XML_FILE = "xmlFile";
    private static final String LOG4J_PROPERTIES = "log4j.properties";
    private static GetOpts opts;

    private static final String DEFAULT_LOG_LEVEL = "warn";

    public static void main(String[] args) throws Exception {

        loadLog4j();
        // parse the opts
        init(args);

        String csvFile = opts.getStr(OPT_CSV_FILE);
        String xmlFile = opts.getStr(OPT_XML_FILE);
        // setup the object
        CSVSource csv = new CSVSourceImpl(new File(csvFile), CSVType.WithHeader);
        DocumentHandler handler = new DocumentHandler();
        handler.setTemplateURI(new File(xmlFile).toURI());
        DataProcessor proc = new DataProcessor(opts.getProperties(), csv, handler);

        // process
        proc.process();
        String outputFile = opts.getStr(OPT_OUT_FILE);
        Writer writer = new StringWriter();
        if (!(outputFile == null || "".equals(outputFile.trim()))) {
            writer = new FileWriter(new File(outputFile));
        } else {
            writer = new PrintWriter(System.out);
        }
        handler.write(writer);
    }

    private static void loadLog4j() {
        File f = new File (LOG4J_PROPERTIES);
        if (f.exists()) {
            PropertyConfigurator.configure(LOG4J_PROPERTIES);
        } else {
            // Default properties
            Properties props = new Properties();
            props.setProperty("log4j.appender.stdout","org.apache.log4j.ConsoleAppender");
            props.setProperty("log4j.appender.stdout.Target","System.out");
            props.setProperty("log4j.appender.stdout.layout","org.apache.log4j.PatternLayout");
            props.setProperty("log4j.appender.stdout.layout.ConversionPattern","%d{ISO8601} %5p %t %c{4}:%L - %m%n");
            props.setProperty("log4j.rootLogger", DEFAULT_LOG_LEVEL + ",stdout");

            PropertyConfigurator.configure(props);
        }
    }

    private static void init(String[] args) {
        opts = new GetOpts(CSVToXMLApp.class);
        OptionLegend legend = new OptionLegend(OPT_XML_FILE, "Template XML file");
        legend.setRequired(true);
        opts.addLegend(legend);
        
        // GetOpts has a build-in properties. Forgot how I deal with that
        opts.setPropertiesLegendHidden(false);
        legend = opts.getLegend(OptionLegend.OPT_PROPERTY_FILE);
        legend.setDescription("Property file defining directives");
        legend.setRequired(true);

        legend = new OptionLegend(OPT_CSV_FILE, "CSV data source");
        legend.setRequired(true);
        opts.addLegend(legend);

        legend = new OptionLegend(OPT_OUT_FILE, "Output XML file. When not set, output will be sent to STDOUT");
        legend.setRequired(false);
        opts.addLegend(legend);

        opts.parseOpts(args);
    }
}
