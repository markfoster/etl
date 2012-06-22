import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.*;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class ProcessState {

     public static final String SYSTEM 			= "System";
     public static final String LOCK   			= "Lock";

     public static final String LOCK_SET		= "SET";
     public static final String LOCK_CLEAR              = "CLEAR";

     public static final String IDLE 			      = "IDLE";
     public static final String PREVIEW_XML_IN_PROGRESS       = "PREVIEW_XML_IN_PROGRESS";    
     public static final String PREVIEW_XML_COMPLETE          = "PREVIEW_XML_COMPLETE";       
     public static final String PREVIEW_DELTA_IN_PROGRESS     = "PREVIEW_DELTA_IN_PROGRESS";    
     public static final String PREVIEW_DELTA_COMPLETE        = "PREVIEW_DELTA_COMPLETE";       
     public static final String PREVIEW_DRUPAL_IN_PROGRESS    = "PREVIEW_DRUPAL_IN_PROGRESS";   
     public static final String PREVIEW_DRUPAL_COMPLETE       = "PREVIEW_DRUPAL_COMPLETE";      
     public static final String PRODUCTION_LOAD_TRIGGER       = "PRODUCTION_LOAD_TRIGGER";      
     public static final String PRODUCTION_PP_IN_PROGRESS     = "PRODUCTION_PP_IN_PROGRESS";    
     public static final String PRODUCTION_PP_COMPLETE        = "PRODUCTION_PP_COMPLETE";       
     public static final String PRODUCTION_DRUPAL_IN_PROGRESS = "PRODUCTION_DRUPAL_IN_PROGRESS";
     public static final String PRODUCTION_DRUPAL_COMPLETE    = "PRODUCTION_DRUPAL_COMPLETE";   

    public void test() {
        StopWatch sw = new StopWatch("a");
        sw.start("test 1");
        ApplicationContext context = SpringUtils.getApplicationContext();
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

    public static String getSystemState() {
	ApplicationContext context = SpringUtils.getApplicationContext();
	JdbcTemplate jt = new JdbcTemplate();
        jt.setDataSource((DataSource)context.getBean("common"));
	String state = (String)jt.queryForObject("select state from process_state where entity = ?", new Object[]{"System"}, String.class);
        return state;
    }

    public static boolean setSystemState(String state) {
        ApplicationContext context = SpringUtils.getApplicationContext();
        JdbcTemplate jt = new JdbcTemplate();
        jt.setDataSource((DataSource)context.getBean("common"));
        int rows = jt.update("update process_state set state = ?, update_id=now() where entity = ?", new Object[] {state, "System"});
        return (rows == 1);
    }

    public static String getEntityState(String entity) {
        ApplicationContext context = SpringUtils.getApplicationContext();
        JdbcTemplate jt = new JdbcTemplate();
        jt.setDataSource((DataSource)context.getBean("common"));
	String state = (String)jt.queryForObject("select state from process_state where entity = ?", new Object[]{entity}, String.class);
        return state;
    }

    public static boolean setEntityState(String entity, String state) {
        ApplicationContext context = SpringUtils.getApplicationContext();
        JdbcTemplate jt = new JdbcTemplate();
        jt.setDataSource((DataSource)context.getBean("common"));
        int rows = jt.update("update process_state set state = ? where entity = ?", new Object[] {state, entity});
        return (rows == 1);
    }

    public static String getLock() {
        ApplicationContext context = SpringUtils.getApplicationContext();
        JdbcTemplate jt = new JdbcTemplate();
        jt.setDataSource((DataSource)context.getBean("common"));
        String state = (String)jt.queryForObject("select state from process_state where entity = ?", new Object[]{"Lock"}, String.class);
        return state;
    }

    public static boolean setLock(String lock) {
        ApplicationContext context = SpringUtils.getApplicationContext();
        JdbcTemplate jt = new JdbcTemplate();
        jt.setDataSource((DataSource)context.getBean("common"));
        int rows = jt.update("update process_state set state = ?, update_id = now() where entity = 'Lock'", new Object[] {lock});
        return (rows == 1);
    }

    public static String getEntityUniqueId(String entity) {
        ApplicationContext context = SpringUtils.getApplicationContext();
        JdbcTemplate jt = new JdbcTemplate();
        jt.setDataSource((DataSource)context.getBean("common"));
        String state = (String)jt.queryForObject("select update_id from process_state where entity = ?", new Object[]{entity}, String.class);
        state = state.replace(".0", ""); // fudge to fix issue
        return state;
    }

    public static boolean setEntityUniqueId(String entity, String state) {
        ApplicationContext context = SpringUtils.getApplicationContext();
        JdbcTemplate jt = new JdbcTemplate();
        jt.setDataSource((DataSource)context.getBean("common"));
        int rows = jt.update("update process_state set update_Id = ? where entity = ?", new Object[] {state, entity});
        return (rows == 1);
    }

    public static void main( String[] args )
    {
        ProcessState ps = new ProcessState();
        ps.test();

        String state = ProcessState.getSystemState();
        System.out.println("State = " + state);

        ProcessState.setSystemState(ProcessState.PREVIEW_DELTA_IN_PROGRESS);
        state = ProcessState.getSystemState();
        System.out.println("State = " + state);

	//String s = (String)jt.queryForObject("select * from provider", String.class);

/**
        List<String> postcodes = jt.query("select postcode from provider", new RowMapper() {
            public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                   return resultSet.getString(1);
            }
        });
        System.out.println(postcodes);
**/

    }
}
