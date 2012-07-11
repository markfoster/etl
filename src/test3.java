import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;
import java.sql.*;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.*;
import org.springframework.jdbc.core.RowMapper;

import org.apache.commons.collections.CollectionUtils;

public class test3 {

    public static void main( String[] args )
    {
        String[] entities = {
             "Chapter",
             "Location_Condition",
             "Location_Regulated_Activity",
             "Nominated_Individual",
             "Outcome",
             "Partner",
             "Provider_Condition",
             "Provider_Regulated_Activity",
             "Registered_Manager",
             "Registered_Manager_Condition",
             "Report_Summary",
             "Service_Type",
             "Service_User_Band",
             "Visit_Date"};

    	ApplicationContext context = 
    		new ClassPathXmlApplicationContext("spring.xml");

	JdbcTemplate jt_prev = new JdbcTemplate();
        jt_prev.setDataSource((DataSource)context.getBean("preview-delta"));

	JdbcTemplate jt_prod = new JdbcTemplate();
        jt_prod.setDataSource((DataSource)context.getBean("production-delta"));

        JdbcTemplate jtAction = jt_prev;

        for (String entity : entities) {

             String eState = ProcessState.getEntityState(entity);
             String eUID   = ProcessState.getEntityUniqueId(entity);

             System.out.println("Entity = " + entity);
             System.out.println("State = " + eState);

             //if (!eState.equals(ProcessState.STATE_FULL)) continue;

             String sql = String.format("SELECT count(0) FROM %s WHERE last_updated != '%s'", entity.toLowerCase(), eUID);
             System.out.println(sql + ";");
             int count = jtAction.queryForInt(sql);
             System.out.println("Count = " + count);
             sql = String.format("SELECT count(0) FROM %s WHERE last_updated = '%s'", entity.toLowerCase(), eUID);
             System.out.println(sql + ";");
             count = jtAction.queryForInt(sql);
             System.out.println("Count = " + count);

             sql = String.format("DELETE FROM %s WHERE last_updated != '%s'", entity.toLowerCase(), eUID);
             System.out.println(sql + ";");

             sql = String.format("TRUNCATE %s", entity.toLowerCase());
             System.out.println(sql + ";");

             //jtAction.execute(sql);
             //System.out.println("Rows = " + rows);
        }

        List result = jt_prev.query("select count(*) FROM chapter", new RowMapper() {
            public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                   return resultSet.getString(1);
            }
        });
        System.out.println("Result = " + result.size());
       
    }
}
