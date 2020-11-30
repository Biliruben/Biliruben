package sailpoint.rule;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sailpoint.api.SailPointContext;
import sailpoint.object.Filter;
import sailpoint.object.QueryOptions;
import sailpoint.object.TaskResult;
import sailpoint.tools.GeneralException;

public class Rule_PartitionedAggResultReporter extends GenericRule {

    @Override
    public Object execute() throws Throwable {
        String AGG_SERVER = "Active_Directory";
        // Get all task results for partitioned aggregation
        List<TaskResult> aggResults = getTaskResults(context);
        StringBuilder buff = new StringBuilder();
        buff.append("agg task,task server,aggregation servers,check deleted server\n");

        // iterate through and extract the sub results
        for (TaskResult aggResult : aggResults) {
            String reportCsv = report(aggResult, AGG_SERVER);
            buff.append(reportCsv);
        }
        
        return buff;
    }
    
    private String report(TaskResult aggResult, String aggregatedApplication) {
        // report on which host launched the task, which host(s) ran the agg, and which host deleted
        List<TaskResult> partitionResults = aggResult.getPartitionResults();
        StringBuilder buff = new StringBuilder();
        buff.append(aggResult.getName()).append(",");
        buff.append(aggResult.getHost()).append(",");
        
        Set<String> aggServers = new HashSet<String>();
        String checkDeletedServer = null;
        
        String cdName = "Check Deleted Objects - " + aggregatedApplication;
        Pattern p = Pattern.compile(aggregatedApplication + " - Accounts");
        for (TaskResult partitionResult : partitionResults) {
            String name = partitionResult.getName();
            Matcher m = p.matcher(name);
            if (m.matches()) {
                // found an aggregation partition
                aggServers.add(partitionResult.getHost());
            } else {
                // not an aggregation partition. So is it a check deleted?
                if (cdName.equals(partitionResult.getName())) {
                    // but it is a CD result
                    checkDeletedServer = partitionResult.getHost();
                }
            }
        }
        buff.append(aggServers).append(",").append(checkDeletedServer).append("\n");
        return buff.toString();
    }
    
    private List<TaskResult> getTaskResults(SailPointContext context) throws GeneralException {
        QueryOptions opts = new QueryOptions();
        opts.add(Filter.eq("type", "AccountAggregation"));
        opts.add(Filter.eq("partitioned", true));
        List<TaskResult> results = context.getObjects(TaskResult.class, opts);
        return results;
    }

}
