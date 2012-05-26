import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;
import java.sql.*;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.*;
import org.springframework.jdbc.core.RowMapper;

public class jdbcexample {

    public static void main( String[] args )
    {
    	ApplicationContext context = 
    		new ClassPathXmlApplicationContext("spring.xml");

	JdbcTemplate jt = new JdbcTemplate();
        jt.setDataSource((DataSource)context.getBean("preview-delta"));

        int rowCount = jt.queryForInt("select count(0) from provider");
        System.out.println("Providers = " + rowCount);
	//String s = (String)jt.queryForObject("select * from provider", String.class);

        List<String> postcodes = jt.query("select postcode from provider", new RowMapper() {
            public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                   return resultSet.getString(1);
            }
        });
        System.out.println(postcodes);


/** 
        CustomerDAO customerDAO = (CustomerDAO) context.getBean("customerDAO");
        Customer customer = new Customer(1, "mkyong",28);
        customerDAO.insert(customer);
 
        Customer customer1 = customerDAO.findByCustomerId(1);
        System.out.println(customer1);
**/ 
    }
}
