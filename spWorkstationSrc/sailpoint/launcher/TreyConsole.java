package sailpoint.launcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

import com.biliruben.util.csv.CSVSource;
import com.biliruben.util.csv.CSVSource.CSVType;

import biliruben.threads.ThreadRunner;

import com.biliruben.util.csv.CSVSourceImpl;

import sailpoint.api.Bootstrapper;
import sailpoint.api.DatabaseVersionException;
import sailpoint.api.IncrementalObjectIterator;
import sailpoint.api.ManagedAttributer;
import sailpoint.api.ObjectUtil;
import sailpoint.api.PersistenceManager;
import sailpoint.api.PersistenceManager.LockParameters;
import sailpoint.api.SailPointContext;
import sailpoint.api.SailPointFactory;
import sailpoint.object.Application;
import sailpoint.object.AttributeDefinition;
import sailpoint.object.Attributes;
import sailpoint.object.Bundle;
import sailpoint.object.Certification;
import sailpoint.object.CertificationEntity;
import sailpoint.object.CertificationItem;
import sailpoint.object.Filter;
import sailpoint.object.Filter.MatchMode;
import sailpoint.object.Identity;
import sailpoint.object.Link;
import sailpoint.object.ManagedAttribute;
import sailpoint.object.Profile;
import sailpoint.object.QueryOptions;
import sailpoint.object.SailPointObject;
import sailpoint.object.Schema;
import sailpoint.object.Scope;
import sailpoint.object.TaskSchedule;
import sailpoint.persistence.HPMWrapper;
import sailpoint.server.HeartbeatService;
import sailpoint.server.Importer;
import sailpoint.server.SailPointConsole;
import sailpoint.spring.SpringStarter;
import sailpoint.tools.BrandingServiceFactory;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Util;
import sailpoint.tools.XmlUtil;
import sailpoint.tools.xml.XMLObjectFactory;

public class TreyConsole extends SailPointConsole {

    private Set<String[]> _names;
    private PrintWriter _out;
    private int _propNameMaxLength;

    static private Log log = LogFactory.getLog(TreyConsole.class);
    
    private static class VisitingMonitor extends ImportMonitor {

        private Bootstrapper _strapOn;

        public VisitingMonitor(PrintWriter out, SailPointContext ctx) {
            super(out);
            this._strapOn = new Bootstrapper(ctx);
        }
        
        @Override
        public void report(SailPointObject obj) {
            super.report(obj);
            try {
                this._strapOn.visit(obj);
            } catch (GeneralException e) {
                log.error("Some shit went down", e);
            }
        }
    }
    
    private static abstract class WorkloadThread<W> extends Thread {
        boolean _halted = false;
        Deque<W> _work;
        PrintWriter _out;
        
        WorkloadThread(Deque<W> workQueue, PrintWriter out) {
            this._work = workQueue;
            this._out = out;
        }
    }
    
    private static class RoleOrganizer extends WorkloadThread<Map<String, Object>> {
        public static final String CHILD_ROLE_KEY = "child";
        public static final String PARENT_ROLE_KEY = "parent";
        public static final String MODE_KEY = "mode";
        
        private static final int COMMIT_LIMIT = 10;
        
        public static String PERMITS = "permits";
        public static String REQUIRES = "requires";
        public static String INHERITS = "inherits";
        // this needs to be in alpha order
        public static String[] MODES =  new String[] {
                INHERITS,
                PERMITS,
                REQUIRES
        };

        
        private RoleOrganizer(Deque<Map<String, Object>> workLoad, PrintWriter out) {
            super(workLoad, out);
        }
        
        @Override
        public void run() {
            
            
            SailPointContext ctx = null;
            try {
                ctx = SailPointFactory.createContext();
                
                while (!this._halted) {
                    try {
                        Map<String, Object> work = null;
                        if (this._work.isEmpty()) {
                            sleep(250);
                            continue;
                        } else {
                            try {
                                work = this._work.pop();
                            } catch (NoSuchElementException nse) {
                                // ignore and continue
                                continue;
                            }
                            if (log.isTraceEnabled()) log.trace("Work: " + work);
                            String parentRoleName = (String)work.get(PARENT_ROLE_KEY);
                            List<String> childRoleNames = (List<String>)work.get(CHILD_ROLE_KEY);
                            //_out.println("Parent: " + parentRoleName);
                            //_out.println("Child: " + childRoleName);
                            Bundle parentRole = ctx.getObjectByName(Bundle.class, parentRoleName);

                            if (parentRole == null) {
                                log.warn("Missing parent role");
                                continue;
                            }

                            String mode = (String)work.get(MODE_KEY);
                            log.trace("Mod: " + mode);
                            if (Util.isNullOrEmpty(mode)) {
                                log.warn("No mode specified");
                                continue;
                            }

                            for (String childRoleName : childRoleNames) {
                                Bundle childRole = ctx.getObjectByName(Bundle.class, childRoleName);
                                if (childRole == null) {
                                    // next!
                                    log.warn(childRoleName + " was not found");
                                    continue;
                                }
                                if (PERMITS.equals(mode)) {
                                    // the parent permits the child
                                    if (!Util.nullSafeContains(parentRole.getPermits(), childRole)) {
                                        parentRole.addPermit(childRole);
                                    }
                                } else if (REQUIRES.equals(mode)) {
                                    // the parent requires the child
                                    if (!Util.nullSafeContains(parentRole.getRequirements(), childRole)) {
                                        parentRole.addRequirement(childRole);
                                    }
                                } else if (INHERITS.equals(mode)) {
                                    // child inherits the parent
                                    if (!Util.nullSafeContains(childRole.getInheritance(), parentRole)) {
                                        childRole.addInheritance(parentRole);
                                        ctx.saveObject(childRole);
                                    }
                                }

                                ctx.saveObject(parentRole);
                                ctx.saveObject(childRole);
                                ctx.commitTransaction();
                            }
                            ctx.decache();
                        }
                    } catch (Throwable t) {
                        log.error(t.getMessage(), t);
                    }
                }
            } catch (Throwable t) {
                log.error(t.getMessage(), t);
            } finally {
                try {
                    if (ctx != null) SailPointFactory.releaseContext(ctx);
                } catch (GeneralException e) {
                    e.printStackTrace(_out);
                    log.error(e.getMessage(), e);
                }
            }
        }
    }
    
    private static class GenRoleThread extends WorkloadThread<Map<String, Object>> {
        
        private static final String KEY_ROLE_NAME = "name";
        private static final String KEY_ROLE_TYPE = "type";
        private static final String KEY_ROLE_PROFILES = "profiles";
        private static final String KEY_ACTION = "_action";
        private static final String KEY_UPDATE_OP = "upOp";
        private static final String KEY_UPDATE_CLASS = "upClass";
        private static final String KEY_UPDATE_AS_OJB = "upAsObj";
        
        private enum Action {
                Create,
                Update,
                Delete
        }

        private int count;
        private String threadName;
        private GenRoleThread(Deque<Map<String, Object>> workQueue, PrintWriter out) {
            super(workQueue, out);
        }
        
        @Override
        public void run() {
            SailPointContext ctx = null;
            threadName = Thread.currentThread().getName();
            try {
                count = 1;
                ctx = SailPointFactory.createContext();
                Map<String, Object> work = null;
                while (!_halted) {
                    try {
                        if (!_work.isEmpty()) {
                            try {
                                work = _work.pop();
                            } catch (NoSuchElementException e) {
                                log.warn(e);
                                Thread.sleep(100);
                                work = null;
                            }
                            if (log.isTraceEnabled()) {
                                log.trace(threadName + " : " + work);
                            }
                        } else {
                            work = null;
                        }

                        if (work != null) {
                            String name = (String)work.get(KEY_ROLE_NAME);
                            if (Util.isNullOrEmpty(name)) {
                                log.warn("Null name for: " + work);
                                continue;
                            }
                            String actionStr = (String)work.get(KEY_ACTION);
                            if (Util.isNullOrEmpty(actionStr)) {
                                log.info("Null action, defaulting to CREATE");
                                actionStr = Action.Create.toString();
                            }
                            Action action = null;
                            try {
                                action = Action.valueOf(actionStr);
                            } catch (IllegalArgumentException e) {
                                log.error(actionStr + " is not a valid action: " + Arrays.toString(Action.values()));
                                continue;
                            }

                            switch (action) {
                                case Create: createGenRole(ctx, name, work);
                                break;
                                case Delete: break; // not implemented yet
                                case Update: updateGenRole(ctx, name, work);
                                break;
                            }
                        }
                    } catch (Throwable t) {
                        t.printStackTrace(_out);
                    }
                    // why?
                    //sleep(250);
                }
                log.debug("Created " + count + " objects");
            } catch (Throwable t) {
                
            } finally {
                if (ctx != null) {
                    try {
                        SailPointFactory.releaseContext(ctx);
                    } catch (GeneralException e) {
                        e.printStackTrace(_out);
                    }
                }
            }
        }
        
