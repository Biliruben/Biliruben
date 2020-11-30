package sailpoint.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import biliruben.threads.*;
import biliruben.threads.ThreadRunner.TRThread;

import sailpoint.api.Certificationer;
import sailpoint.api.IncrementalObjectIterator;
import sailpoint.api.SailPointContext;
import sailpoint.api.SailPointFactory;
import sailpoint.api.certification.CertificationDecisioner;
import sailpoint.object.Attributes;
import sailpoint.object.Certification;
import sailpoint.object.CertificationAction;
import sailpoint.object.CertificationItem;
import sailpoint.object.Filter;
import sailpoint.object.Identity;
import sailpoint.object.QueryOptions;
import sailpoint.object.TaskResult;
import sailpoint.object.TaskSchedule;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Util;

public class AutoRevokeCertifications extends AbstractTaskExecutor {

    private static final String ARG_THREADS = "threads";
    private static final String ARG_REVOKER = "revoker";
    private static final String ARG_SIGN = "sign";
    private static final String ARG_FILTER = "filter";
    private static final String ARG_PERCENT = "percent";
    
    private static class Revoker implements Runnable {
        
        private String _id;
        private String _revoker;
        private SailPointContext _ctx;
        private boolean _sign;
        private int _percent;

        Revoker(String certId, String user, boolean sign, int percent) {
            this._id = certId;
            this._revoker = user;
            this._sign = sign;
            this._percent = percent;
        }

        @Override
        public void run() {
            try {
                if (_ctx == null) {
                    _ctx = SailPointFactory.createContext(_revoker);
                }
                Certification c = _ctx.getObjectById(Certification.class, _id);
                QueryOptions ops = new QueryOptions();
                ops.add(Filter.eq("parent.certification.id", this._id));
                List<String> revokeditemIds = new ArrayList<String>();
                List<String> approvedItemIds = new ArrayList<String>();
                Iterator<Object[]> results = _ctx.search(CertificationItem.class, ops, "id");
                Random rando = new Random();
                while (results.hasNext()) {
                    int i = rando.nextInt(100);
                    if (i < this._percent) {
                        revokeditemIds.add((String)results.next()[0]);
                    } else {
                        approvedItemIds.add((String)results.next()[0]);
                    }
                }

                Identity me = _ctx.getObjectByName(Identity.class, _revoker);
                CertificationDecisioner decider = new CertificationDecisioner(_ctx, _id, me);
                CertificationDecisioner.Decision decision = new CertificationDecisioner.Decision(CertificationAction.Status.Remediated.name(), 
                        new CertificationDecisioner.SelectionCriteria(revokeditemIds));
                decider.decide(Arrays.asList(decision));
                _ctx.commitTransaction();
                decision = new CertificationDecisioner.Decision(CertificationAction.Status.Approved.name(),
                        new CertificationDecisioner.SelectionCriteria(approvedItemIds));
                decider.decide(Arrays.asList(decision));
                _ctx.commitTransaction();
                if (_sign && c.isComplete()) {
                    Certificationer signer = new Certificationer(_ctx);
                    c = _ctx.getObjectById(Certification.class, c.getId());
                    signer.sign(c, me);
                    _ctx.commitTransaction();
                    _ctx.decache();
                }
                SailPointFactory.releaseContext(_ctx);
            } catch (GeneralException e) {
                throw new RuntimeException(e);
            }
            
        }
    }

    @Override
    public void execute(SailPointContext context, TaskSchedule schedule,
            TaskResult result, Attributes<String, Object> args)
            throws Exception {
        // get a list of all open certifications
        QueryOptions opts = new QueryOptions();
        opts.add(Filter.eq("complete", false));
        String filter = args.getString(ARG_FILTER);
        if (!Util.isNullOrEmpty(filter)) {
            Filter f = Filter.compile(filter);
            opts.add(f);
        }
        opts.setDistinct(true);
        Iterator<Object[]> results = context.search(Certification.class, opts, "id");
        // iterate full list before processing; closes cursor
        List<String> certIds = new ArrayList<String>();
        while (results.hasNext()) {
            String id = (String) results.next()[0];
            certIds.add(id);
        }
        int percent = args.getInt(ARG_PERCENT, 100);
        int threads = args.getInt(ARG_THREADS, 4);
        boolean sign = args.getBoolean(ARG_SIGN, false);
        String revoker = args.getString(ARG_REVOKER);
        if (Util.isNullOrEmpty(revoker)) {
            revoker = "spadmin";
        }
        
        ThreadRunner tr = new ThreadRunner(threads);
        
        // process each certification, threadidly
        for (String certId : certIds) {
            Revoker r = new Revoker(certId, revoker, sign, percent);
            tr.add(r);
        }
        
        while (tr.hasWork()) {
            Thread.sleep(250);
        }
        
        tr.shutDown();

    }

    @Override
    public boolean terminate() {
        // TODO Auto-generated method stub
        return false;
    }

}
