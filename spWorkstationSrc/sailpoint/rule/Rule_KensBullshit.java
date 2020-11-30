package sailpoint.rule;

import java.util.Arrays;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import oracle.jdbc.driver.OracleConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.Version;

public class Rule_KensBullshit extends GenericRule {



    Log log = LogFactory.getLog("sailpoint.rule.SupportRuleIDXCK");

    /// log4j trace: log4j.logger.sailpoint.rule.SupportRuleIDXCK=all
    ///
    /// @version: $Revision: 52333 $; $Date: 2015-10-06 14:49:31 -0500 (Tue, 06 Oct 2015) $;
    /////////////////////////////////////////////////////////////////////////////////////
    /// TOUCH ABOVE THIS LINE AT OWN PERIL!!!

    ///
    /// OPTION 1) flag to control read-only mode
    /// false - do NOT build SQL Update statments to modify DB (default)
    /// true - build SQL Update statments to modify DB (proabably run them too)
    ///

    boolean flagIDXUpdate = false; // true updates IIQ tables !!!

    ///
    /// OPTION 2) flag to select table coverage 
    /// false - process "usual suspects", subset of tables w/IDX column (default)
    /// true - process all tables w/IDX column
    ///

    boolean flagIDXAll = false; // true processes all IDX tables

    ///
    /// OPTION 3) flag to override IDX table coverage
    /// false - use idx info from rule (default)
    /// true - use (user-cfged) override settings (table,key,column) 
    /// (optional "strValueOverride" setting to scan only that column value)
    ///

    boolean flagIDXOverride = false; // true uses "override" strings
    String strTblOverride = "SPT_TABLENAME";
    String strKeyOverride = "PRIMARY_KEY_NAME"; // usually "id"
    String strColOverride = "PARENT_COLUMN_NAME";
    String strValueOverride = ""; // empty checks all parent values

    ///
    /// OPTION 4) flag value to fix all tbl entries (w/single SQL stmt)
    /// false - fix (mis-ordered) idx-values by each column-value (default)
    /// true - recompute all idx-values in table
    ///

    boolean flagFixAllTblCols = false; // true recalculates all IDX values

    ///
    /// OPTION 5) flag value to not run SQL stmts
    /// false - execute SQL Select,Update stmts (default)
    /// true - do NOT execute SQL stmts
    ///
    /// log4j-level INFO outputs SQL stmts (DBA review/execution)
    ///

    boolean flagNoRunSQL = false; // true doesn't execute SQL stmts

    ///
    /// OPTION 6) string to use in place of runtime IIQ version
    /// empty string gets version from runtime IIQ env (default)
    /// "5.5","6.0","6.1","6.2","6.3","6.4"
    ///

    String strIIQVerOverride = ""; // non-empty string skips getVersion() call

    ///
    /// OPTION 7) string to use in place of runtime DB vendor
    /// flavors SQL "UPDATE" syntax to suit RDBMS taste
    ///
    /// empty string pulls DB vendor from runtime IIQ env (default)
    /// "mysql","sqlserver","oracle","db2"
    ///

    String strSQLSyntaxOverride = ""; // non-empty string skips SQL meta-data

    String strSQLUpdateSyntax = "";

    ///
    ///
    /// TOUCH BELOW THIS LINE AT OWN PERIL!!!
    /////////////////////////////////////////////////////////////////////////////////////
    ///


