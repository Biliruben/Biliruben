package sailpoint.rule;
import java.util.List;
import sailpoint.api.PolicyUtil;
import sailpoint.api.PolicyUtil.EntitlementSummary;
import sailpoint.api.PolicyUtil.RoleSummary;
import sailpoint.api.PolicyUtil.ApplicationSummary;
import sailpoint.api.PolicyUtil.ItemSummary;
import sailpoint.object.*;

public class Rule_RenderSOD extends GenericRule {
    
    // Begin: Does not convey
    private Identity identity;
    private PolicyViolation violation;
    private SailPointObject policy;
    private SailPointObject constraint;
    // End: Does not convey

    
    String conflictingProfiles = "Profile(s):";

    String[] stupidDescriptions = {
    "Philanders With Own Wife",
    "Cannot Operate Paper",
    "Stabs With Wrong End",
    "Huffs Crayons",
    "Overpaid Prostitute",
    "Flatulates Over Long Trip",
    "Erupts With Verbal Diarrhea",
    "Only Speaks Pig Latin",
    "Needs Instruction To Breathe",
    "Dresses Up Like Barbie",
    "Eliminates Within Pants",
    "Pees Sitting Down",
    "Drools",
    "Brings Down Average IQ",
    "Shakes After Tucking In",
    "Doesnt Known Own Name",
    "Thinks Without Thought",
    "Squats With Difficulty",
    "Cannot Find Spleen",
    "Catches Flies With Open Mouth",
    "Scared Of Puppies",
    "Cannot Chew Gum And Fight",
    "Wipes Without Washing Afterwards",
    "Dangerous To Operate Heavy Machinery",
    "Speaks With Bad Gas"
    };

    @Override
    public Object execute() throws Throwable {
        // here is where the model is built
        EntitlementSummary summary = 
              PolicyUtil.summarizeViolationEntitlements(context, identity, violation, null);

        if (summary != null) {
                StringBuilder b = new StringBuilder();

                b.append("<b>Profile(s) description: </b><br/>");
                summarizeRoles(b, summary.left);
                if (!conflictingProfiles.equals("Profile(s):"))
                      conflictingProfiles = conflictingProfiles+", "; 
                summarizeRoles(b, summary.right);

                violation.setDescription(b.toString());
                
                String ownerName = "N/A";
                if (violation.getOwner() != null)
                      ownerName = violation.getOwner().getName();
                      
                System.out.println( "Policy Violation" + 
                                        "Policy: "+policy.getName() +
                                        "Rule: "+constraint.getName() +
                                        "Identity: "+identity.getDisplayName() +
                                        "Policy Actor: "+ownerName +
                                        conflictingProfiles +
                                        null);
        }
        
        System.out.println("Makey boom boom rule!");
        
        return null;
    }

    
    //This method will get the object "Profile" from the DB then get the description of the profile
    private String getDescription(String value, String app){
          int loc = (int) (Math.random() * stupidDescriptions.length); 
          String description = stupidDescriptions[loc];
          return description;
    }

    private void summarizeApp(StringBuilder b, ApplicationSummary app) {
            List atts = app.attributes;

            if (atts != null && atts.size() > 0) {
                  for (int i = 0 ; i < atts.size() ; i++) {
                    ItemSummary as = (ItemSummary) atts.get(i);
                    List values = as.values;
                    
                    if (values == null) {
                            b.append("null");
                    } else {
                          for (int j = 0 ; j < values.size() ; j++) {  
                          // because of a bug within the method getting the profiles values (only the first value is correctly extracted) 
                          // All misconfigured data will be filtred, we keep only real applications so attributes from IIQ like Service or city will be filtred to
                            if (!app.name.equalsIgnoreCase("IIQ")){
                                    b.append(app.name);
                                    b.append(": ");
                                    b.append("<b>"+values.get(j).toString()+"</b>");
                                    b.append(": ");
                                    b.append("<i><FONT COLOR=#008B8B>"+getDescription(values.get(j).toString(), app.name)+"</FONT></i>");
                                    b.append("<br/>");
                                    
                                    conflictingProfiles = conflictingProfiles+" "+app.name+": "+values.get(j).toString();
                            }
                          }
                    }
                  }
            }

            // let's put permissions each on a line since they
            // tend to be larger than attribute values?
            List perms = app.permissions;
            if (perms != null && perms.size() > 0) {
                    for (int i = 0 ; i < perms.size() ; i++) {
                            ItemSummary ps = (ItemSummary) perms.get(i);

                            b.append(app.name);
                            b.append(": ");

                            List values = ps.values;
                            if (values == null) {
                                    b.append("none");
                            }
                            else if (values.size() > 1) {
                                    // this will bracket it with []
                                    b.append(values.toString());
                            }
                            else {
                                    b.append("'");
                                    b.append(values.get(i).toString());
                                    b.append("'");
                            }

                            b.append(" on ");
                            b.append(ps.name);
                            b.append("<br/>");
                    }
            }

    }

    private void summarizeRole(StringBuilder b, RoleSummary role) {
            // you won't have a role name for Entitlement SOD
            if (role.name != null) {
                    b.append("Role: ");
                    b.append(role.name);
                    b.append("<br/>");
            }
            List apps = role.applications;
            if (apps != null) {
                    for (int i = 0 ; i < apps.size() ; i++) 
                            summarizeApp(b, (ApplicationSummary) apps.get(i));
            }
    }

    private void summarizeRoles(StringBuilder b, List roles) {
            // in practice there will only be one role
            if (roles != null) {
                    for (int i = 0 ; i < roles.size() ; i++) {
                            summarizeRole(b, (RoleSummary) roles.get(i));
                    }
            }
    }

}
