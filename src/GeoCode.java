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
import java.math.BigDecimal;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;

import org.springframework.util.*;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.*;
import javax.sql.DataSource;

public class GeoCode {

        private static final String googleKey = "Y6D95O6w_dXHs58ve2ixLrSV8tU=";
        private static final String googleID  = "gme-carequalitycommission";

	// URL prefix to the geocoder
        private static final String GEOCODER_REQUEST_PREFIX_FOR_XML = "http://maps.googleapis.com/maps/api/geocode/xml";

        private static Map<String, List> geoMap = new HashMap();

        public static void main(String[] args) {
		GeoCode gc = new GeoCode();
                //gc.test();
/**
                String postcode = "RG263DN";
                float[] latlng  = gc.getAddress("", "", "", "", postcode, "");
		System.out.println("Coords for '" + postcode + "' = " + latlng);
**/
	}

        /**
        * getAddress
        * @param postcode - a valid postal code
        * @return float[] - latitude and longitude coordinates
        * Get the address coordinate details from a postalcode
        */
        public static float[] getAddress(String postcode) {
                float[] latlng = {0.0f, 0.0f};
                try {
                    latlng = getCache(postcode);
                } catch (Exception ex) {
                }

               	float lat = 0.0f;
                float lng = 0.0f;
                ApplicationContext context = SpringUtil.getApplicationContext();
                JdbcTemplate jT_Common = new JdbcTemplate();
                jT_Common.setDataSource((DataSource)context.getBean("common"));

                String sql = "SELECT latitude, longitude FROM geocode_cache WHERE postcode = '" + postcode + "'";
                List coords = jT_Common.queryForList(sql);
                if (!coords.isEmpty() && coords.size() == 1) {
                    Map map = (Map)coords.get(0); 
                    lat = ((java.math.BigDecimal)map.get("latitude")).floatValue();
                    lng = ((java.math.BigDecimal)map.get("longitude")).floatValue();
                } else {
	            float[] ilatlng = getAddress("", "", "", "", postcode, "");
                    lat = ilatlng[0];
                    lng = ilatlng[1];
                }
                return latlng;
        }

        public static float[] getAddress(String address1, 
                                  String address2, 
                                  String city, 
                                  String county, 
                                  String postcode, 
                                  String country) {

		// Check to see if the address exists in the cache, the method returns the coordinates of the address
		// if the coordinates exist the latitude will be greater than 0
               	float lat = 0.0f;
                float lng = 0.0f;

                float[] coord = {lat,lng};

                if (coord[0] != 0) {
                    // retrieve address from cache and return lat / long
                    return coord;
		}                    

		String address = postcode + " " + country;

                // prepare a URL to the geocoder
            	String request = " ";
            	URL url = null;
                try {
			// curl "http://maps.googleapis.com/maps/api/geocode/xml?address=+DA14+4EG&sensor=false&region=gb"
			address = postcode;
            		url = new URL(GEOCODER_REQUEST_PREFIX_FOR_XML + "?address=" + URLEncoder.encode(address, "UTF-8") + "&client=" + googleID + "&sensor=false");
                	UrlSigner signer = new UrlSigner(googleKey);
                	request = signer.signRequest(url.getPath(),url.getQuery());
                } catch (Exception e) {
                	System.out.println(e);
                }

                URL signedUrl = null;
                HttpURLConnection httpUrlConn = null;

                Document geocoderResultDocument = null;
                try {
                	signedUrl = new URL(url.getProtocol() + "://" + url.getHost() + request);
                	httpUrlConn = (HttpURLConnection)signedUrl.openConnection();
                	// Open the connection and get results as InputSource.
                        httpUrlConn.connect();
                        InputSource geocoderResultInputSource = new InputSource(httpUrlConn.getInputStream());

                        // read result and parse into XML Document
                        geocoderResultDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(geocoderResultInputSource);

                } catch (Exception e) {
                        // unable to connect to api - need to put error handling in here
                        String errorMsg = "Unable to connect to Google API : " + e.toString();
                        System.out.println(e);
                }
                finally {
                        httpUrlConn.disconnect();
                }

                // prepare XPath
                XPath xpath = XPathFactory.newInstance().newXPath();

                // extract the result
                NodeList resultNodeList = null;
                // b) extract the locality for the first result
                try {
			resultNodeList = (NodeList)xpath.evaluate("/GeocodeResponse/status", geocoderResultDocument, XPathConstants.NODESET);
			System.out.println(resultNodeList.item(0).getTextContent());
			if ("OK".equals(resultNodeList.item(0).getTextContent())) {
				// Extract the coordinates of the first result
				resultNodeList = (NodeList) xpath.evaluate("/GeocodeResponse/result[1]/geometry/location/*", 
                                                                           geocoderResultDocument, 
                                                                           XPathConstants.NODESET);
				lat = Float.NaN;
				lng = Float.NaN;
				for(int i=0; i<resultNodeList.getLength(); ++i) {
					Node node = resultNodeList.item(i);
					if("lat".equals(node.getNodeName())) lat = Float.parseFloat(node.getTextContent());
					if("lng".equals(node.getNodeName())) lng = Float.parseFloat(node.getTextContent());
				}
				System.out.println("lat/lng=" + lat + "," + lng);

				coord[0] = lat;
				coord[1] = lng;
			} else {
				String googleError = resultNodeList.item(0).getTextContent();
                                WatchDog.log(WatchDog.WATCHDOG_ENV_PREV, "Geocode",
                                     String.format("Google Geocode returned an issue for %s: %s", postcode, googleError), 
                                     WatchDog.WATCHDOG_ALERT);
			}
		} catch (Exception e) {
                        WatchDog.log(WatchDog.WATCHDOG_ENV_PREV, "Geocode",
                                     String.format("Failed to get coords for %s", postcode), WatchDog.WATCHDOG_WARNING);
		}

                // update the cache
                setCache(postcode, coord);
        	return coord;
	}

