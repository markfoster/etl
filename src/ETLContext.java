import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import org.apache.log4j.Logger;

public class ETLContext {

        Logger logger = Logger.getLogger("ETLContext");

        private static Map audit = null;
        private static Map report = null;
        private static Map docs = null;
        private static int id = 0;

        private static final ETLContext context = new ETLContext();

        private ETLContext() { }

	public static ETLContext getContext() {
		return context;
	}

	public Map getAuditMap() {
		return this.audit;
	}

	public void setAuditMap(Map audit) {
		this.audit = audit;
	}

	public Map getDocumentMap() {
		return this.docs;
	}

	public void setDocumentMap(Map docs) {
		this.docs = docs;
	}

        public int getRunId () {
                return this.id;
        }

        public void setRunId (int id) {
                int nId = id;
                if (id == 0) {
                    Random r = new Random();
                    nId = r.nextInt();
                    if (nId < 0) nId = -nId;
                } 
                ProcessState.setRunId(nId);
                this.id = nId;
        }

        /****
        *
        */
        public Map readReport(String reportFile) {
                this.report = new HashMap();
                try {
                    BufferedReader CSVFile = new BufferedReader(new FileReader(reportFile));          
                    String dataRow = CSVFile.readLine();
                    while (dataRow != null){
                            String[] dataArray = dataRow.split(",");
                            List l = new ArrayList();
                            String key = dataArray[0];
                            for (int i=1; i<dataArray.length; i++) {
                                 l.add(dataArray[i]);
                            }
                            report.put(key, l);
                            dataRow = CSVFile.readLine(); // Read next line of data.
                    }
                    CSVFile.close();
                } catch (Exception ex) {
                    logger.error("Error processing XML report", ex);
                    return null; 
                }
                return report;
        }
}
