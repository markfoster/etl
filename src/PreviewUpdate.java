import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.hibernate.*;
import org.hibernate.stat.*;
import javax.sql.DataSource;
import java.util.*;
import java.math.*;
import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;
import net.sf.beanlib.hibernate.*;
import org.apache.commons.beanutils.*;

import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * @author Mark
 * 
 */
public class PreviewUpdate extends DeltaUpdate {

    Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * 
     */
    public void init() {
	HibernateUtil.buildSessionFactory("preview_delta", "hibernate.cfg.xml");
	HibernateUtil.buildSessionFactory("preview_pp", "hib.cfg.prev_pp.xml");
	HibernateUtil.buildSessionFactory("production_delta", "hib.cfg.prod_delta.xml");
    }

    /**
     * 
     */
    public void run() {
	ETLContext eContext = ETLContext.getContext();
	Map aMap = eContext.getAuditMap();

	// process each of the entities for possible actions
	Set entities = aMap.keySet();
	Iterator i = entities.iterator();
	while (i.hasNext()) {
	    String entity = (String) i.next();
	    Map actions = (Map) aMap.get(entity);
	    // if active then update the profile databases...
            ProcessState.setEntityRunId(entity, 0);
	    if (actions.get("active") != null) {
		try {
                    String eState = ProcessState.getEntityState(entity);

                    ProcessState.setEntityRunId(entity, ProcessState.getRunId());

                    if (entity.equals(Entity.OUTCOME) && eState.equals(ProcessState.STATE_FULL)) {
                        // get a quick count of the items in the entity table
                        int iQuickCount = getDeltaCount(entity);
                        optimiseDataLoad(entity, iQuickCount);
                        continue;
                    }

                    if (entity.equals(Entity.VISIT_DATE) && eState.equals(ProcessState.STATE_FULL)) {
                        // get a quick count of the items in the entity table
                        int iQuickCount = getDeltaCount(entity);
                        optimiseDataLoad(entity, iQuickCount);
                        continue;
                    }

		    if (entity.equals(Entity.PROVIDER) || entity.equals(Entity.LOCATION)) {
			updateGeocoding(entity);
			updateProductionDelta(entity);
			continue;
		    }
		    updatePreviewProfile(entity);
		    updateProductionDelta(entity);

		    // if we are processing a FULL upload then cleanup...
		    //if (eState.equals(ProcessState.STATE_FULL)) {
		    //    updatePreviewCleanup(entity, ProcessState.getEntityUniqueId(entity));
		    //}
		} catch (Exception ex) {
		    logger.error("Preview Load error", ex);
		    WatchDog.log(WatchDog.WATCHDOG_ENV_PREV, "previewload", ex.getMessage(), WatchDog.WATCHDOG_WARNING);

		}
	    }
	}
    }

    /**
     * 
     */
    public void cleanup() {
	String[] tables = { "chapter", "location_condition", "location_regulated_activity", "nominated_individual", "outcome", "partner", "provider_condition",
	        "provider_regulated_activity", "registered_manager", "registered_manager_condition", "report_summary", "service_type", "service_user_band", "visit_date" };
	try {
	    ApplicationContext context = SpringUtil.getApplicationContext();
	    JdbcTemplate jt = new JdbcTemplate();
	    jt.setDataSource((DataSource) context.getBean("preview-delta"));
	    for (String table : tables) {
		try {
		    jt.execute("TRUNCATE TABLE " + table);
		} catch (Exception ex) {
		    WatchDog.log(WatchDog.WATCHDOG_ENV_PREV, "cleanup", String.format("Cannot truncate table : %s", table), WatchDog.WATCHDOG_WARNING);
		}
	    }
	} catch (Exception ex) {
	    WatchDog.log(WatchDog.WATCHDOG_ENV_PREV, "cleanup", String.format("Problem with the delete table truncation: %s", ex.getMessage()), WatchDog.WATCHDOG_WARNING);
	}
    }

