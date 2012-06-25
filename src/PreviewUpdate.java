import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.hibernate.*;
import javax.sql.DataSource;
import java.util.*;
import java.math.*;
import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;

public class PreviewUpdate extends DeltaUpdate {

    Logger logger = Logger.getLogger(this.getClass().getName()); 

    public void init() {
        HibernateUtil.buildSessionFactory("preview_delta",    "hibernate.cfg.xml");
        HibernateUtil.buildSessionFactory("preview_pp",       "hib.cfg.prev_pp.xml");
        HibernateUtil.buildSessionFactory("production_delta", "hib.cfg.prod_delta.xml");
    }
    
    public void run() {
        ETLContext eContext = ETLContext.getContext();
        Map aMap = eContext.getAuditMap();

        // process each of the entities for possible actions
        Set entities = aMap.keySet();
        Iterator i = entities.iterator();
        while (i.hasNext()) {
           String entity = (String)i.next();
           Map actions = (Map)aMap.get(entity);
           // if active then update the profile databases...
           if (actions.get("active") != null) {
               if (entity.equals(Entity.PROVIDER) || entity.equals(Entity.LOCATION)) {
                   updateGeocoding(entity);
                   updateProductionDelta(entity);
                   continue;
               }
               updatePreviewProfile(entity);
               updateProductionDelta(entity);
           }
        }
    }

    public void updatePreviewProfile(String entity) {
        Session s_delta = HibernateUtil.currentSession("preview_delta").getSession(EntityMode.POJO);
        Session s_pp    = HibernateUtil.currentSession("preview_pp");

        Query q = s_delta.createQuery("FROM " + entity);
        logger.info("Query = " + q);
        List results = q.list();
        for (int i = 0; i < results.size(); i++) {
             String action = "";
             Object previewObject = results.get(i);
             if (previewObject instanceof CQC_Entity) {
                 action = ((CQC_Entity)previewObject).getActionCode().toString();
             } else {
                 logger.warn("Unknown object in result set");
             }
             logger.info(previewObject);
             logger.info(previewObject.getClass().getName());
             try {
                logger.info("Action = " + action);
                Transaction tx = s_pp.beginTransaction();
                if (action.equals("D")) {
                    s_pp.delete(previewObject);
                } else {
                    s_pp.saveOrUpdate(previewObject);
                }
                tx.commit();
             } catch (Exception ex) {
                 logger.error(String.format("updatePreviewProfile: %s", entity), ex);
             }
        }
    }

    public void updateProductionDelta(String entity) {
        logger.info(String.format("Running Production Delta update for %s", entity));

        Session s_prev_delta = HibernateUtil.currentSession("preview_delta").getSession(EntityMode.POJO);
        Session s_prod_delta = HibernateUtil.currentSession("production_delta");

        logger.info(String.format("Processing DELETE records for %s", entity));

        Query query = s_prev_delta.createQuery("FROM " + entity + " WHERE action_code = 'D'");
        logger.info("Query = " + query);
        List results = query.list();
        for (int i = 0; i < results.size(); i++) {
             String action = "";
             Object previewObject  = results.get(i);
             if (previewObject instanceof CQC_Entity) {
                 action = ((CQC_Entity)previewObject).getActionCode().toString();
             } else {
                 logger.warn("Unknown object in result set");
             }
             Object productionObject = null;
             String productionAction = "";
             try {
                productionObject = s_prod_delta.load(previewObject.getClass(), (java.io.Serializable)previewObject);
                if (productionObject instanceof CQC_Entity) {
                    productionAction = ((CQC_Entity)productionObject).getActionCode().toString();
                }
                logger.info("Production delta object found, action code = " + productionAction);
             } catch (Exception ex) {
                productionObject = null;
             }
             try {
                Transaction tx = s_prod_delta.beginTransaction();
                if (productionObject == null)
                    s_prod_delta.saveOrUpdate(previewObject);
                else {
                    if (!productionAction.equals("D")) {
                        ((CQC_Entity)productionObject).setActionCode('D');
                        s_prod_delta.saveOrUpdate(productionObject);
                    }
                }
                tx.commit();
             } catch (Exception ex) {
                 logger.error(String.format("updateProductionDelta: %s", entity), ex);
                 productionObject = null;
             }
        }

        logger.info(String.format("Processing UPDATE and INSERT records for %s", entity));

        query = s_prev_delta.createQuery("FROM " + entity + " WHERE action_code IN ('I', 'U')");
        logger.info("Query = " + query);
        results = query.list();
        for (int i = 0; i < results.size(); i++) {
             String action = "";
             Object previewObject  = results.get(i);
             if (previewObject instanceof CQC_Entity) {
                 action = ((CQC_Entity)previewObject).getActionCode().toString();
             } else {
                 logger.warn("Unknown object in result set");
             }
             Object productionObject = null;
             String productionAction = "";
             String changeAction = "I";
             try {
                productionObject = s_prod_delta.load(previewObject.getClass(), (java.io.Serializable)previewObject);
                if (productionObject instanceof CQC_Entity) {
                    productionAction = ((CQC_Entity)productionObject).getActionCode().toString();
                    changeAction = "U";
                }
                logger.info("Production delta object found, action code = " + productionAction);
             } catch (Exception ex) {
                productionObject = null;
             }

             if (!action.equals(changeAction))
                 logger.warn(String.format("updateProductionDelta:%s", entity) + " " + 
                             String.format("Update Action:%s, Change Action:%s", action, changeAction));

             try {
                Transaction tx = s_prod_delta.beginTransaction();
                if (productionObject == null)
                    s_prod_delta.saveOrUpdate(previewObject);
                else {
                    if (productionAction.equals("D") || productionAction.equals("I") ) {
                        ((CQC_Entity)productionObject).setActionCode('U');
                        s_prod_delta.saveOrUpdate(productionObject);
                    } else if (action.equals("U") && productionAction.equals("I") ) {
                        ((CQC_Entity)previewObject).setActionCode('I');
                        s_prod_delta.saveOrUpdate(previewObject);
                    } else {
                        s_prod_delta.saveOrUpdate(previewObject);
                    }
                }
                tx.commit();
             } catch (Exception ex) {
                 logger.error(String.format("updateProductionDelta:%s", entity), ex);
                 productionObject = null;
             }
        }
    }

