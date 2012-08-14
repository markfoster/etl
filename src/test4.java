import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.io.*;

import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.apache.commons.collections.CollectionUtils;

public class test4 {

    public void runExecCmd(String cmd) {
        try {
            Runtime runtime = Runtime.getRuntime();
            String[] cmdR = new String[] { "/bin/bash", "-c", cmd };
            System.out.println(cmdR);

            Process process = runtime.exec(new String[] { "/bin/bash", "-c", cmd });
            int exitValue = process.waitFor();
            System.out.println("exit value: " + exitValue);
            BufferedReader buf = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            while ((line = buf.readLine()) != null) {
                System.out.println("exec response: " + line);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void main( String[] args ) {

        ApplicationContext context =
                new ClassPathXmlApplicationContext("spring.xml");

        DriverManagerDataSource ds = (DriverManagerDataSource)context.getBean("preview-delta");

        String user = ds.getUsername();
        String pass = ds.getPassword();
        String url = ds.getUrl();
        String host = "";
        try { host = new java.net.URI(url.substring(5)).getHost(); } catch (Exception ex) { }
        System.out.println(host);
        String table = "outcome";

        String date = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
        String file = String.format("%s_%s.sql", table, date);
        String cmd = String.format("/usr/bin/mysqldump --no-create-info --compact --host=%s --user=%s --password=%s %s %s > /var/tmp/%s",
                      host, user, pass, "preview_delta", table, file);

        System.out.println(cmd);

        //String cmd = "/usr/bin/mysqldump --no-create-info --compact --host=" + host + " --user=cqcdms_p --password=ncZ1x6CWSDa preview_pp outcome > /var/tmp/outcome_.sql";
        //String cmd = "/usr/bin/mysqldump --user=cqcdms_p --password=ncZ1x6CWSDa --databases preview_delta outcome | gzip -9 > " + "/var/tmp/test.sql.gz"; 

        test4 t = new test4();
        //t.runExecCmd(cmd);

        //cmd = "/usr/bin/mysql --host=10.38.1.65 --user=cqcdms_p --password=ncZ1x6CWSDa preview_delta -e \"TRUNCATE outcome;\"";
        //t.runExecCmd(cmd);
    }
}
