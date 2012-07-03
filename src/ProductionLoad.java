import java.text.*;

import javax.transaction.Synchronization;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

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

public class ProductionLoad {

    Logger logger = Logger.getLogger(this.getClass().getName());

    public void init() {
        HibernateUtil.buildSessionFactory("production_delta", "hib.cfg.prod_delta.xml");
        HibernateUtil.buildSessionFactory("production_pp",    "hib.cfg.prod_delta.xml");
    }

    public void run() {
        ETLContext eContext = ETLContext.getContext();
        List entities = ProcessState.getEntitiesForUpdate();
        Iterator i = entities.iterator();
        while (i.hasNext()) {
           String entity = (String)i.next();
           if (entity.equals(Entity.PROVIDER) || entity.equals(Entity.LOCATION)) {
                   continue;
           }
           updateProductionProfile(entity);

           // if we are processing a FULL upload then cleanup...
           String eState = ProcessState.getEntityState(entity);
           if (eState.equals(ProcessState.STATE_FULL)) {
               updateProductionCleanup(entity, ProcessState.getEntityUniqueId(entity));
           }
        }
    }

    public void updateProductionProfile(String entity) {
        Session s_delta = HibernateUtil.currentSession("production_delta");
        Session s_pp    = HibernateUtil.currentSession("production_pp");

        WatchDog.log(WatchDog.WATCHDOG_ENV_PROD, "prodppload",
                     String.format("Updating Production PP for %s", entity), WatchDog.WATCHDOG_INFO);

        int iInserts = 0, iDeletes = 0, iUpdates = 0;

        Query q = s_delta.createQuery("FROM " + entity);
        logger.info("Query = " + q);
        List results = q.list();
        for (int i = 0; i < results.size(); i++) {
             String action = "";
             Object prodObject = results.get(i);
             if (prodObject instanceof CQC_Entity) {
                 action = ((CQC_Entity)prodObject).getActionCode().toString();
             } else {
                 logger.warn("Unknown object in result set");
             }
             try {
                Transaction tx = s_pp.beginTransaction();
                if  (action.equals("D")) {
                    iDeletes++;
                    s_pp.delete(prodObject);
                } else if (action.equals("I")) {
                    iInserts++;
                    s_pp.saveOrUpdate(prodObject);
                } else if (action.equals("U")) {
                    iUpdates++;
                    s_pp.saveOrUpdate(prodObject);
                }
                tx.commit();
             } catch (Exception ex) {
                 logger.error(String.format("updateProductionProfile: %s", entity), ex);
             }
        }
        WatchDog.log(WatchDog.WATCHDOG_ENV_PROD, "prodppload",
                     String.format("%s... Deleted: %d, Updated: %d, Inserted: %d", entity, iDeletes, iUpdates, iInserts),
                     WatchDog.WATCHDOG_INFO);
    }

    public void updateProductionCleanup(String entity, String uid) {
        logger.warn(String.format("updateProductionCleanup: %s, '%s' - TO DO", entity, uid));
    }
}