    /**
     * Update all the empty Geocode values for Provider and Location entities
     */
    public static boolean updateGeocoding(String entity) {
        ApplicationContext context = SpringUtil.getApplicationContext();
        JdbcTemplate jT_Common = new JdbcTemplate();
        jT_Common.setDataSource((DataSource)context.getBean("common"));

        JdbcTemplate jT_PreviewDelta = new JdbcTemplate();
        jT_PreviewDelta.setDataSource((DataSource)context.getBean("preview-delta"));

        String ent_table = entity.toLowerCase();
        String sql = "SELECT ";
        if        (entity.equals(Entity.PROVIDER)) {
           sql += "provider_id";
        } else if (entity.equals(Entity.LOCATION)) {
           sql += "provider_id, location_id";
        }
        sql += ", postcode FROM " + ent_table + " WHERE action_code IN ('I', 'U')";
        String uId = ProcessState.getEntityUniqueId(entity); 
        sql += " AND postcode IS NOT NULL AND last_updated = '" + uId + "'";
        List postcodes = jT_PreviewDelta.queryForList(sql); 
        //System.out.println(postcodes);
        Iterator itr = postcodes.iterator(); 
        float[] latlng = { 0.0f, 0.0f };
        while (itr.hasNext()) {
             Map element = (Map)itr.next(); 
             //System.out.println("PC = " + element.get("postcode") + " : " + element + " : " + element.getClass().getName());
             String postcode = (String)element.get("postcode");
             latlng = GeoCode.getAddress(postcode);
             sql = "UPDATE " + ent_table + " SET latitude = ?, longitude = ? WHERE provider_id = ?";
             int rows = 0;
             jT_PreviewDelta.update(sql, new Object[] {new BigDecimal(latlng[0]), new BigDecimal(latlng[1]), element.get("provider_id")} );
        }  
        return true;
    }

    public void test() {
        HibernateUtil.buildSessionFactory("preview_delta", "hibernate.cfg.xml");
        HibernateUtil.buildSessionFactory("preview_pp",    "hib.cfg.prev_pp.xml");
        Session s_delta = HibernateUtil.currentSession("preview_delta").getSession(EntityMode.POJO);
        //Session s_pp    = HibernateUtil.currentSession("preview_pp").getSession(EntityMode.POJO);
        Session s_pp    = HibernateUtil.currentSession("preview_pp");
   
        Query q = s_delta.createQuery("FROM Outcome where action_code='I'");
        System.out.println("Query = " + q);
        List results = q.list();
        for (int i = 0; i < results.size(); i++) {
             Object a = results.get(i);
             if (a instanceof CQC_Entity) {
                 System.out.println("Action code = " + ((CQC_Entity)a).getActionCode());
             }
             System.out.println(a);
             System.out.println(a.getClass().getName());

             Object b = null;
             try {
                b = s_pp.load(a.getClass(), (java.io.Serializable)a);
             System.out.println("Found item in PP: " + b);
             System.out.println(b.getClass().getName());
             if (b instanceof Service_Type) {
                 Service_Type st = (Service_Type)b;
                 System.out.println(st.getProviderId());
                 System.out.println(st.getServiceTypeId());
                 System.out.println(st.getActionCode());
             }
             } catch (Exception ex) {
System.out.println("object not found");
b = null;
             }
             //Transaction tx = s_pp.beginTransaction();
             //s_pp.delete(a);
             //s_pp.saveOrUpdate(a);
             //tx.commit();
        }
        
    }

    /**
     * Test method
     */
    public static void main( String[] args ) {
        PreviewUpdate pu = new PreviewUpdate();
        //pu.geocode(Entity.PROVIDER);
        pu.test();
    }
}
