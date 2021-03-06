import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.logging.*;

/**
 * 
 * @author Mark
 * 
 */
public class HibernateUtil {

    private static Log                             log                    = LogFactory.getLog(HibernateUtil.class);
    private static HashMap<String, SessionFactory> sessionFactoryMap      = new HashMap<String, SessionFactory>();

    public static final ThreadLocal                sessionMapsThreadLocal = new ThreadLocal();

    private static SessionFactory                  sessionFactory;
    private static Configuration                   configuration;

    static {
	try {
	    sessionFactory = new Configuration().configure().buildSessionFactory();
	} catch (Throwable ex) {
	    throw new ExceptionInInitializerError(ex);
	}
    }

    /**
     * 
     * @return
     */
    public static Configuration getConfiguration() {
	return configuration;
    }

    /**
     * 
     * @return
     */
    public static SessionFactory getSessionFactory() {
	return sessionFactory;
    }

    /**
     * 
     */
    public static void shutdown() {
	getSessionFactory().close();
    }

    /**
     * 
     * @param key
     * @return
     */
    public static SessionFactory getSessionFactory(String key) {
	return sessionFactoryMap.get(key);
    }

    /**
     * 
     * @param key
     * @return
     * @throws HibernateException
     */
    public static Session currentSession(String key) throws HibernateException {
	HashMap<String, Session> sessionMaps = (HashMap<String, Session>) sessionMapsThreadLocal.get();
	if (sessionMaps == null) {
	    sessionMaps = new HashMap();
	    sessionMapsThreadLocal.set(sessionMaps);
	}
	// Open a new Session, if this Thread has none yet
	Session s = (Session) sessionMaps.get(key);
	if (s == null) {
	    s = ((SessionFactory) sessionFactoryMap.get(key)).openSession();
	    sessionMaps.put(key, s);
	} else if (!s.isOpen()) {
	    s = ((SessionFactory) sessionFactoryMap.get(key)).openSession();
	    sessionMaps.put(key, s);
	}
	return s;
    }

    /**
     * 
     * @return
     * @throws HibernateException
     */
    public static Session currentSession() throws HibernateException {
	return currentSession("default");
    }

    /**
     * 
     * @throws HibernateException
     */
    public static void closeSessions() throws HibernateException {
	HashMap<String, Session> sessionMaps = (HashMap<String, Session>) sessionMapsThreadLocal.get();
	sessionMapsThreadLocal.set(null);
	if (sessionMaps != null) {
	    for (Session session : sessionMaps.values()) {
		if (session.isOpen())
		    session.close();
	    }
	    ;
	}
    }

    /**
     * 
     * @param key
     */
    public static void closeSession(String key) {
	HashMap<String, Session> sessionMaps = (HashMap<String, Session>) sessionMapsThreadLocal.get();
	sessionMapsThreadLocal.set(null);
	if (sessionMaps != null) {
	    Session session = sessionMaps.get(key);
	    if (session != null && session.isOpen())
		session.close();
	}
    }

    /**
     * 
     * @param configs
     */
    public static void buildSessionFactories(HashMap<String, String> configs) {
	try {
	    // Create the SessionFactory
	    for (String key : configs.keySet()) {
		URL url = HibernateUtil.class.getResource(configs.get(key));
		SessionFactory sessionFactory = new Configuration().configure(url).buildSessionFactory();
		sessionFactoryMap.put(key, sessionFactory);
	    }

	} catch (Exception ex) {
	    log.error("Initial SessionFactory creation failed.", ex);
	    throw new ExceptionInInitializerError(ex);

	} // end of the try - catch block
    }

    /**
     * 
     * @param key
     * @param path
     */
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

    /**
     * 
     */
    public static void buildSessionFactory() {
	String key = "default";
	try {
	    // Create the SessionFactory
	    configuration = new Configuration().configure("hibernate.cfg.xml");
	    SessionFactory sessionFactory = configuration.buildSessionFactory();
	    sessionFactoryMap.put(key, sessionFactory);
	} catch (Throwable ex) {
	    log.error("Initial SessionFactory creation failed.", ex);
	    throw new ExceptionInInitializerError(ex);
	} // end of the try - catch block
    }

} // end of the class
