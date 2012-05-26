import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.*;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;

public class DBTest {

	private static Connection conn;

        public static void main(String[] args) {
  try
    {
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      String url = "jdbc:mysql://localhost/etl";
      conn = DriverManager.getConnection(url, "root", "root");
      DBTest test = new DBTest();
      Map codes = new HashMap();
      codes.put("RG26 3DN", false);
      test.doTests(codes);
      test.check(codes);
      conn.close();
    } catch (Exception ex) {
		}
	}


	public void doTests(Map codes) throws Exception {
    //System.out.println("[OUTPUT FROM SELECT]");
    String query = "select postcode from provider order by postcode";
    try
    {
      Statement st = conn.createStatement();
      ResultSet rs = st.executeQuery(query);
      while (rs.next())
      {
        String s = rs.getString("postcode");
        codes.put(s, false);
        //System.out.println("Post code = " + s);
      }
    }
    catch (SQLException ex)
    {
      System.err.println(ex.getMessage());
    }
	}

        public void check(Map codes) throws Exception {
		Iterator it = codes.entrySet().iterator();
		while (it.hasNext()) {
        		Map.Entry pairs = (Map.Entry)it.next();
        		//System.out.println(pairs.getKey() + " = " + pairs.getValue());

    String query = "select latitude, longitude from cqc_address_cache where postal_code = '" + pairs.getKey() + "'";
    try
    {
      Statement st = conn.createStatement();
      ResultSet rs = st.executeQuery(query);
      if (rs.next()) {
      do
      {
        float lat = rs.getFloat("latitude"); 
        float lng = rs.getFloat("longitude"); 
        //System.out.println("Lat = " + lat + ", Long = " + lng);
      }
      while (rs.next());
      } else {
        System.out.println("NOT FOUND " + pairs.getKey());
      }
    }
    catch (SQLException ex)
    {
      System.err.println(ex.getMessage());
    }
        		it.remove(); // avoids a ConcurrentModificationException
    		}
        }

}
