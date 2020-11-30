package sailpoint.services.integration;

import sailpoint.integration.RuleIntegration;
import sailpoint.object.Bundle;
import sailpoint.object.IntegrationConfig;
import sailpoint.object.IntegrationExecutor;

public class RuleIntegrationExecutor extends RuleIntegration implements IntegrationExecutor {

	public RuleIntegrationExecutor() {
		super();
	}

	public void finishRoleDefinition(
			IntegrationConfig config, 
			Bundle src,
			RoleDefinition dest ) 
		throws Exception {
		throw new UnsupportedOperationException("finishRoleDefinition() not implemented");
	}
}
	
