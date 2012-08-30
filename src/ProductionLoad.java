import java.text.*;

import javax.transaction.Synchronization;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;
import javax.sql.DataSource;

import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Mappings;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.KeyValue;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.metadata.*;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.EntityMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import org.apache.commons.beanutils.*;

import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 
 * @author Mark
 * 
 */
public class ProductionLoad {

    Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * 
     */
    public void init() {
	HibernateUtil.buildSessionFactory("production_delta", "hib.cfg.prod_delta.xml");
	HibernateUtil.buildSessionFactory("production_pp", "hib.cfg.prod_pp.xml");
    }

    /**
     * 
     */
    public boolean run() {
	ETLContext eContext = ETLContext.getContext();
	List entities = ProcessState.getEntitiesForUpdate();
        if (entities.size() == 0) return false;
	Iterator i = entities.iterator();
	while (i.hasNext()) {
	    String entity = (String) i.next();

            String eState = ProcessState.getEntityState(entity);

            if (entity.equals(Entity.OUTCOME) || entity.equals(Entity.VISIT_DATE)) {
                // get a quick count of the items in the entity table
                int iQuickCount = getDeltaCount(entity);
                if (eState.equals(ProcessState.STATE_FULL)) {
                    optimiseDataLoad(entity, iQuickCount);
                    continue;
                } else if (iQuickCount > Entity.MAX_ALLOWED) {
                    WatchDog.log(WatchDog.WATCHDOG_ENV_PREV, "prodload", 
                           "Error: Cannot load production data for delta load > " + iQuickCount,
                           WatchDog.WATCHDOG_WARNING);
                    backupExcessiveDataLoad(entity);
                    continue;
                }
            }

	    if (entity.equals(Entity.PROVIDER) || entity.equals(Entity.LOCATION)) {
		continue;
	    }
	    updateProductionProfile(entity);

	    // MSF: 24/07/12 - Bit of an issue here, as we need to check whether an entity is actually involved in the current batch
            //                 before clearing the tables down. The FULL state could have been left over from a previous run.
	    // if we are processing a FULL upload then cleanup...
	    //String eState = ProcessState.getEntityState(entity);
	    //if (eState.equals(ProcessState.STATE_FULL)) {
            //    updateProductionCleanup(entity, ProcessState.getEntityUniqueId(entity));
	    //    ProcessState.setEntityState(entity, ProcessState.STATE_DELTA);
	    //}
	}
        return true;
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
	    jt.setDataSource((DataSource) context.getBean("production-delta"));
	    for (String table : tables) {
		try {
		    jt.execute("TRUNCATE TABLE " + table);
		} catch (Exception ex) {
		    WatchDog.log(WatchDog.WATCHDOG_ENV_PREV, "cleanup", String.format("Cannot truncate table : %s %s", table, ex.getMessage()), WatchDog.WATCHDOG_WARNING);
		}
	    }
	} catch (Exception ex) {
	    WatchDog.log(WatchDog.WATCHDOG_ENV_PREV, "cleanup", String.format("Problem with the delete table truncation: %s", ex.getMessage()), WatchDog.WATCHDOG_WARNING);
	}
    }

    /**
     * 
     * @param entity
     */
    public void updateProductionProfile(String entity) {
	Session s_delta = HibernateUtil.currentSession("production_delta");
	Session s_pp = HibernateUtil.currentSession("production_pp");

	WatchDog.log(WatchDog.WATCHDOG_ENV_PROD, "prodppload", String.format("Updating Production PP for %s", entity), WatchDog.WATCHDOG_INFO);

	int iInserts = 0, iDeletes = 0, iUpdates = 0;

	Transaction tx = null;

        // get a quick count of the items in the entity table
        int iQuickCount = getDeltaCount(entity);
        if (iQuickCount > Entity.MAX_ALLOWED) {
            logger.warn("Items are >  " + Entity.MAX_ALLOWED + " optimising load");
            optimiseDataLoad(entity, iQuickCount);
            return;
        }

	Query q = s_delta.createQuery("FROM " + entity);
	logger.info("Query = " + q);
	List results = q.list();
	int iTotal = results.size(), iCount = 0;
	for (int i = 0; i < iTotal; i++) {

	    iCount++;

	    int iDelta = 1;
	    if (iTotal > 10000)
		iDelta = 500;
	    else if (iTotal > 1000)
		iDelta = 50;
	    if (i % iDelta == 0)
		logger.info(String.format(" -> Record %d / %d", iCount, iTotal));

	    if (tx == null)
		tx = s_pp.beginTransaction();

	    String action = "";
            String pk = "";
	    Object prodObject = results.get(i);
	    if (prodObject instanceof CQC_Entity) {
		action = ((CQC_Entity) prodObject).getActionCode().toString();
                pk = ((CQC_Entity) prodObject).getPK();
		logger.debug("Object (" + action + ") = " + ((CQC_Entity) prodObject).getPK());
	    } else {
		logger.warn("Unknown object in result set");
	    }
	    try {
		if (action.equals("D")) {
		    iDeletes++;
                    WatchDog.log(WatchDog.WATCHDOG_ENV_PREV, "prodppload",
                                 String.format("Deleting %s, %s", entity, pk), WatchDog.WATCHDOG_WARNING);
		    s_pp.delete(prodObject);
		} else if (action.equals("I")) {
		    iInserts++;
		    // s_pp.merge(prodObject);
		    s_pp.saveOrUpdate(prodObject);
		} else if (action.equals("U")) {
		    iUpdates++;
		    s_pp.saveOrUpdate(prodObject);
		}
	    } catch (Exception ex) {
		logger.error(String.format("updateProductionProfile: %s", entity), ex);
	    }

	    if (tx != null && iTotal < 100) {
		tx.commit();
		tx = null;
	    }
	    if (tx != null && iCount % 100 == 0) {
		tx.commit();
		tx = null;
	    }
	}

	if (tx != null) {
	    logger.warn("Performing final / drop-through commit");
	    tx.commit();
	}

	WatchDog.log(WatchDog.WATCHDOG_ENV_PROD, "prodppload",
	        String.format("%s... Deleted: %d/%d, Updated: %d/%d, Inserted: %d/%d", entity, iDeletes, iDeletes, iUpdates, iUpdates, iInserts, iInserts), WatchDog.WATCHDOG_INFO);
    }

    /**
     * 
     * @param entity
     * @param uid
     */
    public void updateProductionCleanup(String entity, String uid) {
	logger.warn(String.format("updateProductionCleanup: %s, '%s'", entity, uid));
	try {
	    ApplicationContext context = SpringUtil.getApplicationContext();
	    JdbcTemplate jt = new JdbcTemplate();
	    jt.setDataSource((DataSource) context.getBean("production-pp"));
	    String sql = String.format("DELETE FROM %s WHERE last_updated != ?", entity.toLowerCase());
	    logger.info("updateProductionCleanup: SQL = " + sql);
	    int rows = jt.update(sql, new Object[] { uid });
            WatchDog.log(WatchDog.WATCHDOG_ENV_PREV, "prodppload",
                String.format("%s... Wiped: %d", entity, rows), WatchDog.WATCHDOG_INFO);
	} catch (Exception ex) {
	    WatchDog.log(WatchDog.WATCHDOG_ENV_PREV, "fullcleanup", String.format("Problem with the delete table: %s", ex.getMessage()), WatchDog.WATCHDOG_WARNING);
	}
    }

    /**
     * Use Spring to get a quick count of an entity table
     */
    private int getDeltaCount(String entity) {
        ApplicationContext context = SpringUtil.getApplicationContext();
        JdbcTemplate jt_delta = new JdbcTemplate();
        jt_delta.setDataSource((DataSource)context.getBean("production-delta"));
        String sql = String.format("SELECT count(*) FROM %s", entity.toLowerCase());
        int count = jt_delta.queryForInt(sql);
        logger.info(String.format("Delta table count for %s = %d", entity, count));
        return count;
    }


    /**
     * Use MySQLDump and MySQL load to optimise large entity loads
     */
    public void optimiseDataLoad(String entity, int count) {
       int iDeletes = 0, iUpdates = 0, iInserts = count;

       try {
           ApplicationContext context = SpringUtil.getApplicationContext();
           DriverManagerDataSource ds = (DriverManagerDataSource)context.getBean("production-delta");
           String user = ds.getUsername();
           String pass = ds.getPassword();
           String url  = ds.getUrl();
           String host = "";
           try { host = new java.net.URI(url.substring(5)).getHost(); } catch (Exception ex) {}

           String table = entity.toLowerCase();
           String date = new java.text.SimpleDateFormat("yyyyMMdd_hhmmss").format(new java.util.Date());
           String file = String.format("%s_prod_%s.sql", table, date);
           String cmd = String.format("/usr/bin/mysqldump --no-create-info --compact --host=%s --user=%s --password=%s %s %s > /var/tmp/%s",
                          host, user, pass, "production_delta", table, file);
           logger.info(cmd);
           ETLContext.getContext().runExecCmd(cmd);
           logger.info("complete");

           // update the production_pp database
           cmd = String.format("/usr/bin/mysql --host=%s --user=%s --password=%s %s -e \"TRUNCATE %s;\"",
                          host, user, pass, "production_pp", table);
           logger.info(cmd);
           ETLContext.getContext().runExecCmd(cmd);
           cmd = String.format("/usr/bin/mysql --host=%s --user=%s --password=%s %s < /var/tmp/%s",
                          host, user, pass, "production_pp", file);
           logger.info(cmd);
           ETLContext.getContext().runExecCmd(cmd);
           logger.info("complete");

       } catch (Exception ex) {
           WatchDog.log(WatchDog.WATCHDOG_ENV_PREV, "prodopload", String.format("Problem with the optimised load: %s",
                        ex.getMessage()), WatchDog.WATCHDOG_WARNING);
       }

       WatchDog.log(WatchDog.WATCHDOG_ENV_PROD, "prodppload",
                String.format("%s... Deleted: %d/%d, Updated: %d/%d, Inserted: %d/%d [OPTIMISED]", 
                entity, iDeletes, iDeletes, iUpdates, iUpdates, iInserts, iInserts), WatchDog.WATCHDOG_INFO);
    }

    private void backupExcessiveDataLoad(String entity) {
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
           String file = String.format("%s_prod_err_%s.sql", table, date);
           String cmd = String.format("/usr/bin/mysqldump --no-create-info --compact --host=%s --user=%s --password=%s %s %s > /var/tmp/%s",
                          host, user, pass, "preview_delta", table, file);
           logger.info(cmd);
           ETLContext.getContext().runExecCmd(cmd);
           logger.info("complete");
       } catch (Exception ex) {
           WatchDog.log(WatchDog.WATCHDOG_ENV_PREV, "previewopload", String.format("Problem with the backup: %s",
                        ex.getMessage()), WatchDog.WATCHDOG_WARNING);
       }
    }

}
