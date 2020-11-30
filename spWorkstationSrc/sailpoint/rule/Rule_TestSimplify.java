package sailpoint.rule;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import sailpoint.object.Attributes;
import sailpoint.object.Identity;
import sailpoint.object.ProvisioningPlan;
import sailpoint.object.ProvisioningProject;
import sailpoint.provisioning.PlanCompiler;
import sailpoint.tools.xml.XMLObjectFactory;

public class Rule_TestSimplify extends GenericRule {

    String planXml = "<!DOCTYPE ProvisioningProject PUBLIC \"sailpoint.dtd\" \"sailpoint.dtd\">" +
"    <ProvisioningProject identity=\"Aaron.Nichols\">" +
"      <Attributes>" +
"        <Map>" +
"          <entry key=\"noAttributeSyncExpansion\">" +
"            <value>" +
"              <Boolean>true</Boolean>" +
"            </value>" +
"          </entry>" +
"          <entry key=\"noLocking\">" +
"            <value>" +
"              <Boolean>true</Boolean>" +
"            </value>" +
"          </entry>" +
"          <entry key=\"source\" value=\"Rule\"/>" +
"        </Map>" +
"      </Attributes>" +
"      <MasterPlan>" +
"        <ProvisioningPlan>" +
"          <AccountRequest application=\"IIQ\" op=\"Modify\">" +
"            <AttributeRequest assignmentId=\"f052d138ec864427884faff58618e363\"" +
"    name=\"assignedRoles\" op=\"Add\" value=\"Auto-Assignment Business\"/>" +
"            <AttributeRequest assignmentId=\"f052d138ec864427884faff58618e363\"" +
"    name=\"assignedRoles\" op=\"Add\" value=\"Auto-Assignment Business\"/>" +
"          </AccountRequest>" +
"        </ProvisioningPlan>" +
"      </MasterPlan>" +
"      <ProvisioningPlan targetIntegration=\"IIQ\">" +
"        <AccountRequest application=\"IIQ\">" +
"          <AttributeRequest assignmentId=\"f052d138ec864427884faff58618e363\"" +
"    name=\"assignedRoles\" op=\"Add\" value=\"Auto-Assignment Business\"/>" +
"          <AttributeRequest assignmentId=\"f052d138ec864427884faff58618e363\"" +
"    name=\"assignedRoles\" op=\"Add\" value=\"Auto-Assignment Business\"/>" +
"        </AccountRequest>" +
"      </ProvisioningPlan>" +
"      <ProvisioningTarget assignmentId=\"f052d138ec864427884faff58618e363\"" +
"    role=\"Auto-Assignment Business\">" +
"        <AccountSelection applicationId=\"402875554b7cde03014b7de0d7890063\"" +
"    applicationName=\"HR_Employees\" selection=\"1c\">" +
"          <AccountInfo displayName=\"Aaron.Nichols\" nativeIdentity=\"1c\"/>" +
"        </AccountSelection>" +
"      </ProvisioningTarget>" +
"    </ProvisioningProject>";
    
    String getXml() throws Throwable {
        File f = new File("C:\\etn_data\\24418 - complex plan after simplify is still complex\\modTestPLan.xml");
        BufferedReader r = new BufferedReader(new FileReader(f));
        StringBuilder buff = new StringBuilder();
        String line = "";
        while (line != null && r.ready()) {
            line = r.readLine();
            buff.append(line);
        }
        r.close();
        return buff.toString();
    }
    
    @Override
    public Object execute() throws Throwable {
        XMLObjectFactory f = XMLObjectFactory.getInstance();
        String xml = getXml();
        ProvisioningProject project = (ProvisioningProject) f.parseXml(context, xml, false);
        ProvisioningPlan masterPlan = project.getMasterPlan();
        Identity aaron = context.getObjectByName(Identity.class, "Aaron.Nichols");
        System.out.println("aaron: " + aaron);
        masterPlan.setIdentity(aaron);
        PlanCompiler compiler = new PlanCompiler(context);
        ProvisioningProject compiled = compiler.compile(new Attributes(), masterPlan, new Attributes());

        // now to simplify
        compiler = new PlanCompiler(context, compiled);
        compiler.simplify();
        ProvisioningProject simplified = compiler.getProject();

        return simplified.toXml();
    }

}
