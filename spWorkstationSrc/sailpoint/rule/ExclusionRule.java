package sailpoint.rule;

import java.util.List;
import java.util.Map;

import sailpoint.api.CertificationContext;
import sailpoint.object.AbstractCertifiableEntity;
import sailpoint.object.Attributes;
import sailpoint.object.Certification;
import sailpoint.object.Identity;

/**
<Signature returnType='String'>
  <Returns>
    <Argument name='explanation' type='String'>
      <Description>
        An optional explanation describing why the items were excluded.
      </Description>
    </Argument>
  </Returns>
  */
public abstract class ExclusionRule extends GenericRule {

	protected AbstractCertifiableEntity entity; 
	protected Certification certification;
	protected CertificationContext certContext;
	protected List items;
	protected List itemsToExclude;
	protected Map state;
	protected Identity identity;

	@Override
	public void preExecute(Attributes<String, Object> attrs) {
		super.preExecute(attrs);	
		entity = (AbstractCertifiableEntity)attrs.get("entity");
		certification = (Certification)attrs.get("certification");
		certContext = (CertificationContext)attrs.get("certContext");
		items = (List)attrs.get("items");
		itemsToExclude = (List)attrs.get("itemsToExclude");
	}

}
