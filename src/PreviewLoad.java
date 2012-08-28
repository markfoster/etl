import java.text.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import javax.transaction.Synchronization;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.io.OutputFormat;

import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Mappings;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.KeyValue;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.metadata.*;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.EntityMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.util.StopWatch;

/**
 * 
 * @author Mark
 * 
 */
public class PreviewLoad {

    Logger             logger   = Logger.getLogger(this.getClass().getName());
    private Session    g_session;
    private String     g_nowAsString;
    private ETLContext context;
    String             basedir  = "";
    Properties         sysprops = null;

    /**
     * 
     */
    public void init() {
	context = ETLContext.getContext();
	HibernateUtil.buildSessionFactory();
	g_session = HibernateUtil.getSessionFactory().openSession().getSession(EntityMode.DOM4J);
	sysprops = (Properties) SpringUtil.getApplicationContext().getBean("sysprops");
	basedir = sysprops.getProperty("basedir");
    }

    /**
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
	PreviewLoad pl = new PreviewLoad();
	pl.init();
	pl.test();
    }

    /**
     * 
     */
    public void test() {
	ETLContext.getContext().setRunId(0);
	System.out.println(ETLContext.getContext().getRunId());
	try {
	    loadAudit();
	    validateAudit();
	} catch (Exception ex) {
	    logger.warn(ex);
	}
    }

    /**
     * 
     * @throws Exception
     */
    public void loadAudit() throws Exception {
	String xml = "", xsd = "", result = "";
	try {
	    logger.info(basedir + "/xml/pp_audit_xml.xml");
	    xml = fileToString(basedir + "/xml/pp_audit_xml.xml");
	    xsd = fileToString(basedir + "/xsd/PP_AUDIT_XML.xsd");
	    checkXML(xml);
	    checkXSD(xsd);
	    validateXML(xml, xsd);
	} catch (Exception ex) {
	    ETLContext.getContext().alertMail(3, "Audit XML load failure", ex.getMessage());
	    WatchDog.log(WatchDog.WATCHDOG_ENV_PREV, "auditload", ex.getMessage(), WatchDog.WATCHDOG_WARNING);
	    throw new Exception("Audit Load or Validate issue: " + ex.getMessage());
	}
	// Parse the audit file
	Map audits = parse(xml);
	context.setAuditMap(audits);
    }

    /**
     * 
     * @return
     * @throws Exception
     */
    public boolean validateAudit() throws Exception {
	Map aMap = context.getAuditMap();
	Map rMap = context.readReport(basedir + "/xml/report.csv");
	if (null == rMap) {
	    throw new Exception("Cannot load actual metrics from " + basedir + "/xml/report.csv");
	}
        logger.info("Audit Map : " + aMap);
        logger.info("Report Map : " + rMap);
	Iterator i = aMap.keySet().iterator();
	while (i.hasNext()) {
	    String key = (String) i.next();
	    Map actions = (Map) aMap.get(key);
	    List aList = new ArrayList();
	    aList.add(actions.get("inserts"));
	    aList.add(actions.get("updates"));
	    aList.add(actions.get("deletes"));
	    List rList = (List) rMap.get(key.toLowerCase());
	    if (!aList.equals(rList)) {
		String err = String.format("Entity %s metrics issues: Audit=%s, Actual=%s", key, aList, rList);
		logger.warn(err);
		ETLContext.getContext().alertMail(3, "Audit XML Metrics validation failure", err);
		WatchDog.log(WatchDog.WATCHDOG_ENV_PREV, "auditload", err, WatchDog.WATCHDOG_WARNING);
		throw new Exception(err);
	    }
	}
	return true;
    }

