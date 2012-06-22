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
        HibernateUtil.buildSessionFactory("preview_delta", "hibernate.cfg.xml");
        HibernateUtil.buildSessionFactory("preview_pp",    "hib.cfg.prev_pp.xml");
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
                   continue;
               }
               updatePreviewProfile(entity);
               updateProductionProfile(entity);
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
             Object a = results.get(i);
             if (a instanceof CQC_Entity) {
                 action = ((CQC_Entity)a).getActionCode().toString();
             }
             logger.info(a);
             logger.info(a.getClass().getName());
             Object b = null;
             try {
                //b = s_pp.load(a.getClass(), (java.io.Serializable)a);
                //System.out.println("Found item in PP: " + b);
                logger.info("Action = " + action);
                Transaction tx = s_pp.beginTransaction();
                //s_pp.delete(a);
                s_pp.saveOrUpdate(a);
                tx.commit();
             } catch (Exception ex) {
                 System.out.println("object not found");
                 b = null;
             }
        }
    }

    public void updateProductionProfile(String entity) {
    }

    /**
     * Update all the empty Geocode values for Provider and Location entities
     */
    public static boolean updateGeocoding(String entity) {
        ApplicationContext context = SpringUtils.getApplicationContext();
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
        System.out.println(sql);
        List postcodes = jT_PreviewDelta.queryForList(sql); 
        System.out.println(postcodes);
        Iterator itr = postcodes.iterator(); 
        float[] latlng = { 0.0f, 0.0f };
        while (itr.hasNext()) {
             Map element = (Map)itr.next(); 
             System.out.println("PC = " + element.get("postcode") + " : " + element + " : " + element.getClass().getName());
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
