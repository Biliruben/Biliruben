package sailpoint.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import sailpoint.api.BasicMessageRepository;
import sailpoint.api.Emailer;
import sailpoint.api.MessageRepository;
import sailpoint.api.ObjectUtil;
import sailpoint.api.SailPointContext;
import sailpoint.object.Configuration;
import sailpoint.object.EmailOptions;
import sailpoint.object.EmailTemplate;
import sailpoint.object.Identity;
import sailpoint.object.Resolver;
import sailpoint.object.TaskDefinition;
import sailpoint.object.TaskResult;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Util;

public class Rule_TaskCompletionEmailRule extends GenericRule {

    MessageRepository _errorHandler;
    TaskResult result;

    /**
     * Method to send email
     * @throws GeneralException 
    */
    private void sendEmailOnTaskCompletion(String emailTemplate, List recipients, TaskResult result, SailPointContext context) throws GeneralException {
        String message = "";
        String status = "";
        TaskDefinition def;
        Configuration sysConfig;

        def = result.getDefinition();
        EmailTemplate notifyEmail =  context.getObjectByName(EmailTemplate.class, emailTemplate);
        if (null == notifyEmail) {
            log.error ("From Task Completion Email Rule: ERROR: could not find email template [ " + emailTemplate + "]");
            return;
        }
        notifyEmail = (EmailTemplate) notifyEmail.deepCopy((Resolver)context);
        if (null == notifyEmail) {
            log.error ("From Task Completion Email Rule: ERROR: failed to deepCopy template [ " + emailTemplate + "]");
            return;
        }
        // For now, we'll just use a map with a few pre-selected properties.
        Map mArgs = new HashMap();

        mArgs.put("taskResult", result);
        mArgs.put("taskName", def.getName());
        mArgs.put("taskDesc", def.getDescription());
        if (result.isError()) {
            status = "Error";
        }
        else  if (result.isWarning()) {
            status = "Warning";
        }
        else if (result.isSuccess()) {
            status = "Success";
        }

        mArgs.put("taskStartTime", result.getLaunched() );
        mArgs.put("taskEndTime", result.getCompleted() );
        mArgs.put("status", status);
        if (result.getMessages() != null) {
            mArgs.put("message", result.getMessages());
        }
        mArgs.put ("resultId", result.getId());

        EmailOptions ops = new EmailOptions(recipients, mArgs);
        new Emailer(context, _errorHandler).sendEmailNotification(notifyEmail , ops);
    }

    private boolean isEmailNotificationEnabled(TaskResult result, Configuration sysConfig) {
        boolean sendEmail = false;
        String notifyStr = null;

        TaskDefinition def = result.getDefinition();
        notifyStr = (String) def.getString(Configuration.TASK_COMPLETION_EMAIL_NOTIFY);
        if (notifyStr == null) {
            notifyStr = sysConfig.getString(Configuration.TASK_COMPLETION_EMAIL_NOTIFY);
        }

        if (notifyStr != null) {
            if (notifyStr.equals("Always") ||
                ((notifyStr.equals("Failure")) && result.isError()) || 
                ((notifyStr.equals("Warning")) &&
                 (result.isWarning() || result.isError()))) {
                sendEmail = true;
            }
        }
                
        return sendEmail;
    }

    private Object getIdentityNames(TaskResult result, Configuration sysConfig) {
        
        TaskDefinition def = result.getDefinition();
        Object identityNames = def.getArgument(Configuration.TASK_COMPLETION_RECIPIENTS);
        if (identityNames == null) {
            identityNames = sysConfig.get(Configuration.TASK_COMPLETION_RECIPIENTS);
        }
        return identityNames;
    }

    private String getEmailTemplate(TaskResult result, Configuration sysConfig) {
        TaskDefinition def = result.getDefinition();
        String emailTemplate = def.getString(Configuration.TASK_COMPLETION_EMAIL_TEMPLATE);
        if (emailTemplate == null) {
            emailTemplate = sysConfig.getString(Configuration.TASK_COMPLETION_EMAIL_TEMPLATE);
            if (emailTemplate == null)
                emailTemplate = Configuration.DEFAULT_TASK_COMPLETION_EMAIL_TEMPLATE;
        }
        return emailTemplate;
    }

    private List getEmailAddress (String identityName, SailPointContext context) throws GeneralException {
        Identity identity = context.getObjectByName(Identity.class, identityName);
        if (identity != null) 
        {
            List addresses = ObjectUtil.getEffectiveEmails(context, identity);
            if (!Util.isEmpty(addresses)) {
                return(addresses);
            }
            else
            {
               if(log.isWarnEnabled()) {
                   log.warn("From Task Completion Email Rule: Missing Email Address for Email Recipient: " + identityName );
               }
            }
        }
        return (null);
    }

    private List getEmailRecipient (Object identityNames, SailPointContext context) throws GeneralException {
        List recipients = null;
        String val = null;
        StringTokenizer st = null;
        if (identityNames != null) {
            recipients = new ArrayList ();
            // From Task definition, single identity
            if (identityNames instanceof String  && !((String)identityNames).contains(",")) {
                List addresses = getEmailAddress (identityNames.toString(), context);
                if (addresses != null) {
                   recipients.addAll (addresses);
                }
            }
            // From Task definition, multiple identities
            else if (identityNames instanceof String  && ((String)identityNames).contains(",") == true) {
                List<String> nameList = Util.csvToList((String)identityNames);
                for (String identityName : nameList) {
                    List addresses = getEmailAddress (identityName, context);
                    if (addresses != null) {
                        recipients.addAll (addresses);
                    }
                }  
            } 
            // From system configuration single or multiple identities it comes as list
            else if (identityNames instanceof List) {
                for (String identityName : (List<String>)identityNames) {
                    List addresses = getEmailAddress (identityName, context);
                    if (addresses != null) {
                        recipients.addAll(getEmailAddress (identityName, context));
                    }
                }
            }
        }
        return (recipients);
    }



    @Override
    public Object execute() throws Throwable {
        // Main
        Configuration sysConfig = context.getConfiguration();
        boolean sendEmailNotify = isEmailNotificationEnabled(result, sysConfig);

        if (sendEmailNotify) {
            // jsl - why consturct this here, just make it in
            // sendEmailOnTaskCompletion where it is used?
            // why do this at all since no one consumes it?
            _errorHandler = new BasicMessageRepository();

            Object identityNames = getIdentityNames(result, sysConfig);
            String emailTemplate = getEmailTemplate(result, sysConfig);
            List recipients = getEmailRecipient(identityNames, context);

            if (recipients != null && !Util.isEmpty(recipients)) {
                  // Send Email
                 sendEmailOnTaskCompletion(emailTemplate, recipients, result, context);
            }
            else {
                if(log.isWarnEnabled()) {
                    log.warn("From Task Completion Email Rule: Cannot send task completion email Notification. Reason : Missing Email Address for Email Recipients");
                }
            }
        }

        return null;
    }

}