    /**
     * 
     */
    public void run() {

	Map docs = new HashMap();

	try {
	    loadAudit();
	    validateAudit();
	} catch (Exception ex) {
	    WatchDog.log(WatchDog.WATCHDOG_ENV_PREV, "auditload", "Problem loading the audit: " + ex.getMessage(), WatchDog.WATCHDOG_EMERG);
	}

	try {
	    Map audits = context.getAuditMap();
	    Set keys = audits.keySet();
	    Iterator i = keys.iterator();
	    while (i.hasNext()) {
		String key = (String) i.next();

		g_session = HibernateUtil.getSessionFactory().openSession().getSession(EntityMode.DOM4J);

		Map actions = (Map) audits.get(key);
		String metric = String.format("Entity: %s, Actions: %s", key, actions);
		WatchDog.log(WatchDog.WATCHDOG_ENV_PREV, "auditload", metric, WatchDog.WATCHDOG_INFO);
		if (actions.get("active") == null)
		    continue;

		// if (!key.equals("Chapter")) continue;
		String xmlFile = basedir + "/xml/pp_" + key.toLowerCase() + "_xml.xml";
		String xsdFile = basedir + "/xsd/PP_" + key.toUpperCase() + "_XML.xsd";
		logger.info("Load and Validate (" + xmlFile + ", " + xsdFile + ")");
		Document doc = null;
		String xml = "", xsd = "", result = "";

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String nowAsString = df.format(new Date());
		logger.info("Update ID = " + nowAsString);
		g_nowAsString = nowAsString;
		ProcessState.setEntityState(key, (String) actions.get("load"));
		ProcessState.setEntityUniqueId(key, nowAsString);
		String sql = String.format("DELETE FROM %s", key.toLowerCase());
		g_session.createSQLQuery(sql).executeUpdate();

		loadXML(xmlFile, key);

		g_session.close();
	    }
	} catch (Exception ex) {
	    WatchDog.log(WatchDog.WATCHDOG_ENV_PREV, "xmlload", "Problem loading or validating xml data: " + ex.getMessage(), WatchDog.WATCHDOG_EMERG);
	} finally {

	}
    }

    public void test_run() {

        Map docs = new HashMap();

        try {
            loadAudit();
        } catch (Exception ex) {
            WatchDog.log(WatchDog.WATCHDOG_ENV_PREV, "auditload", "Problem loading the audit: " + ex.getMessage(), WatchDog.WATCHDOG_EMERG);
        }

        try {
            Map audits = context.getAuditMap();
            Set keys = audits.keySet();
            Iterator i = keys.iterator();
            while (i.hasNext()) {
                String key = (String) i.next();

                g_session = HibernateUtil.getSessionFactory().openSession().getSession(EntityMode.DOM4J);

                Map actions = (Map) audits.get(key);
                String metric = String.format("Entity: %s, Actions: %s", key, actions);
                WatchDog.log(WatchDog.WATCHDOG_ENV_PREV, "auditload", metric, WatchDog.WATCHDOG_INFO);
                if (actions.get("active") == null)
                    continue;

                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String nowAsString = df.format(new Date());
                logger.info("Update ID = " + nowAsString);
                g_nowAsString = nowAsString;
                ProcessState.setEntityState(key, (String) actions.get("load"));
                ProcessState.setEntityUniqueId(key, nowAsString);

                g_session.close();
            }
        } catch (Exception ex) {
            WatchDog.log(WatchDog.WATCHDOG_ENV_PREV, "xmlload", "Problem loading or validating xml data: " + ex.getMessage(), WatchDog.WATCHDOG_EMERG);
        } finally {

        }
    }

    /**
     * 
     * @param doc
     * @param checks
     */
    public void checkData(Document doc, Map checks) {
	logger.info("In checkData()");
	System.out.println(doc);
	System.out.println(checks);
    }

    /**
     * 
     */
    public void readGeocodes() {
	Session session = null;
	try {
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
	} catch (Exception e) {
	    e.printStackTrace();
	    System.out.println(e.getMessage());
	} finally {
	    session.close();
	}
    }

    /**
     * 
     * @param entity
     */
    public void saveEntity(String entity) {
	System.out.println("In saveProviders()");
	Session session = null;
	try {
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
	} catch (Exception e) {
	    System.out.println("saveProviders exception...");
	    e.printStackTrace();
	    System.out.println(e.getMessage());
	} finally {
	    if (session != null)
		session.close();
	}
    }

