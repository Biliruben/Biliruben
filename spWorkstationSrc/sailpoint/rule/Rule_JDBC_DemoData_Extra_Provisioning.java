package sailpoint.rule;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import sailpoint.object.Application;
import sailpoint.object.ProvisioningPlan;
import sailpoint.object.ProvisioningPlan.AccountRequest;
import sailpoint.object.ProvisioningPlan.AttributeRequest;
import sailpoint.object.ProvisioningResult;
import sailpoint.object.Schema;

public class Rule_JDBC_DemoData_Extra_Provisioning extends GenericRule {

    Application application;
    Schema schema;
    Connection connection;
    ProvisioningPlan plan;

    @Override
    public Object execute() throws Throwable {
        List accounts = plan.getAccountRequests();
        System.out.println("string is " + plan.toXml());
        System.out.println("before for loop");
        ProvisioningResult provResult = new ProvisioningResult();
        if (accounts.size() > 0) {
            for (int i = 0; i < accounts.size(); i++) {
                AccountRequest temp = (AccountRequest) accounts.get(i);
                System.out.println(temp.getApplication());
                AccountRequest.Operation op = temp.getOperation();
                String accountId = temp.getNativeIdentity();
                System.out.println("identity  " + accountId);
                System.out.println("operation " + op);
                StringBuilder query = new StringBuilder();
                try {
                    Statement stmt = connection.createStatement();
                    switch (op) {
                    case Create:
                    case Disable:
                    case Delete:
                    case Enable:
                    case Lock:
                    case Unlock:
                        throw new UnsupportedOperationException("Aw, hell no!");
                    
                    case Modify:
                        query.append("update demodataextra ");
                        for (AttributeRequest attr : temp.getAttributeRequests()) {
                            query.append(" set ").append(attr.getName()).append(" = '").append(attr.getValue()).append("'");
                        }
                        break;
                    }
                    query.append("where employeeId = '").append(temp.getNativeIdentity()).append("';");
                    // creating Query String
                    System.out.println("query is  " + query.toString());
                    stmt.executeUpdate(query.toString());
                } catch (SQLException e) {
                    provResult.setStatus(ProvisioningResult.STATUS_FAILED);
                    provResult.addError(e);
                }
            }
        }

        System.out.println("after the for loop");
        return provResult;
    }

}
