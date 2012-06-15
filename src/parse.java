import java.text.*;

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

        private Session g_session;
        private String  g_nowAsString; 

        public static void main(String[] arg) throws IOException {

                HibernateUtil.buildSessionFactory();
                parse p = new parse();

		//p.saveEntity("Location");
                //if (true) System.exit(0);

                //String xml = fileToString("hbm/Geocode.hbm.xml");
                //if (p.checkXML(xml) != null) System.out.println("XML is well formed");

                String xml = fileToString("xml/pp_audit_xml.xml");
                String xsd = fileToString("xsd/PP_AUDIT_XML.xsd");
		String result = "";
                try {
                	if (p.checkXML(xml) != null) 
                            System.out.println("XML is well formed");
                    	p.checkXSD(xsd);
 			p.validateXML(xml, xsd);
                } catch (Exception ex) {
                	System.err.println("Audit XML issues: " + ex.getMessage());
			System.exit(0);
		}

                Map<String, Document> docs = new HashMap();

		// Parse the audit file
		Map audits = p.parse(xml);
                Set keys = audits.keySet();
                Iterator i = keys.iterator();
                while (i.hasNext()) {
                       String key = (String)i.next();
                       //if (key != "Provider" && key != "Location") continue;
                       //if (key != "Provider") continue;
                       if (key != "Chapter") continue;
		       String xmlFile = "xml/pp_" + key.toLowerCase() + "_xml.xml";
		       String xsdFile = "xsd/PP_" + key.toUpperCase() + "_XML.xsd";
		       //logger.info("Load and Validate (" + xmlFile + ", " + xsdFile + ")");
		       System.out.println("Load and Validate (" + xmlFile + ", " + xsdFile + ")");
	               xml = fileToString(xmlFile);	
	               xsd = fileToString(xsdFile);	
                       Document doc = null;
                       try {
                       	   doc = p.checkXML(xml);
                           docs.put(key, doc);
                           p.checkXSD(xsd);
                           p.validateXML(xml, xsd);
			   //p.validateActions(key, xml, (Map)audits.get(key));
			   p.loadXML(doc, key);
			   //p.checkData(doc, (Map)audits.get(key));
		       } catch (Exception ex) {
			   System.err.println("Invalid XML: " + ex.getMessage());
                           System.exit(0);
		       }
                       //System.out.print(key + ": ");
                       //System.out.println(smap.get(key));
                }

		int updates = Integer.parseInt("1");
                System.out.println("Updates = " + updates);

		//p.saveEntity("Location");
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
			System.out.println("Session = " + session);

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

	public void saveEntity(String entity) {
		System.out.println("In saveProviders()");
		Session session = null;
		try {
			//session = HibernateUtil.getSessionFactory().openSession().getSession(EntityMode.DOM4J);
			System.out.println("Session = " + session);
			session = HibernateUtil.currentSession();
			System.out.println("Session = " + session);

	        	// List all providers
			Session dom4jSession = session.getSession(EntityMode.DOM4J);
			Query q = dom4jSession.createQuery("FROM " + entity);
			List results = q.list();
            		org.dom4j.Document document = DocumentHelper.createDocument();
            		Element rootElement = document.addElement("List_Of_" + entity);
            		for (int i = 0; i < results.size(); i++) {
                		Element catalog = (Element) results.get(i);
                		rootElement.add(catalog);
            		}
            		OutputFormat format = OutputFormat.createPrettyPrint();

            		XMLWriter output = new XMLWriter(new FileWriter(new java.io.File("/tmp/" + entity + ".xml")), format);
            		output.setIndentLevel(1);
            		output.write(document);
            		output.close();

            		StringWriter sw = new StringWriter();
            		XMLWriter writer = new XMLWriter(sw, format);
            		writer.write(document);
            		System.out.println(sw.toString());
		} catch(Exception e) {
			System.out.println("saveProviders exception...");
			e.printStackTrace();
			System.out.println(e.getMessage());
		} finally {
                        if (session != null)
			    session.close();
		}
	}

   	public boolean validateActions(String entity, String xml, Map actions) {
		logger.info("Validating Actions for " + entity);
		logger.info("Passed map = " + actions);
                Document doc = null;

		int iDelete = 0, iUpdate = 0, iInsert = 0;

                try {
			SAXReader sax = new SAXReader();
			doc = sax.read(new StringReader(xml));
			List list = doc.selectNodes("//" + entity + "/Action_Code");
			for (Iterator iter = list.iterator(); iter.hasNext(); ) {
            	             Element e = (Element)iter.next();
            		     String action = e.getText();
                             if (action.equals("I")) iInsert++;
                             if (action.equals("U")) iUpdate++;
                             if (action.equals("D")) iDelete++;
                             //logger.info("action  = " + action);
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
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowAsString = df.format(new Date());
        logger.info("Now = " + nowAsString);
              g_nowAsString = nowAsString;
	      Session session = HibernateUtil.getSessionFactory().openSession().getSession(EntityMode.DOM4J);
              g_session = session;
              treeWalk(doc, entity);
              return true;
       }

	/**
	 * Walk the tree 
	 * @param document
	 * @param entity
	 */
    private void treeWalk(Document document, String entity) {
        treeWalk(document.getRootElement(), entity);
    }

	/**
	 * Walk the tree 
	 * @param document
	 * @param entity
	 */
    private void treeWalk(Element element, String entity) {
        for (int i = 0, size = element.nodeCount(); i < size; i++) {
            Node node = element.node(i);
            if (node instanceof Element) {
                Element e = (Element)node;
                if (e.getName().equals(entity)) {
                    System.out.println("treeWalk 1 element = " + e.getName() + "; " + e.getText());
                    treeProcess(e);
		}
                else
                   treeWalk((Element)node, entity);
            } else {
                //System.out.println("treeWalk element = " + node.getName() + "; " + node.getText());
                //org.hibernate.util.XMLHelper.dump(node);
                //treeProcess(element);
            }
        }
    }	

    private void treeProcess(Element element) {
        Element eUpdated = DocumentHelper.createElement("Last_Updated");
	eUpdated.setText(g_nowAsString);
        element.add(eUpdated);
        //org.hibernate.util.XMLHelper.dump(element);
        try {
           Transaction tx = g_session.beginTransaction();
           g_session.save(element);
           tx.commit();
           //session.evict(obj);
           //session.flush();
        } catch (Exception ex) {
           logger.error("Error", ex);
        }
    }

       public boolean loadXML(Document doc, String entity, int i) {
                logger.info("Loading XML into delta database : " + entity);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowAsString = df.format(new Date());
        logger.info("Now = " + nowAsString);
                try {
			Session session = HibernateUtil.getSessionFactory().openSession().getSession(EntityMode.DOM4J);
                        //Session session = HibernateUtil.currentSession();
                        logger.info("Node selection starts");
                        List list = doc.selectNodes("//" + entity);
                        logger.info("Node selection complete");
                        logger.info("Load starting...");
                        for (Iterator iter = list.iterator(); iter.hasNext(); ) {
                             Object obj = iter.next();
                             Element e = (Element)obj;

                             //org.hibernate.util.XMLHelper.dump(e);

                             Element eUpdated = DocumentHelper.createElement("Last_Updated");
			     eUpdated.setText(nowAsString);
                             e.add(eUpdated);

/**
                             Element eAction = e.element("Action_Code");
                             System.out.println("Action Code = " + eAction.getName() + "; " + eAction.getText());

                             eAction = e.element("Provider_Id");
                             //eAction.setText("T-" + eAction.getText());
                             System.out.println("Provider Id = " + eAction.getName() + "; " + eAction.getText());

                             //org.hibernate.util.XMLHelper.dump(e);
			     //for (Iterator iter1 = e.elementIterator(); iter1.hasNext(); ) {
                             //     e1 = (Element)iter1.next();
                             //     System.out.println("Element : " + e1.getName() + "; " + e1.getText());
			     //}
**/

			     //try {
		             Transaction tx = session.beginTransaction();
             		     session.save(obj);
       			     tx.commit();
             		     //session.evict(obj);
             		     //session.flush();
			     //} catch (Exception ex) {
                             //    logger.error("Error", ex);
       			    // }
                        }
                } catch (Exception e) {
                        logger.error("Error", e);
                        return false;
                }
                logger.info("Load complete : " + entity);
                return true;
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
            	     logger.info(e.getName() + " : " + attribute.getName() + "=" + attribute.getValue());
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