        private boolean updateGenRole(SailPointContext ctx, String name, Map<String, Object> work) throws GeneralException {
            // The intent is to modify any part of the target role. My original use case deals with setting an arbitrary
            // Attributes object. Specifying that data in a CSV is... dumb. So the next best thing is to "copy" an existing
            // object from the database. So why not add the following fields:
            // upClass,upOp,upAsObj
            // upClass: "Attributes.class" - infer sailpoint.object unless specified. ...do I need this?
            // upOp: "Set" (or merge or add or...?) ... not implemented yet. Currently only does 'set'
            // upAsObj: "id:property" - split on the colon, fetch the object byId, and assume 'getProperty' is the getter method
            Bundle role = ctx.getObjectByName(Bundle.class, name);
            if (role == null) {
                log.warn(name + " does not exist, nothing to update.");
                return false;
            }
            String asObjToken = (String)work.get(KEY_UPDATE_AS_OJB);
            if (Util.isNullOrEmpty(asObjToken)) {
                log.warn("No example object to emulate. Need key: " + KEY_UPDATE_AS_OJB); 
                return false;
            }
            String[] asObjArry = asObjToken.split(":");
            if (asObjArry.length != 2) {
                log.warn("AsObj token is malformed (not ID:Property): " + asObjToken);
                return false;
            }
            String asObjId = asObjArry[0];
            String asObjProperty = asObjArry[1];
            Bundle asRole = ctx.getObjectById(Bundle.class, asObjId);
            if (asRole == null) {
                log.warn("AsObj Role not found: " + asObjId);
                return false;
            }
            String getMethodName = "get" + asObjProperty; // Assume input will have this cased appropriately
            String setMethodName = "set" + asObjProperty;

            try {
                Method sourceMethod = asRole.getClass().getMethod(getMethodName, null);
                Object sourceValue = sourceMethod.invoke(asRole, null);
                Method targetMethod = role.getClass().getMethod(setMethodName, sourceValue.getClass());
                targetMethod.invoke(role, sourceValue);
            } catch (NoSuchMethodException e) {
                // Can't find one of the methods. Your data might suck.
                // generic msg is fine
                log.error(e.getMessage(), e);
                return false;
            } catch (SecurityException e) {
                // Fun security issue, bail
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                // I wrote this badly, just bail
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            } catch (IllegalArgumentException e) {
                // I wrote this badly, just bail
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                // I wrote this badly, just bail
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
            
            ctx.saveObject(role);
            ctx.commitTransaction();
            ctx.decache();
            // holy shit, we made it here? Nice!
            return true;
        }
        
        private boolean createGenRole(SailPointContext ctx, String name, Map<String, Object> work) 
                throws GeneralException {
            Bundle role = ctx.getObjectByName(Bundle.class, name);
            if (role != null) {
                log.warn(name + " already exists");
                return false;
            }
            String type = (String)work.get(KEY_ROLE_TYPE);
            if (Util.isNullOrEmpty(type)) {
                log.warn("Null type for: " + work);
                return false;
            }

            count++;

            List<Profile> profiles = (List<Profile>)work.get(KEY_ROLE_PROFILES);
            role = new Bundle();
            role.setName(name);
            role.setType(type);
            Attributes<String, Object> attributes = new Attributes<String, Object>();
            attributes.put("createdThread", threadName);
            if (!Util.isEmpty(profiles)) {
                for (Profile p : profiles) {
                    role.add(p);
                }
            }
            role.setAttributes(attributes);
            ctx.saveObject(role);
            ctx.commitTransaction();
            ctx.decache();
            return true;
        }

    }
    
    
    private static class DeleterThread extends WorkloadThread<String> {
        private Class<? extends SailPointObject> _clazz;

        private DeleterThread(Class<? extends SailPointObject> clazz, Deque<String> workQueue, PrintWriter out) {
            super(workQueue, out);
            _clazz = clazz;
        }

