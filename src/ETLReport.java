import org.apache.log4j.Logger;

import java.text.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import javax.sql.*;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.*;

/**
* Creates a report of the Delta Update process recently completed
*/
public class ETLReport {

	Logger logger = Logger.getLogger(this.getClass().getName());

	public static void main(String[] arg) throws IOException {
		ETLReport p = new ETLReport();
                p.run();
        }

	public void run() {
                ApplicationContext context = SpringUtil.getApplicationContext();
                JdbcTemplate jt = new JdbcTemplate();
                jt.setDataSource((DataSource)context.getBean("common"));

                List<String> entities = jt.query("SELECT * FROM process_state WHERE entity=?",  new Object[]{"System"}, new RowMapper() {
                     public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                          return resultSet.getString(1);
                }
                });

                logger.info(entities);

                StringBuffer reportOutput = new StringBuffer();

                String sysState = ProcessState.getSystemState();
                int    runId    = ProcessState.getRunId();
                
                //sysState = "PREVIEW_DRUPAL_COMPLETE";
                //runId = 991597794;

                String mStart = "PROD_DELTA_LOAD_IN_PROGRESS";
                String mEnd   = "PROD_DELTA_LOAD_IN_PROGRESS";
                String type   = "PRODUCTION";
                if      (sysState.equals("PROD_DRUPAL_COMPLETE")) {
                     mStart = "PROD_DELTA_LOAD_IN_PROGRESS";
                     mEnd   = "PROD_DRUPAL_COMPLETE";
                     type   = "PRODUCTION";
                }
                else if (sysState.equals("PREVIEW_DRUPAL_COMPLETE")) {
                     mStart = "PREVIEW_DELTA_LOAD_IN_PROGRESS";
                     mEnd   = "PREVIEW_DRUPAL_COMPLETE";
                     type   = "PREVIEW";
                }
                else {
                     logger.warn("System is currently in state: " + sysState);
                     return;
                }
                
                logger.info("System state = " + sysState);
                logger.info("Run Id       = " + runId);

                String start = (String)jt.queryForObject("SELECT timestamp FROM watchdog WHERE uid = ? AND message LIKE 'Acquired lock % " + mStart + "'",  
                                             new Object[]{ new Integer(runId)}, String.class );
                start = start.replace(".0", ""); // fudge to fix issue
                logger.info("Started  = " + start);
                String end   = (String)jt.queryForObject("SELECT timestamp FROM watchdog WHERE uid = ? AND message LIKE '% to " + mEnd + "'", 
                                             new Object[]{ new Integer(runId)}, String.class );
                end = end.replace(".0", ""); // fudge to fix issue
                logger.info("Finished = " + end);

                StringBuffer emailOutput = new StringBuffer();

                emailOutput.append("System state = " + sysState + "\n");
                emailOutput.append("Run Id       = " + runId    + "\n");
                emailOutput.append("Start        = " + start    + "\n");
                emailOutput.append("Finished     = " + end      + "\n");
                emailOutput.append("\n\n");

                List<Map> entries = jt.queryForList("SELECT timestamp, message FROM watchdog WHERE uid = ? AND type != 'pp_scheduler'",  new Object[]{ new Integer(runId)} );
                for (Map line : entries) {
                     String time = (String)line.get("timestamp").toString().replace(".0", "");
                     String mesg = (String)line.get("message").toString();
                     if (mesg.matches("Skipping.*")) continue;
                     if (mesg.matches("Updating.*")) continue;
                     if (mesg.matches("Processing.*")) continue;
                     if (mesg.matches("Changed internal.*")) continue;
                     String repLine = "\"" + time + "\"; \"" + mesg + "\"";
                     logger.info(repLine);
                     reportOutput.append(repLine + "\n");
                }

                // locate any critial (or worse) issues found within this run id
                entries = jt.queryForList("SELECT timestamp, message FROM watchdog WHERE uid = ? AND severity <= 3",  new Object[]{ new Integer(runId)} );
                if (!entries.isEmpty()) {
                    reportOutput.append("\n");
                    reportOutput.append("Critical issues logged:");
                    reportOutput.append("\n");
                    for (Map line : entries) {
                         String time = (String)line.get("timestamp").toString().replace(".0", "");
                         String mesg = (String)line.get("message").toString();
                         String repLine = "\"" + time + "\"; \"" + mesg + "\"";
                         logger.info(repLine);
                         reportOutput.append(repLine + "\n");
                    }
                }

                emailOutput.append(reportOutput.toString());
                //ETLContext.getContext().reportMail(String.format("Report for %s run (%d)",  type, runId), emailOutput.toString());

                jt.update("INSERT INTO reporting (run_id, type, start, finish, message, last_updated) VALUES (?, ?, ?, ?, ?, now())", 
                       new Object[] {new Integer(runId), type, start, end, reportOutput.toString() } );

                // write out a report file
                String reportFilename = "";
                try {
                    // Generate the report file
                    DateFormat df = new SimpleDateFormat("yyyyMMdd_hhmmss"); 
                    String dateStamp = df.format(new java.util.Date());
                    String filestub = "report_"+dateStamp;
                    reportFilename = "/mnt/www/reports/"+filestub;
                    FileWriter file = new FileWriter(reportFilename);
                    BufferedWriter out = new BufferedWriter(file);
                    out.write(reportOutput.toString());
                    out.close();
               
                    // Update the index.html file 
                    StringBuffer fileOutput = new StringBuffer();
                
                    df = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss"); 
                    dateStamp = df.format(new java.util.Date());
                    String output = String.format("\n<!-- -->%s <a href=\"%s\">%s</a></br>\n", 
                                          dateStamp, filestub, String.format("Report for %s run (%d)", type, runId));

		    fileOutput.append("<h2>Reports:</h2></br>\n");
		    fileOutput.append(output);

		    try {
		        BufferedReader indexFile = new BufferedReader(new FileReader("/mnt/www/reports/index.html"));
		        String dataRow = indexFile.readLine();
		        int iCount = 0;
		        while (dataRow != null) {
		            if (++iCount > 30) break;
		            if (dataRow.matches("<!--.*")) {
		                fileOutput.append(dataRow);
		                fileOutput.append("\n");
                            }
                            dataRow = indexFile.readLine(); // Read next line of data.
		        }
		        indexFile.close();
		    } catch (Exception ex) {
                        logger.error("Failed to read index file.", ex);
		    }
		    
		    file = new FileWriter("/mnt/www/reports/index.html");
                    out = new BufferedWriter(file);
                    out.write(fileOutput.toString());
                    out.close();

                } catch (Exception ex) {
                    logger.error("Failed to write report file.", ex);
                }

                //ETLContext.getContext().reportMail(String.format("Report for %s run (%d)",  type, runId), emailOutput.toString());
                ETLContext.getContext().reportMailFromFile(String.format("Report for %s run (%d)",  type, runId), reportFilename);
	}
}
