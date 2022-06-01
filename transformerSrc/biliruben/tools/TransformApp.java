package biliruben.tools;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionLegend;

import biliruben.transformer.DataProcessor;
import biliruben.transformer.TransformException;
import biliruben.transformer.adapter.AbstractTransformerSourceAdapter;
import biliruben.transformer.adapter.TransformerSourceAdapter;
import biliruben.transformer.handler.AbstractHandler;
import biliruben.transformer.handler.DataHandler;

public class TransformApp {

    private static final String OPT_TEMPLATE = "template";
    private static final String OPT_OUTPUT = "output";
    private static final String OPT_INPUT = "input";
    private static final String OPT_TARGET = "target";
    private static final String OPT_SOURCE = "source";
    private static final String LOG4J_PROPERTIES = "log4j.properties";
    private static final String DEFAULT_LOG_LEVEL = "warn";
    private static GetOpts opts;

    private static Log log = LogFactory.getLog(TransformApp.class);

    public static void main(String[] args) throws Exception {
        loadLog4j();
        init(args);

        String srcType = opts.getStr(OPT_SOURCE);
        String targetType = opts.getStr(OPT_TARGET);
        String templateUri = opts.getStr(OPT_TEMPLATE);
        String input = opts.getStr(OPT_INPUT);
        String output = opts.getStr(OPT_OUTPUT);
        
        Class<? extends AbstractTransformerSourceAdapter> sourceAdapterClass = AbstractTransformerSourceAdapter.getTransformer(srcType);
        log.debug("Found sourceAdapterClass: " + sourceAdapterClass);
        if (sourceAdapterClass == null) {
            log.error("No TransformerSourceAdapter class found for type: " + srcType);
            System.exit(1);
        }
        Class<? extends AbstractHandler> targetHandlerClass = AbstractHandler.getHandler(targetType);
        log.debug("Found targetHandlerClass: " + targetHandlerClass);
        if (targetHandlerClass == null) {
            log.error("No DataHandler class found for type: " + targetType);
            System.exit(1);
        }
        
        // Why you use URI and just use files!?
        // The use of URIs is a notion of future enhancement. For now, I'm only dealing in
        // files and STDIN/OUT. But maybe at some point in the future I might care about
        // using other resources. When I do, I'll already have enough basic framework in place
        // that existing apps shouldn't have to pivot as much.
        
        // build source adapter
        TransformerSourceAdapter adapter = sourceAdapterClass.newInstance();
        if (input != null) {
            URI uri = getUri(input);
            if (uri != null && "file".equalsIgnoreCase(uri.getScheme())) {
                Reader fileReader = new FileReader(new File(uri));
                adapter.setReader(fileReader);
            } else {
                log.error("URI is for a mistting file or is unsupported: " + input);
                System.exit(2);
            }
        } else {
            // use STDIN
            Reader stdinReader = new InputStreamReader(System.in);
            adapter.setReader(stdinReader);
        }

        // build handler
        DataHandler handler = targetHandlerClass.newInstance();
        if (templateUri != null && !"".equals(templateUri.trim())) {
            URI uri = getUri(templateUri);
            handler.setTemplateURI(uri);
        }
        Writer writer = null;
        if (output != null) {
            URI uri = getUri(output);
            if (uri != null && "file".equalsIgnoreCase(uri.getScheme())) {
                writer = new FileWriter(new File(uri));
            } else {
                log.error("URI is for a missing file or is unsupported: " + output);
                System.exit(2);
            }
        } else {
            // use stdout
            writer = new OutputStreamWriter(System.out);
        }

        // setup the processor and do the deed
        DataProcessor processor = new DataProcessor(opts.getProperties(), adapter, handler);
        processor.process();
        handler.write(writer);
    }

    private static URI getUri (String forUriOrFile) throws IOException, TransformException {
        log.trace("getUri: forUriOrFile = " + forUriOrFile);
        // First, see if it points to a File
        URI uri = null;
        File f = new File(forUriOrFile);
        if (f.exists()) {
            // roll with it
            uri = f.toURI();
        }
        if (uri == null) {
            try {
                uri = URI.create(forUriOrFile);
            } catch (IllegalArgumentException e) {
                // we kind of expect this to happen. So info it
                // we don't need the stack trace
                throw new TransformException(forUriOrFile + " is neither file nor URI", e);
            }
        }

        log.trace("Returning " + uri);
        return uri;
    }

    private static void init(String[] args) {
        opts = new GetOpts(TransformApp.class);
        
        // Required options:
        // iterator type
        // output type
        // 
        // Optional options
        // input file (defaults to STDIN)
        // output file (defaults to STDOUT)
        // template file (I'm assuming this isn't necessary for all input types)
        /*
         * So how to go about doing this. 
         * - 'iterator type': We want the user to just provide a file format. something like
         *      'xml', 'json', 'csv', etc. So I should build a map of filetype to class (or
         *      provide some other method of lookup to identify the proper class).
         *      
         * - 'output type': This correlates to the DataHandler. Same considerations have to be
         *      applied as are to 'iterator type'
         *      
         * - 'input / output file': These just become Input/OutputStreams. The App handles these
         *      streams and options can be handled directly and hard coded.
         *      
         * - 'template file': Let's think broader about this. The template 'file' could actually be
         *      anything, not just a file. Could be a URL! Whatever it is, the DataHandler is the
         *      consumer. Let's assume whatever the artifact is, it's globally accessible via URI.
         *      The DataHandler interface is refactored to include a `setTemplateURI` which takes a
         *      URI. The App can handle "assumptions", like a non-URI parameter is assumed to be a 
         *      local file and thus it constructs the URI for the user.
         */
        OptionLegend legend = new OptionLegend(OPT_SOURCE);
        legend.setRequired(true);
        legend.setDescription("Source type");
        opts.addLegend(legend);
        
        legend = new OptionLegend(OPT_TARGET);
        legend.setRequired(true);
        legend.setDescription("Target type");
        opts.addLegend(legend);
        
        legend = new OptionLegend(OPT_INPUT);
        legend.setRequired(false);
        legend.setDescription("Input file. If not provided, uses STDIN");
        opts.addLegend(legend);
        
        legend = new OptionLegend(OPT_OUTPUT);
        legend.setRequired(false);
        legend.setDescription("Output file. If not provided, uses STDOUT");
        opts.addLegend(legend);
        
        legend = new OptionLegend(OPT_TEMPLATE);
        legend.setRequired(false);
        legend.setDescription("Template file or URI");
        opts.addLegend(legend);

        // GetOpts has a build-in properties. Forgot how I deal with that
        opts.setPropertiesLegendHidden(false);
        legend = opts.getLegend(OptionLegend.OPT_PROPERTY_FILE);
        legend.setDescription("Property file defining directives");
        legend.setRequired(true);

        opts.parseOpts(args);
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
}