        private static float[] getCache(String postcode) throws Exception {
                float lat = 0.0f;
                float lng = 0.0f;

                // check the internal map
                if (geoMap.size() == 0) {
                    geoMap = populateCache(); 
                }

                List cCoords = (List)geoMap.get(postcode);
                if (cCoords != null) {
                    lat = ((BigDecimal)cCoords.get(0)).floatValue();
                    lng = ((BigDecimal)cCoords.get(1)).floatValue();
                } else {
                ApplicationContext context = SpringUtil.getApplicationContext();
                JdbcTemplate jT_Common = new JdbcTemplate();
                jT_Common.setDataSource((DataSource)context.getBean("common"));
                String sql = "SELECT latitude, longitude FROM geocode_cache WHERE postcode = '" + postcode + "'";
                List coords = jT_Common.queryForList(sql);
                if (!coords.isEmpty() && coords.size() == 1) {
                    Map map = (Map)coords.get(0);
                    lat = ((java.math.BigDecimal)map.get("latitude")).floatValue();
                    lng = ((java.math.BigDecimal)map.get("longitude")).floatValue();
                } else {
                    throw new Exception("no cache value found");
                }
                }
                float[] latlng = {lat, lng};
                return latlng;
        }

        private static void setCache(String postcode, float[] coords) {
        }

        private static Map populateCache() {
                float lat = 0.0f;
                float lng = 0.0f;
                
                ApplicationContext context = SpringUtil.getApplicationContext();
                JdbcTemplate jT_Common = new JdbcTemplate();
                jT_Common.setDataSource((DataSource)context.getBean("common"));
                
                StopWatch sw = new StopWatch("a");
                sw.start("test 1");

                String sql = "SELECT postcode, latitude, longitude FROM geocode_cache";
                
                Map<String, List> geoMap = (Map)jT_Common.query(sql, new ResultSetExtractor() {  
                    public Object extractData(ResultSet rs) throws SQLException { 
                        Map map = new HashMap();  
                        while (rs.next()) {  
                            List latlng = new ArrayList();  
                            String postcode = rs.getString("postcode");  
                            latlng.add(rs.getBigDecimal("latitude"));
                            latlng.add(rs.getBigDecimal("longitude"));
                            map.put(postcode, latlng);  
                            //System.out.println(latlng + " = " + latlng.size());  
                        }  
                        System.out.println("Geocode map size = " + map.size());  
                        return map;  
                    };  
                });  

                sw.stop();
                System.out.println(sw.prettyPrint());
                return geoMap;
        }

}
