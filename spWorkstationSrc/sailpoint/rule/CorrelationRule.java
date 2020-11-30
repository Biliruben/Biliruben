package sailpoint.rule;

import java.util.Map;

import sailpoint.object.Application;
import sailpoint.object.Attributes;
import sailpoint.object.Link;
import sailpoint.object.ResourceObject;

public abstract class CorrelationRule extends GenericRule {

	public static final String ARG_LINK = "link";
	public static final String ARG_ACCOUNT = "account";
	public static final String ARG_APPLICATION = "application";
	public static final String ARG_ENVIRONMENT = "environment";
	protected Map environment;
	protected Application application;
	protected ResourceObject account;
	protected Link link;

	/*
	 *     <Returns>
      <Argument name='identityName'>
        <Description>
          The name of an Identity object.
        </Description>
      </Argument>
      <Argument name='identity'>
        <Description>
          A fully resolved Identity object if the rule wants
          to do its own queries.
        </Description>
      </Argument>
      <Argument name='identityAttributeName'>
        <Description>
          The name of the extended attribute that can be used 
          to locate an existing identity.
        </Description>
      </Argument>
      <Argument name='identityAttributeValue'>
        <Description>
          The value of the named extended attribute that can be used 
          to locate an existing identity. This attribute is used 
          together with the identityAttributeName argument.
        </Description>
      </Argument>
    </Returns>
	 */

	/*
	 *       
	  <Argument name='context'>
          A sailpoint.api.SailPointContext object that can be used to 
      <Argument name='environment' type='Map'>
      <Argument name='application'>
        <Description>
          Application being aggregated.
        </Description>
      </Argument>
      <Argument name='account'>
          A sailpoint.object.ResourceObject returned from the
      <Argument name='link'>
          Existing link to this account.
  	  </Signature>
	 */
	
	@Override
	public void preExecute(Attributes<String, Object> attrs) {
		super.preExecute(attrs);
		environment = (Map)attrs.get(ARG_ENVIRONMENT);
		application = (Application)attrs.get(ARG_APPLICATION);
		account = (ResourceObject)attrs.get(ARG_ACCOUNT);
		link = (Link)attrs.get(ARG_LINK);
	}
}
