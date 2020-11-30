/* (c) Copyright 2008 SailPoint Technologies, Inc., All Rights Reserved. */

/*
 * TODO: License
 */
package sailpoint.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import flexjson.JSONSerializer;
import sailpoint.api.EmailNotifier;
import sailpoint.api.SailPointContext;
import sailpoint.object.EmailFileAttachment;
import sailpoint.object.EmailOptions;
import sailpoint.object.EmailTemplate;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Util;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * An EmailNotifier to use for simulating a hang from an SMTP server
 *
 * @author <a href="mailto:trey.kirk@sailpoint.com">Trey Kirk</a>
 */
public class ConfigurableHangingEmailNotifier implements EmailNotifier {

    private static final Log LOG = LogFactory.getLog(ConfigurableHangingEmailNotifier.class);

    //////////////////////////////////////////////////////////////////////
    //
    // Unit test control
    //
    // Added these so we can monitor what we're trying to send closer
    // to the actual SMTP transport in SMTPEmailNotifier. - jsl
    //
    //////////////////////////////////////////////////////////////////////

    /**
     * This switch gets flipped on by the unittests to cause all emails to go
     * through this notifier.
     */
    public static boolean enabled = false;
    
    /**
     * The number of notifications that have been sent.
     */
    private static int _sent;

    /**
     * When positive, we will capture the last numToCapture emails
     * sent in a static CompiledEmail list.
     */
    private static int _numToCapture;

    /**
     * The last email sent.
     */
    private static Stack<EmailDetails> _sentStack = new Stack<EmailDetails>();
    
    // TODO: tqm need to change _sentStack to be a datastructure which
    // contains emailtemplate as well as time so that we don't need the following
    // time specific stuff
    /**
     * is there a need to capture the time when email was sent
     */
    private static boolean _captureTimes = false;

    /**
     * Flag to indicate we should give the email to the
     * Notifier immediately when processing email. If this
     * flag is non-null and true the email will not 
     * be queued. Queuing is the default behavior for
     * performance reasons, but for unit-testing its important
     * that the emails are sent out immediately.
     */
    private Boolean _immediate;
    
    /*
     * Number of seconds to "hang" the email process
     */
    private int _hangSeconds;


    public static void setEnabled(boolean b) {
        enabled = b;
    }

    public static boolean isEnabled() {
        return enabled;
    }
    
    public static int getSentCount() {
        return _sent;
    }

    public static void setNumToCapture(int i) {
        // Clear the stack to make sure it's not too big for the new size.
        _sentStack.clear();
        _numToCapture = i;
    }

    public static void setCaptureTimes(boolean val) {
        _captureTimes = val;
    }
    
    public static boolean isCaptureTimes() {
        return _captureTimes;
    }
    
    /**
     * Reset the sent count and stack.
     */
    public static void reset() {
        _sent = 0;
        _sentStack.clear();
    }
    
    /**
     * Get the last email that was sent (if capturing is enabled).  This removes
     * the last email from the stack.
     */
    public static EmailDetails popLast() {
        return _sentStack.pop();
    }

    /**
     * Removes the last <code>num</code> emails from the stack.
     */
    public static EmailDetails popLast(int num) {

        EmailDetails last = null;

        for (int i=0; i<num; i++) {
            last = _sentStack.pop();
        }

        return last;
    }
    
    public static long getSentTime(int num) {

        EmailDetails details = _sentStack.get(num);

        return details != null ? details.getSentTime() : 0;
    }
    
    public static List<Long> getSentTimes() {

        List<Long> times = new ArrayList<Long>();
        for(int i=0;i<_sentStack.size();i++){
            EmailDetails details = _sentStack.get(i);
            times.add(details.getSentTime());
        }

        return times;
    }

    /**
     * Dump any emails that are on the stack.
     */
    public static void dump() throws GeneralException {
        System.out.println("Dumping " + _sentStack.size() + " emails...");

        for (EmailDetails email : _sentStack) {
            //System.out.println(email.toXml());
        }
    }

    //////////////////////////////////////////////////////////////////////
    //
    // EmailTemplate interface
    //
    //////////////////////////////////////////////////////////////////////