    ///
    ///
    /// funcArray_BuildIDXDB()
    /// check to build array with "override" settings
    /// else build array of IDX table,key,column triples for many IIQ versions
    /// return array
    ///
    /// arraylist holds (version-specific) table,column names w/IDX feature
    /// always add table,primary-key,column names as ordered triplets !!!
    /// BeanShell barfs on ArrayList<String,String,String> or Arrays.asArrayList() !!!
    ///
    ArrayList funcArray_BuildIDXDB(String strVer) {
        ArrayList retArray = new ArrayList();

        log.debug("IIQ version [" +strVer+ "]");

        if (flagIDXOverride) {
            retArray.add(strTblOverride); retArray.add(strKeyOverride); retArray.add(strColOverride);
        } else if (!flagIDXAll) {
            retArray.add("spt_certification"); retArray.add("id"); retArray.add("parent");
            retArray.add("spt_certification_entity"); retArray.add("id"); retArray.add("certification_id");
            retArray.add("spt_certification_item"); retArray.add("id"); retArray.add("certification_entity_id");
            retArray.add("spt_link"); retArray.add("id"); retArray.add("identity_id");
            retArray.add("spt_mitigation_expiration"); retArray.add("id"); retArray.add("identity_id");
            retArray.add("spt_remediation_item"); retArray.add("id"); retArray.add("work_item_id");
            //  } else if (strVer.equals("5.0") || strVer.equals("5.1") || strVer.equals("5.2") || strVer.equals("5.5")) {
        } else if (strVer.equals("5.5")) {
            retArray.add("spt_account_group_inheritance"); retArray.add("account_group"); retArray.add("account_group");
            retArray.add("spt_account_group_perms"); retArray.add("accountgroup"); retArray.add("accountgroup");
            retArray.add("spt_account_group_target_perms"); retArray.add("accountgroup"); retArray.add("accountgroup");
            retArray.add("spt_activity_constraint"); retArray.add("id"); retArray.add("policy");
            retArray.add("spt_activity_data_source"); retArray.add("id"); retArray.add("application");
            retArray.add("spt_activity_time_periods"); retArray.add("application_activity"); retArray.add("application_activity");
            retArray.add("spt_app_secondary_owners"); retArray.add("application"); retArray.add("application");
            retArray.add("spt_application_remediators"); retArray.add("application"); retArray.add("application");
            retArray.add("spt_archived_cert_entity"); retArray.add("id"); retArray.add("certification_id");
            retArray.add("spt_bundle_children"); retArray.add("bundle"); retArray.add("bundle");
            retArray.add("spt_bundle_permits"); retArray.add("bundle"); retArray.add("bundle");
            retArray.add("spt_bundle_requirements"); retArray.add("bundle"); retArray.add("bundle");
            retArray.add("spt_capability_children"); retArray.add("capability_id"); retArray.add("capability_id");
            retArray.add("spt_capability_rights"); retArray.add("capability_id"); retArray.add("capability_id");
            retArray.add("spt_certification"); retArray.add("id"); retArray.add("parent");
            retArray.add("spt_certification_def_tags"); retArray.add("cert_def_id"); retArray.add("cert_def_id");
            retArray.add("spt_certification_entity"); retArray.add("id"); retArray.add("certification_id");
            retArray.add("spt_certification_item"); retArray.add("id"); retArray.add("certification_entity_id");
            retArray.add("spt_certifiers"); retArray.add("certification_id"); retArray.add("certification_id");
            retArray.add("spt_child_certification_ids"); retArray.add("certification_archive_id"); retArray.add("certification_archive_id");
            retArray.add("spt_dashboard_content_rights"); retArray.add("dashboard_content_id"); retArray.add("dashboard_content_id");
            retArray.add("spt_dictionary_term"); retArray.add("id"); retArray.add("dictionary_id");
            retArray.add("spt_entitlement_group"); retArray.add("id"); retArray.add("identity_id");
            retArray.add("spt_entitlement_snapshot"); retArray.add("id"); retArray.add("certification_item_id");
            retArray.add("spt_generic_constraint"); retArray.add("id"); retArray.add("policy");
            retArray.add("spt_group_permissions"); retArray.add("entitlement_group_id"); retArray.add("entitlement_group_id");
            retArray.add("spt_identity_assigned_roles"); retArray.add("identity_id"); retArray.add("identity_id");
            retArray.add("spt_identity_bundles"); retArray.add("identity_id"); retArray.add("identity_id");
            retArray.add("spt_identity_capabilities"); retArray.add("identity_id"); retArray.add("identity_id");
            retArray.add("spt_identity_controlled_scopes"); retArray.add("identity_id"); retArray.add("identity_id");
            retArray.add("spt_identity_workgroups"); retArray.add("identity_id"); retArray.add("identity_id");
            retArray.add("spt_link"); retArray.add("id"); retArray.add("identity_id");
            retArray.add("spt_mitigation_expiration"); retArray.add("id"); retArray.add("identity_id");
            retArray.add("spt_process_application"); retArray.add("process"); retArray.add("process");
            retArray.add("spt_profile"); retArray.add("id"); retArray.add("bundle_id");
            retArray.add("spt_profile_constraints"); retArray.add("profile"); retArray.add("profile");
            retArray.add("spt_profile_permissions"); retArray.add("profile"); retArray.add("profile");
            retArray.add("spt_remediation_item"); retArray.add("id"); retArray.add("work_item_id");
            retArray.add("spt_request_arguments"); retArray.add("signature"); retArray.add("signature");
            retArray.add("spt_request_definition_rights"); retArray.add("request_definition_id"); retArray.add("request_definition_id");
            retArray.add("spt_request_returns"); retArray.add("signature"); retArray.add("signature");
            retArray.add("spt_rule_dependencies"); retArray.add("rule_id"); retArray.add("rule_id");
            retArray.add("spt_rule_signature_arguments"); retArray.add("signature"); retArray.add("signature");
            retArray.add("spt_rule_signature_returns"); retArray.add("signature"); retArray.add("signature");
            retArray.add("spt_schema_attributes"); retArray.add("applicationschema"); retArray.add("applicationschema");
            retArray.add("spt_scope"); retArray.add("id"); retArray.add("parent_id");
            retArray.add("spt_sign_off_history"); retArray.add("id"); retArray.add("certification_id");
            retArray.add("spt_snapshot_permissions"); retArray.add("snapshot"); retArray.add("snapshot");
            retArray.add("spt_sodconstraint"); retArray.add("id"); retArray.add("policy");
            retArray.add("spt_sodconstraint_left"); retArray.add("sodconstraint"); retArray.add("sodconstraint");
            retArray.add("spt_sodconstraint_right"); retArray.add("sodconstraint"); retArray.add("sodconstraint");
            retArray.add("spt_sync_roles"); retArray.add("config"); retArray.add("config");
            retArray.add("spt_task_definition_rights"); retArray.add("task_definition_id"); retArray.add("task_definition_id");
            retArray.add("spt_task_signature_arguments"); retArray.add("signature"); retArray.add("signature");
            retArray.add("spt_task_signature_returns"); retArray.add("signature"); retArray.add("signature");
            retArray.add("spt_work_item"); retArray.add("id"); retArray.add("certification_ref_id");
            retArray.add("spt_work_item_owners"); retArray.add("config"); retArray.add("config");
            retArray.add("spt_workflow_rule_libraries"); retArray.add("rule_id"); retArray.add("rule_id");
            retArray.add("spt_workflow_target"); retArray.add("id"); retArray.add("workflow_case_id");

            if (strVer.equals("5.1") || strVer.equals("5.2") || strVer.equals("5.5")) {
                retArray.add("spt_archived_cert_item"); retArray.add("id"); retArray.add("parent_id");
                retArray.add("spt_authentication_answer"); retArray.add("id"); retArray.add("identity_id");
                retArray.add("spt_certification_groups"); retArray.add("certification_id"); retArray.add("certification_id");
                retArray.add("spt_dashboard_reference"); retArray.add("id"); retArray.add("identity_dashboard_id");
                retArray.add("spt_password_policy_holder"); retArray.add("id"); retArray.add("application");

                if (strVer.equals("5.1")) {
                    retArray.add("spt_identity_history_item"); retArray.add("id"); retArray.add("identity_history_id");
                }
                else if (strVer.equals("5.2")) {
                    retArray.add("spt_identity_history_item"); retArray.add("id"); retArray.add("identity_history_id");
                    retArray.add("spt_identity_role_metadata"); retArray.add("identity_id"); retArray.add("identity_id");
                }
                else if (strVer.equals("5.5")) {
                    retArray.add("spt_identity_request_item"); retArray.add("id"); retArray.add("identity_request_id");
                    retArray.add("spt_identity_role_metadata"); retArray.add("identity_id"); retArray.add("identity_id");
                }
            }
        } else if (strVer.equals("6.0") || strVer.equals("6.1") || strVer.equals("6.2") || strVer.equals("6.3") || strVer.equals("6.4")) {
            retArray.add("spt_account_group_inheritance"); retArray.add("account_group"); retArray.add("account_group");
            retArray.add("spt_account_group_perms"); retArray.add("accountgroup"); retArray.add("accountgroup");
            retArray.add("spt_account_group_target_perms"); retArray.add("accountgroup"); retArray.add("accountgroup");
            retArray.add("spt_activity_constraint"); retArray.add("id"); retArray.add("policy");
            retArray.add("spt_activity_data_source"); retArray.add("id"); retArray.add("application");
            retArray.add("spt_activity_time_periods"); retArray.add("application_activity"); retArray.add("application_activity");
            retArray.add("spt_app_secondary_owners"); retArray.add("application"); retArray.add("application");
            retArray.add("spt_application_remediators"); retArray.add("application"); retArray.add("application");
            retArray.add("spt_arch_cert_item_apps"); retArray.add("arch_cert_item_id"); retArray.add("arch_cert_item_id");
            retArray.add("spt_archived_cert_item"); retArray.add("id"); retArray.add("parent_id");
            retArray.add("spt_authentication_answer"); retArray.add("id"); retArray.add("identity_id");
            retArray.add("spt_batch_request_item"); retArray.add("id"); retArray.add("batch_request_id");
            retArray.add("spt_bundle_children"); retArray.add("bundle"); retArray.add("bundle");
            retArray.add("spt_bundle_permits"); retArray.add("bundle"); retArray.add("bundle");
            retArray.add("spt_bundle_requirements"); retArray.add("bundle"); retArray.add("bundle");
            retArray.add("spt_capability_children"); retArray.add("capability_id"); retArray.add("capability_id");
            retArray.add("spt_capability_rights"); retArray.add("capability_id"); retArray.add("capability_id");
            retArray.add("spt_cert_action_assoc"); retArray.add("parent_id"); retArray.add("parent_id");
            retArray.add("spt_cert_item_applications"); retArray.add("certification_item_id"); retArray.add("certification_item_id");
            retArray.add("spt_certification"); retArray.add("id"); retArray.add("parent");
            retArray.add("spt_certification_def_tags"); retArray.add("cert_def_id"); retArray.add("cert_def_id");
            retArray.add("spt_certification_entity"); retArray.add("id"); retArray.add("certification_id");
            retArray.add("spt_certification_groups"); retArray.add("certification_id"); retArray.add("certification_id");
            retArray.add("spt_certification_item"); retArray.add("id"); retArray.add("certification_entity_id");
            retArray.add("spt_certifiers"); retArray.add("certification_id"); retArray.add("certification_id");
            retArray.add("spt_child_certification_ids"); retArray.add("certification_archive_id"); retArray.add("certification_archive_id");
            retArray.add("spt_dashboard_content_rights"); retArray.add("dashboard_content_id"); retArray.add("dashboard_content_id");
            retArray.add("spt_dashboard_reference"); retArray.add("id"); retArray.add("identity_dashboard_id");
            retArray.add("spt_dictionary_term"); retArray.add("id"); retArray.add("dictionary_id");
            retArray.add("spt_entitlement_group"); retArray.add("id"); retArray.add("identity_id");
            retArray.add("spt_entitlement_snapshot"); retArray.add("id"); retArray.add("certification_item_id");
            retArray.add("spt_generic_constraint"); retArray.add("id"); retArray.add("policy");
            retArray.add("spt_group_permissions"); retArray.add("entitlement_group_id"); retArray.add("entitlement_group_id");
            retArray.add("spt_identity_assigned_roles"); retArray.add("identity_id"); retArray.add("identity_id");
            retArray.add("spt_identity_bundles"); retArray.add("identity_id"); retArray.add("identity_id");
            retArray.add("spt_identity_capabilities"); retArray.add("identity_id"); retArray.add("identity_id");
            retArray.add("spt_identity_controlled_scopes"); retArray.add("identity_id"); retArray.add("identity_id");
            retArray.add("spt_identity_request_item"); retArray.add("id"); retArray.add("identity_request_id");
            retArray.add("spt_identity_role_metadata"); retArray.add("identity_id"); retArray.add("identity_id");
            retArray.add("spt_identity_workgroups"); retArray.add("identity_id"); retArray.add("identity_id");
            retArray.add("spt_jasper_files"); retArray.add("result"); retArray.add("result");
            retArray.add("spt_link"); retArray.add("id"); retArray.add("identity_id");
            retArray.add("spt_managed_attr_inheritance"); retArray.add("managedattribute"); retArray.add("managedattribute");
            retArray.add("spt_managed_attr_perms"); retArray.add("managedattribute"); retArray.add("managedattribute");
            retArray.add("spt_managed_attr_target_perms"); retArray.add("managedattribute"); retArray.add("managedattribute");
            retArray.add("spt_mitigation_expiration"); retArray.add("id"); retArray.add("identity_id");
            retArray.add("spt_password_policy_holder"); retArray.add("id"); retArray.add("application");
            retArray.add("spt_process_application"); retArray.add("process"); retArray.add("process");
            retArray.add("spt_profile"); retArray.add("id"); retArray.add("bundle_id");
            retArray.add("spt_profile_constraints"); retArray.add("profile"); retArray.add("profile");
            retArray.add("spt_profile_permissions"); retArray.add("profile"); retArray.add("profile");
            retArray.add("spt_remediation_item"); retArray.add("id"); retArray.add("work_item_id");
            retArray.add("spt_request_arguments"); retArray.add("signature"); retArray.add("signature");
            retArray.add("spt_request_definition_rights"); retArray.add("request_definition_id"); retArray.add("request_definition_id");
            retArray.add("spt_request_returns"); retArray.add("signature"); retArray.add("signature");
            retArray.add("spt_rule_dependencies"); retArray.add("rule_id"); retArray.add("rule_id");
            retArray.add("spt_rule_signature_arguments"); retArray.add("signature"); retArray.add("signature");
            retArray.add("spt_rule_signature_returns"); retArray.add("signature"); retArray.add("signature");
            retArray.add("spt_schema_attributes"); retArray.add("applicationschema"); retArray.add("applicationschema");
            retArray.add("spt_scope"); retArray.add("id"); retArray.add("parent_id");
            retArray.add("spt_sign_off_history"); retArray.add("id"); retArray.add("certification_id");
            retArray.add("spt_snapshot_permissions"); retArray.add("snapshot"); retArray.add("snapshot");
            retArray.add("spt_sodconstraint"); retArray.add("id"); retArray.add("policy");
            retArray.add("spt_sodconstraint_left"); retArray.add("sodconstraint"); retArray.add("sodconstraint");
            retArray.add("spt_sodconstraint_right"); retArray.add("sodconstraint"); retArray.add("sodconstraint");
            retArray.add("spt_sync_roles"); retArray.add("config"); retArray.add("config");
            retArray.add("spt_task_definition_rights"); retArray.add("task_definition_id"); retArray.add("task_definition_id");
            retArray.add("spt_task_signature_arguments"); retArray.add("signature"); retArray.add("signature");
            retArray.add("spt_task_signature_returns"); retArray.add("signature"); retArray.add("signature");
            retArray.add("spt_work_item"); retArray.add("id"); retArray.add("certification_ref_id");
            retArray.add("spt_work_item_owners"); retArray.add("config"); retArray.add("config");
            retArray.add("spt_workflow_rule_libraries"); retArray.add("rule_id"); retArray.add("rule_id");
            retArray.add("spt_workflow_target"); retArray.add("id"); retArray.add("workflow_case_id");

            if (strVer.equals("6.1")) {
                retArray.add("spt_app_dependencies"); retArray.add("application"); retArray.add("application");
                retArray.add("spt_target_sources"); retArray.add("application"); retArray.add("application");
            } else if (strVer.equals("6.2") || strVer.equals("6.3") || strVer.equals("6.4")) {
                retArray.add("spt_app_dependencies"); retArray.add("application"); retArray.add("application");
                retArray.add("spt_dynamic_scope_exclusions"); retArray.add("dynamic_scope_id"); retArray.add("dynamic_scope_id");
                retArray.add("spt_dynamic_scope_inclusions"); retArray.add("dynamic_scope_id"); retArray.add("dynamic_scope_id");
                retArray.add("spt_quick_link_dynamic_scopes"); retArray.add("quick_link_id"); retArray.add("quick_link_id");
                retArray.add("spt_target_sources"); retArray.add("application"); retArray.add("application");
            }
        } else { log.error("IIQ version unmatched [" +strVer+ "]"); }

        return retArray;
    }


