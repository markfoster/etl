
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
//import org.w3c.dom.Document;
//import org.w3c.dom.*;
//import org.w3c.dom.NodeList;
//import org.xml.sax.InputSource;
import org.apache.log4j.Logger;

import java.io.*;
import java.io.StringReader;
import java.util.*;

import org.dom4j.XPath;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.io.OutputFormat;

import org.hibernate.EntityMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class parse {

	//Logger logger = Logger.getLogger(this.getClass().getName());
	Logger logger = Logger.getLogger("stdout");

        public static void main(String[] arg) throws IOException {

                HibernateUtil.buildSessionFactory();
                System.out.println("Hello");
                parse p = new parse();

                //String xml = fileToString("hbm/Geocode.hbm.xml");
                //if (p.checkXML(xml) != null) System.out.println("XML is well formed");

                String xml = fileToString("xml/audit.xml");
                String xsd = fileToString("xsd/PP_AUDIT_XML.xsd");
		String result = "";
                try {
                if (p.checkXML(xml) != null) System.out.println("XML is well formed");
                    	p.checkXSD(xsd);
 			p.validateXML(xml, xsd);
                } catch (Exception ex) {
                	System.out.println("Audit XML issues: " + ex.getMessage());
			System.exit(0);
		}

                Map<String, Document> docs = new HashMap();

		// Parse the audit file
		Map audits = p.parse(xml);
                Set keys = audits.keySet();
                Iterator i = keys.iterator();
                while (i.hasNext()) {
                       String key = (String)i.next();
                       if (key != "Provider") continue;
		       String xmlFile = "xml/" + key + ".xml";
		       String xsdFile = "xsd/PP_" + key.toUpperCase() + "_XML.xsd";
		       System.out.println("Load and Validate (" + xmlFile + ", " + xsdFile + ")");
	               xml = fileToString(xmlFile);	
	               xsd = fileToString(xsdFile);	
                       Document doc = null;
                       try {
                       	   doc = p.checkXML(xml);
                           docs.put(key, doc);
                           p.checkXSD(xsd);
                           p.validateXML(xml, xsd);
			   p.validateActions(key, xml, (Map)audits.get(key));
			   p.loadXML(doc, key);
			   //p.checkData(doc, (Map)audits.get(key));
		       } catch (Exception ex) {
			   System.out.println("Invalid XML: " + ex.getMessage());
                           System.exit(0);
		       }
                       //System.out.print(key + ": ");
                       //System.out.println(smap.get(key));
                }

		int updates = Integer.parseInt("1");
                System.out.println("Updates = " + updates);

                //HibernateUtil.buildSessionFactory();
		//p.saveProviders();
		//p.saveOutcomes();
                //p.readGeocodes();
        }

	public void checkData(Document doc, Map checks) {
		logger.info("In checkData()");
		System.out.println(doc);
		System.out.println(checks);
        }

        public void readGeocodes() {
                Session session = null;
                try {
                        //session = HibernateUtil.getSessionFactory().openSession().getSession(EntityMode.DOM4J);
                        session = HibernateUtil.currentSession();
                        // List all providers
                        Session dom4jSession = session.getSession(EntityMode.DOM4J);
                        Query q = dom4jSession.createQuery("FROM cqc_address_cache");
                        List results = q.list();
                        org.dom4j.Document document = DocumentHelper.createDocument();
                        Element rootElement = document.addElement("Geocodes");
                        for (int i = 0; i < results.size(); i++) {
                                Element catalog = (Element) results.get(i);
                                rootElement.add(catalog);
                        }
                        OutputFormat format = OutputFormat.createPrettyPrint();
                } catch(Exception e) {
                        e.printStackTrace();
                        System.out.println(e.getMessage());
                } finally {
                        session.close();
                }
        }

	public void saveProviders() {
		Session session = null;
		try {
			//session = HibernateUtil.getSessionFactory().openSession().getSession(EntityMode.DOM4J);
			session = HibernateUtil.currentSession();
	        	// List all providers
			Session dom4jSession = session.getSession(EntityMode.DOM4J);
			Query q = dom4jSession.createQuery("FROM Provider");
			List results = q.list();
            		org.dom4j.Document document = DocumentHelper.createDocument();
            		Element rootElement = document.addElement("Providers");
            		for (int i = 0; i < results.size(); i++) {
                		Element catalog = (Element) results.get(i);
                		rootElement.add(catalog);
            		}
            		OutputFormat format = OutputFormat.createPrettyPrint();

            		XMLWriter output = new XMLWriter(new FileWriter(new java.io.File("/tmp/providers.xml")), format);
            		output.setIndentLevel(1);
            		output.write(document);
            		output.close();

            		StringWriter sw = new StringWriter();
            		XMLWriter writer = new XMLWriter(sw, format);
            		writer.write(document);
            		System.out.println(sw.toString());
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		} finally {
			session.close();
		}
	}

   	public boolean validateActions(String entity, String xml, Map actions) {
		System.out.println("Validating Actions for " + entity);
		System.out.println("Passed map = " + actions);
                Document doc = null;

		int iDelete = 0, iUpdate = 0, iInsert = 0;

                try {
			SAXReader sax = new SAXReader();
			doc = sax.read(new StringReader(xml));
			List list = doc.selectNodes("//" + entity + "/Action_Code");
			System.out.println(list);
			for (Iterator iter = list.iterator(); iter.hasNext(); ) {
            	             Element e = (Element)iter.next();
            		     String action = e.getText();
                             if (action.equals("I")) iInsert++;
                             if (action.equals("U")) iUpdate++;
                             if (action.equals("D")) iDelete++;
                             logger.info("action  = " + action);
        		}
                        int cInsert = 0, cDelete = 0, cUpdate = 0;
                        cInsert = Integer.parseInt((String)actions.get("inserts"));
                        cUpdate = Integer.parseInt((String)actions.get("updates"));
                        cDelete = Integer.parseInt((String)actions.get("deletes"));
                         
                        if (iInsert != cInsert) System.out.println("Mis-match in Inserts");
                        if (iUpdate != cUpdate) System.out.println("Mis-match in Updates");
                        if (iDelete != cDelete) System.out.println("Mis-match in Deletes");

                        return false;
                } catch (Exception e) {
                        logger.error("Error", e);
                        return false;
                }
        }

       public boolean loadXML(String xml, String entity) {
                System.out.println("Loading XML " + entity);
                Document doc = null;
                try {
                        SAXReader sax = new SAXReader();
                        doc = sax.read(new StringReader(xml));
                        return loadXML(doc, xml);
                } catch (Exception e) {
                        logger.error("Error", e);
                        return false;
                }
       }

       public boolean loadXML(Document doc, String entity) {
                System.out.println("Loading XML " + entity);
                try {
                        List list = doc.selectNodes("//" + entity);
                        for (Iterator iter = list.iterator(); iter.hasNext(); ) {
                             Object e = iter.next();
                             System.out.println(e);
                             //HibernateUtil.currentSession().save(entity, e);
                        }
                        return false;
                } catch (Exception e) {
                        logger.error("Error", e);
                        return false;
                }
        }
 
        public Map parse(String xml) {
 		Document doc = null;
		try {
			SAXReader sax = new SAXReader();
			doc = sax.read(new StringReader(xml));
                } catch (Exception e) {
                        logger.error("Error", e);
                        return null;
                }
		TreeMap<String, Map<String, String>> smap = new TreeMap<String, Map<String, String>>();
 		visit(smap, doc.getRootElement(), 0);
/**
                Set keys = smap.keySet(); 
                Iterator i = keys.iterator();
                while (i.hasNext()) {
                       String key = (String)i.next();  
		       System.out.print(key + ": "); 
		       System.out.println(smap.get(key));
                }
**/
 		return smap; 
	}

 	public void visit(Map map, Element e, int level) {
		// iterate through child elements of root
        	for (Iterator i = e.elementIterator(); i.hasNext(); ) {
                     Element e1 = (Element)i.next();
		     if (e1.attributeCount() > 0)
                         getAttributes(map, e1);
                     visit(map, e1, level+1);
        	}
	}

    	private void getAttributes(Map map, Element e) {
                Map<String, String> attrMap = new HashMap<String, String>();
		for (Iterator i = e.attributeIterator(); i.hasNext(); ) {
                     Attribute attribute = (Attribute)i.next();
            	     System.out.println(e.getName() + " : " + attribute.getName() + "=" + attribute.getValue());
                     attrMap.put((String)attribute.getName(), (String)attribute.getValue());
        	}
                map.put(e.getName(), attrMap);
    	}

	public Document checkXML(String xml) throws Exception {
		logger.info("in checkXML()");
                Document doc = null;
                try {
                        SAXReader sax = new SAXReader();
                        doc = sax.read(new StringReader(xml));
                } catch (Exception e) {
                        logger.error("Error", e);
                        return null;
                }
                return doc;
	}

	public String checkXSD(String xsd) throws Exception {
		logger.info("in checkXSD()");
		try {
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			factory.newSchema(new StreamSource(new StringReader(xsd)));
		} catch (Exception e) {
			logger.error("Error", e);
			throw new Exception(e.getMessage());
		}
		return null;
	}

        public void dumpXMLNode(Element e) {
                logger.info("Dumping element...");
                org.hibernate.util.XMLHelper.dump(e);
        }

        public String validateXML(String xml, String xsd) throws Exception {
 		logger.info("in validateXML()");
		MyErrorHandler errorHandler = new MyErrorHandler();
		try {
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory.newSchema(new StreamSource(new StringReader(xsd)));
			Validator validator = schema.newValidator();
			validator.setErrorHandler(errorHandler);
			validator.validate(new StreamSource(new StringReader(xml)));
		} catch (Exception e) {
			logger.info("Error", e);
			throw new Exception(e.getMessage());
		}
		String getResult = errorHandler.getResult();
		if (!getResult.isEmpty())
                    throw new Exception(getResult);
		return "The XML is Well Formed and VALID";
        }

	static class MyErrorHandler implements ErrorHandler {
		StringBuilder result = new StringBuilder();
		public void fatalError(SAXParseException e) throws SAXException {
			result.append(e.toString());
		}
		public void error(SAXParseException e) throws SAXException {
			result.append(e.toString());
		}
		public void warning(SAXParseException e) throws SAXException {
			result.append(e.toString());
		}
		public String getResult() {
			return result.toString();
		}
	}

    /**
     * Description of the Method
     *
     * @param file
     *          The file to be turned into a String
     * @return  The file as String encoded in the platform
     * default encoding
     */
	private static String fileToString( String file ) throws IOException {
    BufferedReader reader = new BufferedReader( new FileReader (file));
    String line  = null;
    StringBuilder stringBuilder = new StringBuilder();
    String ls = System.getProperty("line.separator");
    while( ( line = reader.readLine() ) != null ) {
        stringBuilder.append( line );
        stringBuilder.append( ls );
    }
    return stringBuilder.toString();
 	}

}