    /**
     * 
     * @param entity
     * @param xml
     * @param actions
     * @return
     */
    public boolean validateActions(String entity, String xml, Map actions) {
	logger.info("Validating Actions for " + entity);
	logger.info("Passed map = " + actions);
	Document doc = null;

	int iDelete = 0, iUpdate = 0, iInsert = 0;

	try {
	    SAXReader sax = new SAXReader();
	    doc = sax.read(new StringReader(xml));
	    List list = doc.selectNodes("//" + entity + "/Action_Code");
	    for (Iterator iter = list.iterator(); iter.hasNext();) {
		Element e = (Element) iter.next();
		String action = e.getText();
		if (action.equals("I"))
		    iInsert++;
		if (action.equals("U"))
		    iUpdate++;
		if (action.equals("D"))
		    iDelete++;
		// logger.info("action  = " + action);
	    }
	    int cInsert = 0, cDelete = 0, cUpdate = 0;
	    cInsert = Integer.parseInt((String) actions.get("inserts"));
	    cUpdate = Integer.parseInt((String) actions.get("updates"));
	    cDelete = Integer.parseInt((String) actions.get("deletes"));

	    if (iInsert != cInsert)
		System.out.println("Mis-match in Inserts");
	    if (iUpdate != cUpdate)
		System.out.println("Mis-match in Updates");
	    if (iDelete != cDelete)
		System.out.println("Mis-match in Deletes");

	    return false;
	} catch (Exception e) {
	    logger.error("Error", e);
	    return false;
	}
    }

    /**
     * 
     * @param xmlfile
     * @param entity
     * @return
     */
    public boolean loadXML(String xmlfile, String entity) {
	StopWatch sw = new StopWatch();
	sw.start("XML load");
	Document doc = null;
	try {
	    SAXReader reader = new SAXReader();
	    Map attrMap = (Map) ETLContext.getContext().getAuditMap().get(entity);
	    int iTotal = Integer.parseInt((String) attrMap.get("total"));
/**
	    if (iTotal > 300000) {
		logger.warn("Items are > 300,000, terminating load");
		sw.stop();
		return false;
	    }
**/
	    logger.info(String.format("XML load for %s, %d elements", entity, iTotal));
	    String elementPath = String.format("/List_Of_%s/%s", entity, entity);
	    logger.info("ElementPath = '" + elementPath + "'");
	    reader.addHandler(elementPath, new CQCEventHandler(this, entity, iTotal));
	    doc = reader.read(new File(xmlfile));
	} catch (Exception e) {
	    logger.error("Error", e);
	}
	sw.stop();
	double sTime = sw.getTotalTimeSeconds();
	logger.info(String.format("XML load for %s, completed in %f seconds", entity, sTime));
	return true;
    }

    /**
     * 
     * @author Mark
     *
     */
    private class CQCEventHandler implements ElementHandler {

	Logger      logger   = Logger.getLogger(this.getClass().getName());
	int         iCount   = 0;
	int         iTotal   = 0;
	int         iDelta   = 1;
	String      entity   = "";
	PreviewLoad instance = null;

	/**
	 * 
	 * @param instance
	 * @param entity
	 * @param iTotal
	 */
	public CQCEventHandler(PreviewLoad instance, String entity, int iTotal) {
	    this.iTotal = iTotal;
	    this.instance = instance;
	    this.entity = entity;
	    if (iTotal > 10000)
		iDelta = 2000;
	    else if (iTotal > 1000)
		iDelta = 200;
	}

	/**
	 * @param path
	 */
	public void onStart(ElementPath path) {
	    // do nothing here...
	    Element element = path.getCurrent();
	    // logger.info("onStart " + element);
	}

	/**
	 * @param path
	 */
	public void onEnd(ElementPath path) {
	    iCount++;
	    // process a ROW element
	    Element row = path.getCurrent();
	    if (iCount % iDelta == 0 || iCount == iTotal) {
		String string = String.format("Processing XML element: %s %d / %d", row.getName(), iCount, iTotal);
		logger.info(string);
	    }
	    // add the instance to the database
	    instance.treeProcess(row, entity);
	    // prune the tree
	    row.detach();
	}
    }