    /**
     * 
     * @param entity
     * @param uid
     */
    public void updatePreviewCleanup(String entity, String uid) {
	logger.info(String.format("updatePreviewCleanup: %s, '%s'", entity, uid));
	try {
	    ApplicationContext context = SpringUtil.getApplicationContext();
	    JdbcTemplate jt = new JdbcTemplate();
	    jt.setDataSource((DataSource) context.getBean("preview-pp"));
	    String sql = String.format("DELETE FROM %s WHERE last_updated != ?", entity.toLowerCase());
	    logger.info("updatePreviewCleanup: SQL = " + sql);
	    int rows = jt.update(sql, new Object[] { uid });
            WatchDog.log(WatchDog.WATCHDOG_ENV_PREV, "previewppload",
                String.format("%s... Wiped: %d", entity, rows), WatchDog.WATCHDOG_INFO);
	} catch (Exception ex) {
	    WatchDog.log(WatchDog.WATCHDOG_ENV_PREV, "fullcleanup", String.format("Problem with the delete table: %s", ex.getMessage()), WatchDog.WATCHDOG_WARNING);
	}
    }

    /**
     * 
     * @param entity
     */
    public void updatePreviewProfile(String entity) {

	WatchDog.log(WatchDog.WATCHDOG_ENV_PREV, "previewppload", String.format("Updating Preview PP for %s", entity), WatchDog.WATCHDOG_INFO);

	Session s_delta = HibernateUtil.currentSession("preview_delta").getSession(EntityMode.POJO);
	Session s_pp    = HibernateUtil.currentSession("preview_pp");

	// Statistics stats =
	// HibernateUtil.getSessionFactory("preview_pp").getStatistics();
	// stats.setStatisticsEnabled(true);

	int iInserts = 0, iDeletes = 0, iUpdates = 0;

        // get a quick count of the items in the entity table
        int iQuickCount = getDeltaCount(entity); 
        if (iQuickCount > Entity.MAX_ALLOWED) {
            logger.warn("Items are >  " + Entity.MAX_ALLOWED + " optimising load");
            optimiseDataLoad(entity, iQuickCount);
            return;
        }

	SQLQuery squery = s_delta.createSQLQuery("SELECT * FROM " + entity.toLowerCase());
	squery.addEntity(entity);
	logger.info("Query = " + squery);
	// Query q = s_delta.createQuery("FROM " + entity);
	// logger.info("Query = " + q);
	try {
	    List results = squery.list();
	    int iTotal = results.size(), iCount = 0;
	    logger.info("Processing " + iTotal + " records.");

	    Transaction tx = null;

	    for (int i = 0; i < results.size(); i++) {
		String action = "";

		if (tx == null)
		    tx = s_pp.beginTransaction();

		Object previewObject = results.get(i);
                String pk = "";
		if (previewObject instanceof CQC_Entity) {
		    action = ((CQC_Entity) previewObject).getActionCode().toString();
                    pk = ((CQC_Entity) previewObject).getPK();
		} else {
		    logger.warn("Unknown object in result set");
		}
		int iDelta = 1;
		iCount++;
		if (iTotal > 10000)
		    iDelta = 1000;
		else if (iTotal > 1000)
		    iDelta = 200;
		if (iCount % iDelta == 0) {
		    logger.info(String.format("-> Processing item %d / %d", iCount, iTotal));
		}
		try {
		    if (action.equals("D")) {
			iDeletes++;
                        WatchDog.log(WatchDog.WATCHDOG_ENV_PREV, "previewppload",
                                    String.format("Deleting %s, %s", entity, pk), WatchDog.WATCHDOG_WARNING);
			s_pp.delete(previewObject);
		    } else if (action.equals("I")) {
			iInserts++;
			s_pp.saveOrUpdate(previewObject);
		    } else if (action.equals("U")) {
			iUpdates++;
			s_pp.saveOrUpdate(previewObject);
		    }

		    if (tx != null && iTotal < 100) {
			tx.commit();
			tx = null;
		    }
		    if (tx != null && iCount % 100 == 0) {
			tx.commit();
			tx = null;
		    }

		} catch (Exception ex) {
		    logger.error(String.format("updatePreviewProfile: %s", entity), ex);
		}
	    }

	    if (tx != null)
		tx.commit();

	} catch (Exception ex) {
	    logger.error(String.format("updatePreviewProfile: %s", entity), ex);
	    WatchDog.log(WatchDog.WATCHDOG_ENV_PREV, "previewppload", String.format("Exception detected: %s", ex.getMessage()), WatchDog.WATCHDOG_EMERG);
	} finally {
	    logger.warn("Closing sessions...");
	    HibernateUtil.closeSession("preview_delta");
	    HibernateUtil.closeSession("preview_pp");
	}

	WatchDog.log(WatchDog.WATCHDOG_ENV_PREV, "previewppload",
	        String.format("%s... Deleted: %d/%d, Updated: %d/%d, Inserted: %d/%d", entity, iDeletes, iDeletes, iUpdates, iUpdates, iInserts, iInserts), WatchDog.WATCHDOG_INFO);
    }

