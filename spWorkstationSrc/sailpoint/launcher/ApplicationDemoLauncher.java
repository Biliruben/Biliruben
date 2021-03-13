package sailpoint.launcher;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.api.DatabaseVersionException;
import sailpoint.api.SailPointContext;
import sailpoint.api.SailPointFactory;
import sailpoint.api.Terminator;
import sailpoint.object.Application;
import sailpoint.object.SailPointObject;
import sailpoint.object.Schema;
import sailpoint.spring.SpringStarter;
import sailpoint.tools.BrandingServiceFactory;
import sailpoint.tools.Console;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Util;
import sailpoint.tools.xml.XMLObjectFactory;

/*
 * Demonstration utility that creates a temporary Application
 * and subjects it to various activities that should trip the
 * hooks to capture null schemas
 * @author trey.kirk
 *
 */
public class ApplicationDemoLauncher extends Console {

    static private Log log = LogFactory.getLog(ApplicationDemoLauncher.class);

    private static SailPointContext context;

    static private String getArg(String option, String[] args) {
        for (int i = 0; i < args.length; ++i) {
            if (args[i].equals(option) && (i + 1 < args.length)) {
                return args[i + 1];
            }
        }

        return null;
    }

    private static String parseSpringOverride(String[] args) {

        String override = null;

        int index = 0; 
        while (override == null && index < args.length) {
            String arg = args[index];
            if (arg.startsWith("-")) {
                // -c and -f both have secondary args
                if (arg.equals("-c") || arg.equals("-f"))
                    index++;
            }
            else {
                // assume the first thing without a prefix
                // is our override file
                override = arg;
            }
            index++;
        }
        return override;
    }



    /*
     * We need to use the SpringStarter to launch so we can get a context. This
     * emulates how the IIQ Console launches up, which means other unintended
     * consequences like services. So register this
     * as a unique hostname that doesn't get confused with a real consolr
     * or a servlet
     */
    final static String CONSOLE_SUFFIX = "-applicationDemoLauncher";

    /*
     * Test Applicaiton name
     */
    final static String TEST_APPLICATION_NAME = "$$TEST_APPLICATION$$";


    public static void main(String[] args) {

        int exitStatus = 0;

        // for testing with multiple consoles
        String host = getArg("-h", args);
        if (host != null && host.length() > 0) {
            println("Setting iiq.hostname to " + host);
            System.setProperty("iiq.hostname", host);
        }
        else {
            // IIQHH-793 Since no hostname was specified, give
            // console a different hostname by appending "-console"
            // to end of hostname, to distinguish it from a possible
            // webapp running on the same host.

            try {
                InetAddress addr = InetAddress.getLocalHost();
                String nativeHostName = addr.getHostName();
                if (Util.isNotNullOrEmpty(nativeHostName)) {
                    String calculatedHostName = nativeHostName + CONSOLE_SUFFIX;
                    System.setProperty("iiq.hostname", calculatedHostName);
                    println("Setting iiq.hostname to " + calculatedHostName);
                }
            }
            catch (UnknownHostException e) {
                // not good, but we will walk away whistling, and
                // let the system to handle as it has previously
            }
        }

        boolean allowHeartbeat = false;

        // look for a spring file override
        String override = parseSpringOverride(args);

        String dflt = BrandingServiceFactory.getService().getSpringConfig();
        SpringStarter ss = new SpringStarter(dflt, override);

        String configFile = ss.getConfigFile();
        if (!configFile.startsWith(dflt))
            println("Reading spring config from: " + configFile);

        try {
            // suppress the background schedulers
            // also full text since it can lead to contention for
            // index files with the app server
            // TODO: upgrader has to do similar things, see if we can factor out
            // a shared list for the console apps, or better yet have the ServiceDefinitions
            // way where they can run
            String[] suppressedServices = {"Request", "Task", "FullText", "SMListener", "PluginSync"};
            String suppressedStr = getArg("-s", args);
            if (Util.isNotNullOrEmpty(suppressedStr)) {
                suppressedServices = Util.csvToArray(suppressedStr);
            }

            ss.setSuppressedServices(suppressedServices);
            if (!allowHeartbeat)
                ss.addSuppressedServices("Heartbeat");

            long start = 0l, end = 0l;
            if (log.isInfoEnabled()) {
                log.info("Starting springstarter...");
                start = System.currentTimeMillis();
            }
            ss.start();
            if (log.isInfoEnabled()) {
                end = System.currentTimeMillis();
                long timeTaken = (end - start)/1000;
                log.info("Done starting springstarter in " + timeTaken + "s.");
            }

            try {
                context = SailPointFactory.createContext();

                // Instead of creating the console and running it, run our
                // test methods here.
                setApplicationSchemaNull();
                parseXmlWithNull();
            
            } catch (GeneralException ge) {
                log.error(ge.getMessage(), ge);
            } finally {
                SailPointFactory.releaseContext(context);
            }
        }
        catch (DatabaseVersionException dve) {
            // format this more better  
            println(dve.getMessage());
            exitStatus = 1;
        }
        catch (Throwable t) {
            println(t);
            exitStatus = 1;
        }
        finally {
            try {   
                ss.close();
            }
            catch (Throwable t) {
                // I guess Spring shutdown failure should also cause this...
                println(t);
                exitStatus = 1;
            }
        }

        System.exit(exitStatus);
    }

    private static void parseXmlWithNull() throws GeneralException {
        log.warn ("Nulling the schema via XML parsing");
        // to make it easier for us, we'll build it as a POJO and incidentally
        // trip Application.setSchemas, which is fine
        // 
        // With the pojo, then convert to an XML. From there
        // is where we perform the exercise of parsing an XML clob
        // that doesn't include Schemas,
        Application app = createTestApplication();
        cleanUpTestApplication();
        // app is evicted by now, clear its ID
        app.setId(null);
        app.setSchemas(null);
        String appXml = app.toXml();
        log.warn ("Parsing Application with null Schemas");
        SailPointObject xmlApp = (SailPointObject)XMLObjectFactory.getInstance().parseXml(context, appXml, false);
        log.warn("Saving XML object");
        context.saveObject(xmlApp);
        log.warn("Committing XML object");
        context.commitTransaction();
        cleanUpTestApplication();
    }

    private static void setApplicationSchemaNull() throws GeneralException {
        log.warn("Set null schemas");
        Application app = createTestApplication();
        app.setSchemas(null);
        context.saveObject(app);
        context.commitTransaction();
        cleanUpTestApplication();
        
        log.warn("Set empty schemas");
        app = createTestApplication();
        app.setSchemas(new ArrayList<Schema>());
        context.saveObject(app);
        context.commitTransaction();
        cleanUpTestApplication();
    }

    private static Application createTestApplication() throws GeneralException {
        Application app = context.getObjectByName(Application.class, TEST_APPLICATION_NAME);
        if (app != null) {
            cleanUpTestApplication();
        }
        app = new Application ();
        app.setName(TEST_APPLICATION_NAME);
        Schema schema = new Schema("account", "account");
        app.addSchema(schema);
        schema = new Schema("group", "group");
        app.addSchema(schema);
        context.saveObject(app);
        context.commitTransaction();
        return app;
    }
    
    private static void cleanUpTestApplication() throws GeneralException {
        Application app = context.getObjectByName(Application.class, TEST_APPLICATION_NAME);
        if (app != null) {
            Terminator t = new Terminator(context);
            t.deleteObject(app);
            context.commitTransaction();
            context.decache();
        }
        
    }

}
