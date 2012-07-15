import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;

public class DeltaUpdate {

    public DeltaUpdate() {
    }

    /**
     * Test method
     */
    public static void main(String[] args) {
	WatchDog.log(500, WatchDog.WATCHDOG_ENV_PREV, "Parse", "Parse error", WatchDog.WATCHDOG_CRITICAL);
    }
}
