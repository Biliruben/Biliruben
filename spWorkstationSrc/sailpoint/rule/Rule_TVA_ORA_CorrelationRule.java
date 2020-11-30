package sailpoint.rule;
import sailpoint.object.Identity;
import sailpoint.object.Link;
import sailpoint.object.Application;
import sailpoint.api.IdentityService;
import java.util.*;

public class Rule_TVA_ORA_CorrelationRule extends CorrelationRule {

    // R1: Round 1 using (secondary) LINK:DBSNMP
    // R2: Round 2 using (primary) LINK:CONTROLSA_DROP
    // R3: Round 3 using (primary) LINK:ORACLE_OCM
    @Override
    public Object execute() throws Throwable {

        System.out.println("Oracle Correlation Rule Start");
        Map returnMap = new HashMap();
        String lid = account.getStringAttribute("Account ID");
        // R1: lid = "DBSNMP"
        // R2: lid = "CONTROLSA_DROP"
        // R3: lid = "ORACLE_OCM"
        String sec = "true";
        boolean svc = account.getBoolAttribute("Service Account Flag");
        /* R1:
        <entry key="Service Account Flag">
          <value>
            <Boolean></Boolean>
          </value>
        </entry>
        svc = false
         */
        /* R2:
          <entry key="Service Account Flag">
            <value>
              <Boolean>true</Boolean>
            </value>
          </entry> 
          svc = true
         */
        /* R3:
        <entry key="Service Account Flag">
          <value>
            <Boolean>true</Boolean>
          </value>
        </entry>
        svc = true
         */
        System.out.println("Account ID: "+lid);
        System.out.println("SVC: "+svc);

        //Check to see if acct id is a valid user
        if (lid!=null) 
            // R1: true
            // R2: true
            // R3L true
        {
            Identity id = context.getObjectByName(Identity.class, lid);
            // R1: Is there an Identity named 'DBSNMP'?
            // R2: Is there an Identity named 'CONTROLSA_DROP'?
            // R3: Is there an Identity named 'ORACLE_OCM'?
            if (id!=null)
            {   
                if (id.isCorrelated()&&!svc)
                {
                    sec="false";
                }
            }
        }   
        System.out.println("SEC: "+sec);

        if (sec.equalsIgnoreCase("true"))
            // R1, R2, R3: probably
        {
            //build secondary identitycubename
            String l = "ZZZ:ORA%%SEC_ACCT_APPNAME_DIVIDER%%"+application.getName().toUpperCase()+":";
            // R1, R2: l = "ZZZ:ORA_ESSA:"
            // R3: l = "ZZZ:ORA_ESSD:"

            //Get account type
            if ( svc ) 
                // R1: svc is false
                // R2: svc is true
                // R3: svc is true
            {
                l+="S";
                // R2: l = "ZZZ:ORA_ESSA:S"
                // R3: l = "ZZZ:ORA_ESSD:D"
            } else {
                l+="UNKNOWN"; // l = "UNKNOWN"
                // R1: l = "ZZZ:ORA_ESSA:UNKNOWN"
            }
            l += ":"+lid;
            // R1: l = "ZZZ:ORA_ESSA:UNKNOWN:DBSNMP"
            // R2: l = "ZZZ:ORA_ESSA:S:CONTROLSA_DROP"
            // R3: l = "ZZZ:ORA_ESSD:S:ORACLE_OCM"
            System.out.println("LID: "+lid);

            //Create secondary cube if it doesn't exist
            Identity t = context.getObject(Identity.class, l); // will defer to getObjectByName
            // R1: Is there an Identity named "ZZZ:ORA_ESSA:UNKNOWN:DBSNMP"?
            // R2: Is there an Identity named "ZZZ:ORA_ESSA:S:CONTROLSA_DROP"?
            // R3: Is there an Identity named "ZZZ:ORA_ESSD:S:ORACLE_OCM"?
            if (t==null)
            { 
                // This circumvents any creation rule that would otherwise run for uncorrelated
                // accounts. Just sayin'...
                System.out.println("Creating new identity cube");       
                Identity identity = new Identity();
                identity.setName(l);
                context.saveObject(identity);
                context.commitTransaction();
            }
            // R1: lid = "ZZZ:ORA_ESSA:UNKNOWN:DBSNMP"
            // R2: lid = "ZZZ:ORA_ESSA:S:CONTROLSA_DROP"
            // R3: lid = "ZZZ:ORA_ESSD:S:ORACLE_OCM"
            lid = l;
        }       
        returnMap.put("identityName",lid);

        return returnMap;
    }

}
