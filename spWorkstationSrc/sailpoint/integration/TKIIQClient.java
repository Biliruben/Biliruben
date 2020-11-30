package sailpoint.integration;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TKIIQClient extends IIQClient {

    public TKIIQClient() throws Exception {
        // TODO Auto-generated constructor stub
    }

    public TKIIQClient(String url) throws Exception {
        super(url);
        // TODO Auto-generated constructor stub
    }

    public TKIIQClient(String username, String password) throws Exception {
        super(username, password);
        // TODO Auto-generated constructor stub
    }

    public TKIIQClient(String url, String username, String password)
            throws Exception {
        super(url, username, password);
        // TODO Auto-generated constructor stub
    }


    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        try {
            final IIQClient client = new TKIIQClient();
            if (args.length > 2) {
                String url = args[0];
                String user = args[1];
                String pwd = args[2];
                System.out.println("Using connection parameters:\n\tURL: " + url +
                        "\n\tUser: " + user +
                        "\n\tPwd: " + pwd);
                if (!url.endsWith("/")) {
                    url += "/";
                }
                client.configure(url, user, pwd);
            } else {
                System.out.println("Using iiqclient.properties for connection details.");
            }
            //client.configure("http://localhost:8080/identityiq", "spadmin", "admin");
            class Pinger implements Runnable {
                private int threadId;

                Pinger(int threadId) {
                    this.threadId = threadId;
                }

                @Override
                public void run() {
                    try {
                        String result = client.ping();
                        println("Thread " + threadId + " response: " + result);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            };

            int threads = 20;
            for (int i = 0; i < threads; i++) {
                Pinger r = new Pinger(i);
                println("Thread " + i);
                Thread t = new Thread(r);
                t.start();
            }
        }
        catch (Throwable t) {
            println(t);
        }
    }
    
    /////////////////
    //
    // Constants
    //
    //////////////////////////////////////////////////////////////////////

    private static Log log = LogFactory.getLog(IIQClient.class);

    private static final String CONFIG_FILE = "/iiqclient.properties";

    private static final String PROP_PREFIX = "iiqClient.";
    private static final String PROP_BASE_URL = PROP_PREFIX + "baseURL";
    private static final String PROP_USERNAME = PROP_PREFIX + "username";
    private static final String PROP_PASSWORD = PROP_PREFIX + "password";
    private static final String PROP_TIMEOUT = PROP_PREFIX + "timeout";   
    private static final String PROP_ROLE_MODE = PROP_PREFIX + "roleMode"; 
    private static final String PROP_REQUEST_COMMENTS_ENABLED = PROP_PREFIX + "requestCommentsEnabled"; 

    /**
     * Default base URL to the IIQ server.
     */
    public static final String DEFAULT_URL = 
    "http://localhost:8080/identityiq";


    //////////////////////////////////////////////////////////////////////
    //
    // Fields
    //
    //////////////////////////////////////////////////////////////////////

    /**
     * Base URL to the IIQ server.
     */
    String _baseUrl;

    /**
     * The HttpClient to use to send requests.
     */
    HttpClient _client;

    /**
     * Username. This is the person who logs in/requests WS calls
     * This is set as part of the client set up
     * Keeping here as some WS need this info 
     * Username is assumed as plain text for now
     */
    String _username;
    
    /**
     * Role Mode. Default is assignable.
     * It supports "permitted" when specified in the properties file
     */
    String _roleMode = "assignable";
    
    /**
     * Request comments enabled 
     * @deprecated Unused
     */
    String _requestCommentsEnabled = "true";
    
    /**
     * 
     * trace std output
     */
    boolean _trace = false;
     

    //////////////////////////////////////////////////////////////////////
    //
    // Constructor. Use version with username, password (encrypted allowed 
    // also) for including authentication as part of the public methods
    // being used for the web service, specially needed for ARM
    //
    //////////////////////////////////////////////////////////////////////

    /**
     * Return the username being used for authentication.
     */
    public String getUsername() {
        return _username;
    }

    /**
     * Set the username to use for authentication.
     */
    public void setUsername(String _username) {
        this._username = _username;
    }

    private void setupClient(String url, String username, String password,
                             String timeout, Map options)
        throws Exception {

        _baseUrl = url;
        _client = new ApacheHttpClient();
        _client.setup(checkHttpsUrl(url), getPort(url), username, password,
                      timeout, options);
    }

    //////////////////////////////////////////////////////////////////////
    //
    // Configuration
    //
    //////////////////////////////////////////////////////////////////////

    /**
     * Configure the IIQClient reading the properties from CONFIG_FILE.
     */
    public void configure() throws Exception {
        this.configure(null, null, null);
    }
    
    /**
     * Configure the IIQClient reading the properties from CONFIG_FILE.
     * If there is no properties file, just initialize using the given
     * parameters.
     * 
     * @param  baseURL   The base URL to use to override the properties file.
     * @param  user      The user to use to override the properties file.
     * @param  password  The password to use to override the properties file.
     */
    public void configure(String baseURL, String user, String password)
        throws Exception {

        Properties props = new Properties();

        // Consider allowing reading this file location from a system
        // property.
        InputStream is = IIQClient.class.getResourceAsStream(CONFIG_FILE);
        if (null != is) {
        
            props.load(is);

            if (null == baseURL) {
                baseURL = props.getProperty(PROP_BASE_URL);
            }
            traceUrl("baseURL read "+ baseURL);

            if (null == user) {
                user = props.getProperty(PROP_USERNAME);
            }
            if (null == password) {
                password = props.getProperty(PROP_PASSWORD);
            }
            String timeout = props.getProperty(PROP_TIMEOUT);

            if (null == baseURL) {
                baseURL = DEFAULT_URL;
            }
            
            _roleMode = props.getProperty(PROP_ROLE_MODE);
            if (_roleMode != null) {
                _roleMode = _roleMode.trim();
            }
            _requestCommentsEnabled = props.getProperty(PROP_REQUEST_COMMENTS_ENABLED);
            if (_requestCommentsEnabled != null) {
                _requestCommentsEnabled = _requestCommentsEnabled.trim();
            }

            // Pull any other generic properties that we support and put them in
            // the options map.
            Map options = new HashMap();
            for (int i=0; i<HttpClient.OPTS.length; i++) {
                String val = props.getProperty(PROP_PREFIX + HttpClient.OPTS[i]);
                if (null != val) {
                    options.put(HttpClient.OPTS[i], val);
                }
            }
            
            setupClient(baseURL, user, password, timeout, options);
        } else {
            // in the absense of a file still need to setup
            setupClient(baseURL, user, password, null, null);
        }
    }

    /**
     * Set the base URL to use for requests.
     */
    public void setBaseUrl(String s) {
        _baseUrl = (s != null) ? s.trim() : null;
    }

    /**
     * Return the base URL being used for requests.
     */
    public String getBaseUrl() {
        return _baseUrl;
    }
    
    /**
     * @deprecated Unused
     */
    public void setRequestCommentsEnabled(String s) {
        _requestCommentsEnabled = (s != null) ? s.trim() : null;
    }

    /**
     * @deprecated Unused
     */
    public String getRequestCommentsEnabled() {
        return _requestCommentsEnabled;
    }


    //////////////////////////////////////////////////////////////////////
    //
    // URI Building
    //
    //////////////////////////////////////////////////////////////////////

    private String formatUrl(String resource, String identity, 
                             String arg1, String value1)
        throws Exception {

        List resources = new ArrayList();
        resources.add(resource);
        if (null != identity) {
            resources.add(identity);
        }
        return formatUrl(resources, arg1, value1);
    }
    
    //convenience methods. trying to keep it at 1.4    
    private String formatUrl(List resources) throws Exception {
        return formatUrl(resources, null, null);
    }
    
    private String formatUrl(List resources, String arg1, String value1)
        throws Exception {

        Map parameters = null;
        if (arg1 != null && value1 != null) {
            parameters = new HashMap();
            parameters.put(arg1, value1);
        }
        return formatUrl(resources, parameters);
    }

    private String formatUrl(List resources, Map queryParameters) {

        // Construct a URI with the base so we can extract the components.
        URI baseURI = null;
        try {
            baseURI = new URI((null != _baseUrl) ? _baseUrl : DEFAULT_URL);
        }
        catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        StringBuffer pathBuf = new StringBuffer(baseURI.getPath());
        
        // web.xml must plug the REST servlet under here
        // note that currently Axis has "services" so can't conflict
        // UPDATE: Axis was removed in 5.2, want to change the name now?
        if (pathBuf.charAt(pathBuf.length()-1) != '/') {
            pathBuf.append("/");
        }
        pathBuf.append("rest");

        if (resources != null) {
            for (Iterator it=resources.iterator(); it.hasNext(); ) {
                Object o = it.next();
                String s = ((o != null) ? o.toString() : null);
                if (s != null && s.length() > 0) {
                    pathBuf.append("/").append(s);
                }
            }
        }

        String query = null;
        if ((null != queryParameters) && !queryParameters.isEmpty()) {
            StringBuffer queryBuf = new StringBuffer();
            String sep = "";
            for (Iterator it=queryParameters.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry entry = (Map.Entry) it.next();
                List vals = null;
                Object val = entry.getValue();
                if (val instanceof List) {
                    vals = (List) val;
                }
                else {
                    vals = new ArrayList();
                    vals.add(val);
                }

                for (Iterator valIt=vals.iterator(); valIt.hasNext(); ) {
                    queryBuf.append(sep).append(entry.getKey()).append("=").append(valIt.next());
                    sep = "&";
                }
            }
            query = queryBuf.toString();
        }
        
        // Build a URI with the components and convert it to a string.  This
        // ensures proper escaping.
        String url = null;
        try {
            URI uri = new URI(baseURI.getScheme(), null, baseURI.getHost(),
                              baseURI.getPort(), pathBuf.toString(), query, null);
            url = uri.toString();
        }
        catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
                          
        return url;
    }
        

    //////////////////////////////////////////////////////////////////////
    //
    // Ping
    //
    //////////////////////////////////////////////////////////////////////

    /**
     * The path to the ping REST resource.
     */
    public static final String RESOURCE_PING = "ping";

    /**
     * Ping the IIQ REST endpoint and return a success string if the client
     * could successfully connect and authenticate with the configured
     * credentials.  An unsuccessful ping will throw and exception.
     */
    public String ping() throws Exception {

        //String url = formatUrl(RESOURCE_PING, null, null, null);
        String url = formatUrl("trey/ping/10000", null, null, null);
        System.out.println("URL: " + url);
        String result = clientGet(url);       
        Object obj = JsonUtil.parse(result);
        if (obj != null)
            result = obj.toString();
        
        return result;
    }

    //////////////////////////////////////////////////////////////////////
    //
    // Authentication. IIQClient constructor to pass username, password
    //
    //////////////////////////////////////////////////////////////////////
    
    /**
     * The path to the authentication REST resource.
     */
    public static final String RESOURCE_AUTHENTICATION = "authentication";

    /**
     * Attempt to authenticate using the configured credentials.
     *
     * @return A JSON string of a Map that contains the name of the
     *    authenticated "identity" and an "outcome" with details about
     *    the authentication attempt (eg - "expiredPassword", "success",
     *    etc...).
     */
    public String authenticate() 
    throws Exception {
        
        String url = formatUrl(RESOURCE_AUTHENTICATION, null, null, null); 
        String result = clientGet(url);     
        return result;
    }
    
    //////////////////////////////////////////////////////////////////////
    //
    // getConfiguration. Passes the name of a configuration value to IIQ
    // and gets the value back.
    //
    //////////////////////////////////////////////////////////////////////
    
    /**
     * The path to the configuration REST resource.
     */
    public static final String RESOURCE_CONFIGURATION = "configuration";

    /**
     * The name of the query parameter that specifies which configuration
     * value to return in {@link #getConfiguration(String)}.
     */
    public static final String ARG_ATTRIBUTE_NAME = "attributeName";
    
    /**
     * Return a system configuration value for the requested attribute.
     */
    public String getConfiguration(String configName) 
    throws Exception {
        
        String url = formatUrl(RESOURCE_CONFIGURATION, null, ARG_ATTRIBUTE_NAME, configName);
        String result = clientGet(url);
        return result;
    }
    
    //////////////////////////////////////////////////////////////////////
    //
    // SOD Policy Checking
    //
    //////////////////////////////////////////////////////////////////////

    /**
     * The path to the policies REST resource.
     */
    public static final String RESOURCE_POLICIES = "policies";

    /**
     * The path to the check role policies REST sub-resource.
     */
    public static final String RESOURCE_CHECK_ROLE_POLICIES = 
    "checkRolePolicies";

    /**
     * Query parameter to specify the name of the identity.
     */
    public static final String ARG_IDENTITY = "identity";

    /**
     * Query parameter that specifies the roles to check for policy violations
     * in {@link #checkRolePolicies(String, List)}.
     */
    public static final String ARG_ROLES = "roles";

    /**
     * Given a list of potential role assignments, check to see if assigning
     * these roles would violate any SOD policies.
     *
     * @param identity  The name of the identity to check.
     * @param roles     The names of the roles to check.
     *
     * @return A List of Strings that have the violated policy names and
     *    constraints separated by a colon; empty if there are no violations.
     */
    public List checkRolePolicies(String identity, List roles) 
        throws Exception {
       
        List violations = null;
        
        List resources = new ArrayList();
        resources.add(RESOURCE_POLICIES);
        resources.add(RESOURCE_CHECK_ROLE_POLICIES);
        
        Map params = new HashMap();
        params.put(ARG_IDENTITY, identity);
        params.put(ARG_ROLES, roles);
        
        String url = formatUrl(resources, params);
        String body = clientGet(url);
        Object obj = JsonUtil.parse(body);
        if (obj instanceof List) {
            //note already parsed output here
            violations = (List) obj;
        }
        return violations;
    }


    /////////////////////////////////////////////////////////////////////////
    // Password Service
    //////////////////////////////////////////////////////////////////////////
    
    /**
     * Helper class used by {@link IIQClient#checkPasswordPolicy(String, String)}.
     */
    public static class PasswordService
    {
        /**
         * Constants used for password policy checking.
         */
        public static class Consts
        {
            /**
             * The name of the request body parameter for the identity name.
             */
            public static final String PARAM_ID = "id";

            /**
             * The name of the request body parameter for the password.
             */
            public static final String PARAM_PASSWORD = "password";

            /**
             * The path to the check password policies REST sub-resource.
             */
            public static final String RESOURCE_CHECK_PASSWORD_POLICIES = "checkPasswordPolicies";
        }
        
        /**
         * Encapsulates results from {@link IIQClient#checkPasswordPolicy(String, String)}
         */
        public static class CheckPasswordResult extends RequestResult
        {
            private static final String ATT_VALID = "valid";
            
            private boolean valid;

            /**
             * Return whether the password was valid.
             */
            public boolean isValid()
            {
                return valid;
            }

            public void setValid(boolean valid)
            {
                this.valid = valid;
            }

            /**
             * Convert this result to a Map.
             */
            public Map toMap()
            {
                Map result = super.toMap();
                
                result.put(ATT_VALID, new Boolean(isValid()));
                
                return result;
            }
            
            // have to do it this way because it is done like this
            // in the base class :-(, use the static method below
            /**
             * Initialize this result from the given map.
             */
            public void fromMap(Map map)
            {
                super.fromMap(map);
                
                setValid(((Boolean)map.get(ATT_VALID)).booleanValue());
            }
            
            /**
             * Create a result from the given map.
             */
            public static CheckPasswordResult createResultFromMap(Map map)
            {
                CheckPasswordResult result = new CheckPasswordResult();
                result.fromMap(map);
                return result;
            }
        }
    }
    
    //////////////////////////////////////////////////////////////////////
    //
    // Roles Assignable resource excluding roles already assigned to the specific identity
    // Includes "start", "limit", "query" (for starting letter) as keys
    // of the map with only String based values. 
    // All these parameters are optional. If no request parameters
    // required, even a null can be passed.
    // 
    //////////////////////////////////////////////////////////////////////
    
    /**
     * The path to the roles REST resource.
     */
    public static final String RESOURCE_ROLES = "roles";

    /**
     * The path to the assignable permits roles REST sub-resource.
     */
    public static final String SUB_RESOURCE_ASSIGNABLE_PERMITS = "assignablePermits";

    /**
     * Query parameter that specifies the role mode.
     */
    public static final String ARG_ROLE_MODE = "roleMode";
    
    /**
     * Return the roles that can be assigned to the given identity based on the
     * role mode.  If the role mode is "permitted", this returns all roles
     * permitted by the assigned roles of the identity.  If the role mode is
     * "assignable" or not specified, this returns all assigned roles the
     * identity does not currently have.
     *
     * @param parameterMap  The Map to add the query parameters into; may
     *                      contain pagination parameters.
     * @param identity      The name of the identity.
     *
     * @return A String representation of a ListResult.
     */
    public String rolesAssignablePermits(Map parameterMap, String identity) 
    throws Exception {
        
        String result = null;  
        if (identity == null) {
          return result;    
        }

        List resources = new ArrayList();
        resources.add(RESOURCE_ROLES);
        resources.add(SUB_RESOURCE_ASSIGNABLE_PERMITS);

        if (parameterMap == null) {
            parameterMap = new HashMap();
        }
        //mode controls what we get finally. Default is assignable. It can be permitted
        if (_roleMode != null) {
            parameterMap.put(ARG_ROLE_MODE, _roleMode);
        }
        parameterMap.put(ARG_IDENTITY, identity);

        String url = formatUrl(resources, parameterMap);  
        result = clientGet(url);    
        
        return result;
    }
    

    //////////////////////////////////////////////////////////////////////
    //
    // Show Identity including already assigned roles.
    // Here identity is passed from the ui as selected.
    // It may be any identity submitted, not necessarily the user 
    // who logged in
    // 
    //////////////////////////////////////////////////////////////////////
  
    /**
     * The path to the identities REST resource.
     */
    public static final String RESOURCE_IDENTITIES = "identities";
        
    /**
     * Return a JSON string that has a map with the roles and viewable
     * attributes for the requested identity.
     *
     * @param identity  The name of the identity to return.
     */
    public String showIdentity(String identity)
    throws Exception {
        
        String result = null;  
        if (identity == null) {
          return result;    
        }           
        List resources = new ArrayList();
        resources.add(RESOURCE_IDENTITIES);
        resources.add(identity);
        String url = formatUrl(resources);
        result = clientGet(url); 
        return result;
     }
    
    //////////////////////////////////////////////////////////////////////
    //
    // Identity service
    // 
    //////////////////////////////////////////////////////////////////////
    
    /**
     * The path to the managed identities REST sub-resource.
     */
    public static final String RESOURCE_MANAGED_IDENTITIES = "managedIdentities";
    
    /**
     * Get the list of Identities for which the given Identity is authorized.
     * Because the results can't be generated by a single search, paging and
     * subset searching must be done by the caller.  
     * 
     * <p>
     * The number of search results is capped at 500 to preserve resources 
     * when searching deep hierarchies.  One risk of this cap is the 
     * possibility of maxing out the search results on the hierarchy search
     * before the scoped search is even engaged.
     * </p>
     * 
     * <p>
     * The depth of subordinate recursion is capped at 5 for similar reasons.
     * In a deep hierarchy, we want to at least capture the first few layers
     * completely.
     * </p>
     * 
     * @return List JSON formatted List of identities
     */
    public String getIdentityList(String identity)
    throws Exception {
        
        String result = null;  
        if (identity == null) {
          return result;    
        }           
        
        List resources = new ArrayList();
        resources.add(RESOURCE_IDENTITIES);
        resources.add(identity);
        resources.add(RESOURCE_MANAGED_IDENTITIES);
        String url = formatUrl(resources);
        result = clientGet(url); 
        return result;
     }
    
    //////////////////////////////////////////////////////////////////////
    //
    // GroupDefinition Service
    // 
    //////////////////////////////////////////////////////////////////////
    
    /**
     * The path to the group definition REST sub-resource.
     */
    public static final String RESOURCE_GROUP_DEFINITION = "groupDefinition";

    //////////////////////////////////////////////////////////////////////
    //
    // Update Identity Assigned Roles References. This could include updates
    // for new role ids and removal of existing role ids
    // 
    //////////////////////////////////////////////////////////////////////

    /**
     * @deprecated No longer supported.
     */
    public static final String SUB_RESOURCE_ROLE_REQUESTS = "roleRequests";

    /**
     * @deprecated No longer supported.
     */
    public static final String ARG_ROLE_REQUESTS = "roleRequests";

    /**
     * @deprecated No longer supported.
     */
    public static final String ARG_COMMENTS = "comments";
 
    /**
     * @deprecated No longer supported.
     */
    public String updateIdentityRoleReferences(String identity, 
                                               String roleJSON,
                                               String requestComments) 
    throws Exception {
        
        String result = null;  
        if (identity == null || getUsername() == null) {
          return result;    
        } 
        
        List resources = new ArrayList();
        resources.add(RESOURCE_IDENTITIES);
        resources.add(identity);
        resources.add(SUB_RESOURCE_ROLE_REQUESTS);
        String url = formatUrl(resources);    
        Map updateIdentityRolesRequestInfo = new HashMap();
        if (roleJSON != null) {
            updateIdentityRolesRequestInfo.put(ARG_ROLE_REQUESTS, JsonUtil.parse(roleJSON));
        }
        if (requestComments != null) {
            updateIdentityRolesRequestInfo.put(ARG_COMMENTS, requestComments);
        }
        if (_roleMode != null) {
            updateIdentityRolesRequestInfo.put(ARG_ROLE_MODE, _roleMode);
        }
        String jsonUpdateIdentityRolesInput = JsonUtil.render(updateIdentityRolesRequestInfo);
        result = clientPost(url, jsonUpdateIdentityRolesInput);
        return result;
    }
    
    //////////////////////////////////////////////////////////////////////
    //
    // Show requests submitted status for different identity roles
    //
    //////////////////////////////////////////////////////////////////////
    
    /**
     * @deprecated No longer supported.
     */
    public String userRequestsIdentityRoles()
       throws Exception {
        
        String result = null; 
        if (getUsername() == null) {
          return result;    
        } 

        List resources = new ArrayList();
        resources.add(RESOURCE_IDENTITIES);
        resources.add(getUsername());
        resources.add(SUB_RESOURCE_ROLE_REQUESTS);
        
        String url = formatUrl(resources);
        result = clientGet(url);
        return result;
    }


    ////////////////////////////////////////////////////////////////////
    // New Identity Service Methods
    ///////////////////////////////////////////////////////
    
    /**
     * Helper class for identity create and update methods.
     */
    public static class IdentityService
    {
        /**
         * Constants used for identity create and update.
         */
        public static class Consts
        {
            /**
             * JSON attributes in the POST request bodies to create and
             * update an identity.
             */
            public static class AttributeNames
            {
                /**
                 * The name of the Identity.
                 */
                public static final String USER_NAME = "name";
                /**
                 * The identity's first name.
                 */
                public static final String FIRST_NAME = "firstname";
                /**
                 * The identity's last name.
                 */
                public static final String LAST_NAME = "lastname";
                /**
                 * The identity's email.
                 */
                public static final String EMAIL = "email";
                /**
                 * The name of the manager for the identity.
                 */
                public static final String MANAGER = "manager";
                /**
                 * The identity's password.
                 */
                public static final String PASSWORD = "password";
            }
        }

        /**
         * Encapsulates the result or creating or updating an identity.
         */
        public static class CreateOrUpdateResult extends RequestResult
        {
            private static final String ATT_PERFORMED = "performed";
            
            private boolean performed;
            
            /**
             * Return whether the requested changes were performed.
             */
            public boolean isPerformed()
            {
                return performed;
            }

            /**
             * Set whether the requested changes were performed.
             */
            public void setPerformed(boolean created)
            {
                this.performed = created;
            }

            /**
             * Convert this result to a Map.
             */
            public Map toMap()
            {
                Map result = super.toMap();
                result.put(ATT_PERFORMED, new Boolean(isPerformed()));
                return result;
            }
            
            // have to do it this way because it is done like this
            // in the base class :-(, use the static method below
            /**
             * Initialize this result from the given map.
             */
            public void fromMap(Map map)
            {
                super.fromMap(map);
                
                setPerformed(((Boolean)map.get(ATT_PERFORMED)).booleanValue());
            }
            
            /**
             * Create a result from the given map.
             */
            public static CreateOrUpdateResult createResultFromMap(Map map)
            {
                CreateOrUpdateResult result = new CreateOrUpdateResult();
                result.fromMap(map);
                return result;
            }
        }
    }
    
    ///////////////////////////////////////////////////////
    // Authorization Service
    //////////////////////////////////////////////////////////
    
    /**
     * Helper class {@link IIQClient#checkAuthorization(String, String)}.
     */
    public static class AuthorizationService
    {
        /**
         * Constants used by the authorization service.
         */
        public static class Consts
        {
            /**
             * Query parameter that holds name of the right to check.
             */
            public static final String PARAM_RIGHT = "right";

            /**
             * The path to the check authorization REST resource.
             */
            public static final String RESOURCE_CHECK_AUTHORIZATION = "checkAuthorization";
        }

        /**
         * Encapsulates the result of checking authorization.
         */
        public static class CheckAuthorizationResult extends RequestResult
        {
            private static final String ATT_AUTHORIZED = "authorized";
            
            private boolean authorized;

            /**
             * Return whether the user is authorized for the requested right.
             */
            public boolean isAuthorized()
            {
                return authorized;
            }

            /**
             * Set whether the user is authorized for the requested right.
             */
            public void setAuthorized(boolean authorized)
            {
                this.authorized = authorized;
            }

            /**
             * Convert this result to a Map.
             */
            public Map toMap()
            {
                Map result = super.toMap();

                result.put(ATT_AUTHORIZED, new Boolean(isAuthorized()));

                return result;
            }
            
            /**
             * Initialize this result from the given map.
             */
            public void fromMap(Map map)
            {
                super.fromMap(map);
                
                setAuthorized(((Boolean)map.get(ATT_AUTHORIZED)).booleanValue());
            }
            
            /**
             * Create a result from the given map.
             */
            public static CheckAuthorizationResult createResultFromMap(Map map)
            {
                CheckAuthorizationResult result = new CheckAuthorizationResult();
                result.fromMap(map);
                return result;
            }
        }

    }
    
    
    
    //////////////////////////////////////////////////////////////////////
    //
    // IDENTITY AGGREGATION
    //
    //////////////////////////////////////////////////////////////////////
    
    /**
     * The path to the aggregate identity REST sub-resource.
     */
    public static final String RESOURCE_AGGREGATE = "aggregate";

    /**
     * Query parameter with the name of the application to aggregate.
     */
    public static final String PARAM_APPLICATION = "application";

    /**
     * POST body parameter that contains options for aggregation.
     */
    public static final String AGGREGATION_OPTIONS = "IIQ_aggregationOptions";
    
    /**
     * Aggregate the given resource object for the requested app onto the given
     * identity.
     * 
     * @param  identity        The name of the identity to aggregate onto.
     * @param  appName         The name of the app for the resource object.
     * @param  resourceObject  A map of attribute names and values.
     * @param  aggOptions      Optional map of options for aggregation.
     * 
     * @return The RequestResult.
     */
    public RequestResult aggregateAccount(String identity, String appName,
                                          Map resourceObject, Map aggOptions)
        throws Exception {
        
        // /identities/<identity name>/aggregate&application=<appName>
        List resources = new ArrayList();
        resources.add(RESOURCE_IDENTITIES);
        resources.add(identity);
        resources.add(RESOURCE_AGGREGATE);

        Map paramMap = new HashMap();
        paramMap.put(PARAM_APPLICATION, appName);

        String url = formatUrl(resources, paramMap);

        Map post = new HashMap(resourceObject);
        if (null != aggOptions) {
            post.put(AGGREGATION_OPTIONS, aggOptions);
        }

        String postData = JsonUtil.render(post);
        String result = clientPost(url, postData);

        Map map = (Map) JsonUtil.parse(result);
        map = (null != map) ? map : new HashMap();

        return new RequestResult(map);
    }
    
    
    //////////////////////////////////////////////////////////////////////
    //
    // Delete Pending Request for the Same Original Requestor
    //
    ////////////////////////////////////////////////////////////////////// 
    
    /**
     * The path to the requests REST resource.
     */
    public static final String RESOURCE_REQUESTS = "requests";
    
    /**
     * Delete the request with the given ID.
     */
    public String deletePendingRequest(String requestId)
        throws Exception { 
        String result = null; 
        if (getUsername() == null) {
          return result;    
        } 

        List resources = new ArrayList();
        resources.add(RESOURCE_REQUESTS);
        resources.add(requestId);

        String url = formatUrl(resources);
        result = clientDelete(url);
        return result;
    }
    
    ///////////////////////////////////////////////////////////////////////
    //
    // Workflow
    //
    ///////////////////////////////////////////////////////////////////////
    
    /**
     * The path to the workflows REST resource.
     */
    public static final String RESOURCE_WORKFLOWS = "workflows";

    /**
     * The path to the workflow launch REST sub-resource.
     */
    public static final String SUB_RESOURCE_WORKFLOW_LAUNCH = "launch";
    
    /**
     * Path parameter with the name or ID of the workflow.
     */
    public static final String PARAM_RESOURCE_WORKFLOW_DEFINITION = "workflowDef";

    /**
     * POST request body parameter that contains workflow launch arguments.
     */
    public static final String ARG_WORKFLOW_INPUTS = "workflowArgs";
   
    /**
     * Service that can launch a workflow remotely using IIQ. 
     *      
     * URI:     
     *     workflows/$(nameOfDef)/launch
     *     
     * The RequestResult's requestId will contain the id 
     * of the TaskResult that was created because of the 
     * workflow execution so it's status can be checked.
     * 
     * This method is called by the IRM integration to
     * perform provisioning activities like enable/
     * disable and role requests.
     * 
     * @param workflowName  The name or ID of the workflow to launch.
     * @param launchArgs    The arguments for the workflow launch.
     *
     * @return RequestResult with the results of the launch.
     */
    public RequestResult launchWorkflow(String workflowName, Map launchArgs) 
        throws Exception {
        
        ArrayList resources = new ArrayList();
        resources.add(RESOURCE_WORKFLOWS);
        resources.add(workflowName);
        resources.add(SUB_RESOURCE_WORKFLOW_LAUNCH);
        
        String url = formatUrl(resources);
        
        Map inputs = new HashMap();        
        if ( launchArgs != null ) {
            inputs.put(ARG_WORKFLOW_INPUTS, launchArgs);
        }
        String argsJson = JsonUtil.render(inputs);        
                
        RequestResult result = null;
        String json = clientPost(url, argsJson);
        if ( json != null ) {
            Map map = (Map)JsonUtil.parse(json); 
            result = new RequestResult();
            result.fromMap(map);
        }        
        return result;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    //
    // Task Results 
    //
    ///////////////////////////////////////////////////////////////////////////    
    
    /**
     * The path to the task results REST resource.
     */
    public static final String RESOURCE_TASK_RESULTS = "taskResults" ;

    /**
     * The path to the task result status REST sub-resource.
     */
    public static final String SUB_RESOURCE_TASKRESULT_STATUS = "status";
    
    /**
     * The path to the cancel workflow task result REST sub-resource.
     */
    public static final String SUB_RESOURCE_TASKRESULT_CANCELWORKFLOW = "cancelWorkflow";
    
    /**
     * Return details about the task result given
     * the name or id of the task result.
     * 
     * URI:     
     *    taskResults/$(resultIdOrName)/status
     * 
     * This method is used by the IRM integration to 
     * detect the status of a TaskResult that was
     * launched. ( because of a workflow execution )  
     * 
     * @param taskId  The ID of the task to return.
     * 
     * @return A RequestResults with the task result status.
     * @throws Exception
     */
    public RequestResult getTaskResultStatus(String taskId) 
        throws Exception {
        
        ArrayList resources = new ArrayList();
        resources.add(RESOURCE_TASK_RESULTS);
        resources.add(taskId);
        resources.add(SUB_RESOURCE_TASKRESULT_STATUS);
                
        String url = formatUrl(resources);                
        RequestResult result = null;
        String json = clientGet(url);
        if ( json != null ) {
            Map map = (Map)JsonUtil.parse(json); 
            result = new RequestResult();
            result.fromMap(map);
        }        
        return result;
        
    }
    
    /**
     * Cancel a workflow.
     * 
     * URI:
     *    taskResults/${resultId}/cancelWorkflow
     * 
     * 
     * @param taskId    The ID of the workflow to cancel.
     * @param comments  The comments about the cancel request.
     *
     * @return A RequestResult with the results of canceling the workflow.
     * @throws Exception
     */
    public RequestResult cancelWorkflow(String taskId, String comments) 
        throws Exception {

        ArrayList resources = new ArrayList();
        resources.add(RESOURCE_TASK_RESULTS);
        resources.add(taskId);
        resources.add(SUB_RESOURCE_TASKRESULT_CANCELWORKFLOW);
                
        String url = formatUrl(resources);

        Map inputs = new HashMap();
        if ( comments != null ) {
            inputs.put("comments", comments);
        }

        RequestResult result = null;
        String json = clientPost(url, inputs);
        if ( json != null ) {
            Map map = (Map)JsonUtil.parse(json); 
            result = new RequestResult();
            result.fromMap(map);
        }        
        return result;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    //
    // Links    
    //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * The path to the identity links REST sub-resource.
     */
    private static final Object SUB_RESOURCE_LINKS = "links";

    /**
     * Flag to indicate to the webs service that it should return 
     * locked, disabled and supportsSetPasssword flags.  The 
     * extra state requires the entire Link to be fetched.
     */
    private static final Object ARG_INCLUDE_LINK_STATE = "includeLinkState";

    /**
     * Get the links associated with the named identity.
     * This method returns a ListResult which has a payload
     * of objects in the getObjects() method.
     *
     * URI :
     *     identities/$(identityName)/linksWithState
     *     
     * @param identityName  The name of ID of the identity.
     * @return ListResult containing the links.
     * @throws Exception
     */
    public ListResult getLinks(String identityName, boolean includeLinkState) throws Exception {
        
        ArrayList resources = new ArrayList();
        
        resources.add(RESOURCE_IDENTITIES);
        resources.add(identityName);
        resources.add(SUB_RESOURCE_LINKS);
                
        Map parameterMap = new HashMap();
        parameterMap.put(ARG_IDENTITY, identityName);
        parameterMap.put(ARG_INCLUDE_LINK_STATE, new Boolean(includeLinkState));
        String url = formatUrl(resources, parameterMap);        
        String json = clientGet(url);
        Map map = (Map)JsonUtil.parse(json);
        ListResult result = null;
        if ( map != null ) {
            result = new ListResult(map);
        } 
        return result;        
    }
    
    //////////////////////////////////////////////////////////////////////
    //
    // Remote Login
    //
    //////////////////////////////////////////////////////////////////////
    
    /**
     * The path to the identity remote login REST sub-resource.
     */
    public static final String SUB_RESOURCE_REMOTE_LOGIN = "remoteLogin";

    /**
     * POST request body parameter with the host requesting remote login.
     */
    public static final String PARAM_HOST = "host";

    /**
     * @deprecated Unused
     */
    public static final String RESULT_REMOTE_TOKEN_ID = "remoteTokenId";
    
    /**
     * Create a RemoteLoginToken on the IIQ Server side. This 
     * token can be passed back in IIQ requests to allow 
     * access into the IIQ application.
     * 
     * This was initially developed for the BMC IRM integration
     * where IRM wants to call into IIQ interface as the current
     * authenticated SRM user in non-SSO environments.
     * 
     * @param identityName  The name of the identity for remote login.
     * @return The remoteTokenId
     * @throws Exception
     */
    public String remoteLogin(String identityName) 
        throws Exception {

        ArrayList resources = new ArrayList();
        String encodedName = URLEncoder.encode(identityName, "UTF-8");
        resources.add(RESOURCE_IDENTITIES);
        resources.add(encodedName);
        resources.add(SUB_RESOURCE_REMOTE_LOGIN);

        String url = formatUrl(resources);
        
        Map inputs = new HashMap();  
        String host = java.net.InetAddress.getLocalHost().getHostAddress();            
        if ( host == null ) 
            host = "unknown";        
        inputs.put( PARAM_HOST,host);
        
        String tokenId = null;
        String argsJson = JsonUtil.render(inputs);
        String json = clientPost(url, argsJson);        
        if ( json != null ) {
            tokenId = (String)JsonUtil.parse(json);
        }
        return tokenId;
    }
    
    //////////////////////////////////////////////////////////////////////
    //
    // Password Intercept
    //
    //////////////////////////////////////////////////////////////////////

    /**
     * The path to the password intercept REST resource.
     */
    public static final String RESOURCE_PASSWORD_INTERCEPT = "passwordIntercept";

    /**
     * POST request body argument to specify the application for password
     * intercept.
     */
    public static final String ARG_APPLICATION = "application";

    /**
     * POST request body argument to specify the password for password
     * intercept.
     */
    public static final String ARG_PASSWORD = "password";

    /**
     * Notify IIQ of a password intercept event.
     *
     * @param application  The name of the application on which the password changed.
     * @param identity     The name of the identity.
     * @param password     The new password.
     *
     * @return A success string.
     */
    public String passwordIntercept(String application, String identity, String password)
        throws Exception {
       
        String result = null;
        
        ArrayList resources = new ArrayList();
        resources.add(RESOURCE_PASSWORD_INTERCEPT);

        Map post = new HashMap();
        post.put(ARG_APPLICATION, application);
        post.put(ARG_IDENTITY, identity);
        post.put(ARG_PASSWORD, password);
        
        String url = formatUrl(resources);
        System.out.println("URL: " + url);
        result = clientPost(url, JsonUtil.render(post));

        return result;
    }

    //////////////////////////////////////////////////////////////////////
    //
    // HttpClient
    //
    //////////////////////////////////////////////////////////////////////
    
    private static boolean checkHttpsUrl(String url) {
        return (null != url) && url.toLowerCase().startsWith("https");
    }

    private int getPort(String url) throws MalformedURLException {

        int port = -1;

        if (null != url) {
            URL checkurl = new URL(url);
            port = checkurl.getPort();
        }

        if (port < 0) {
            port = 80; 
        }
        traceStr("Port in Https " + port);
        return port;
    }

    private String clientGet(String url) throws Exception {
        traceUrl(url);
        int status = _client.get(url);
        return processWSResponse(status, _client.getBody());
    }

    private String clientDelete(String url) throws Exception {
        traceUrl(url);
        int status = _client.delete(url);
        return processWSResponse(status, _client.getBody());
    }

    private String clientPost(String url, String postData) throws Exception {
        traceUrl(url);
        int status = _client.post(url, postData);
        return processWSResponse(status, _client.getBody());
    }

    private String clientPost(String url, Map postData) throws Exception {
        traceUrl(url);
        int status = _client.post(url, postData);
        return processWSResponse(status, _client.getBody());
    }

    private String clientPut(String url, String data) throws Exception {
        traceUrl(url);
        int status = _client.put(url, data);
        return processWSResponse(status, _client.getBody());
    }

    private String processWSResponse(int status, String responseBody) 
        throws Exception {
        // Other 200 codes indicate success ... may want to change this if our
        // web services start returning other 200 codes (eg - 201 created).
        if (status != 200)
            throwException(status, responseBody);
        return responseBody;
    }

    private void throwException(int status, String responseBody) 
    throws Exception {

        // is it necessary to have the status in here?  Won't
        // make sense to the end user anyway...
        throw new Exception(itoa(status) + ": " + responseBody);
    }   

    /**
     * Convert the given int to a string.
     */
    static public String itoa(int i) {
        return new Integer(i).toString();
    }


    //////////////////////////////////////////////////////////////////////
    //
    // Test
    //
    //////////////////////////////////////////////////////////////////////

    private void traceUrl(String url) {
        if (_trace)
            println("Sending: " + url);
    }
    
    private void traceStr(String str) {
        if (_trace)
            println("Tracing " + str);
    }
    
    /**
     * Set whether to print trace information to stdout.
     */
    public void setTrace(boolean b) {
        _trace = b;
    }

    /**
     * Print the given object to stdout.
     */
    public static void println(Object o) {
        System.out.println(o);
    } 

}
