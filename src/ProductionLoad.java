import java.text.*;

import javax.transaction.Synchronization;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

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

public class ProductionLoad {

	Logger logger = Logger.getLogger(this.getClass().getName());
        private Session g_session;
        private String g_nowAsString;
        private ETLContext context;
        String basedir = "";
        Properties sysprops = null;

	public void init() {
		context = ETLContext.getContext();
		HibernateUtil.buildSessionFactory();
                g_session = HibernateUtil.getSessionFactory().openSession()
                                .getSession(EntityMode.DOM4J);
		sysprops = (Properties)SpringUtil.getApplicationContext().getBean("sysprops");
                basedir = sysprops.getProperty("basedir");
        }

	public void run() {
	}
}