    /**
     * Default constructor.
     */
    public ConfigurableHangingEmailNotifier() {
        // This can be set via spring, but the default behavior
        // need to be true because many tests require the email
        // to be sent immediately.
        _immediate = new Boolean(true);
        _hangSeconds = 600;
    }

    /**
     * Currently a NOOP - may eventually want to write to a file to test the
     * email output.
     */
    public void sendEmailNotification(SailPointContext context,
                                      EmailTemplate template,
                                      EmailOptions options)
        throws GeneralException {

        // No-op
        LOG.debug("Ignoring email to: " + options.getTo());

        // tick a counter for the unit tests
        _sent++;

        try {
            hang();
        } catch (InterruptedException ie) {
            throw new GeneralException (ie);
        }
        if (_numToCapture > 0) {
            String result = "";

            try {
                Message msg = createMessage(options);
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                msg.writeTo(out);
                out.write('\n');

                result = new String(out.toByteArray());
            } catch(Exception ex) {
                LOG.debug("Unable to create raw message text", ex);
            }

            EmailDetails details = new EmailDetails(template, options, new Long(System.currentTimeMillis()), result);
            _sentStack.push(details);

            // Pull the bottom one off the stack if we're over the size limit.
            if (_sentStack.size() > _numToCapture) {
                _sentStack.remove(0);
            }
            
            //System.out.println(_last.toXml());
        }
    }
    
    private void hang() throws InterruptedException {
        if (_hangSeconds < 0) {
            // never hang on a negative value
            return;
        }
        long hangMs = _hangSeconds * 1000;
        if (_hangSeconds == 0) {
            // They want it to hang indefinitely...
            hangMs = 10000; // Let the thread come up for air every 10 seconds
        }
        do {
            LOG.warn("Hanging send notification for: " + _hangSeconds + " seconds");
            Thread.sleep(hangMs);
        } while (_hangSeconds > 0);
    }

    public Boolean sendImmediate() {
        return _immediate;
    }

    /**
     * If set to true the email will NOT be queued and instead
     * it will be directly sent to the notifier. This is 
     * typically set by our spring configuration.
     */
    public void setSendImmediate(Boolean immediate) {
        _immediate = immediate;
    }
    
    public void setHangSeconds(int hangSeconds) {
        _hangSeconds = hangSeconds;
    }

    /**
     * Creates a MimeMessage similar to SMTPEmailNotifier, but only includes attachments.
     * More info can be added if we want to test the validity of other items in the raw message source.
     */
    private Message createMessage(EmailOptions options) throws MessagingException {
        Message msg = new MimeMessage((Session)null);
        MimeMultipart multipart = new MimeMultipart();
        List<EmailFileAttachment> attachmentList = options.getAttachments();

        for (EmailFileAttachment attachment : Util.iterate(attachmentList)) {
            MimeBodyPart fileAttachment = new MimeBodyPart();
            fileAttachment.setFileName(attachment.getFileName());
            DataHandler dh =
                    new DataHandler(new SMTPEmailNotifier.AttachmentDataSource(attachment));
            fileAttachment.setDataHandler(dh);
            multipart.addBodyPart(fileAttachment);
        }

        msg.setContent(multipart);
        msg.saveChanges();

        return msg;
    }

    public static class EmailDetails{

        private EmailTemplate template;
        private EmailOptions options;
        private Long sentTime;
        private String rawMessage;

        public EmailDetails(EmailTemplate template, EmailOptions options, Long sentTime, String message) {
            this.template = template;
            this.options = options;
            this.sentTime = sentTime;
            this.rawMessage = message;
        }

        public EmailTemplate getTemplate() {
            return template;
        }

        public void setTemplate(EmailTemplate template) {
            this.template = template;
        }

        public EmailOptions getOptions() {
            return options;
        }

        public void setOptions(EmailOptions options) {
            this.options = options;
        }

        public Long getSentTime() {
            return sentTime;
        }

        public void setSentTime(Long sentTime) {
            this.sentTime = sentTime;
        }

        public String getRawMessage() { return this.rawMessage; }

        public String toJson(){
            return new JSONSerializer().prettyPrint(true).exclude("*.class").deepSerialize(this);
        }
    }
}
