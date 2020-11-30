package scratch;

        import java.util.Map;
        import java.util.HashMap;
        imoprt sailpoint.object.Configuration;
        import sailpoint.rapidsetup.plan.*;

public class ScratchPad {

    public static void main(String[] args) {

        Map additionalArgs = new HashMap();
        additionalArgs.put("requestType", requestType);

        if(Configuration.RAPIDSETUP_CONFIG_LEAVER.equals(requestType)) {    
          
             LeaverAppConfigProvider provider = new CompositeConfigProvider(
                 LeaverConfigBuilder.forEveryone(
                     setDisableAccount("nedraTestAD", LeaverAppConfigProvider.IMMEDIATE)
                 ).build(context)
              );
        } else {

             LeaverAppConfigProvider provider = new CompositeConfigProvider(
                 LeaverConfigBuilder.forEveryone(
                     setDeleteAccount("nedraTestAD", LeaverAppConfigProvider.IMMEDIATE)
                 ).build(context)
              );
        }

        // this returns the individual requests used in the provisioning plans using the passed in configuration objects
        return BasePlanBuilder.leaverPlan(context, identityName, additionalArgs, provider, leaverPlanBuilder.isTerminateIdentity()).
            getAppRequests(context, identityName, appName, mode, nativeId);    
    
    }
        

}