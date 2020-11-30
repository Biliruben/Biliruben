package sailpoint.rule;


import java.util.ArrayList;
import java.util.List;

import sailpoint.object.AccountGroup;
import sailpoint.object.EmailOptions;
import sailpoint.object.EmailTemplate;
import sailpoint.object.Identity;

public class Rule_SampleValueChange extends ValueChangeRule {

	
	/*
	 *
   <Signature returnType="void">
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

	 */

	
	@Override
	public Object execute() throws Throwable {
		
		// Sample value change rule to use for Department
		
		// Sample Task: When department changes, notify old and new managers.  This example assumes
		// the departments are modeled as AccountGroups and managers are the owners
		
		String oldDepartmentName = (String)oldValue;
		AccountGroup oldDepartment = context.getObject(AccountGroup.class, oldDepartmentName);
		// old mgr
		Identity oldMgr = oldDepartment.getOwner();
		
		String newDepartmentName = (String)newValue;
		AccountGroup newDepartment = context.getObject(AccountGroup.class, newDepartmentName);
		// new mgr
		Identity newMgr = newDepartment.getOwner();
		
		// notify
		EmailTemplate template = context.getObject(EmailTemplate.class, "Department Change Notification"); // custom template required
		EmailOptions options = new EmailOptions();
		options.setSendImmediate(true);
		List toList = new ArrayList();
		toList.add(newMgr.getEmail());
		toList.add(oldMgr.getEmail());
		options.setTo(toList);
		options.setVariable("oldDepartment", oldDepartmentName); // These variables would be used in the template,
		options.setVariable("newDepartment", newDepartmentName); // ala, $oldDepartment and $newDepartment

		// sends notification
		context.sendEmailNotification(template, options);
		
		// Value Change rules return nothing
		return null;
	}

}