    ///
    ///
    /// funcStr_getSQLSyntax()
    /// build DB connection to use metadata to find vendor 
    /// return stringname of RDBMS vendor
    ///
    String funcStr_getSQLSyntax() {

        String strDBName;
        Connection conn;

        if (0 < strSQLSyntaxOverride.length())
            strDBName = strSQLSyntaxOverride;
        else {
            try {
                log.debug("connecting to SQL metadata");
                conn = context.getConnection(); // when running in "direct" datasource envs (sailpointcontext)
                DatabaseMetaData myDBinfo = conn.getMetaData();
                String strTmpName = myDBinfo.getDatabaseProductName(); 

                log.debug("finding SQL metadata [" +strTmpName+ "]");

                /// assign SQL Syntax based on initial company name
                if (strTmpName.startsWith("MySQL"))
                    strDBName = "mysql";
                else if (strTmpName.startsWith("Microsoft"))
                    strDBName = "sqlserver";
                else if (strTmpName.startsWith("Oracle"))
                    strDBName = "oracle";
                else
                    strDBName = "db2";

            } catch(java.sql.SQLException e) {
                //
                // since rule re-uses same JDBC connection,
                // close connection on error (not in "finally" !!)
                //
                log.error("finding SQL metadata failed: " +e.toString());
                if ( conn != null ) {
                    try {
                        conn.close();
                        conn = null;
                    } catch(java.sql.SQLException t) {
                        log.error("closing SQL metadata failed: " +t.toString());
                    }
                }
                throw e;
            }
        }

        log.debug("using SQL syntax: " +strDBName);
        return strDBName;
    }


