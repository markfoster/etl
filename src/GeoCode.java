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

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;

public class GeoCode {

        private static final String googleKey = "Y6D95O6w_dXHs58ve2ixLrSV8tU=";
        private static final String googleID  = "gme-carequalitycommission";


	// URL prefix to the geocoder
        private static final String GEOCODER_REQUEST_PREFIX_FOR_XML = "http://maps.googleapis.com/maps/api/geocode/xml";

        public static void main(String[] args) {
		GeoCode gc = new GeoCode();
                //gc.getAddress("", "", "", postcode, "");
                //gc.checkAddress(postcode);
	}

        public float[] getAddress(String address1, 
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

                // System.out.println("Signed URL :" + url.getProtocol() + "://" + url.getHost() + request);
                // prepare an HTTP connection to the geocoder
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
                        String errorMsg = "Unable to connect to Google API" + e.toString();
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
			resultNodeList = (NodeList) xpath.evaluate("/GeocodeResponse/status", geocoderResultDocument, XPathConstants.NODESET);
			System.out.println(resultNodeList.item(0).getTextContent());
			if ("OK".equals(resultNodeList.item(0).getTextContent())) {
				// Extract the coordinates of the first result
				resultNodeList = (NodeList) xpath.evaluate("/GeocodeResponse/result[1]/geometry/location/*", geocoderResultDocument, XPathConstants.NODESET);
				lat = Float.NaN;
				lng = Float.NaN;
				for(int i=0; i<resultNodeList.getLength(); ++i) {
					Node node = resultNodeList.item(i);
					if("lat".equals(node.getNodeName())) lat = Float.parseFloat(node.getTextContent());
					if("lng".equals(node.getNodeName())) lng = Float.parseFloat(node.getTextContent());
				}
				System.out.println("lat/lng=" + lat + "," + lng);

				//writeData(adr1, adr2, city, county, Pcode, country, lat, lng,conndb);

				coord[0] = lat;
				coord[1] = lng;
			} else {

				StringBuffer mess = new StringBuffer("Error in java module geocodeAddress - Google unable to geocode address : ");
				mess.append(address);
				mess.append (" , ");
				mess.append(resultNodeList.item(0).getTextContent());
				//watchdog.watchdogUpdate("GeocodeAddress",mess.toString(),1,"n/a"," "," ","localhost", conndb);

			}
		} catch (Exception e) {
			System.out.println(e);
		}
        	return coord;
	}
}
