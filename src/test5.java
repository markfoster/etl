import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.io.*;

import org.apache.commons.collections.CollectionUtils;

public class test5 {

    public void run() {
        List l = ProcessState.getEntitiesForUpdate();
        System.out.println(l);
    }

    public static void main( String[] args ) {
        test5 t = new test5();
        t.run();
    }
}
