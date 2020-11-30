package sailpoint.rule;

import sailpoint.api.ObjectUtil;
import sailpoint.api.PersistenceManager;
import sailpoint.api.PersistenceManager.LockParameters;
import sailpoint.object.Application;

public class Rule_SET3580POC extends GenericRule {

    @Override
    public Object execute() throws Throwable {
        String APP_NAME = "Active_Directory";

        // Get the ID/object of Active_Directory
        Application application = context.getObjectByName(Application.class, APP_NAME);
        log.warn("Application after init: " + application);

        // Lock the object
        Application lockedApp = ObjectUtil.lockObject(context, Application.class, LockParameters.createById(application.getId(), PersistenceManager.LOCK_TYPE_PERSISTENT));
        // print java object 
        log.warn("Locked Application: " + lockedApp);

        // get by name
        Application namedApp = context.getObjectByName(Application.class, APP_NAME);
        // print java object
        log.warn("Application by Name: " + namedApp);

        // get by id
        Application idApp = context.getObjectById(Application.class, application.getId());
        // print java object
        log.warn("Application by Id: " + idApp);

        // get by name
        Application namedApp2 = context.getObjectByName(Application.class, APP_NAME);
        // print java object
        log.warn("Application by Name: " + namedApp2);
        
        //serialize each
        log.warn("Original: " + application.toXml());
        log.warn("Locked: " + lockedApp.toXml());
        log.warn("Named: " + namedApp.toXml());
        log.warn("ID: " + idApp.toXml());
        log.warn("Named2: " + namedApp2.toXml());
        return null;
    }

}
