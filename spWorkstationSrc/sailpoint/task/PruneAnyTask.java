package sailpoint.task;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.api.SailPointContext;
import sailpoint.object.Attributes;
import sailpoint.object.Filter;
import sailpoint.object.Partition;
import sailpoint.object.QueryOptions;
import sailpoint.object.Request;
import sailpoint.object.RequestDefinition;
import sailpoint.object.SailPointObject;
import sailpoint.object.TaskResult;
import sailpoint.object.TaskSchedule;
import sailpoint.tools.Compressor;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Message;
import sailpoint.tools.Util;

public class PruneAnyTask extends AbstractTaskExecutor {

    private static final Log log = LogFactory.getLog(PruneAnyTask.class);


    private static final int DEFAULT_PARTITION_SIZE = 1000;
    public static final String PRUNER_REQUEST_DEFINITION = "System Maintenance Pruner Partition";
    private static final String PARTITION_NAME_BASE = "Prune object partition: ";
    public static final String ARG_PARTITION_SIZE = "partitionSize";
    public static final String ARG_ID_LIST = "idList";
    public static final String RET_OBJS_DELETED = "objectsDeleted";
    public static final String CLASS_ID_DELIM = "::";


    private static final String ARG_PRUNE_CLASS = "pruneClass";
    private static final String ARG_PRUNE_FILTER = "pruneFilter";


    private int _partitionSize;

    private SailPointContext _context;
    private boolean _terminate;


    private Filter _pruneFilter;


    private String _pruneClass;

    @Override
    public void execute(SailPointContext context, TaskSchedule schedule, 
            TaskResult result, Attributes<String, Object> args)
            throws Exception {
        _context = context;
        init (args, result);

        if (result.hasErrors()) {
            return;
        }

        try {
            List<Partition> partitions = createPartitions(context);
            if (Util.size(partitions) == 0) {
                // didn't find anything
                result.setAttribute(RET_OBJS_DELETED, 0);
            } else {
                // generate partitions
                List<Request> requests = new ArrayList<Request>();
                RequestDefinition reqDef = getRequestDefinition(context);
                for (Partition partition : partitions) {
                    Request req = createRequestForPartition(partition, reqDef, args);
                    requests.add(req);
                }

                if (!_terminate) {
                    launchPartitions(context, result, requests);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.addMessage(new Message(Message.Type.Error, e.getMessage(), e));
            return;
        }

    }

    private RequestDefinition getRequestDefinition(SailPointContext context) throws GeneralException {
        RequestDefinition definition = context.getObjectByName(RequestDefinition.class, PRUNER_REQUEST_DEFINITION);
        if (definition == null) {
            throw new GeneralException("Could not find RequestDefinition: " + PRUNER_REQUEST_DEFINITION);
        }
        return definition;
    }

    private Request createRequestForPartition(Partition partition, RequestDefinition requestDef,
            Attributes<String,Object> args) throws GeneralException {
        Request request = new Request(requestDef);
        request.setName(partition.getName());
        // We need to copy the args; shallow should be fine
        request.setAttributes(new Attributes<String, Object>(args));
        request.put(ARG_ID_LIST, partition.getAttribute(ARG_ID_LIST));

        return request;
    }

    private List<Partition> createPartitions(SailPointContext context) throws GeneralException, ClassNotFoundException {
        List<String> objClassIds = new ArrayList<String>();
        // Each method adds a String representation of an object to delete in the form of
        // Class::ID. Those strings are then batched into Partition objects.
        addObjects (objClassIds);
        List<Partition> partitions = new ArrayList<Partition>();
        List<String> partitionIds = new ArrayList<String>();
        int count = 1;
        for (String objClassId : objClassIds){
            partitionIds.add(objClassId);
            count++;
            if (partitionIds.size() >= _partitionSize) {
                Partition partition = createPartition(partitionIds, count - partitionIds.size(), count - 1);
                partitions.add(partition);
                partitionIds = new ArrayList<String>();
            }
        }
        // don't forget the leftovers
        if (partitionIds.size() > 0) {
            Partition partition = createPartition(partitionIds, count - partitionIds.size(), count - 1);
            partitions.add(partition);
        }

        return partitions;
    }

    private Partition createPartition(List<String> partitionIds, int startingPos, int endingPos) throws GeneralException {
        Partition partition = new Partition();
        partition.setName(PARTITION_NAME_BASE + " " + startingPos + " to " + endingPos);
        partition.setSize(partitionIds.size());
        String compressedString = Compressor.compress(Util.listToCsv(partitionIds));
        partition.setAttribute(ARG_ID_LIST, compressedString);
        return partition;
    }

    private void addObjects(List<String> objClassIds) throws GeneralException, ClassNotFoundException {
        // first do the tasks with no expiration date and subject them
        // to the global maximum
        // then the ones with specific expirations
        QueryOptions opts = new QueryOptions();
        if (_pruneFilter != null) {
            opts.addFilter(_pruneFilter);
        }
        Class<SailPointObject> clazz = (Class<SailPointObject>) Class.forName(_pruneClass);
        addObjectIds(objClassIds, clazz, opts);
    }

    private <T extends SailPointObject> void addObjectIds(List<String> objClassIds, Class<T> clazz, QueryOptions ops) 
            throws GeneralException {
        Iterator<Object[]> results = _context.search(clazz, ops, "id");
        while (results.hasNext()) {
            String id = (String)results.next()[0];
            objClassIds.add(clazz.getName() + CLASS_ID_DELIM + id);
        }
    }

    private void init(Attributes<String, Object> args, TaskResult result) throws GeneralException {
        _pruneClass = args.getString(ARG_PRUNE_CLASS);
        String pruneFilterSrc = args.getString(ARG_PRUNE_FILTER);
        if (Util.isNotNullOrEmpty(pruneFilterSrc)) {
            _pruneFilter = Filter.compile(pruneFilterSrc);
        }
        _partitionSize = args.getInt(ARG_PARTITION_SIZE, DEFAULT_PARTITION_SIZE);
    }

    @Override
    public boolean terminate() {
        // TODO Auto-generated method stub
        return false;
    }

}
