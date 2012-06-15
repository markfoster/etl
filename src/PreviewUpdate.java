import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;
import java.util.List;
import java.util.Iterator;
import java.util.Map;

public class PreviewUpdate extends DeltaUpdate {

    /**
     * Update all the empty GeoCode values for Provider and Location entities
     */
    public static boolean geocode(String entity) {
        ApplicationContext context = SpringUtils.getApplicationContext();
        JdbcTemplate jT_Common = new JdbcTemplate();
        jT_Common.setDataSource((DataSource)context.getBean("common"));

        JdbcTemplate jT_PreviewDelta = new JdbcTemplate();
        jT_PreviewDelta.setDataSource((DataSource)context.getBean("preview_delta"));

        String ent_table = entity.toLowerCase();
        String sql = "SELECT ";
        if        (entity.equals(Entity.PROVIDER)) {
           sql += "provider_id";
        } else if (entity.equals(Entity.LOCATION)) {
           sql += "provider_id, location_id";
        }
        sql += " ,postcode FROM " + ent_table + " WHERE action_code IN ('I', 'U')";
        String uId = ProcessState.getEntityUniqueId(entity); 
        sql += " AND postcode IS NOT NULL AND last_updated = '" + uId + "'";
        sql += " limit 20";
        System.out.println(sql);
        List postcodes = jT_PreviewDelta.queryForList(sql); 
        System.out.println(postcodes);
        Iterator itr = postcodes.iterator(); 
        while (itr.hasNext()) {
             Map element = (Map)itr.next(); 
             System.out.println("PC = " + element.get("postcode") + " : " + element + " : " + element.getClass().getName());
             String postcode = (String)element.get("postcode");
             GeoCode.getAddress(postcode);
        }  
        int rows = 1;
        //int rows = jT_Common.update("INSERT INTO watchdog (uid, type, message, severity, link) VALUES (?, ?, ?, ?, ?)", 
        //                  new Object[] {new Integer(runId), type, message, new Integer(severity), env} );
        return (rows == 1);
    }

    /**
     * Test method
     */
    public static void main( String[] args ) {
        PreviewUpdate pu = new PreviewUpdate();
        pu.geocode(Entity.PROVIDER);
    }
}
