package sailpoint.services.integration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.api.SailPointContext;
import sailpoint.api.SailPointFactory;
import sailpoint.integration.IntegrationInterface;
import sailpoint.integration.ProvisioningPlan;
import sailpoint.integration.RequestResult;
import sailpoint.integration.RoleDefinition;
import sailpoint.object.Rule;
import sailpoint.tools.GeneralException;

/**
 * Neil McGlennon
 * neil.mcglennon@sailpoint.com
 * 
 * This is a Rule based Integration
 * Each method calls out to a Rule, 
 * which should be useful for quick integrations.
 */

public class RuleIntegration implements IntegrationInterface {
	
	private static final Log log = LogFactory.getLog( RuleIntegration.class );

	private Map<String,Object> config;
		
	private static final String ARGS_PING = "ping";
	private static final String ARGS_PROVISION = "provision";
	private static final String ARGS_REQUEST_STATUS = "requestStatus";
	private static final String ARGS_LIST_ROLES = "listRoles";
	private static final String ARGS_ADD_ROLE = "addRole";
	private static final String ARGS_DELETE_ROLE = "deleteRole";
	
	public void configure( Map configMap ) 
		throws Exception {
        
		if ( configMap == null )
            throw new Exception( "Missing arguments" );
        else
        	config = configMap;
		
	}

	public String ping() throws Exception {
		
		Map<String,Object> inputMap = new HashMap<String,Object>();
		
		Map<String,Object> rulesMap = (Map) config.get( "rules" );
		String ruleName = (String) rulesMap.get( ARGS_PING );
        Map<String,Object> configMap = (Map) config.get( ARGS_PING );
		
        try {
			
			inputMap.put( "context", this );
			inputMap.put( "log", log );
	        inputMap.put( "config", configMap );
	        
	        Object o = runRule( ruleName , inputMap );

	        if ( null == o )
	        	throw new GeneralException( "Rule [" + ruleName + "] did not return any status." );
	        else if (o instanceof String ) 
	        	return (String) o;
	        else
	        	throw new GeneralException( "Rule [" + ruleName + "] did not return a String." );
	        
		} catch ( GeneralException e ) {			
			
			log.debug( "Exception: " + e ); 	
			throw new Exception ( e );
		}
	}

	public RequestResult provision( 
			String identity, 
			ProvisioningPlan plan )
		throws Exception {
		
		Map<String,Object> inputMap = new HashMap<String,Object>();
		Map<String,Object> outputMap = new HashMap<String,Object>();
		
		Map<String,String> rulesMap = (Map) config.get( "rules" );
		String ruleName = (String) rulesMap.get( ARGS_PROVISION );
        Map<String,Object> configMap = (Map) config.get( ARGS_PROVISION );
		
        try {
			
			inputMap.put( "context", this );
			inputMap.put( "log", log );
	        inputMap.put( "config", configMap );
			inputMap.put( "identityName", identity );
			inputMap.put( "provisioningPlan", plan );
	        
	        Object o = runRule( ruleName , inputMap );

	        outputMap.put( "status", sailpoint.integration.RequestResult.STATUS_SUCCESS );
	        
	        return new RequestResult( outputMap );
	        
		} catch ( GeneralException e ) {			
			
			log.debug( "Exception: " + e );
			outputMap.put( "status", sailpoint.integration.RequestResult.STATUS_FAILURE );
			outputMap.put( "errors", e );
			
			return new RequestResult( outputMap );
		}
	} 
	
	public List listRoles() 
		throws Exception {
		
		Map<String, Object> inputMap = new HashMap<String,Object>();
		
		Map<String, String> rulesMap = (Map) config.get( "rules" );
		String ruleName = (String) rulesMap.get( ARGS_LIST_ROLES );
        Map<String, Object> configMap = (Map) config.get( ARGS_LIST_ROLES );
		
        try {
			
			inputMap.put( "context", this );
			inputMap.put( "log", log );
	        inputMap.put( "config", configMap );
	        
	        Object o = runRule( ruleName , inputMap );
	        
	        if ( null == o )
	        	return null;
	        else if (o instanceof List ) 
	        	return (List) o;
	        else
	        	throw new GeneralException( "Rule [" + ruleName + "] did not return a List." );
	        
		} catch ( GeneralException e ) {			
			
			log.debug( "Exception: " + e );		
			return null;
		}
	}