    /**
    * 
    */
    public void updateProductionDelta(String entity) throws Exception {

	WatchDog.log(WatchDog.WATCHDOG_ENV_PREV, "proddeltaload", String.format("Updating Production Delta for %s", entity), WatchDog.WATCHDOG_INFO);

	Session s_prev_delta = HibernateUtil.currentSession("preview_delta").getSession(EntityMode.POJO);
	Session s_prod_delta = HibernateUtil.currentSession("production_delta");

        // Process Deletes

	WatchDog.log(WatchDog.WATCHDOG_ENV_PROD, "proddeltaload", String.format("Processing DELETE records for %s", entity), WatchDog.WATCHDOG_DEBUG);

        Query query = s_prev_delta.createQuery("FROM " + entity + " WHERE action_code = 'D'");
        logger.info("Query = " + query);
        List results = query.list();

        int iTotal = results.size(), iCount = 0;
        logger.info("Processing " + iTotal + " records.");

        Transaction tx = null;
        
        for (int i = 0; i < iTotal; i++) {
        
            if (tx == null) {
                tx = s_prod_delta.beginTransaction();
            }
        
            int iDelta = 1;
            iCount++;
            if (iTotal > 10000)
                iDelta = 1000;
            else if (iTotal > 1000)
                iDelta = 50;
            if (iCount % iDelta == 0 || iCount == iTotal)
                logger.info(String.format(" -> Record %d / %d", iCount, iTotal));

            String action = "";
            Object previewObject = results.get(i);
            if (previewObject instanceof CQC_Entity) {
                action = ((CQC_Entity) previewObject).getActionCode().toString();
            } else {
                logger.warn("Unknown object in result set");
            }
            Object productionObject = null;
            String productionAction = "";
            try {
                Object test = BeanUtils.cloneBean(previewObject);
                productionObject = s_prod_delta.load(test.getClass(), (java.io.Serializable) test);
                if (productionObject instanceof CQC_Entity) {
                    productionAction = ((CQC_Entity) productionObject).getActionCode().toString();
                }
                // logger.info("Production delta object found, action code = " +
                // productionAction);
            } catch (Exception ex) {
                productionObject = null;
            }
            try {
                if (productionObject == null)
                    s_prod_delta.saveOrUpdate(previewObject);
                else {
                    if (!productionAction.equals("D")) {
                        ((CQC_Entity) productionObject).setActionCode('D');
                        s_prod_delta.saveOrUpdate(productionObject);
                    }
                }
                
                if (tx != null && iTotal < 100) {
                    tx.commit();
                    tx = null;
                }
                if (tx != null && iCount % 100 == 0) {
                    tx.commit();
                    tx = null;
                }
                
                
            } catch (Exception ex) {
                logger.error(String.format("updateProductionDelta: %s", entity), ex);
                productionObject = null;
            }
        }
        
        try {
            if (tx != null)
                tx.commit();
            logger.warn("Closing sessions...");
            HibernateUtil.closeSession("preview_delta");
            HibernateUtil.closeSession("production_delta");
        } catch (Exception ex) {
        }

        // Process Updates and Inserts

	WatchDog.log(WatchDog.WATCHDOG_ENV_PROD, "proddeltaload", String.format("Processing UPDATE and INSERT records for %s", entity), WatchDog.WATCHDOG_DEBUG);

	s_prev_delta = HibernateUtil.currentSession("preview_delta").getSession(EntityMode.POJO);
	s_prod_delta = HibernateUtil.currentSession("production_delta");

        // get a quick count of the items in the entity table
        int iQuickCount = getDeltaCount(entity);
        if (iQuickCount > Entity.MAX_ALLOWED) {
            logger.warn("Items are >  " + Entity.MAX_ALLOWED + " optimising load");
            return;
        }

	// query = s_prev_delta.createSQLQuery("FROM " + entity +
	// " WHERE action_code IN ('I', 'U') limit 1000");
	SQLQuery squery = s_prev_delta.createSQLQuery("SELECT * FROM " + entity.toLowerCase() + " WHERE action_code IN ('I', 'U')");
	squery.addEntity(entity);
	logger.info("Query = " + squery);
	results = squery.list();
	iTotal = results.size();
        iCount = 0;
	logger.info("Processing " + iTotal + " records.");

	tx = null;

	for (int i = 0; i < iTotal; i++) {

	    if (tx == null) {
		tx = s_prod_delta.beginTransaction();
	    }

	    int iDelta = 1;
	    iCount++;
	    if (iTotal > 10000)
		iDelta = 1000;
	    else if (iTotal > 1000)
		iDelta = 50;
	    if (iCount % iDelta == 0 || iCount == iTotal)
		logger.info(String.format(" -> Record %d / %d", iCount, iTotal));

	    String action = "";
	    Object previewObject = results.get(i);
	    if (previewObject instanceof CQC_Entity) {
		action = ((CQC_Entity) previewObject).getActionCode().toString();
		logger.debug(String.format("Found %s : %s", entity, ((CQC_Entity) previewObject).getPK()));
	    } else {
		logger.warn("Unknown object in result set");
	    }
	    Object productionObject = null;
	    String productionAction = "";
	    String changeAction = "I";
	    try {
		Object test = BeanUtils.cloneBean(previewObject);
		productionObject = s_prod_delta.load(test.getClass(), (java.io.Serializable) test);
		if (productionObject instanceof CQC_Entity) {
		    productionAction = ((CQC_Entity) productionObject).getActionCode().toString();
		    changeAction = "U";
		}
		// logger.info("Production delta object found, action code = " +
		// productionAction);
	    } catch (Exception ex) {
		productionObject = null;
	    }

	    // if (!action.equals(changeAction))
	    // logger.warn(String.format("updateProductionDelta: %s", entity) +
	    // " " +
	    // String.format("Update Action:%s, Change Action:%s", action,
	    // changeAction));

	    try {
		if (productionObject == null) {
		    s_prod_delta.saveOrUpdate(previewObject);
		    // s_prod_delta.merge(previewObject);
		} else {
                    String pk = ((CQC_Entity) previewObject).getPK();
		    if (action.equals("U") && productionAction.equals("I")) {
			((CQC_Entity) previewObject).setActionCode('I');
			s_prod_delta.merge(previewObject);
			logger.info(String.format("updateProductionDelta: %s", entity) + " " + String.format("UA: %s, PA: %s", action, productionAction));
                        WatchDog.log(WatchDog.WATCHDOG_ENV_PREV, "proddeltaload",
                                    String.format("Changing action for delta from U to I for %s", pk), WatchDog.WATCHDOG_WARNING);

		    } else if (action.equals("D") && productionAction.equals("I")) {
			logger.warn(String.format("updateProductionDelta: %s", entity) + " " + String.format("UA: %s, PA: %s", action, productionAction));
			logger.info("Deleting Production Delta record: " + pk);
                        WatchDog.log(WatchDog.WATCHDOG_ENV_PREV, "proddeltaload",
                                    String.format("Deleting delta record: %s", pk), WatchDog.WATCHDOG_WARNING);
			s_prod_delta.delete(productionObject);
		    } else {
			s_prod_delta.merge(previewObject);
		    }
		}

		if (tx != null && iTotal < 100) {
		    tx.commit();
		    tx = null;
		}
		if (tx != null && iCount % 100 == 0) {
		    tx.commit();
		    tx = null;
		}

	    } catch (Exception ex) {
		logger.error(String.format("updateProductionDelta: %s", entity), ex);
		productionObject = null;
	    }

	}

	try {
	    if (tx != null)
		tx.commit();
	    logger.warn("Closing sessions...");
	    HibernateUtil.closeSession("preview_delta");
	    HibernateUtil.closeSession("production_delta");
	} catch (Exception ex) {
	}

    }

