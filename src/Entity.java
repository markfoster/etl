import java.util.Iterator;

import org.dom4j.Document;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.KeyValue;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;

import org.w3c.dom.*;

public class Entity {

	// Entities
	public static final String PROVIDER = "Provider";
	public static final String LOCATION = "Location";

	public static String getPrimaryKey(org.dom4j.Element element, String entity) {
		String primaryKey = "";
		try {
			Configuration cfg = HibernateUtil.getConfiguration();
			PersistentClass pc = cfg.getClassMapping(entity);
			KeyValue kv = pc.getIdentifier();
			// System.out.println(">> identifier simple : " + kv);
			// System.out.println(">> identifier simple : " +
			// kv.isSimpleValue());
			if (kv instanceof Component) {
				Component comp = (Component) kv;
				Iterator<Property> key_props = comp.getPropertyIterator();
				while (key_props.hasNext()) {
					Property p = key_props.next();
					String name = p.getName();
					String nodeName = p.getNodeName();
					if (primaryKey.length() != 0) {
						primaryKey += "/";
					}
					org.dom4j.Element eKey = (org.dom4j.Element) element
							.element(nodeName);
					if (eKey != null)
						primaryKey += eKey.getText();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}

		return primaryKey;
	}

}