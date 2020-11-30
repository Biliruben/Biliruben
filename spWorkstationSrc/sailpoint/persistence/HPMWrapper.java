package sailpoint.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.hql.QueryTranslator;
import org.hibernate.hql.classic.ClassicQueryTranslatorFactory;

import sailpoint.api.SailPointContext;
import sailpoint.object.QueryOptions;
import sailpoint.object.SailPointObject;
import sailpoint.tools.GeneralException;

public class HPMWrapper extends HibernatePersistenceManager {

    public HPMWrapper(SailPointContext context) {
        super();
        Session session = HibernatePersistenceManager.getSession(context);
        this.setSessionFactory(session.getSessionFactory());
        this.getSession();
    }
    public String getQuery(SailPointContext context, Class<? extends SailPointObject> cls, QueryOptions opts, List<String> properties, boolean includeBindingValues) throws GeneralException {

        Session session = this.getSession();
        SessionFactory factory = session.getSessionFactory();
        HQLFilterVisitor v = visitHQLFilter(cls, opts, properties);
        String hql = v.getQueryString();
        ClassicQueryTranslatorFactory classicFactory = new ClassicQueryTranslatorFactory();
        QueryTranslator queryTranslator = classicFactory.createQueryTranslator(hql, hql, Collections.EMPTY_MAP, (SessionFactoryImplementor)factory);
        queryTranslator.compile(v.getParameterMap(), false);
        String queryString = queryTranslator.getSQLString();
        if (includeBindingValues) {
            // queryString still has ?
            // stolen from GlueSQL
            // ...there's gotta be a better way to do this... Probably using jdbc
            Pattern questionablePattern = Pattern.compile("(.*?)\\?(.*)");
            StringBuilder buff = new StringBuilder();
            int limit = v.getParameterMap().size();
            for (int i = 0; i < limit; i++) {
                String bindVar = "" + v.getParameterMap().get("param" + i);
                Matcher m = questionablePattern.matcher(queryString);
                if (m.matches()) {
                    buff.append(m.group(1)).append("'").append(bindVar).append("'").append(m.group(2));
                    queryString = buff.toString();
                    buff = new StringBuilder();
                }
            }
        }

        return queryString;
    }
}