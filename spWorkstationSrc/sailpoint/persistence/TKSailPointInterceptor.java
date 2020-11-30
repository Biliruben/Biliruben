package sailpoint.persistence;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.type.Type;

import sailpoint.object.Bundle;

public class TKSailPointInterceptor extends SailPointInterceptor {
    
    private Map<Serializable, Object> _bundles = new HashMap<Serializable, Object>();

    static private Log log = LogFactory.getLog(TKSailPointInterceptor.class);
    
    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state,
            String[] propertyNames, Type[] types) {
        testObject(entity, id);
        return super.onSave(entity, id, state, propertyNames, types);
    }
    
    @Override
    public boolean onLoad(Object entity, Serializable id, Object[] state,
            String[] propertyNames, Type[] types) {
        testObject(entity, id);
        return super.onLoad(entity, id, state, propertyNames, types);
    }
    
    private void testObject(Object entity, Serializable id) {
        if (entity instanceof Bundle) {
            Object o = _bundles.get(id);
            if (o != null && o != entity) {
                log.warn("Object mismatch occurred: " + id, new Exception());
            } else if (o == null) {
                log.warn("First save for: " + id, new Exception());
            }
            _bundles.put(id, o);
        }

    }
    

}