    /**
     * Use MySQLDump and MySQL load to optimise large entity loads
     */
    public void optimiseDataLoad(String entity, int count) {
       int iDeletes = 0, iUpdates = 0, iInserts = count;
    
       try {
       	   ApplicationContext context = SpringUtil.getApplicationContext();
       	   DriverManagerDataSource ds = (DriverManagerDataSource)context.getBean("preview-delta");
       	   String user = ds.getUsername();
       	   String pass = ds.getPassword();
       	   String url  = ds.getUrl();
       	   String host = "";
       	   try { host = new java.net.URI(url.substring(5)).getHost(); } catch (Exception ex) {}

       	   String table = entity.toLowerCase();
       	   String date = new java.text.SimpleDateFormat("yyyyMMdd_hhmmss").format(new java.util.Date());
       	   String file = String.format("%s_%s.sql", table, date);
       	   String cmd = String.format("/usr/bin/mysqldump --no-create-info --compact --host=%s --user=%s --password=%s %s %s > /var/tmp/%s",
       	                  host, user, pass, "preview_delta", table, file);
       	   logger.info(cmd);
       	   ETLContext.getContext().runExecCmd(cmd);
       	   logger.info("complete");

       	   // update the preview_pp database
       	   cmd = String.format("/usr/bin/mysql --host=%s --user=%s --password=%s %s -e \"TRUNCATE %s;\"",
       	                  host, user, pass, "preview_pp", table);
       	   logger.info(cmd);
       	   ETLContext.getContext().runExecCmd(cmd);
       	   cmd = String.format("/usr/bin/mysql --host=%s --user=%s --password=%s %s < /var/tmp/%s",
       	                  host, user, pass, "preview_pp", file);
       	   logger.info(cmd);
       	   ETLContext.getContext().runExecCmd(cmd);
       	   logger.info("complete");

       	   // update the production_delta database
       	   cmd = String.format("/usr/bin/mysql --host=%s --user=%s --password=%s %s -e \"TRUNCATE %s;\"",
       	                  host, user, pass, "production_delta", table);
       	   logger.info(cmd);
       	   ETLContext.getContext().runExecCmd(cmd);
       	   cmd = String.format("/usr/bin/mysql --host=%s --user=%s --password=%s %s < /var/tmp/%s",
       	                  host, user, pass, "production_delta", file);
       	   logger.info(cmd);
       	   ETLContext.getContext().runExecCmd(cmd);
       	   logger.info("complete");
       } catch (Exception ex) {
           WatchDog.log(WatchDog.WATCHDOG_ENV_PREV, "previewopload", String.format("Problem with the optimised load: %s",
                        ex.getMessage()), WatchDog.WATCHDOG_WARNING);
       }
 
       WatchDog.log(WatchDog.WATCHDOG_ENV_PREV, "previewppload",
                String.format("%s... Deleted: %d/%d, Updated: %d/%d, Inserted: %d/%d [OPTIMISED]", 
                entity, iDeletes, iDeletes, iUpdates, iUpdates, iInserts, iInserts), WatchDog.WATCHDOG_INFO);
    }

