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
//import org.w3c.dom.Document;
//import org.w3c.dom.*;
//import org.w3c.dom.NodeList;
//import org.xml.sax.InputSource;
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

public class ETLProductionLoad {

	Logger logger = Logger.getLogger(this.getClass().getName());

	public static void main(String[] arg) throws IOException {
		ETLProductionLoad p = new ETLProductionLoad();
                p.run();
        }

	public void run() {
		
                //ProcessState.setSystemState(ProcessState.PROD_LOAD_TRIGGER);
                //ProcessState.setLock(ProcessState.LOCK_CLEAR);

                // Check the process system status
                String pState = ProcessState.getSystemState();
                if (!pState.equals(ProcessState.PROD_LOAD_TRIGGER)) {
                    logger.error("Initiating Production Load but system state = " + pState);
                    WatchDog.log(WatchDog.WATCHDOG_ENV_PROD, "prodload", "State != PROD_LOAD_TRIGGER", WatchDog.WATCHDOG_CRITICAL);
                    System.exit(1);
                }
 
                // Check the process lock status
                String pLock = ProcessState.getLock();
                if (!pLock.equals(ProcessState.LOCK_CLEAR)) {
                    logger.error("Initiating Production Load but lock state = " + pLock);
                    WatchDog.log(WatchDog.WATCHDOG_ENV_PROD, "prodload", "Lock != CLEAR exiting", WatchDog.WATCHDOG_CRITICAL);
                    System.exit(1);
                }

                // Populate the preview delta from XML files	
                ProcessState.setLock(ProcessState.LOCK_SET);
                ProcessState.setSystemState(ProcessState.PROD_DELTA_LOAD_IN_PROGRESS);
                WatchDog.log(WatchDog.WATCHDOG_ENV_PROD, "prodload",
                                  "Acquired lock and changed process state to PROD_DELTA_LOAD_IN_PROGRESS",
                                  WatchDog.WATCHDOG_INFO);
		ProductionLoad pl = new ProductionLoad();
                pl.init();
                pl.run();
                ProcessState.setLock(ProcessState.LOCK_CLEAR);
                ProcessState.setSystemState(ProcessState.PROD_DELTA_LOAD_COMPLETE);
                WatchDog.log(WatchDog.WATCHDOG_ENV_PROD, "prodload",
                                  "Released lock and changed process state to PROD_DELTA_LOAD_COMPLETE",
                                  WatchDog.WATCHDOG_INFO);
	}

}
