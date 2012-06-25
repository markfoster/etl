import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;

public class WatchDog {

    // Environment
    public static final String WATCHDOG_ENV_PROD = "PROD";
    public static final String WATCHDOG_ENV_PREV = "PREV";

    // Severity type
    public static final int WATCHDOG_EMERG    = 0;
    public static final int WATCHDOG_ALERT    = 1;
    public static final int WATCHDOG_CRITICAL = 2;
    public static final int WATCHDOG_ERROR    = 3;
    public static final int WATCHDOG_WARNING  = 4;
    public static final int WATCHDOG_NOTICE   = 5;
    public static final int WATCHDOG_INFO     = 6;
    public static final int WATCHDOG_DEBUG    = 7;    

    /**
     * Log a message to the watchdog table
     */
    public static boolean log(int runId, String env, String type, String message, int severity) {
        ApplicationContext context = SpringUtil.getApplicationContext();
        JdbcTemplate jt = new JdbcTemplate();
        jt.setDataSource((DataSource)context.getBean("common"));
        int rows = jt.update("INSERT INTO watchdog (uid, type, message, severity, link) VALUES (?, ?, ?, ?, ?)", 
                          new Object[] {new Integer(runId), type, message, new Integer(severity), env} );
        return (rows == 1);
    }

    /**
     * Test method
     */
    public static void main( String[] args ) {
        WatchDog.log(500, WatchDog.WATCHDOG_ENV_PREV, "Parse", "Parse error", WatchDog.WATCHDOG_CRITICAL);
    }
}