	public RequestResult addRole( RoleDefinition roleDef ) 
		throws Exception {
		
		Map<String,Object> inputMap = new HashMap<String,Object>();
		Map<String,Object> outputMap = new HashMap<String,Object>();
		
		Map<String,Object> rulesMap = (Map) config.get( "rules" );
		String ruleName = (String) rulesMap.get( ARGS_ADD_ROLE );
        Map<String,Object> configMap = (Map) config.get( ARGS_ADD_ROLE );
		
        try {
			
			inputMap.put( "context", this );
			inputMap.put( "log", log );
	        inputMap.put( "config", configMap );
			inputMap.put( "roleDefinition", roleDef );
	        
	        Object o = runRule( ruleName , inputMap );

	        outputMap.put( "status", sailpoint.integration.RequestResult.STATUS_SUCCESS );
	        
	        return new RequestResult( outputMap );
	        
		} catch ( GeneralException e ) {			
			
			log.debug( "Exception: " + e );
			outputMap.put( "status", sailpoint.integration.RequestResult.STATUS_FAILURE );
			outputMap.put( "errors", e );
			
			return new RequestResult( outputMap );
		}
	}

	public RequestResult deleteRole( String roleName ) 
		throws Exception {
		
		Map<String,Object> inputMap = new HashMap<String,Object>();
		Map<String,Object> outputMap = new HashMap<String,Object>();
		
		Map<String,Object> rulesMap = (Map) config.get( "rules" );
		String ruleName = (String) rulesMap.get( ARGS_DELETE_ROLE );
        Map<String,Object> configMap = (Map) config.get( ARGS_DELETE_ROLE );
		
        try {
			
			inputMap.put( "context", this );
			inputMap.put( "log", log );
	        inputMap.put( "config", configMap );
			inputMap.put( "roleName", roleName );
	        
	        Object o = runRule( ruleName , inputMap );

	        outputMap.put( "status", sailpoint.integration.RequestResult.STATUS_SUCCESS );
	        
	        return new RequestResult( outputMap );
	        
		} catch ( GeneralException e ) {			
			
			log.debug( "Exception: " + e );
			outputMap.put( "status", sailpoint.integration.RequestResult.STATUS_FAILURE );
			outputMap.put( "errors", e );
			
			return new RequestResult ( outputMap );
		}		
	}
	
	public RequestResult getRequestStatus(String reference) 
		throws Exception {
		
		Map<String,Object> inputMap = new HashMap<String,Object>();
		Map<String,Object> outputMap = new HashMap<String,Object>();
		
		Map<String,Object> rulesMap = (Map) config.get( "rules" );
		String ruleName = (String) rulesMap.get( ARGS_REQUEST_STATUS );
        Map<String,Object> configMap = (Map) config.get( ARGS_REQUEST_STATUS );
		
        try {
			
			inputMap.put( "context", this );
			inputMap.put( "log", log );
	        inputMap.put( "config", configMap );
			inputMap.put( "reference", reference );
	        
	        Object o = runRule( ruleName , inputMap );
	        
	        outputMap.put( "status", o );
	        outputMap.put( "requestID", reference );
			
	        return new RequestResult( outputMap );
	        
		} catch ( GeneralException e ) {			
			
			log.debug( "Exception: " + e );
			outputMap.put( "status", sailpoint.integration.RequestResult.STATUS_FAILURE );
			outputMap.put( "errors", e );
			
			return new RequestResult( outputMap );
		}
	}
	
	private Object runRule( String ruleName, Map <String, Object> ruleContext ) 
		throws Exception {
		
		System.out.println("Entering runRule.");
	
		Object o = null;
	
		log.debug( "ruleName[" + ruleName + "]" );
		log.debug( "ruleContext[" + ruleContext + "]" );
	
		try {
		
			//SailPointContext context = SailPointFactory.getCurrentContext();
			SailPointContext context = SailPointFactory.createContext();
		
			if ( null == ruleContext.get( "context" ) )
				ruleContext.put( "context", context );
			if ( null == ruleContext.get( "log" ) )
				ruleContext.put( "log", log );
        
			Rule rule = context.getObject( Rule.class, ruleName );
		
			o = context.runRule( rule, ruleContext );

		} catch( Exception e ) {
			
			log.debug( "Exception: " + e );
			throw new GeneralException(e);
		
		}
	
		return o;
	}

}
