import java.util.*;
import java.util.List;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

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
        public static final String CHAPTER                      = "Chapter";
        public static final String LOCATION                     = "Location";
        public static final String LOCATION_CONDITION           = "Location_Condition";
        public static final String LOCATION_REGULATED_ACTIVITY  = "Location_Regulated_Activity";
        public static final String NOMINATED_INDIVIDUAL         = "Nominated_Individual";
        public static final String OUTCOME                      = "Outcome";
        public static final String PARTNER                      = "Partner";
        public static final String PROVIDER                     = "Provider";
        public static final String PROVIDER_CONDITION           = "Provider_Condition";
        public static final String PROVIDER_REGULATED_ACTIVITY  = "Provider_Regulated_Activity";
        public static final String REGISTERED_MANAGER           = "Registered_Manager";
        public static final String REGISTERED_MANAGER_CONDITION = "Registered_Manager_Condition";
        public static final String REPORT_SUMMARY               = "Report_Summary";
        public static final String SERVICE_TYPE                 = "Service_Type";
        public static final String SERVICE_USER_BAND            = "Service_User_Band";
        public static final String VISIT_DATE                   = "Visit_Date";


        private static Map primaryKeys = new HashMap(); // map of primary key properties list

	public static String getPrimaryKey(org.dom4j.Element element, String entity) {
		String primaryKey = "";
                if (primaryKeys.containsKey(entity)) {
                    List keys = (List)primaryKeys.get(entity);
                    Iterator key_props = keys.iterator();
                    while (key_props.hasNext()) {
                           String nodeName = (String)key_props.next();
                           if (primaryKey.length() != 0) primaryKey += "/";
                           org.dom4j.Element eKey = (org.dom4j.Element)element.element(nodeName);
                           if (eKey != null) primaryKey += eKey.getText();
                    }
                } else {
		try {
			Configuration cfg = HibernateUtil.getConfiguration();
			PersistentClass pc = cfg.getClassMapping(entity);
			KeyValue kv = pc.getIdentifier();
			if (kv instanceof Component) {
				Component comp = (Component) kv;
				Iterator<Property> key_props = comp.getPropertyIterator();
                                List keys = new ArrayList();
				while (key_props.hasNext()) {
					Property p = key_props.next();
					String name = p.getName();
					String nodeName = p.getNodeName();
                                        keys.add(nodeName);
					if (primaryKey.length() != 0) {
						primaryKey += "/";
					}
					org.dom4j.Element eKey = (org.dom4j.Element) element
							.element(nodeName);
					if (eKey != null)
						primaryKey += eKey.getText();
				}
                                primaryKeys.put(entity, keys);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
                }
		return primaryKey;
	}
}