    ///
    ///
    /// funcLst_FindBadIDXCols()
    /// run first SQL stmt to find (non-null) columns w/null IDX-values
    /// run second SQL stmt to find columns w/(non-null) IDX-value having duplicates or gaps
    /// merge results as ArrayList of (String-valued) column w/bad IDX values
    ///
    /// notes: ignore entries w/null values in both column,idx (ala spt_entitlement_snapshot)
    /// yoohay ANSI!!! DB2,MySQL,MS-MySQL,Oracle execute same SELECT stmts
    ///
    /// SELECT DISTINCT +strCol+ FROM +strTbl+
    ///   WHERE ((+strCol+ IS not null)
    ///     AND ( +strCol+  = ' +strValueOverride+ ')  <<<< optional specific idx-set
    ///     AND (idx IS null)) ORDER BY +strCol+
    ///
    /// SELECT DISTINCT tblA.+strCol+ FROM +strTbl+ tblA
    ///   JOIN (SELECT +strCol+, idx, count(*) AS dupTst FROM +strTbl+
    ///     WHERE ( +strCol+  = ' +strValueOverride+ ')  <<<< optional specific idx-set
    ///     GROUP BY +strCol+, idx) tblB
    ///     ON tblB.+strCol+ = tblA.+strCol+ AND tblB.idx = tblA.idx
    ///   JOIN (SELECT +strCol+, MIN(idx) as minTst, (COUNT(+strCol+) - MAX(idx)) AS gapTst FROM +strTbl+
    ///     WHERE ( +strCol+  = ' +strValueOverride+ ')  <<<< optional specific idx-set
    ///     GROUP BY +strCol+) tblC
    ///     ON tblC.+strCol+ = tblA.+strCol+
    ///   WHERE ((tblB.dupTst <> 1) OR (tblC.minTst <> 0) OR (tblC.gapTst <> 1))
    ///   ORDER BY tblA.+strCol+
    ///


