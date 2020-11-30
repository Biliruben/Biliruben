package sailpoint.api;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.object.AccountSelectorRules;
import sailpoint.object.Application;
import sailpoint.object.ApplicationAccountSelectorRule;
import sailpoint.object.AttributeDefinition;
import sailpoint.object.BaseAttributeDefinition;
import sailpoint.object.Bundle;
import sailpoint.object.Filter;
import sailpoint.object.Filter.CompositeFilter;
import sailpoint.object.Filter.LeafFilter;
import sailpoint.object.Filter.LogicalOperation;
import sailpoint.object.Form;
import sailpoint.object.FormRef;
import sailpoint.object.IdentitySelector;
import sailpoint.object.IdentitySelector.MatchExpression;
import sailpoint.object.IdentitySelector.MatchTerm;
import sailpoint.object.Profile;
import sailpoint.object.SailPointObject;
import sailpoint.object.Schema;
import sailpoint.object.Template;
import sailpoint.object.Visitor;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Util;

public class Bootstrapper extends Visitor {
    
    private static final String TEMPORARY_IDENTITY_ATTRIBUTE = "tbd";
    private static Log log = LogFactory.getLog(Bootstrapper.class);
    private SailPointContext _ctx;

    public Bootstrapper(SailPointContext ctx) {
        super();
        this._ctx = ctx;
    }
    
    @Override
    public void visit(SailPointObject obj) throws GeneralException {
        if (obj == null) {
            // don't wanna double-check for nulls all the time, so just blow them up here
            return;
        }
        try {
            super.visit(obj);
        } catch (GeneralException e) {
            String msg = "Couln't visit: " + obj;
            System.out.println(msg);
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
        }
    }
    
    
    @Override
    public void visitBundle(Bundle bundle) throws GeneralException {
        visit(bundle.getAccountSelectorRule());
        visit(bundle.getJoinRule());
        AccountSelectorRules selectorRules = bundle.getAccountSelectorRules();
        if (selectorRules != null) {
            visit(selectorRules.getBundleLevelAccountSelectorRule());
            List<ApplicationAccountSelectorRule> accountSelectorRules = selectorRules.getApplicationAccountSelectorRules();
            if (accountSelectorRules != null) {
                Iterator<ApplicationAccountSelectorRule> it = accountSelectorRules.iterator();
                while (it != null && it.hasNext()) {
                    ApplicationAccountSelectorRule next = it.next();
                    visit(next.getApplication());
                    visit(next.getRule());
                }
            }
        }
        Set<Application> applications = bundle.getApplications();
        if (!Util.isEmpty(applications)) {
            for (Application a : applications) {
                visit(a);
            }
        }
        List<Bundle> bundles = bundle.getBundles();
        if (bundles != null) {
            for (Bundle subBundle : bundles) {
                visit(subBundle);
            }
        }
        
        List<Profile> profiles = bundle.getProfiles();
        if (profiles != null) {
            for (Profile profile : profiles) {
                visit(profile);
            }
        }
        
        IdentitySelector selector = bundle.getSelector();
        if (selector != null) { 
            MatchExpression matchExpression = selector.getMatchExpression();
            if (matchExpression != null) {
                List<MatchTerm> terms = matchExpression.getTerms();
                if (!Util.isEmpty(terms)) {
                    Stack<MatchTerm> termStack = new Stack<MatchTerm>();
                    termStack.addAll(terms);
                    while (!termStack.isEmpty()) {
                        MatchTerm term = termStack.pop();
                        if (term.isContainer()) {
                            List<MatchTerm> children = term.getChildren();
                            termStack.addAll(children);
                        } else {
                            /*
                        Application termApp = term.getApplication();
                        String propertyName = termApp.getName();
                             */
                        }


                    }
                }
            }
        }
        //selector.getFilter();
        
        
        
        /*
        List<RoleAssignment> assignments = bundle.getRoleAssignments();
        if (assignments != null) {
            for (RoleAssignment assignment : assignments) {
                // probably outta do something here
            }
        }
        List<RoleDetection> detections = bundle.getRoleDetections();
        
        List<Template> templates = bundle.getTemplates();
        if (templates != null) {
            for (Template template : templates) {

            }
        }
        */
        
        /*
        List<Form> provForms = bundle.getProvisioningForms();
        if (!Util.isEmpty(provForms)) {
            for (Form template : provForms) {
                FormRef formRef = template.getFormRef();
                if (formRef != null) {
                    String formName = formRef.getName();
                    if (!Util.isNullOrEmpty(formName)) {
                        Form f = new Form();
                        f.setName(formName);
                        _ctx.saveObject(f);
                        _ctx.commitTransaction();
                    }
                }
            }
        }
        */
    }
    
    private void associateApplicationAttribute(Application app, String property, boolean isMulti) {
        
        Schema acctSchema = app.getSchema(Application.SCHEMA_ACCOUNT);
        if (acctSchema == null) {
            acctSchema = new Schema(Application.SCHEMA_ACCOUNT, null);
            app.addSchema(acctSchema);
        }
        
        AttributeDefinition definition = acctSchema.getAttributeDefinition(property);
        if (definition == null) {
            acctSchema.addAttributeDefinition(property, BaseAttributeDefinition.TYPE_STRING);
            definition = acctSchema.getAttributeDefinition(property);
            definition.setMulti(isMulti);
        }
    }
    
    @Override
    public void visitProfile(Profile obj) throws GeneralException {
        super.visitProfile(obj);
        Application app = obj.getApplication();
        Stack<Filter> stack = new Stack<Filter>();
        for (Filter f : obj.getConstraints()) {
            // initialize the stack
            stack.push(f);
        }
        while (!stack.isEmpty()) {
            Filter f = stack.pop();
            if (f instanceof LeafFilter) {
                LeafFilter lf = (LeafFilter)f;
                LogicalOperation op = lf.getOperation();
                String property = lf.getProperty();
                boolean isMulti = false;
                switch (op) {
                case COLLECTION_CONDITION:
                case IN:
                case ISEMPTY:
                case CONTAINS_ALL:
                    isMulti = true;
                    break;
                default:
                    // place holder
                    break;
                }
                associateApplicationAttribute(app, property, isMulti);

            } else if (f instanceof CompositeFilter) {
                CompositeFilter cf = (CompositeFilter)f;
                for (Filter child : cf.getChildren()) {
                    stack.push(child);
                }
            }
        }
        _ctx.saveObject(app);
        _ctx.commitTransaction();
    }
    
    @Override
    public void visitApplication(Application obj) throws GeneralException {
        super.visitApplication(obj);
        Schema acctSchema = obj.getSchema(Application.SCHEMA_ACCOUNT);
        if (acctSchema == null) {
            acctSchema = new Schema(Application.SCHEMA_ACCOUNT, Application.SCHEMA_ACCOUNT);
            acctSchema.setIdentityAttribute(TEMPORARY_IDENTITY_ATTRIBUTE);
            obj.setSchema(acctSchema);
        }
        obj.getAccountCorrelationConfig();
    }
    

}
