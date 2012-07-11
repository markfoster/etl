import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;
import org.apache.log4j.Logger;

public class WatchDog {

    static Logger logger = Logger.getLogger("WatchDog");

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
    public static boolean log(String env, String type, String message, int severity) {
        int runId = 0;
        ETLContext c = ETLContext.getContext();
        if (null != c) runId = c.getRunId();
        return WatchDog.log(runId, env, type, message, severity); 
    }

    /**
     * Log a message to the watchdog table
     */
    public static boolean log(int runId, String env, String type, String message, int severity) {
        ApplicationContext context = SpringUtil.getApplicationContext();
        JdbcTemplate jt = new JdbcTemplate();
        jt.setDataSource((DataSource)context.getBean("common"));
        int rows = jt.update("INSERT INTO watchdog (uid, type, message, severity, link) VALUES (?, ?, ?, ?, ?)", 
                          new Object[] {new Integer(runId), WatchDog.ellipsize(type,16), message, new Integer(severity), env} );

        if (severity == WATCHDOG_EMERG) {
            ProcessState.setSystemState(ProcessState.IDLE);
            ProcessState.setLock(ProcessState.LOCK_CLEAR);
            logger.fatal(message);
            System.exit(-1);
        } else {
            logger.warn(message);
        }

        return (rows == 1);
    }

    public static String ellipsize(String text, int max) {
    if (text.length() <= max)
        return text;

    // Start by chopping off at the word before max
    // This is an over-approximation due to thin-characters...
    int end = text.lastIndexOf(' ', max - 3);

    // Just one long word. Chop it off.
    if (end == -1)
        return text.substring(0, max-3) + "...";

    // Step forward as long as textWidth allows.
    int newEnd = end;
    do {
        end = newEnd;
        newEnd = text.indexOf(' ', end + 1);

        // No more spaces.
        if (newEnd == -1)
            newEnd = text.length();

    } while ((text.substring(0, newEnd) + "...").length() < max);

    return text.substring(0, end) + "...";
    }

    /**
     * Test method
     */
    public static void main( String[] args ) {
        WatchDog.log(500, WatchDog.WATCHDOG_ENV_PREV, "Parse", "Parse error", WatchDog.WATCHDOG_CRITICAL);
    }
}
