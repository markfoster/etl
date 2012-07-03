import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;
import java.sql.*;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.*;
import org.springframework.jdbc.core.RowMapper;

import org.apache.commons.collections.CollectionUtils;

public class jdbcexample {

    public static void main( String[] args )
    {
    	ApplicationContext context = 
    		new ClassPathXmlApplicationContext("spring.xml");

	Object a = context.getBean("sysprops");
        System.out.println(a);

	JdbcTemplate jt = new JdbcTemplate();
        jt.setDataSource((DataSource)context.getBean("preview-delta"));

        jt.execute("TRUNCATE TABLE outcome");

        
/**
	JdbcTemplate jt = new JdbcTemplate();
        jt.setDataSource((DataSource)context.getBean("preview-pp"));

	JdbcTemplate jt_cqcdms = new JdbcTemplate();
        jt_cqcdms.setDataSource((DataSource)context.getBean("preview-cqcdms"));

        //List<Integer> providers = jt.queryForList("select provider_id FROM lookup where location_id=''");
        //System.out.println("Providers = " + providers);

        List<String> providers = jt.query("select provider_id FROM lookup where location_id=''", new RowMapper() {
            public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                   return resultSet.getString(1);
            }
        });
        System.out.println("Providers = " + providers.size());
       
        List<String> providersC = jt_cqcdms.query("SELECT field_provider_id_value FROM content_type_provider", new RowMapper() {
            public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                   return resultSet.getString(1);
            }
        });
        System.out.println("Providers = " + providersC.size());
 
        //Collection c = CollectionUtils.subtract(providersC, providers);
        Collection c = CollectionUtils.subtract(providers, providersC);
 
        System.out.println("Providers = " + c);
        System.out.println("Providers = " + c.size());

        providers = jt.query("select DISTINCT location_id FROM lookup where location_id!=''", new RowMapper() {
            public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                   return resultSet.getString(1);
            }
        });
        System.out.println("Locations = " + providers.size());

        providersC = jt_cqcdms.query("SELECT field_location_id_value FROM content_type_location", new RowMapper() {
            public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                   return resultSet.getString(1);
            }
        });
        System.out.println("Locations = " + providersC.size());

        //c = CollectionUtils.subtract(providersC, providers);
        c = CollectionUtils.subtract(providers, providersC);

        System.out.println("Locations = " + c);
        System.out.println("Locations = " + c.size());



	//String s = (String)jt.queryForObject("select * from provider", String.class);
**/

/**
        List<String> postcodes = jt.query("select postcode from provider", new RowMapper() {
            public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                   return resultSet.getString(1);
            }
        });
        System.out.println(postcodes);
**/

/** 
        CustomerDAO customerDAO = (CustomerDAO) context.getBean("customerDAO");
        Customer customer = new Customer(1, "mkyong",28);
        customerDAO.insert(customer);
 
        Customer customer1 = customerDAO.findByCustomerId(1);
        System.out.println(customer1);
**/ 
    }
}