    ArrayList funcLst_FindBadIDXCols(String strTbl, String strKey, String strCol) {

        Connection conn;
        ArrayList retLst = new ArrayList();
        ArrayList retLstA = new ArrayList();
        ArrayList retLstB = new ArrayList();

        String strSQLStmtNull = "";
        String strSQLStmt = "";

        if ((false == flagIDXOverride) || (null == strValueOverride) || (0 == strValueOverride.length())) {
            strSQLStmtNull = "SELECT DISTINCT " +strCol+ " FROM " +strTbl+ " WHERE ((" +strCol+ " IS NOT null) AND (idx IS null)) ORDER BY " +strCol+ "";
            strSQLStmt = "SELECT DISTINCT tblA." +strCol+ " FROM " +strTbl+ " tblA JOIN (SELECT " +strCol+ ", idx, count(*) AS dupTst FROM " +strTbl+ " GROUP BY " +strCol+ ", idx) tblB ON tblB." +strCol+ " = tblA." +strCol+ " AND tblB.idx = tblA.idx JOIN (SELECT " +strCol+ ", MIN(idx) as minTst, (COUNT(" +strCol+ ") - MAX(idx)) AS gapTst FROM " +strTbl+ " GROUP BY " +strCol+ ") tblC ON tblC." +strCol+ " = tblA." +strCol+ " WHERE ((tblB.dupTst <> 1) OR (tblC.minTst <> 0) OR (tblC.gapTst <> 1)) ORDER BY " +strCol+ "";
        }else {
            strSQLStmtNull = "SELECT DISTINCT " +strCol+ " FROM " +strTbl+ " WHERE ((" +strCol+ " IS NOT null) AND (" +strCol+ " = '" +strValueOverride+ "') AND (idx IS null)) ORDER BY " +strCol+ "";
            strSQLStmt = "SELECT DISTINCT tblA." +strCol+ " FROM " +strTbl+ " tblA JOIN (SELECT " +strCol+ ", idx, count(*) AS dupTst FROM " +strTbl+ " WHERE (" +strCol+ " = '" +strValueOverride+ "') GROUP BY " +strCol+ ", idx) tblB ON tblB." +strCol+ " = tblA." +strCol+ " AND tblB.idx = tblA.idx JOIN (SELECT " +strCol+ ", MIN(idx) as minTst, (COUNT(" +strCol+ ") - MAX(idx)) AS gapTst FROM " +strTbl+ " WHERE (" +strCol+ " = '" +strValueOverride+ "') GROUP BY " +strCol+ ") tblC ON tblC." +strCol+ " = tblA." +strCol+ " WHERE ((tblB.dupTst <> 1) OR (tblC.minTst <> 0) OR (tblC.gapTst <> 1)) ORDER BY " +strCol+ "";
        }


        try {
            log.info("using SQL null select [" +strSQLStmtNull+ "]");

            if (flagNoRunSQL)
                log.debug("skipping SQL null select [" +strTbl+ ":" +strKey+ ":" +strCol+ "]");
            else {
                log.debug("connecting SQL null select [" +strTbl+ ":" +strKey+ ":" +strCol+ "]");
                conn = context.getConnection(); // when running in "direct" datasource envs (sailpointcontext)
                if (conn instanceof OracleConnection) {
                    OracleConnection oConn = (OracleConnection)conn;
                    oConn.setStatementCacheSize(0);
                }

                log.debug("preparing SQL null select [" +strTbl+ ":" +strKey+ ":" +strCol+ "]");
                PreparedStatement statement = conn.prepareStatement(strSQLStmtNull);

                log.debug("executing SQL null select [" +strTbl+ ":" +strKey+ ":" +strCol+ "]");
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    String col = (String) rs.getString(1);
                    if ((null != col) && (0 < col.length())) retLstA.add(col);
                }
            }

            log.info("using SQL dup,gap select [" +strSQLStmt+ "]");

            if (flagNoRunSQL)
                log.debug("skipping SQL dup,gap select [" +strTbl+ ":" +strKey+ ":" +strCol+ "]");
            else {
                log.debug("connecting SQL select [" +strTbl+ ":" +strKey+ ":" +strCol+ "]");
                conn = context.getConnection(); // when running in "direct" datasource envs (sailpointcontext)
                if (conn instanceof OracleConnection) {
                    OracleConnection oConn = (OracleConnection)conn;
                    oConn.setStatementCacheSize(0);
                }

                log.debug("preparing SQL dup,gap select [" +strTbl+ ":" +strKey+ ":" +strCol+ "]");
                PreparedStatement statement = conn.prepareStatement(strSQLStmt);

                log.debug("executing SQL dup,gap select [" +strTbl+ ":" +strKey+ ":" +strCol+ "]");
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    String col = (String) rs.getString(1);
                    if ((null != col) && (0 < col.length())) retLstB.add(col);
                }
            }
        } catch(java.sql.SQLException e) {
            //
            // since rule re-uses same JDBC connection,
            // close connection on error (not in "finally" !!)
            //
            log.error("executing SQL select failed: " +e.toString());
            if ( conn != null ) {
                try {
                    conn.close();
                    conn = null;
                } catch(java.sql.SQLException e) {
                    log.error("closing SQL select failed: " +e.toString());
                }
            }
            throw e;
        }

        // forget it jake, it's only reverse-merge-sortedlists-town
        int indexA = retLstA.size() - 1, indexB = retLstB.size() - 1;
        while (0 <= indexA && 0 <= indexB) {
            int cmpAB = retLstA.get(indexA).compareTo(retLstB.get(indexB));
            if (cmpAB < 0) retLst.add( retLstB.get(indexB--) );
            else if (cmpAB > 0) retLst.add( retLstA.get(indexA--) );
            else  { indexA--; retLst.add( retLstB.get(indexB--) ); }
        }
        while (0 <= indexA) retLst.add(retLstA.get(indexA--));
        while (0 <= indexB) retLst.add(retLstB.get(indexB--));

        log.debug("returning SQL select: " +retLst.toString());
        return retLst;
    }

    ///
    ///
    /// funcVoid_FixIDXVals()
    /// run SQL stmt to fix mis-ordered columns
    /// (use "id"-order to assign IDX new values on entries w/same COL)
    /// ALL) single SQL stmt to re-calculate IDX on all entries
    /// ONE) extra WHERE clause to filter by specific column
    ///
    /// DB2,Oracle allows reference in assignment, yet no JOINS
    /// "UPDATE +strTbl+ tblB SET tblB.idx = (
    ///       SELECT COUNT(*) FROM +strTbl+ tblA
    ///         WHERE ((tblA.+strCol+ = tblB.+strCol+) AND (tblA.id < tblB.id))
    ///     )
    ///     WHERE (tblB.+strCol+ = '+strColVal+')" <<< difference btwn (ALL),(ONE) 
    ///
    /// MySQL blocks table reference inside assignment, so must JOIN new idxvalues
    /// java.sql.SQLException: You can't specify target table 'tblB' for update in FROM clause
    /// "UPDATE +strTbl+ tblD
    ///   JOIN (SELECT tblB.id,
    ///       ( SELECT count(*) FROM +strTbl+ tblA
    ///         WHERE ((tblA.+strCol+ = tblB." +strCol+) AND (tblA.id < tblB.id))
    ///       ) AS idxValue
    ///       FROM +strTbl+ tblB
    ///       WHERE (tblB.+strCol+ = '+strColVal+') <<< difference btwn (ALL),(ONE) 
    ///     ) tblC ON tblD.id = tblC.id
    ///   SET tblD.idx = tblC.idxValue"
    /// 
    /// SQLServer requires worst parts from MySQL,Oracle
    /// "UPDATE tblD SET tblD.idx = tblC.idxValue
    ///   FROM +strTbl+ tblD
    ///   JOIN (SELECT tblB.id,
    ///       ( SELECT count(*) FROM +strTbl+ tblA
    ///         WHERE ((tblA.+strCol+ = tblB." +strCol+) AND (tblA.id < tblB.id))
    ///       ) AS idxValue
    ///       FROM +strTbl+ tblB
    ///       WHERE (tblB.+strCol+ = '+strColVal+') <<< difference btwn (ALL),(ONE) 
    ///     ) tblC ON tblD.id = tblC.id"
    ///
    void funcVoid_FixIDXVals(String strTbl, String strKey, String strCol, String strColVal) {
        String strSQLStmt = "";

        String strMySQLStmtAll = "UPDATE " +strTbl+ " tblD JOIN (SELECT tblB.id, ( SELECT COUNT(*) FROM " +strTbl+ " tblA WHERE ((tblA." +strCol+ " = tblB." +strCol+ ") AND (tblA.id < tblB.id))) AS idxValue FROM " +strTbl+ " tblB) tblC ON tblD.id = tblC.id SET tblD.idx = tblC.idxValue";
        String strMySQLStmtOne = "UPDATE " +strTbl+ " tblD JOIN (SELECT tblB.id, ( SELECT COUNT(*) FROM " +strTbl+ " tblA WHERE ((tblA." +strCol+ " = tblB." +strCol+ ") AND (tblA.id < tblB.id))) AS idxValue FROM " +strTbl+ " tblB WHERE (tblB." +strCol+ " = '" +strColVal+ "')) tblC ON tblD.id = tblC.id SET tblD.idx = tblC.idxValue";

        String strOracleStmtAll = "UPDATE " +strTbl+ " tblB SET tblB.idx = ( SELECT COUNT(tblA.id) FROM " +strTbl+ " tblA WHERE ((tblA." +strCol+ " = tblB." +strCol+ ") AND (tblA.id < tblB.id))) ";
        String strOracleStmtOne = "UPDATE " +strTbl+ " tblB SET tblB.idx = ( SELECT COUNT(tblA.id) FROM " +strTbl+ " tblA WHERE ((tblA." +strCol+ " = tblB." +strCol+ ") AND (tblA.id < tblB.id))) WHERE (tblB." +strCol+ " = '" +strColVal+ "') ";

        String strSQLServerStmtAll = "UPDATE tblD SET tblD.idx = tblC.idxValue FROM " +strTbl+ " tblD JOIN (SELECT tblB.id, ( SELECT COUNT(*) FROM " +strTbl+ " tblA WHERE ((tblA." +strCol+ " = tblB." +strCol+ ") AND (tblA.id < tblB.id))) AS idxValue FROM " +strTbl+ " tblB) tblC ON tblD.id = tblC.id";
        String strSQLServerStmtOne = "UPDATE tblD SET tblD.idx = tblC.idxValue FROM " +strTbl+ " tblD JOIN (SELECT tblB.id, ( SELECT COUNT(*) FROM " +strTbl+ " tblA WHERE ((tblA." +strCol+ " = tblB." +strCol+ ") AND (tblA.id < tblB.id))) AS idxValue FROM " +strTbl+ " tblB WHERE (tblB." +strCol+ " = '" +strColVal+ "')) tblC ON tblD.id = tblC.id";

        /// assign SQL UPDATE stmt based on RDBMS setting (or cross fingers w/Oracle)
        if (strSQLUpdateSyntax.equals("mysql")) {
            strSQLStmt = (null == strColVal) ? strMySQLStmtAll : strMySQLStmtOne ;
        } else if (strSQLUpdateSyntax.equals("sqlserver")) {
            strSQLStmt = (null == strColVal) ? strSQLServerStmtAll : strSQLServerStmtOne ;
        } else if (strSQLUpdateSyntax.equals("oracle") || strSQLUpdateSyntax.equals("db2")) {
            strSQLStmt = (null == strColVal) ? strOracleStmtAll : strOracleStmtOne ;
        } else { strSQLStmt = (null == strColVal) ? strOracleStmtAll : strOracleStmtOne ; }

        log.info("using SQL update [" +strSQLStmt+ "]");
        try {
            if (flagNoRunSQL)
                log.debug("skipping SQL update [" +strTbl+ ":" +strKey+ ":" +strCol+ "]");
            else {
                log.debug("connecting SQL update [" +strTbl+ ":" +strKey+ ":" +strCol+ "]");
                Connection conn = context.getConnection();
                if (conn instanceof OracleConnection) {
                    OracleConnection oConn = (OracleConnection)conn;
                    oConn.setStatementCacheSize(0);
                }

                log.debug("preparing SQL update [" +strTbl+ ":" +strKey+ ":" +strCol+ "]");
                PreparedStatement statement = conn.prepareStatement(strSQLStmt);

                log.debug("executing SQL update [" +strTbl+ ":" +strKey+ ":" +strCol+ "]");
                statement.executeUpdate();
            }
        } catch(java.sql.SQLException e) {
            log.error("executing SQL update failed: " +e.toString());
            // context re-uses connection, close only on error (not in "finally")
            if ( null != conn ) {
                try {
                    conn.close();
                    conn = null;
                } catch(java.sql.SQLException e) {
                    log.error("closing SQL update failed: " +e.toString());
                }
            }
            throw e;
        }
        log.debug("exiting SQL update");
    }

    ///
    ///
    /// funcStrLst_CheckIDXTbls()
    /// loop thru given triplets (table/key/column)
    /// find table's mis-ordered columns
    /// if fixing enabled, then run SQL stmts to fix IDX mis-orderings
    /// (either fix entire tbl or loop on each col value)
    /// return msgs for tables w/bad,fixed IDX values
    ///
    /// note: can't fix tbls that use IDX as (composite) primary key
    /// note: fwiw, no nulls or dups occur (only gaps) w/IDX in primary key
    ///
    ArrayList funcStrLst_CheckIDXTbls(ArrayList tblIDX) {
        ArrayList strLstMsgs = new ArrayList();

        for (int i = 0; i+2 < tblIDX.size(); i+=3) {
            String strTbl = (String) tblIDX.get(i);
            String strKey = (String) tblIDX.get(i+1);
            String strCol = (String) tblIDX.get(i+2);
            ArrayList lstBadIDXCols = funcLst_FindBadIDXCols(strTbl, strKey, strCol);
            if (((null != lstBadIDXCols) && (0 < lstBadIDXCols.size())) || flagNoRunSQL) {
                if (!flagNoRunSQL) {
                    log.warn(" " + lstBadIDXCols.size()+ " non-sequential IDX sets [" +strTbl+ ":" +strKey+ ":" +strCol+ "]");
                    strLstMsgs.add(" " + lstBadIDXCols.size()+ " non-sequential IDX sets [" +strTbl+ ":" +strKey+ ":" +strCol+ "]");
                }

                if (flagIDXUpdate) {
                    if (strKey.equals("id")) {
                        if (flagFixAllTblCols || flagNoRunSQL)
                            funcVoid_FixIDXVals(strTbl, strKey, strCol, null);
                        else
                            for (int j = 0; j < lstBadIDXCols.size(); j++) 
                                funcVoid_FixIDXVals(strTbl, strKey, strCol, (String) lstBadIDXCols.get(j));

                        if (!flagNoRunSQL) {
                            log.warn("IDX sets re-sequenced [" +strTbl+ ":" +strKey+ ":" +strCol+ "]");
                            strLstMsgs.add("now re-sequenced!!!\n");
                        }
                    }
                    else {
                        strLstMsgs.add("IDX unchanged (composite-key) [" +strTbl+ ":" +strKey+ ":" +strCol+ "]\n");
                    }
                }
            }
        }
        return strLstMsgs;
    }



    @Override
    public Object execute() throws Throwable {
        ///
        ///
        /// main()
        ///
        /// set SQL Update style (likely gonna use it)
        /// build list of tables to check
        /// scan/repair tables in list
        ///
        /// uncomment following line to output stack trace
        /// new Exception().printStackTrace(System.out);
        ///
        ArrayList strRetMsgs = new ArrayList();
        strRetMsgs.add("IDX check started");

        try  {
            strSQLUpdateSyntax = (0 < strSQLSyntaxOverride.length()) ? strSQLSyntaxOverride : funcStr_getSQLSyntax();

            ArrayList lstIDXTbls = funcArray_BuildIDXDB(((0 < strIIQVerOverride.length()) ? strIIQVerOverride : Version.getVersion()));

            if ((null != lstIDXTbls) && (0 < lstIDXTbls.size())) {
                strRetMsgs = funcStrLst_CheckIDXTbls(lstIDXTbls);
                strRetMsgs.add("IDX check completed");
            }
        } catch(Exception e) {
            strRetMsgs.add("IDX check exception thrown");
            log.error("IDX check exception: " +e.toString());
            e.printStackTrace();
        }

        String[] tmpArray = (String[]) strRetMsgs.toArray(new String[strRetMsgs.size()]);
        return Arrays.toString(tmpArray);
    }

}
