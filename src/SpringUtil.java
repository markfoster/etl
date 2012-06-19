import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.*;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class SpringUtil {

     private static ApplicationContext context;

     static {
         try {
             context = new ClassPathXmlApplicationContext("spring.xml");
        } catch (Throwable ex) {
             throw new ExceptionInInitializerError(ex);
         }
     }

     public static ApplicationContext getApplicationContext() {
         return context;
     }

}