    /**
     * Use Spring to get a quick count of an entity table
     */
    private int getDeltaCount(String entity) {
        ApplicationContext context = SpringUtil.getApplicationContext();
        JdbcTemplate jt_delta = new JdbcTemplate();
        jt_delta.setDataSource((DataSource)context.getBean("preview-delta"));
        String sql = String.format("SELECT count(*) FROM %s", entity.toLowerCase());
        int count = jt_delta.queryForInt(sql);
	logger.info(String.format("Delta table count for %s = %d", entity, count));
        return count;
    }

    /**
     * Update all the empty Geocode values for Provider and Location entities
     */
    public boolean updateGeocoding(String entity) {
	if (!entity.equals(Entity.PROVIDER) && !entity.equals(Entity.LOCATION)) {
	    logger.info(String.format("updateGeocoding: Attempted to Geocode entity %s", entity));
	    return false;
	}
	ApplicationContext context = SpringUtil.getApplicationContext();
	JdbcTemplate jT_Common = new JdbcTemplate();
	jT_Common.setDataSource((DataSource) context.getBean("common"));

	JdbcTemplate jT_PreviewDelta = new JdbcTemplate();
	jT_PreviewDelta.setDataSource((DataSource) context.getBean("preview-delta"));

	String ent_table = entity.toLowerCase();

	String sql = String.format("SELECT %s, postcode FROM %s WHERE action_code IN ('I', 'U') AND postcode IS NOT NULL AND latitude IS NULL AND last_updated = '%s'",
	        (entity.equals(Entity.PROVIDER)) ? "provider_id" : "provider_id, location_id", ent_table, ProcessState.getEntityUniqueId(entity));

	/**
	 * String sql = "SELECT "; if (entity.equals(Entity.PROVIDER)) { sql +=
	 * "provider_id"; } else if (entity.equals(Entity.LOCATION)) { sql +=
	 * "provider_id, location_id"; } sql += ", postcode FROM " + ent_table +
	 * " WHERE action_code IN ('I', 'U')"; String uId =
	 * ProcessState.getEntityUniqueId(entity); sql +=
	 * " AND postcode IS NOT NULL AND last_updated = '" + uId + "'";
	 **/
	logger.info("Geocode SQL " + sql);
	List postcodes = jT_PreviewDelta.queryForList(sql);
	Iterator itr = postcodes.iterator();
	float[] latlng = { 0.0f, 0.0f };
	while (itr.hasNext()) {
	    Map element = (Map) itr.next();
	    String postcode = (String) element.get("postcode");
	    try {
		latlng = GeoCode.getAddress(postcode);
		int rows = 0;
		if (entity.equals(Entity.PROVIDER)) {
		    sql = "UPDATE " + ent_table + " SET latitude = ?, longitude = ? WHERE provider_id = ?";
		    rows = jT_PreviewDelta.update(sql, new Object[] { new BigDecimal(latlng[0]), new BigDecimal(latlng[1]), element.get("provider_id") });
                    if (latlng[0] == 0.0 || latlng[1] == 0.0)
                       WatchDog.log(WatchDog.WATCHDOG_ENV_PREV, "previewload", 
                                    String.format("Issue getting geocode for Provider %s", element.get("provider_id")), WatchDog.WATCHDOG_WARNING); 
		} else {
		    sql = "UPDATE " + ent_table + " SET latitude = ?, longitude = ? WHERE provider_id = ? AND location_id = ?";
		    rows = jT_PreviewDelta.update(sql,
			    new Object[] { new BigDecimal(latlng[0]), new BigDecimal(latlng[1]), element.get("provider_id"), element.get("location_id") });
                    if (latlng[0] == 0.0 || latlng[1] == 0.0)
                       WatchDog.log(WatchDog.WATCHDOG_ENV_PREV, "previewload", 
                                    String.format("Issue getting geocode for Location %s/%s", element.get("provider_id"), element.get("location_id")), WatchDog.WATCHDOG_WARNING); 
		}
	    } catch (Exception ex) {
		logger.error(String.format("updateGeocoding: %s", entity), ex);
	    }
	}
	return true;
    }

