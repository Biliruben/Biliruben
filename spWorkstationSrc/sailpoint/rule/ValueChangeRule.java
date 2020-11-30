package sailpoint.rule;

import sailpoint.object.Attributes;
import sailpoint.object.Identity;
import sailpoint.object.ObjectAttribute;

public abstract class ValueChangeRule extends GenericRule {

	Attributes environment;
	Identity identity;
	ObjectAttribute attributeDefinition;
	String attributeName;
	Object oldValue;
	Object newValue;
	
	/*
	 *   <Signature returnType="void">
    <Inputs>
      <Argument name="log">
        <Description>
          The log object associated with the SailPointContext.
        </Description>
      </Argument>
      <Argument name="context">
        <Description>
          A sailpoint.api.SailPointContext object that can be used to query the database if necessary.
        </Description>
      </Argument>
      <Argument name="environment">
        <Description>
          Optional arguments passed from the task executor, if the rule is running within a task.
        </Description>
      </Argument>
      <Argument name="identity">
        <Description>
          The identity which owns the attribute.
        </Description>
      </Argument>
      <Argument name="attributeDefinition">
        <Description>
          The definition of the ObjectAttribute.
        </Description>
      </Argument>
      <Argument name="attributeName">
        <Description>
          The name of the ObjectAttribute.
        </Description>
      </Argument>
      <Argument name="oldValue">
        <Description>
          The original value of the ObjectAttribute.
        </Description>
      </Argument>
      <Argument name="newValue">
        <Description>
          The new value of the ObjectAttribute.
        </Description>
      </Argument>
    </Inputs>
(non-Javadoc)
	 * @see sailpoint.rule.GenericRule#execute()
	 */
}
