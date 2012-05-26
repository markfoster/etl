import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.logging.*;

public class HibernateUtil {
 
    private static Log log = LogFactory.getLog(HibernateUtil.class);
    private static HashMap<String, SessionFactory> sessionFactoryMap = new HashMap<String, SessionFactory>();
 
    public static final ThreadLocal sessionMapsThreadLocal = new ThreadLocal();
 
    public static Session currentSession(String key) throws HibernateException {
        HashMap<String, Session> sessionMaps = (HashMap<String, Session>) sessionMapsThreadLocal.get();
        if (sessionMaps == null) {
            sessionMaps = new HashMap();
            sessionMapsThreadLocal.set(sessionMaps);
        }
        // Open a new Session, if this Thread has none yet
        Session s = (Session) sessionMaps.get(key);
        if (s == null) {
            s = ((SessionFactory)sessionFactoryMap.get(key)).openSession();
            sessionMaps.put(key, s);
        }
        return s;
    }

    public static Session currentSession() throws HibernateException {
        return currentSession("");
    }
 
    public static void closeSessions() throws HibernateException {
        HashMap<String, Session> sessionMaps = (HashMap<String, Session>) sessionMapsThreadLocal.get();
        sessionMapsThreadLocal.set(null);
        if (sessionMaps != null) {
            for (Session session : sessionMaps.values()) {
                if (session.isOpen())
                    session.close();
            };
        }
    }
 
    public static void closeSession() {
        HashMap<String, Session> sessionMaps = (HashMap<String, Session>) sessionMapsThreadLocal.get();
        sessionMapsThreadLocal.set(null);
        if (sessionMaps != null) {
            Session session = sessionMaps.get("");
            if (session != null && session.isOpen())
                session.close();
        }
    }
 
    public static void buildSessionFactories(HashMap<String, String> configs) {
        try {
            // Create the SessionFactory
            for (String key : configs.keySet()) {
                URL url = HibernateUtil.class.getResource(configs.get(key));
                SessionFactory sessionFactory = new Configuration().configure(url).buildSessionFactory();
                sessionFactoryMap.put(key, sessionFactory);
            }
 
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
            log.error("Initial SessionFactory creation failed.", ex);
            throw new ExceptionInInitializerError(ex);
 
        } // end of the try - catch block
    }
 
    public static void buildSessionFactory(String key, String path) {
        try {
            // Create the SessionFactory
            URL url = HibernateUtil.class.getResource(path);
            SessionFactory sessionFactory = new Configuration().configure(url).buildSessionFactory();
            sessionFactoryMap.put(key, sessionFactory);
        } catch (Throwable ex) {
            log.error("Initial SessionFactory creation failed.", ex);
            throw new ExceptionInInitializerError(ex);
        } // end of the try - catch block
    }

    public static void buildSessionFactory() {
        String key = "";
        try {
            // Create the SessionFactory
            SessionFactory sessionFactory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
            sessionFactoryMap.put(key, sessionFactory);
        } catch (Throwable ex) {
            log.error("Initial SessionFactory creation failed.", ex);
            throw new ExceptionInInitializerError(ex);
        } // end of the try - catch block
    }
 
} // end of the class 
