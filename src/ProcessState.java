import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.*;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

import org.apache.log4j.Logger;

public class ProcessState {

     static Logger logger = Logger.getLogger("ProcessState");

     public static final String SYSTEM 			= "System";
     public static final String LOCK   			= "Lock";

     public static final String LOCK_SET		= "SET";
     public static final String LOCK_CLEAR              = "CLEAR";

     public static final String STATE_FULL              = "FULL";
     public static final String STATE_DELTA             = "DELTA";

     public static final String IDLE 			       = "IDLE";
     public static final String PREVIEW_XML_IN_PROGRESS        = "PREVIEW_XML_IN_PROGRESS";    
     public static final String PREVIEW_XML_COMPLETE           = "PREVIEW_XML_COMPLETE";       
     public static final String PREVIEW_DELTA_LOAD_IN_PROGRESS = "PREVIEW_DELTA_LOAD_IN_PROGRESS";    
     public static final String PREVIEW_DELTA_LOAD_COMPLETE    = "PREVIEW_DELTA_LOAD_COMPLETE";       
     public static final String PREVIEW_DRUPAL_IN_PROGRESS     = "PREVIEW_DRUPAL_IN_PROGRESS";   
     public static final String PREVIEW_DRUPAL_COMPLETE        = "PREVIEW_DRUPAL_COMPLETE";      
     public static final String PROD_LOAD_TRIGGER              = "PROD_LOAD_TRIGGER";      
     public static final String PROD_DELTA_LOAD_IN_PROGRESS    = "PROD_DELTA_LOAD_IN_PROGRESS";    
     public static final String PROD_DELTA_LOAD_COMPLETE       = "PROD_DELTA_LOAD_COMPLETE";       
     public static final String PROD_DRUPAL_IN_PROGRESS        = "PROD_DRUPAL_IN_PROGRESS";
     public static final String PROD_DRUPAL_COMPLETE           = "PROD_DRUPAL_COMPLETE";   