    /**
     * 
     * @param element
     * @param entity
     */
    public void treeProcess(Element element, String entity) {
	Element eUpdated = DocumentHelper.createElement("Last_Updated");
	eUpdated.setText(g_nowAsString);
	element.add(eUpdated);
	try {
	    Transaction tx = g_session.beginTransaction();
	    /**	    
	    ** MSF 15/07/12 - Possible optimisation to get / set the GeoCode values at the XML level
	    if (entity.equals(Entity.PROVIDER) || entity.equals(Entity.LOCATION)) {
		try {
		    String postcode = "1234";
		    Element ePostcode = element.element("Postcode");
		    if (ePostcode != null) {
			postcode == ePostcode.getText();
		    }
		    // Geocode the entity
		    float[] coords = GeoCode.getAddress(postcode);
		} catch (Exception ex) {
		    logger.warn("Cannot GeoCode items");
		}
	    }
	    **/	    
	    g_session.saveOrUpdate(element);
	    tx.commit();
	    g_session.evict(element);
	    g_session.flush();
	} catch (Exception ex) {
	    logger.error("Error", ex);
            WatchDog.log(WatchDog.WATCHDOG_ENV_PREV, "previewload", 
                  String.format("Issue loading entity: %s, element: issue:%s", 
                                entity, 
                                Entity.getPrimaryKey(element, entity), ex.getMessage()), 
                  WatchDog.WATCHDOG_WARNING);
	}
    }

    /**
     * 
     * @param xml
     * @return
     */
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
	return smap;
    }

    /**
     * 
     * @param map
     * @param e
     * @param level
     */
    public void visit(Map map, Element e, int level) {
	// iterate through child elements of root
	for (Iterator i = e.elementIterator(); i.hasNext();) {
	    Element e1 = (Element) i.next();
	    if (e1.attributeCount() > 0)
		getAttributes(map, e1);
	    visit(map, e1, level + 1);
	}
    }

    /**
     * 
     * @param map
     * @param e
     */
    private void getAttributes(Map map, Element e) {
	Map<String, String> attrMap = new HashMap<String, String>();
	int iTotal = 0;
	for (Iterator i = e.attributeIterator(); i.hasNext();) {
	    Attribute attribute = (Attribute) i.next();
	    // logger.info(e.getName() + " : " + attribute.getName() + "="
	    // + attribute.getValue());
	    String value = (String) attribute.getValue();
	    attrMap.put((String) attribute.getName(), value);
	    try {
		int iVal = Integer.parseInt(value);
		iTotal += iVal;
		if (iVal > 0)
		    attrMap.put("active", "1");
	    } catch (Exception ex) {
	    }
	}
	attrMap.put("total", (new Integer(iTotal)).toString());
	map.put(e.getName(), attrMap);
    }

    /**
     * 
     * @param xml
     * @return
     * @throws Exception
     */
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

    /**
     * 
     * @param xsd
     * @return
     * @throws Exception
     */
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

    /**
     * 
     * @param e
     */
    public void dumpXMLNode(Element e) {
	logger.info("Dumping element...");
	org.hibernate.util.XMLHelper.dump(e);
    }

    /**
     * 
     * @param xml
     * @param xsd
     * @return
     * @throws Exception
     */
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
     * Generate a String from file contents
     * 
     * @param file The file to be turned into a String
     * @return The file as String encoded in the platform default encoding
     * @throws IOException
     */
    private String fileToString(String file) throws IOException {
	BufferedReader reader = new BufferedReader(new FileReader(file));
	String line = null;
	StringBuilder stringBuilder = new StringBuilder();
	String ls = System.getProperty("line.separator");
	while ((line = reader.readLine()) != null) {
	    stringBuilder.append(line);
	    stringBuilder.append(ls);
	}
	return stringBuilder.toString();
    }

}