    /**
     * 
     */
    public void test() {
	HibernateUtil.buildSessionFactory("preview_delta", "hibernate.cfg.xml");
	HibernateUtil.buildSessionFactory("preview_pp", "hib.cfg.prev_pp.xml");
	HibernateUtil.buildSessionFactory("production_delta", "hib.cfg.prod_delta.xml");

	Session s_prev_delta = HibernateUtil.currentSession("preview_delta").getSession(EntityMode.POJO);
	Session s_prod_delta = HibernateUtil.currentSession("production_delta");

	Query q = s_prev_delta.createQuery("FROM Provider WHERE action_code IN ('I', 'U')");
	System.out.println("Query = " + q);
	List results = q.list();
	for (int i = 0; i < results.size(); i++) {
	    Object a = results.get(i);
	    if (a instanceof CQC_Entity) {
		System.out.println("Action code = " + ((CQC_Entity) a).getActionCode());
	    }
	    System.out.println(a);
	    System.out.println(a.getClass().getName());

	    System.out.println("Preview Delta Object:");
	    if (a instanceof Provider) {
		Provider p = (Provider) a;
		System.out.println("  > " + p.getEmail());
		System.out.println("  > " + p.getLastUpdated());
	    }

	    Object b = null;
	    try {
		Object test = BeanUtils.cloneBean(a);
		b = s_prod_delta.get(test.getClass(), (java.io.Serializable) test);
		System.out.println(" > CONTAINS " + (b != null));

		b = s_prod_delta.merge(a);
		System.out.println("1. Production Delta Object:");
		if (b instanceof Provider) {
		    Provider pSrc = (Provider) a;
		    Provider p = (Provider) b;
		    BeanUtils.copyProperties(pSrc, p);
		    p.setEmail(pSrc.getEmail());
		    System.out.println(" > " + p.getEmail());
		    System.out.println(" > " + p.getLastUpdated());
		}

		s_prod_delta.saveOrUpdate(b);
		s_prod_delta.flush();

		/**
		 * s_prod_delta.evict(b); s_prod_delta.lock(a, LockMode.NONE);
		 * s_prod_delta.refresh(a); b = null; //s_prod_delta.merge(a);
		 * s_prod_delta.flush(); s_prod_delta.saveOrUpdate(a);
		 * //Provider pp = HibernateBeanReplicator.dupEntityBean(a);
		 * s_prod_delta.flush();
		 */

		// b = s_prod_delta.load(a.getClass(), (java.io.Serializable)a);

		System.out.println("2. Production Delta Object:");
		if (b instanceof Provider) {
		    Provider p = (Provider) b;
		    System.out.println(" > " + p.getEmail());
		    System.out.println(" > " + p.getLastUpdated());
		}

		/**
		 * System.out.println("Found item in PP: " + b);
		 * System.out.println(b.getClass().getName()); if (b instanceof
		 * Service_Type) { Service_Type st = (Service_Type)b;
		 * System.out.println(st.getProviderId());
		 * System.out.println(st.getServiceTypeId());
		 * System.out.println(st.getActionCode()); }
		 **/
	    } catch (Exception ex) {
		ex.printStackTrace();
		System.out.println("object not found");
		b = null;
	    }
	    // Transaction tx = s_pp.beginTransaction();
	    // s_pp.delete(a);
	    // s_pp.saveOrUpdate(a);
	    // tx.commit();
	}

    }

    /**
     * Test method
     */
    public static void main(String[] args) {
	PreviewUpdate pu = new PreviewUpdate();
	// pu.geocode(Entity.PROVIDER);
	pu.test();
    }
}