    public static List getEntitiesForUpdate () {
        ApplicationContext context = SpringUtil.getApplicationContext();
        JdbcTemplate jt = new JdbcTemplate();
        jt.setDataSource((DataSource)context.getBean("common"));
        List<String> entities = jt.query("select entity from process_state", new RowMapper() {
            public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                   return resultSet.getString(1);
            }
        });
        entities.remove(ProcessState.SYSTEM);
        entities.remove(ProcessState.LOCK);
        return entities;
    }

    public static int getRunId() {
        ApplicationContext context = SpringUtil.getApplicationContext();
        JdbcTemplate jt = new JdbcTemplate();
        jt.setDataSource((DataSource)context.getBean("common"));
        int nId = jt.queryForInt("select run_id from process_state where entity = ?", new Object[]{"System"});
        return nId;
    }

    public static boolean setRunId(int id) {
        checkEntityExists(ProcessState.SYSTEM);
        ApplicationContext context = SpringUtil.getApplicationContext();
        JdbcTemplate jt = new JdbcTemplate();
        jt.setDataSource((DataSource)context.getBean("common"));
        int rows = jt.update("update process_state set run_id = ?, update_id=now() where entity = ?", new Object[] {new Integer(id), "System"} );
        return (rows == 1);
    }

    public static String getSystemState() {
	ApplicationContext context = SpringUtil.getApplicationContext();
	JdbcTemplate jt = new JdbcTemplate();
        jt.setDataSource((DataSource)context.getBean("common"));
	String state = (String)jt.queryForObject("select state from process_state where entity = ?", new Object[]{"System"}, String.class);
        return state;
    }

    public static boolean setSystemState(String state) {
        checkEntityExists(ProcessState.SYSTEM);
        ApplicationContext context = SpringUtil.getApplicationContext();
        JdbcTemplate jt = new JdbcTemplate();
        jt.setDataSource((DataSource)context.getBean("common"));
        int rows = jt.update("update process_state set state = ?, update_id=now() where entity = ?", new Object[] {state, "System"});
        return (rows == 1);
    }

    public static String getEntityState(String entity) {
        ApplicationContext context = SpringUtil.getApplicationContext();
        JdbcTemplate jt = new JdbcTemplate();
        jt.setDataSource((DataSource)context.getBean("common"));
	String state = (String)jt.queryForObject("select state from process_state where entity = ?", new Object[]{entity}, String.class);
        return state;
    }

    public static boolean setEntityState(String entity, String state) {
        checkEntityExists(entity);
        ApplicationContext context = SpringUtil.getApplicationContext();
        JdbcTemplate jt = new JdbcTemplate();
        jt.setDataSource((DataSource)context.getBean("common"));
        int rows = jt.update("update process_state set state = ? where entity = ?", new Object[] {state, entity});
        return (rows == 1);
    }

    public static String getLock() {
        ApplicationContext context = SpringUtil.getApplicationContext();
        JdbcTemplate jt = new JdbcTemplate();
        jt.setDataSource((DataSource)context.getBean("common"));
        String state = (String)jt.queryForObject("select state from process_state where entity = ?", new Object[]{"Lock"}, String.class);
        return state;
    }

    public static boolean setLock(String lock) {
        checkEntityExists(ProcessState.LOCK);
        ApplicationContext context = SpringUtil.getApplicationContext();
        JdbcTemplate jt = new JdbcTemplate();
        jt.setDataSource((DataSource)context.getBean("common"));
        int rows = jt.update("update process_state set state = ?, update_id = now() where entity = 'Lock'", new Object[] {lock});
        return (rows == 1);
    }

    public static String getEntityUniqueId(String entity) {
        ApplicationContext context = SpringUtil.getApplicationContext();
        JdbcTemplate jt = new JdbcTemplate();
        jt.setDataSource((DataSource)context.getBean("common"));
        String state = (String)jt.queryForObject("select update_id from process_state where entity = ?", new Object[]{entity}, String.class);
        state = state.replace(".0", ""); // fudge to fix issue
        return state;
    }

    public static boolean setEntityUniqueId(String entity, String state) {
        checkEntityExists(entity);
        ApplicationContext context = SpringUtil.getApplicationContext();
        JdbcTemplate jt = new JdbcTemplate();
        jt.setDataSource((DataSource)context.getBean("common"));
        int rows = jt.update("update process_state set update_Id = ? where entity = ?", new Object[] {state, entity});
        return (rows == 1);
    }

    private static void checkEntityExists(String entity) {
        ApplicationContext context = SpringUtil.getApplicationContext();
        JdbcTemplate jt = new JdbcTemplate();
        jt.setDataSource((DataSource)context.getBean("common"));
        String state = null;
        try {
           state = (String)jt.queryForObject("select update_id from process_state where entity = ?", new Object[]{entity}, String.class);
        } catch (Exception ex) { }
        int rows = 0;
        if (state == null) {
            jt.update("INSERT INTO process_state (entity, run_id) VALUES (?, 0)", new Object[] {entity} );
        }
    }

    public static void main( String[] args )
    {
        //ProcessState ps = new ProcessState();
        //ps.test();

        ProcessState.setEntityState("Service_Type", "DELTA");

/**
        String state = ProcessState.getSystemState();
        System.out.println("State = " + state);

        ProcessState.setSystemState(ProcessState.PREVIEW_DELTA_LOAD_IN_PROGRESS);
        state = ProcessState.getSystemState();
        System.out.println("State = " + state);

	//String s = (String)jt.queryForObject("select * from provider", String.class);

        List<String> postcodes = jt.query("select postcode from provider", new RowMapper() {
            public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                   return resultSet.getString(1);
            }
        });
        System.out.println(postcodes);
**/

    }

    public void test() {
        StopWatch sw = new StopWatch("a");
        sw.start("test 1");
        ApplicationContext context = SpringUtil.getApplicationContext();
        Object a = context.getBean("sysprops");
        System.out.println(a);
        sw.stop();
        sw.start("test 2");
        JdbcTemplate jt = new JdbcTemplate();
        jt.setDataSource((DataSource)context.getBean("common"));
        int rowCount = jt.queryForInt("select count(0) from process_state");
        sw.stop();
        sw.start("test 1");
        System.out.println("Entities = " + rowCount);
        List l = jt.queryForList("select * from process_state");
        System.out.println("Entities = " + l);
        sw.stop();
        System.out.println(sw.prettyPrint());
    }
}