        @Override
        public void run() {
            SailPointContext ctx = null;
            String threadName = Thread.currentThread().getName();
            try {
                int count = 0;
                ctx = SailPointFactory.createContext();
                while (!_halted) {
                    String nextId = null;
                    synchronized(_work) {
                        if (!_work.isEmpty()) {
                            nextId = _work.pop();
                        }
                    }
                    if (nextId != null) {
                        SailPointObject object = ctx.getObjectById(this._clazz, nextId);
                        if (object != null) {
                            if (log.isInfoEnabled()) log.info(threadName + ": Deleting " + object.getName());
                            try {
                                ctx.removeObject(object);
                            } catch (GeneralException ge) {
                                ge.printStackTrace();
                            }
                            count++;
                        }
                    }
                    if (count % 20 == 0) {
                        try {
                            log.warn("Deleted " + count);
                            ctx.commitTransaction();
                            ctx.decache();
                        } catch (GeneralException ge) {
                            ge.printStackTrace();
                        }
                    }

                    Thread.sleep(500);
                }
                try {
                    ctx.commitTransaction();
                    ctx.decache();
                } catch (GeneralException ge) {
                    ge.printStackTrace();
                }
            } catch (GeneralException ge) {
                throw new RuntimeException(ge);
            } catch (InterruptedException e) {
                // nothing
            } finally {
                if (ctx != null) {
                    try {
                        SailPointFactory.releaseContext(ctx);
                    } catch (GeneralException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    /**
     * Parse and remove args that are relevant to us before passing
     * them on to sailpoint.tools.Console.
     * This is a stupid parser and has awareness of what Console 
     * will do.
     */
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
    
    static private boolean hasArg(String option, String[] args) {
        boolean exists = false;
        for (int i = 0 ; i < args.length && !exists ; i++) {
            exists = args[i].equals(option);
        }
        return exists;
    }


    static private String getArg(String option, String[] args) {
        for (int i = 0; i < args.length; ++i) {
            if (args[i].equals(option) && (i + 1 < args.length)) {
                return args[i + 1];
            }
        }

        return null;
    }

    public static void main(String[] args) {
        main8(args);
        //main7(args);
    }
    
    /*
    public static void main7(String [] args) {

        int exitStatus = 0;

        // for testing with multiple consoles
        String host = getArg("-h", args);
        if (host != null && host.length() > 0) {
            println("Setting iiq.hostname to " + host);
            System.setProperty("iiq.hostname", host);
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

            SailPointConsole console = new TreyConsole();
            console.run(args);
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
    */

    public static void main8(String[] args) {

        final String CONSOLE_SUFFIX = "-console";

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


        boolean allowHeartbeat = hasArg("-heartbeat", args);

        // look for a spring file override
        String override = parseSpringOverride(args);

        String dflt = BrandingServiceFactory.getService().getSpringConfig();
        SpringStarter ss = new SpringStarter(dflt, override);

        String configFile = ss.getConfigFile();
        if (!configFile.startsWith(dflt))
            println("Reading spring config from: " + configFile);

        try {
            // Only start the Cache service
            ss.minimizeServices();

            String suppressedStr = getArg("-s", args);
            if (Util.isNotNullOrEmpty(suppressedStr)) {
                // no longer supported
                println("The -s argument is no longer supported.  Instead, use -e to specify a list of services to enable.");
                System.exit(1);
            }

            String enabledStr = getArg("-e", args);
            if (Util.isNotNullOrEmpty(enabledStr)) {
                ss.setWhitelistedServices(Util.csvToArray(enabledStr));
            }


            if (allowHeartbeat)
                ss.addWhitelistedService("Heartbeat");
            
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

            SailPointConsole console = new TreyConsole();
            console.run(args);
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

            // set the server as inactive if it it had a heartbeat
            boolean hasHeartbeat = ss.isWhitelistedService("Heartbeat");
            if (hasHeartbeat) {
                String hostName = Util.getHostName();
                if (hostName != null && hostName.endsWith(CONSOLE_SUFFIX)) {
                    // mark this server as inactive
                    HeartbeatService.markServerInactive(hostName);
                }
            }

            // shutdown what was started by Spring
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

    TreyConsole() {
        addCommand("getAny", "Fetches XML for any object", "cmdGetAny");
        addCommand("property", "Displays property information of a given class.", "cmdProperty");
        addCommand("mappedClasses", "Displays classes mapped in the Hibernate configuration.", "cmdMappedClasses");
        addCommand("password", "Changes the password for an Identity", "cmdPassword");
        addCommand("filterTest", "Renders a filter from Java", "cmdFilterTest");
        addCommand("compileFilter", "Compiles a filter", "cmdCompileFilter");
        addCommand("qoTest", "Dumb shit", "cmdQoTest");
        addCommand("cloneWorkgroup", "Clones some workgroup", "cmdCloneWorkgroup");
        addCommand("moveLinks", "Move all of the links in the system to one Identity", "cmdMoveLinks");
        addCommand("resetTaskSchedule", "Resets a task schedule ala the Terminate Orhpan Task", "cmdResetTaskSchedule");
        addCommand("generateCsvObjects", "Reads a CSV file and builds objects dexcribed by the CSV", "cmdGenerateCsvObjects");
        addCommand("knukeIt", "Blanket deletion of a class of objects", "cmdKnukeit");
        addCommand("multiDelete", "Better deletion", "cmdMultiDelete");
        addCommand("bootImport", "Imports objects and bootstraps (better) referenced objects", "cmdBootstrapImport");
        addCommand("listAny", "List any fucking object I want, fuck you!", "cmdListAny");
        addCommand("fuck", "Express your frustations", "cmdFuck");
        addCommand("changePwds", "Change all passwords because, why not", "cmdChangeAllPasswords");
        addCommand("importScopes", "Import the Scopes... OF DOOM!!", "cmdImportScopes");
        addCommand("nestObjects", "Nests children objects into parents given a CSV mapping", "cmdNestObjects");
        addCommand("filterToSQL", "Converts a given filter into SQL", "cmdFilterToSQL");
        addCommand("generateCsvMA", "Generates ManagedAttributes from CSV", "cmdGenerateCsvMA");
        addCommand("generateITCsvRoles", "Generates IT roles from CSV", "cmdGenerateITRollModel");
        addCommand("generateBizCsvRoles", "Generates Business roles from CSV", "cmdGenerateBizRoleModel");
        addCommand("updateCsvRole", "Updates roles with some data", "cmdUpdateCSVRole");
        addCommand("organizeCsvRoles", "Organizes a flat role model into a hierarchy", "cmdOgranizeCSVRoles");
        addCommand("set3580", "Do that bullshit in Set-3580", "cmdSET3580");
        addCommand("scaleTestCompile", "Scale test arbitrary plan compliation", "cmdScaleTestPlanCompile");
    }

    /*
     * Fetches the name and value type of the given property.  Tracks the longest string
     * length of all known property names to ensure prety formatting
     */
    /*
    private void addProperty (Property prop) {
        Value v = prop.getValue();
        Type type = v.getType();
        _propNameMaxLength = prop.getName().length() > _propNameMaxLength ? prop.getName().length() : _propNameMaxLength;
        _names.add(new String[] {prop.getName(), type.getName()});
    }
     */
    /*
     * Loads the provided class's mappings and iterates the properties
     */
    /*
    private void exploreProperties(String name) {
        PersistentClass pc = _config.getClassMapping(name);
        if (pc == null) {
            _out.println("Could not load mappings for " + name);
        } else {
            Iterator propertyIt = pc.getDeclaredPropertyIterator(); // Hibernate doesn't parameterize this iterator
            _names = new HashSet<String[]>();
            Set<String> sortedNames = new TreeSet<String>();
            while (propertyIt.hasNext()) {
                addProperty((Property) propertyIt.next()); // They's all properties, so cast away
            }
            addProperty(pc.getIdentifierProperty()); // Don't forget the identifier property (aka 'id')
            // Second pass thru is for formatting and sorting
            for (String[] propNameArry : _names) {
                String propName = propNameArry[0];
                propName = String.format("%1$-" + _propNameMaxLength + "s", propName);
                String typeName = propNameArry[1];
                sortedNames.add(propName + " : " + typeName);
            }
            // Third pass goes out
            for (String sortedName : sortedNames) {
                _out.println(sortedName);
            }
        }
    }
     */
    public void cmdMoveLinks(List<String> args, PrintWriter out) throws GeneralException {
        if (Util.isEmpty(args)) {
            out.format("moveLinks <identityNameOrId>\n");
            return;
        }
        SailPointContext ctx = SailPointFactory.createContext();
        try {
            String id = args.get(0);

            Identity identity = ctx.getObject(Identity.class, id);
            if (identity == null) {
                out.format("Identity not found: " + id + "\n");
                return;
            }

            QueryOptions opts = new QueryOptions();
            opts.add(Filter.ne("identity", identity));
            IncrementalObjectIterator<Link> it = new IncrementalObjectIterator<Link>(ctx, Link.class, opts);
            int count = 0; 
            while (it.hasNext()) {
                count++;
                Link nextLink = it.next();
                identity.add(nextLink);
                ctx.saveObject(identity);
                ctx.saveObject(nextLink); // probably don't have to do this
                ctx.commitTransaction();
                if (count % 20 == 0) {
                    ctx.decache();
                    identity = ctx.getObject(Identity.class, id);
                }
            }
        } finally {
            SailPointFactory.releaseContext(ctx);
        }
    }

    private void commitAndReportDeletes(SailPointContext ctx, PrintWriter out, int count, int of, String clazz) throws GeneralException {
        ctx.commitTransaction();
        ctx.decache();
        out.println("Deleted " + count + " of " + of + " " + clazz + " objects");
    }


    private void deleteAllOfClass(SailPointContext ctx, PrintWriter out, Class<? extends SailPointObject> theClass, int every) throws GeneralException {
        IncrementalObjectIterator certItemIt = new IncrementalObjectIterator(ctx, theClass, new QueryOptions());
        while (certItemIt.hasNext()) {
            SailPointObject item = certItemIt.next();
            ctx.removeObject(certItemIt.next());
            if (certItemIt.getCount() % every == 0) {
                commitAndReportDeletes(ctx, out, certItemIt.getCount(), certItemIt.getSize(), theClass.getSimpleName());
            }
        }
        if (certItemIt.getCount() % every != 0) {
            commitAndReportDeletes(ctx, out, certItemIt.getCount(), certItemIt.getSize(), theClass.getSimpleName());
        }
    }

    public void cmdKnukeit(List<String> args, PrintWriter out) throws GeneralException {
        SailPointContext ctx = SailPointFactory.createContext();
        try {
            String clazz = args.get(0);
            int every = 50;
            if (args.size() > 1) {
                every = Integer.valueOf(args.get(1));
            }
            if ("Certification".equalsIgnoreCase(clazz)) {
                deleteAllOfClass(ctx, out, CertificationItem.class, every);
                deleteAllOfClass(ctx, out, CertificationEntity.class, every);
                deleteAllOfClass(ctx, out, Certification.class, every);
            } else {
                out.println("Sorry, I can't do that right now");
            }
        } catch (Throwable t) {
            t.printStackTrace(out);
        } finally {
            SailPointFactory.releaseContext(ctx);
        }
    }

    public void cmdMultiDelete(List<String> args, PrintWriter out) throws GeneralException {
        if (Util.size(args) < 1) {
            out.println("Class [threads]");
            return;
        }
        String clazzStr = args.get(0);
        Class clazz;
        try {
            if (!clazzStr.contains(".")) {
                clazzStr = "sailpoint.object." + clazzStr;
            }
            clazz = Class.forName(clazzStr);
        } catch (ClassNotFoundException ge) {
            throw new GeneralException(ge);
        }
        int threadNum = 4;
        if (args.size() > 1) {
            String threadz = args.get(1);
            threadNum = Integer.valueOf(threadz);
        }
        Deque<String> workLoad = new LinkedBlockingDeque<String>();
        SailPointContext ctx = null;
        try {
            ctx = SailPointFactory.createContext();
            Iterator<Object[]> ids = ctx.search(clazz, new QueryOptions(), "id");
            while (ids.hasNext()) {
                workLoad.push((String)ids.next()[0]);
            }
            out.println(workLoad.size() + " workload");
        } finally {
            SailPointFactory.releaseContext(ctx);
        }

        List<DeleterThread> threads = new ArrayList<DeleterThread>();
        for (int i = 0; i < threadNum; i++) {
            DeleterThread t = new DeleterThread(clazz, workLoad, out);
            t.start();
            threads.add(t);
        }

        while (!workLoad.isEmpty()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        for (DeleterThread thread : threads) {
            thread._halted = true;
        }
    }
    
    public void cmdBootstrapImport(List<String> args, PrintWriter out) throws GeneralException {
        SailPointContext ctx = null;
        boolean noid = false;
        String filename = null;

        if (args.size() > 0) {
            String arg = args.get(0);
            if (arg.equals("-noids")) {
                noid = true;
                if (args.size() > 1)
                    filename = args.get(1);
            }
            else
                filename = arg;
        }

        if (filename == null) {
            out.format("import [-noids] <filename>\n");
            out.format("-noids: remove all id attributes before parsing\n");
        } else {

        try {
            String xml = Util.readFile(filename);
            ctx = SailPointFactory.createContext();
                ctx.decache();
                ImportMonitor m = new VisitingMonitor(out, ctx);
                Importer i = new Importer(ctx, m);
                i.setScrubIds(noid);
                /*
                List<ImportCommand> commands = i.getCommands(xml);
                for (ImportCommand cmd : commands) {
                    out.println(cmd);
                    AbstractXmlObject xmlObj = cmd.getArgument();
                    out.println(xmlObj);
                    if (cmd instanceof ImportCommand.Save) {
                        Save saveCmd = (ImportCommand.Save)cmd;
                    }
                }
                */
                i.importXml(xml);
        } finally {
            if (ctx != null) {
                SailPointFactory.releaseContext(ctx);
            }
        }
        
        }
    }

    public void cmdCloneWorkgroup(List<String> args, PrintWriter out) throws GeneralException {
        SailPointContext ctx = SailPointFactory.createContext();
        try {
            if (args.size() < 2) {
                out.format("cloneWorkgroup <template> <total>\n", null);
                return;
            }
            String cloneName = args.get(0);
            int count = Integer.valueOf(args.get(1));
            Filter baseFilter = Filter.eq("workgroup", true);
            QueryOptions opts = new QueryOptions();
            opts.add(baseFilter);
            Identity clone = ctx.getObject(Identity.class, cloneName);
            if (clone == null) {
                out.format("Target workgroup is null; " + cloneName + " not found!\n");
                return;
            }
            String cloneId = clone.getId();
            ctx.decache(); // gets our clone out of the cache
            for (int i = 0; i < count; i++) {
                String newName = getUniqueName(ctx, Identity.class, cloneName, baseFilter);
                out.format("Creating new workgroup %s\n", newName);
                // use the clone as our "base" object
                clone.setId(null);
                clone.setName(newName);
                ctx.saveObject(clone);
                ctx.commitTransaction();
                QueryOptions identityOptions = new QueryOptions();
                identityOptions.add(Filter.eq("workgroups.id", cloneId));
                IncrementalObjectIterator<Identity> idIt = new IncrementalObjectIterator<Identity>(ctx, Identity.class, identityOptions);
                while (idIt.hasNext()) {
                    // refetch our clone to keep context.decache from murderlizing it
                    clone = ctx.getObjectById(Identity.class, clone.getId());
                    Identity nextIdentity = idIt.next();
                    nextIdentity.add(clone);
                    ctx.saveObject(nextIdentity);
                    ctx.commitTransaction();
                }
                // decache, for the clone's sake
                ctx.decache();
            }
        } finally {
            SailPointFactory.releaseContext(ctx);
        }
    }

    private String getUniqueName (SailPointContext ctx, Class<? extends SailPointObject> scope, String templateName,
            Filter baseFilter) throws GeneralException {
        String candidateName = templateName;
        String suffix = "";
        int found = 0;
        int last = 0;
        do {
            QueryOptions opts = new QueryOptions();
            candidateName = templateName + suffix;
            opts.add(baseFilter);
            opts.add(Filter.like("name", candidateName, MatchMode.START));
            found = ctx.countObjects(scope, opts);
            last += found;
            suffix = " " + last;
        } while (found > 0);

        return candidateName;
    }
    
    public void cmdFuck(List<String> args, PrintWriter out) throws GeneralException {
        if (Util.isEmpty(args)) {
            out.println("No thanks, I just had breakfast");
        } else if (args.size() == 1) {
            out.println("I don't think " + args.get(0) + " wants that.");
        } else {
            StringBuilder fuckingBuff = new StringBuilder();
            for (String fucker : args) {
                fuckingBuff.append(fucker).append(" ");
            }
            out.println(fuckingBuff.toString() + " can all go politely fuck themseleves in their respective ani");
        }
    }
    
    public void cmdChangeAllPasswords(List<String> args, PrintWriter out) throws GeneralException {
        if (Util.isEmpty(args)) {
            out.println("To what, an empty password? Fuck you!");
        } else {
            SailPointContext ctx = SailPointFactory.createContext();
            try {
                String password = args.get(0);
                QueryOptions ops = new QueryOptions();
                IncrementalObjectIterator<Identity> objIt = new IncrementalObjectIterator<Identity>(ctx, Identity.class, ops);
                while (objIt.hasNext()) {
                    Identity nextDude = objIt.next();
                    nextDude.setPassword(password);
                    ctx.saveObject(nextDude);
                    if (objIt.getCount() % 20 == 0) {
                        ctx.commitTransaction();
                        ctx.decache();
                    }
                }
                ctx.commitTransaction();
            } finally {
                SailPointFactory.releaseContext(ctx);
            }
        }
    }
    
    public void cmdFilterToSQL(List<String> args, PrintWriter out) throws GeneralException {
        if (Util.size(args) < 2) {
            out.println("usage: class file [countBoolean] [distinctBoolean] [col,col,col]");
            return;
        }
        String clsname = args.get(0);
        String filterFileName = args.get(1);
        String columnString = null;
        boolean count = false;
        boolean distinct = false;
        if (args.size() > 2) {
            if (!"false".equalsIgnoreCase(args.get(2))) {
                count = true;
            }
        }

        if (args.size() > 3) {
            if (!"false".equalsIgnoreCase(args.get(3))) {
                distinct = true;
            }
        }

        if (args.size() > 4) {
            columnString = args.get(4);
        }

        List<String> properties = new ArrayList<String>();
        
        if (columnString != null) {
            properties = Util.csvToList(columnString);
        } else if (count) {
            properties = new ArrayList<String>();
            properties.add("count(*)");
        }
        
        Class<? extends SailPointObject> cls = null;

        if ( isWorkgroupSubtype(clsname) ) {
            cls = Identity.class;
        } else {
            cls = findClass(clsname, out);
        }
        SailPointContext ctx = null;
        FileReader fr = null;

        try {
            
            ctx = SailPointFactory.createContext();

            File filterFile = new File(filterFileName);
            if (!filterFile.exists()) {
                out.println(filterFileName + " not found!");
                return;
            }
        
            fr = new FileReader(filterFile);
            StringBuilder buff = new StringBuilder();
            int i = 0;
            do {
                i = fr.read();
                if (i >= 0) {
                    buff.append((char)i);
                }
            } while (i >= 0);
            Filter f = Filter.compile(buff.toString());
            QueryOptions ops = new QueryOptions();
            ops.add(f);
            ops.setDistinct(distinct);
            HPMWrapper hpmw = new HPMWrapper(ctx);
            String query = "";
            String queryWithParams = "";
            out.println(query);
            out.println(queryWithParams);
            
        } catch (IOException ioe) {
            ioe.printStackTrace(out);
        } finally {
            if (ctx != null)
                SailPointFactory.releaseContext(ctx);
            if (fr != null)
                try {
                    fr.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace(out);
                }
            
        }
    }

    public void cmdResetTaskSchedule(List<String> args, PrintWriter out) throws GeneralException {
        if (args != null && args.size() < 1) {
            out.format("usage: resetTaskSchedule id\n");
        } else {
            SailPointContext ctx = SailPointFactory.createContext();
            String tsId = args.get(0);
            try {
                TaskSchedule ts = ctx.getObject(TaskSchedule.class, tsId);
                out.format("Found schedule: " + ts + "\n");
                if (ts != null) {
                    Date startDate = ts.getNextExecution();
                    Date now = new Date();
                    if (startDate.before(now)) {
                        ts.setNextExecution(now);
                    }
                    ctx.saveObject(ts);
                    ctx.commitTransaction();
                }
            }
            finally
            {
                SailPointFactory.releaseContext(ctx);
            }

        }
    }
    
    public void cmdNestObjects(List<String> args, PrintWriter out) throws GeneralException {
        if (args.size() < 2) {
            out.format("usage: nestObjects type file [setter]\n");
        } else {
            String clsname = args.get(0);
            String csvFile = args.get(1);
            String setter = null;

            SailPointContext ctx = null;
            Class<? extends SailPointObject> cls = null;
            try {
                ctx = SailPointFactory.createContext();

                if ( isWorkgroupSubtype(clsname) ) {
                    cls = Identity.class;
                } else {
                    cls = findClass(clsname, out);
                }

                if (args.size() > 2) {
                    setter = args.get(2);
                } else {
                    setter = "set" + cls.getSimpleName();
                }

                File f = new File(csvFile);
                if (!f.exists()) {
                    out.println(csvFile + " does not exists!");
                    return;
                }

                Method puttinator = null;
                puttinator = cls.getMethod(setter, cls);
                CSVSource csv = new CSVSourceImpl(f, CSVType.WithOutHeader);
                String[] line;
                int i = 0;
                do {
                    line = csv.getNextLine();
                    if (line != null) {
                        String parent = line[0];
                        String kiddo = line[1];

                        SailPointObject pObject = ctx.getObject(cls, parent);
                        SailPointObject cObject = ctx.getObject(cls, kiddo);
                        if (pObject == null) {
                            out.println("Parent object " + parent + ": object not found");
                            continue;
                        } else if (cObject == null) {
                            out.println("Child object " + kiddo + ": object not found");
                            continue;
                        }
                        i++;
                        puttinator.invoke(pObject, cObject);
                        ctx.saveObject(pObject);;
                        ctx.saveObject(cObject);
                        if (i % 20 == 0) {
                            ctx.commitTransaction();
                            ctx.decache();
                        }
                    }
                } while (line != null);
                ctx.commitTransaction();
                ctx.decache();
            } catch (IOException ieo) {
                out.println("Problem nesting objects: " + ieo.getMessage());
                return;
            } catch (NoSuchMethodException e) {
                out.println("Problem finding setter method: " + setter + " on class " + cls);
            } catch (SecurityException e) {
                out.println("Problem accessing setter method: " + setter + " on class " + cls);
            } catch (IllegalAccessException e) {
                out.println("Problem using setter: " + setter + " on class " + cls);
            } catch (IllegalArgumentException e) {
                out.println("Problem using setter: " + setter + " on class " + cls);
            } catch (InvocationTargetException e) {
                out.println("Problem invoking setter: " + setter + " on class " + cls);
            } finally {
                if (ctx != null) 
                    SailPointFactory.releaseContext(ctx);
            }

        }
    }

    public void cmdQoTest(List<String> args, PrintWriter out) throws GeneralException {
        SailPointContext ctx = SailPointFactory.createContext();
        try {
            QueryOptions opts = new QueryOptions();
            List<String> names = new ArrayList<String>();
            names.add("Aaron.Nichols");
            names.add("Top Level Managers");
            names.add("Top Level Managers 1");

            opts.add(Filter.in("certifiers", names));
            Iterator<Object[]> results = ctx.search(Certification.class, opts, "id");

            while (results.hasNext()) {
                StringBuilder buff = new StringBuilder();
                Object[] row = results.next();
                buff.append("id:" + row[0] + "\n");
                out.format(buff.toString(), null);
            }
        } finally {
            SailPointFactory.releaseContext(ctx);
        }
    }
    
    /**
     * A test method to purposely create a LazyInit exception
     * @param args
     * @param out
     */
    public void cmdSET3580(List<String> args, PrintWriter out) {
        if (args.size() < 1) {
            out.println("No! Gimmie an application!");
            return;
        }
        String appName = args.get(0);
        SailPointContext context = null;
        try {
            context = SailPointFactory.createContext();
            Application application = context.getObjectByName(Application.class, appName);
            if (application == null) {
                out.println("Dumb! Application: " + application);
                return;
            }
            // Let's do the fuck-around
            out.println("Application: " + application);
            application.load();
            out.println("Loaded application: " + application + " : "  + application.toXml().length());
            Application lockedApp = ObjectUtil.lockObject(context, Application.class, LockParameters.createById(application.getId(), PersistenceManager.LOCK_TYPE_TRANSACTION));
            out.println("Locked app: " + lockedApp);
            out.println("lockedApp and application are the same: " + (lockedApp == application));
            out.println("Decaching!");
            context.decache();
            out.println("Application after decache: " + application.toXml());
        } catch (Exception e) {
            log.error("I made this!", e);
        } finally {
            if (context != null) {
                try {
                    SailPointFactory.releaseContext(context);
                } catch (GeneralException ge) {
                    log.error(ge.getMessage(), ge);
                }
            }
        }
    }

    public void cmdFilterTest(List<String> args, PrintWriter out)
            throws Exception {
        Filter f = Filter.or(
                Filter.and(
                        Filter.not(Filter.eq("name", "ding-dong")),
                        Filter.eq("name", "shmoopy")), 
                        Filter.eq("name", "sally")
                );
        String filterString = f.getExpression();
        out.println(filterString);
        out.flush();
    }

    public void cmdCompileFilter(List<String> args, PrintWriter out)
            throws Exception {
        if (args.size() < 1) {
            out.format("Filter string is required");
        } else {
            // replace 'quot;' with '"'
            String filterString = args.get(0).replaceAll("&quot;", "\"");
            Filter f = Filter.compile(filterString);
            out.format(f.toString() + "\n");
        }
    }
    /**
     * Property Command: list the provided class's properties
     * @param args
     * @param out
     * @throws Exception
     */
    public void cmdProperty(List<String> args, PrintWriter out) throws Exception {
        /*
        int nargs = args.size();
        _out = out;
        _propNameMaxLength = 1;
        if (nargs != 1) { // balk when no or multiple options passed
            out.format("property <Any Mapped Class>\n");
        }
        else {
            String name = args.get(0);
            if (!name.matches("^.*\\..*")) {  // did they give it a proper path?
                name = "sailpoint.object." + name; // No?  Assume sailpoint.obj
            }
            exploreProperties(name);
        }
         */
        out.println("Naw, this doesn't do anything right now");
    }
    
    public void cmdListAny(List<String> args, PrintWriter out) throws Exception {
        if (args == null || args.size() < 1) {
            out.format("So, just list any ole random class? FOAD");
        } else {
            SailPointContext ctx = createContext();
            String shortClass = args.get(0);
            if (!shortClass.contains(".")) {
                // augment with the full path
                shortClass = "sailpoint.object." + shortClass;
            }
            Class _lastListClass = Class.forName(shortClass);
            try {
                // save for later use in cmdGet
                ArrayList<String> _lastListIds = new ArrayList<String>();

                // jsl - QuartzPersistenceManager doesn't support projection
                // searches, so do it the old fashioned way for now
                if (_lastListClass == TaskSchedule.class) {

                    // TODO: recognize the name filter and sort by name
                    List<SailPointObject> objs = ctx.getObjects(_lastListClass, null);
                    if (objs != null) {
                        // TODO: would be nice to sort these...
                        // when name is an id we need about 34 chars
                        String format = "%-34s %s\n";
                        out.format(format, "Name", "Description");
                        out.println("----------------------------------------" +
                                    "------------------------------------");
    
                        for (SailPointObject obj : objs) {
                            Object id = obj.getId();
                            Object desc = obj.getDescription();
                            if (desc == null) 
                                desc = "";
                            else 
                                desc = ((String)desc).trim();

                            // todo: i18n description
                            
                            out.format(format, id, desc);
                            _lastListIds.add(id.toString());
                        }
                    }
                }
                else {   
                    QueryOptions ops = new QueryOptions();
                    boolean idAdded = false;
                    // djs : since there will be millions of these prevent users from listing 
                    // ALL entitlements with *
                    Method m =
                            _lastListClass.getMethod("getDisplayColumns", (Class[])null);
                    Map<String, String> colsObj = 
                          (Map<String, String>)m.invoke(null, (Object[])null);
                    List<String> colsList =
                                      new ArrayList<String>(colsObj.keySet());

                    if (colsList.size() > 0) {
                        // always sort by the first column
                        ops.setOrderBy(colsList.get(0));
                    }

                    if ( ! colsList.contains("id") ) {
                        colsList.add(0, "id");
                        idAdded = true;
                    }

                    m = _lastListClass.getMethod("getDisplayFormat", (Class[])null);
                    String format = (String)m.invoke(null, (Object[])null);
                    
                    Object[] labels = colsObj.values().toArray();
                    List<String> ledgerList = new ArrayList<String>();
                    for ( Object label : labels ) {
                        int count = label.toString().length();
                        char[] chars = new char[count];
                        while ( count > 0 ) chars[--count] = '-';
                        ledgerList.add(new String(chars));
                    }
                    Object[] ledgers = ledgerList.toArray();
                    
                    Iterator<Object[]> it = ctx.search(_lastListClass, ops, colsList);
                    if (it != null) {
                        
                        out.format(format, labels);
                        out.format(format, ledgers);
    
                        int idIndex = colsList.indexOf("id");
                        int nameIndex = colsList.indexOf("name");

                        while (it.hasNext()) {
                            Object[] current = it.next();
                            
                            if (idIndex >= 0)
                                _lastListIds.add((String)current[idIndex]);

                            // if name is one of the columns and there is
                            // no name, then use the id instead
                            if ( nameIndex >= 0 && idIndex >= 0 ) {
                                if ( current[nameIndex] == null ||
                                        current[nameIndex].toString().length() == 0 ) {
                                    current[nameIndex] = current[idIndex];
                                }
                            }
                            
                            // massage any data for a more usable display
                            for ( int i = 0; i < current.length; i++ ) {
                                Object value = current[i];

                                if (value == null ) {
                                    value = "";
                                }
                                else if (value instanceof String) {
                                    // this is nice for things that came from XML elements
                                    // that often have newlines at the front
                                    // actually there can be newlines embedded in this
                                    // would be nice to filter those as well...
                                    value = ((String)value).trim();
                                }
                                else if ( value instanceof SailPointObject ) {
                                    SailPointObject spo = ((SailPointObject)current[i]);
                                    String nameOrId = spo.getName();
                                    if ( nameOrId == null || nameOrId.length() == 0 )
                                        nameOrId = spo.getId();
                                    value = nameOrId;
                                }
                                else if (value instanceof Date) {
                                    value = Util.dateToString((Date)current[i]);
                                }
                                current[i] = value;
                            }
                            
                            // if we added an id to the query columns, then
                            // remove it now that we are done with it
                            if ( idAdded ) {
                                System.arraycopy(current, 1, current, 0,
                                                          current.length - 1);
                            }

                            out.format(format, current);
                        }
                    }
                }

      
                
            } finally {
                SailPointFactory.releaseContext(ctx);
            }
        }
    }

    public void cmdPassword(List<String> args, PrintWriter out) throws Exception {
        int nargs = args.size();

        if (nargs != 2) {
            out.format("password identity newPassword");
        } else {
            SailPointContext ctx = createContext();
            try {

                Identity id = ctx.getObjectByName(Identity.class, args.get(0));
                id.setPassword(args.get(1));
                ctx.saveObject(id);
                ctx.commitTransaction();
            }
            finally
            {
                SailPointFactory.releaseContext(ctx);
            }

        }
    }

    private boolean isWorkgroupSubtype(String type ) {
        if ( ( Util.getString(type) != null ) && 
                ( type.toLowerCase().startsWith("workg") ) ) {
            return true;
        }
        return false;
    }

    public void cmdGetAny(List<String> args, PrintWriter out) throws Exception {
        int nargs = args.size();
        _out = out;
        if (nargs != 2) {
            out.format("getAny <Class> <name or id>\n");
        }
        else {
            Class cls = null;
            String clsname = args.get(0);
            if ( isWorkgroupSubtype(clsname) ) {
                cls = Identity.class;
            } else {
                cls = Class.forName("sailpoint.object." + clsname);
            }
            String name = args.get(1);
            if (cls != null) {
                SailPointContext ctx = createContext();
                try
                {
                    SailPointObject obj = findObject(ctx, cls, clsname, name, out);
                    if (obj != null)
                        out.format("%s\n", obj.toXml());

                    // temporary, testing Hibernate cache issues
                    //ctx.commitTransaction();
                }
                finally
                {
                    SailPointFactory.releaseContext(ctx);
                }
            }
        }

    }

    /**
     * Since we don't support authentication yet, pass a pseudo-user
     * name to be used as an audit source.
     */
    private SailPointContext createContext() throws GeneralException {

        return SailPointFactory.createContext("Console");
    }


    /**
     * Mapped Classes command: List the classes defined in the Hibernate mapping configuration
     * @param args
     * @param out
     * @throws Exception
     */
    public void cmdMappedClasses(List<String> args, PrintWriter out) throws Exception {
        /*
        Iterator classesIt = _config.getClassMappings();
        Set<String> classes = new TreeSet<String>();
        while (classesIt != null && classesIt.hasNext()) {
            PersistentClass pc = (PersistentClass) classesIt.next();
            classes.add(pc.getClassName()); // for auto-sorting
        }
        for (String clazz : classes) {
            out.println(clazz);
        }
         */
        out.println("Naw, this doesn't do anything");
    }
    
    
    public void cmdImportScopes(List<String> args, PrintWriter out) throws Exception {
        if (args == null || args.size() != 1) {
            out.println("An XML file is required, dummy.");
            return;
        }
        
        // Calculate RoleChangeEvents analyzing Role.
        String filename = args.get(0);

        SailPointContext ctx = createContext();
        try {
                String xml = Util.readFile(filename);

                    // checking in UIPrefs gets "multiple object with same id" error
                    // decache first
                    ctx.decache();
                    
                    XMLObjectFactory factory = XMLObjectFactory.getInstance();
                    
                    Element root = XmlUtil.parse(xml, factory.getDTD(), true);
                    Stack<Scope> scopes = new Stack<Scope>();
                    
                    if (root != null) {
                        if (Importer.EL_SAILPOINT.equals(root.getTagName())) {
                            // iterate over elements; process each Scope object
                            for (Element child = XmlUtil.getChildElement(root);
                                    child != null;
                                    child = XmlUtil.getNextElement(child)) {
                                parseScope(out, ctx, factory, scopes, child);
                            }
                        } else if ("Scope".equalsIgnoreCase(root.getTagName())) {
                            // process the only element as a single Scope
                            parseScope(out, ctx, factory, scopes, root);
                        }
                    }

                    // Map is a hierarchy of scopes. Walk each level and commit it from
                    // top to bottom

                    //ctx.commitTransaction();
        } finally {
            SailPointFactory.releaseContext(ctx);
        }
    }
    
    private void parseScope (PrintWriter out, SailPointContext ctx, XMLObjectFactory factory,Stack<Scope> scopes, Element element) throws GeneralException {
        Object o = factory.parseElement(ctx, element);
        if (o instanceof Scope) {
            out.println("Found scope " + o);
            Scope scope = (Scope)o;
            Scope foundScope = ctx.getObjectByName(Scope.class, scope.getName());
            if (foundScope != null) {
                // use the found scope, not this one
                foundScope.setChildScopes(scope.getChildScopes());
                Scope parent = scope.getParent();
                if (parent != null) {
                    parent.addScope(foundScope);
                    ctx.saveObject(parent);
                }
                ctx.decache();
                ctx.saveObject(foundScope);
            } else {
                ctx.saveObject(scope);
            }
            ctx.commitTransaction();

        } else {
            out.println("Ignoring non-scope " + o);
        }
        
    }
    
    

    public void cmdGenerateCsvObjects(List<String> args, PrintWriter out) throws Exception {
        if (args == null || args.size() == 0) {
            out.println("A CSV file is required: generateCsvObjects [fileName]");
            return;
        }
        File f = new File(args.get(0));
        CSVSource csv = new CSVSourceImpl(f, CSVType.WithHeader);
        SailPointContext ctx = null;
        try {
            ctx = SailPointFactory.createContext("Console");
            Iterator<Map<String, String>> it = csv.iterator();
            while (it.hasNext()) {
                Map<String, String> line = it.next();
                // the only required value is 'class'
                String className = line.get("class");
                try {
                    if (className != null && !"".equals(className.trim())) {
                        Class clazz = Class.forName(className);
                        Object o = clazz.newInstance();
                        SailPointObject so = null;
                        if (o instanceof SailPointObject) {
                            so = (SailPointObject)o;
                        } else {
                            throw new GeneralException(className + " is not of type SailPointObject");
                        }
                        for (String key : line.keySet()) {
                            if (line.get(key) == null) {
                                // if the value is null, don't bother
                                continue;
                            }
                            if ("class".equalsIgnoreCase(key)) {
                                // skip the class key
                                continue;
                            }
                            String strValue = line.get(key);
                            Class propType = PropertyUtils.getPropertyType(so, key);
                            if (propType.isAssignableFrom(String.class)) {
                                // nothing needs to be twerked, just do eet
                                PropertyUtils.setProperty(so, key, strValue);
                            } else if (propType.isAssignableFrom(Number.class)) {
                                // Stupid nombre
                                if (strValue.contains(".")) {
                                    // Double my pleasure
                                    Double dblValue = Double.valueOf(strValue);
                                    PropertyUtils.setProperty(so, key, dblValue);
                                } else {
                                    // no period, no care
                                    Integer intValue = Integer.valueOf(strValue);
                                    PropertyUtils.setProperty(so, key, intValue);
                                }
                            } else if (propType.isAssignableFrom(Identity.class)) {
                                Identity idValue = ctx.getObject(Identity.class, strValue);
                                if (idValue != null) {
                                    PropertyUtils.setProperty(so, key, idValue);
                                }
                            } else if (propType.isAssignableFrom(Date.class)) {
                                Date d = new Date(Long.valueOf(strValue));
                                PropertyUtils.setProperty(so, key, d);
                            } else {
                                // well fuck
                                this._out.println("Skipping property for bean " + so + ": " + key + ":" + strValue);
                                continue;
                            }
                        }
                        ctx.saveObject(so);
                        ctx.commitTransaction();
                        ctx.decache();
                    } else {
                        throw new GeneralException("Class may not be blank: " + line.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }

            }
        } finally {
            if (ctx != null)
                SailPointFactory.releaseContext(ctx);
        }
    }

    public void cmdGenerateCsvMA(List<String> args, PrintWriter out) throws Exception {
        // three arguments:
        // 1: csv file of entitlement values
        // 2: entitlement application
        // 3: entitlement attribute
        if (Util.size(args) < 3) {
            println ("Missing arguments: fileName application attribute");
            return;
        }
        
        String fileName = args.get(0);
        String applicationName = args.get(1);
        String attributeName = args.get(2);
        
        File file = new File(fileName);
        if (!file.exists()) {
            println (fileName + " was not found.");
            return;
        }
        
        SailPointContext context = SailPointFactory.createContext();
        try {
        
        Application application = context.getObjectByName(Application.class, applicationName);
        if (application == null) {
            println (applicationName + " (Application) was not found.");
            return;
        }
        application.load();
        
        CSVSource csv = new CSVSourceImpl(file, CSVType.WithOutHeader);
        String entitlement = null;
        int i = 0;
        do {
            String[] lineArry = csv.getNextLine();
            entitlement = lineArry != null && lineArry.length > 0 ? lineArry[0] : null;
            if (entitlement != null) {
                ManagedAttribute ma = ManagedAttributer.get(context, application, attributeName, entitlement);
                if (ma == null) {
                    ma = new ManagedAttribute(application, attributeName, entitlement);
                    context.saveObject(ma);
                    context.commitTransaction();
                    i++;
                }
                context.decache();
            }
        } while (entitlement != null);
        println ("Generated " + i + " ManagedAttributes");
        } catch (Throwable t) {
            t.printStackTrace(out);
        } finally {
            if (context != null) {
                SailPointFactory.releaseContext(context);
            }
        }
    }
    
    private String truncateStringWithsuffix (String toTruncate, String suffix, int limit) {
        if (toTruncate == null && suffix == null) return null;
        if (suffix == null) suffix = "";
        String retString = toTruncate + suffix;
        int testLen = suffix == null ? limit : limit - suffix.length();
        if (retString.length() > testLen) {
            retString = retString.substring(0, testLen) + suffix;
        }
        return retString;

    }
    
    private File getFile(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            println(fileName + " does not exist");
            file = null;
        }
        return file;
    }
    
    public void cmdUpdateCSVRole(List<String> args, PrintWriter out) throws Exception {
        if (Util.size(args) != 1) {
            println("Options: csvFile");
            return;
        }
        
        File file = getFile(args.get(0));
        SailPointContext ctx = SailPointFactory.createContext();
        try {
            int threadNum = 4;
            Deque<Map<String, Object>> work = new LinkedBlockingDeque<Map<String,Object>>();
            List<GenRoleThread> threads = new ArrayList<GenRoleThread>();
            for (int i = 0; i < threadNum; i++) {
                GenRoleThread t = new GenRoleThread(work, out);
                threads.add(t);
                t.start();
            }
            CSVSource csv = new CSVSourceImpl(file, CSVType.WithHeader);
            String roleNameKey = "business"; // cmd line needs to provide this
            String upAsObjKey = GenRoleThread.KEY_UPDATE_AS_OJB;
            for (Map<String, String> line : csv) {
                String roleName = line.get(roleNameKey);
                String upAsObj = line.get(upAsObjKey);
                String action = line.get(GenRoleThread.KEY_ACTION);
                Map<String, Object> role = new HashMap<String, Object>();
                role.put(GenRoleThread.KEY_ROLE_NAME, roleName);
                role.put(GenRoleThread.KEY_UPDATE_AS_OJB, upAsObj);
                role.put(GenRoleThread.KEY_ACTION, action);
                work.add(role);
            }
            
            while (!work.isEmpty()) {
                Thread.sleep(500);
            }
            for (GenRoleThread t : threads) {
                t._halted = true;
            }
        } finally {
            SailPointFactory.releaseContext(ctx);
        }
    }

    public void cmdGenerateBizRoleModel (List<String> args, PrintWriter out) throws Exception {
        if (Util.size(args) < 1) {
            println("Options: file");
            return;
        }
        
        int ROLE_NAME_LIMIT = 127;
        
        File file = getFile(args.get(0));
        
        SailPointContext context = SailPointFactory.createContext();
        String roleName = null;

        int threadNum = 4;
        Deque<Map<String, Object>> workLoad = new LinkedBlockingDeque<Map<String,Object>>();
        List<GenRoleThread> threads = new ArrayList<GenRoleThread>();
        for (int i = 0; i < threadNum; i++) {
            GenRoleThread t = new GenRoleThread(workLoad, out);
            threads.add(t);
            t.start();
        }
        try {
            CSVSource csv = new CSVSourceImpl(file, CSVType.WithHeader);
            String key = "business";
            for (Map<String, String> line : csv) {
                roleName = line.get(key);
                roleName = truncateStringWithsuffix(roleName, "", ROLE_NAME_LIMIT);
                if (!Util.isNullOrEmpty(roleName)) {
                    Map<String, Object> role = new HashMap<String, Object>();
                    role.put(GenRoleThread.KEY_ROLE_NAME, roleName);
                    role.put(GenRoleThread.KEY_ROLE_TYPE, "business");
                    workLoad.add(role);
                }
            }
            
            while (!workLoad.isEmpty()) {
                Thread.sleep(500);
            }
            
            for (GenRoleThread t : threads) {
                t._halted = true;
            }
            
        } catch (Throwable t) {
            t.printStackTrace(out);
        } finally {
            if (context != null) SailPointFactory.releaseContext(context);
        }
    
        
    }
    
    public void cmdGenerateITRollModel (List<String> args, PrintWriter out) throws Exception {
        // 1. Filename of role model CSV
        // 2. .... that's it. We're inferring the rest
        if (Util.size(args) < 1) {
            println("Options: file");
            return;
        }
        
        int ROLE_NAME_LIMIT = 127;
        String IT_ROLE_NAME_SUFFIX = " - IT";
        int COMMIT_LIMIT = 20;
        
        String fileName = args.get(0);
        File file = new File(fileName);
        if (!file.exists()) {
            println(fileName + " does not exist");
            return;
        }
        
        Deque<Map<String, Object>> workLoad = new LinkedBlockingDeque<Map<String, Object>>();
        int threadNum = 4;
        List<GenRoleThread> threads = new ArrayList<GenRoleThread>();
        for (int i = 0; i < threadNum; i++) {
            GenRoleThread t = new GenRoleThread(workLoad, out);
            threads.add(t);
            // there's no work, so initially the threads will just hang out
            t.start();
        }
        
        SailPointContext context = SailPointFactory.createContext();
        String roleName = null;
        String currentRoleName = null;
        try {
            Application directory = context.getObjectByName(Application.class, "Directory");
            directory.load();
            Application lab1 = context.getObjectByName(Application.class, "Lab 1");
            lab1.load();
            Application lab2 = context.getObjectByName(Application.class, "Lab 2");
            lab2.load();
            if (directory == null || lab1 == null || lab2 == null) {
                println("Some application was not found: " + directory + lab1 + lab2);
                return;
            }
            
            CSVSource csv = new CSVSourceImpl(file, CSVType.WithHeader);
            Map<String, Object> role = null;
            int i = 0;
            List<Profile> profiles = null;
            for (Map<String, String> line : csv) {
                String directoryEnt = line.get("directoryEnt");
                String lab1Ent = line.get("lab1");
                String lab2Ent = line.get("lab2");
                if (log.isTraceEnabled()) {
                    log.trace("directoryEnt: " + directoryEnt);
                    log.trace("lab1: " + lab1);
                    log.trace("lab2: " + lab2);
                }
                roleName = truncateStringWithsuffix(directoryEnt, IT_ROLE_NAME_SUFFIX, ROLE_NAME_LIMIT);
                if (!roleName.equals(currentRoleName)) {
                    // new role; save off the old
                        log.trace("New role: " + roleName);
                    if (role != null) {
                        workLoad.add(role);
                    }
                    i++;
                    currentRoleName = roleName;
                    // No existing role, let's make one!
                    role = new HashMap<String, Object>();
                    role.put(GenRoleThread.KEY_ROLE_TYPE, "it");
                    role.put(GenRoleThread.KEY_ROLE_NAME, currentRoleName);
                    profiles = new ArrayList<Profile>();
                    role.put(GenRoleThread.KEY_ROLE_PROFILES, profiles);
                    // we only need to do this once, so do it here
                    Profile directoryProfile = createProfile(directory, "group", directoryEnt);
                    profiles.add(directoryProfile);
                }

                if (!Util.isNothing(lab1Ent)) {
                    Profile lab1Profile = createProfile(lab1, "group", lab1Ent);
                    if (log.isTraceEnabled()) log.trace("Adding Lab1 profile: " + lab1Profile);
                    profiles.add(lab1Profile);
                }

                if (!Util.isNothing(lab2Ent)) {
                    Profile lab2Profile = createProfile(lab2, "group", lab2Ent);
                    if (log.isTraceEnabled()) log.trace("Adding Lab2 profile: " + lab2Profile);
                    profiles.add(lab2Profile);
                }
            }
            // don't forget the last role
            if (role != null) workLoad.add(role);
            
            while (!workLoad.isEmpty()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {}
            }
            
            for (GenRoleThread t : threads) {
                t._halted = true;
            }

        } catch (Throwable t) {
            if (roleName != null) {
                System.out.println("Failed role: " + currentRoleName);
            }
            t.printStackTrace(out);
        } finally {
            if (context != null) {
                SailPointFactory.releaseContext(context);
            }
        }
    }
    
    public void cmdDecideAll (List<String> args, PrintWriter out) throws Exception {
        /*
         * Given a Certification ID and a decision, decide all items in the cert per
         * entity, using ThreadRunner
         */
        
        if (Util.size(args) < 2) {
            out.println ("decideAll certID Decision [threads]");
            return;
        }
        
        String CERT_ID = args.get(0);
        String DECISION_VALUE = args.get(1);
        
        int capacity = 4;
        if (Util.size(args) > 2) {
            capacity = Integer.valueOf(args.get(3));
        }
        
        ThreadRunner tr = new ThreadRunner(capacity);
    }
    
    public void cmdOgranizeCSVRoles (List<String> args, PrintWriter out) throws Exception {
        // read in the CSV file
        // Fetch the parent and child
        // If both are IT roles, Child inherits Parent
        // If both are Biz, Parent allows Child
        // If parent is Biz and child is IT, Parent requires child
        
        // let the user tell us the "mode" ala: biz2biz, biz2it, it2it
        // 
        
        int ROLE_NAME_LIMIT = 127;
        String parentSuffix = "";
        String childSuffix = "";

        // args: csvFile mode parentCol childCol
        if (Util.size(args) < 4) {
            out.println("csvFile " + Arrays.toString(RoleOrganizer.MODES) + " parentCol childCol [threads] [parentSuffix] [childSuffix]");
            return;
        }
        
        SailPointContext context = null;
        try {
            String fileName = args.get(0);
            File file = new File(fileName);
            if (!file.exists()) {
                out.println(fileName + " not found!");
                return;
            }

            String mode = args.get(1);
            if (Arrays.binarySearch(RoleOrganizer.MODES, mode) < 0) {
                println (mode + " is not a legal mode: " + Arrays.toString(RoleOrganizer.MODES));
                return;
            }
            String parentKey = args.get(2);
            String childKey = args.get(3);
            if (log.isTraceEnabled()) {
                log.trace("parentKey: " + parentKey);
                log.trace("childKey: " + childKey);
                log.trace("mode: " + mode);
                log.trace("MODES: " + RoleOrganizer.MODES);
            }
            int threadNum = 1;
            if (args.size() > 4) {
                threadNum = Integer.valueOf(args.get(4));
            }
            
            if (args.size() > 5) {
                parentSuffix = args.get(5);
            }
            
            if (args.size() > 6) {
                childSuffix = args.get(6);
            }
            
            Deque<Map<String, Object>> workLoad = new LinkedBlockingDeque<Map<String,Object>>();
            List<RoleOrganizer> threads = new ArrayList<RoleOrganizer>();
            for (int i = 0; i < threadNum; i++) {
                RoleOrganizer t = new RoleOrganizer(workLoad, out);
                threads.add(t);
                t.start();
            }

            // got what we need, get'er done
            CSVSource csv = new CSVSourceImpl(file, CSVType.WithHeader);
            context = SailPointFactory.createContext();
            
            log.trace("parentSuffix: " + parentSuffix);
            log.trace("childSuffix: " + childSuffix);

            String currentRoleName = null;
            List<String> childRoles = new ArrayList<String>();
            int i = 0;
            Map<String, Object> payload = new HashMap<String, Object>();
            for (Map<String, String> line : csv) {
                i++;
                String parentName = line.get(parentKey);
                parentName = truncateStringWithsuffix(parentName, parentSuffix, ROLE_NAME_LIMIT);
                if (Util.isNullOrEmpty(parentName)) {
                    out.println("Null value foune for " + parentKey + " at line " + i);
                    continue;
                }
                if (currentRoleName != null && !currentRoleName.equals(parentName)) {
                    // build the payload
                    log.debug("New role, process current: " + currentRoleName);
                    workLoad.add(payload);
                    childRoles = new ArrayList<String>();
                    payload = new HashMap<String, Object>();
                }
                currentRoleName = parentName;
                // same role, just tack on the child list
                String childName = line.get(childKey);
                childName = truncateStringWithsuffix(childName, childSuffix, ROLE_NAME_LIMIT);
                log.trace("parentName: " + parentName);
                log.trace("childName: " + childName);
                // if either null, complain but continue
                if (Util.isNullOrEmpty(childName)) {
                    out.println("Null value found for " + childKey + " at line " + i);
                    continue;
                }
                childRoles.add(childName);
                payload.put(RoleOrganizer.PARENT_ROLE_KEY, parentName);
                payload.put(RoleOrganizer.CHILD_ROLE_KEY, childRoles);
                payload.put(RoleOrganizer.MODE_KEY, mode);
            }
            workLoad.add(payload);
            
            while (!workLoad.isEmpty()) {
                Thread.sleep(500);
            }
            
            for (WorkloadThread t : threads) {
                t._halted = true;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            if (context != null) SailPointFactory.releaseContext(context);
        }

    }
    
    private Application createOrConfirmTestApplication(SailPointContext ctx, String appName) throws GeneralException {
        Application testApp = ctx.getObjectByName(Application.class, appName);
        if (testApp == null) {
            testApp = new Application();
            testApp.setName(appName);
            Schema schema = new Schema();
            schema.setIdentityAttribute("acctId");
            schema.setNativeObjectType("account");
            schema.setObjectType("account");
            AttributeDefinition acctIdDef = new AttributeDefinition("acctId", "string");
            schema.addAttributeDefinition(acctIdDef);
            AttributeDefinition groupDef = new AttributeDefinition("group", "string");
            groupDef.setEntitlement(true);
            groupDef.setMulti(true);
            schema.addAttributeDefinition(groupDef);
            testApp.addSchema(schema);
            ctx.saveObject(testApp);
            ctx.commitTransaction();
        }
        return testApp;
    }
    
    private void createOrConfirmTestRoles(SailPointContext ctx, String roleScript) throws IOException, GeneralException {
        CSVSource csv = new CSVSourceImpl(new File(roleScript), CSVType.WithHeader);
        String roleName = "";
        Iterator<Map<String, String>> csvIter = csv.iterator();
        List<Profile> profiles = new ArrayList<Profile>();
        while (csvIter.hasNext()) {
            Map<String, String> line = csvIter.next();
            String nextRoleName = line.get("role");
            if (roleName.equals("")) {
                roleName = nextRoleName;
            }
            if (!nextRoleName.equals(roleName)) {
                // build the role with what we got
                // build a biz role, and a required IT role (for a better model)
                // 1:1 is fine
                // script will have to define identity list for selector

                
                // init for new role
            }
            Profile p = new Profile();
            String appName = line.get("application");
            Application app = ctx.getObjectByName(Application.class, appName);
            p.setApplication(app);
            String attrName = line.get("attribute");
            String attrValue = line.get("value");
            List<Filter> constraints = new ArrayList<Filter>();
            List<String> values = new ArrayList<String>();
            values.add(attrValue);
            constraints.add(Filter.containsAll(attrName, values));
            p.setConstraints(constraints);
            profiles.add(p);
        }
    }
    
    /*
    private Bundle buildBundle(String baseRoleName, List<Profile> profiles) {
        Bundle biz = new Bundle();
        String bizName = baseRoleName + "-Biz";
        String itName = baseRoleName + "-IT";
        biz.setName(bizName);
        biz.setType("business");
    }
    */
    
    public void cmdScaleTestPlanCompile (List<String> args, PrintWriter out) throws Exception {
        // Gen 100 test Identities
        // baseAccount application should be pre-loaded
        // If I'm preloading the application, then just pre-load the Identities; Skip this step
        SailPointContext context = null;
        try {
            context = SailPointFactory.createContext();
        // Gen 10 test applications
            String baseAppName = "CompileTestApp-";
            for (int i = 0; i < 10; i++) {
                String appName = baseAppName + i;
                createOrConfirmTestApplication(context, appName);
            }
        
        // Gen 10 roles (based on script)
            // script will be csv:
            // role name,application name,attribute name,attribute value
            // role name,application name,attribute name,attribute value
            // role name,application name,attribute name,attribute value
            // Multi-profiles are spanned across line
            String roleScript = args.get(0);
            createOrConfirmTestRoles(context, roleScript);
            
            
        
        // 

        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            if (context != null) SailPointFactory.releaseContext(context);
        }
    }

    private Profile createProfile(Application application, String attribute, String value) {
        Profile p = new Profile();
        p.setApplication(application);
        List<Filter> constraints = new ArrayList<Filter>();
        constraints.add(Filter.contains(attribute, value));
        p.setConstraints(constraints);
        return p;
    }
}
