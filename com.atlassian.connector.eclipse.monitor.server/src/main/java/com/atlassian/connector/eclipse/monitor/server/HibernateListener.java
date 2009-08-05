package com.atlassian.connector.eclipse.monitor.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateListener implements ServletContextListener {

	private static Logger log = LoggerFactory.getLogger(HibernateListener.class);
	
    public void contextInitialized(ServletContextEvent event) {
    	log.debug("Setting up hibernate session factory");
        HibernateUtil.getSessionFactory(); // Just call the static initializer of that class    
    }

    public void contextDestroyed(ServletContextEvent event) {
    	log.debug("Closing hibernate session factory");
        HibernateUtil.getSessionFactory().close(); // Free all resources
    }
}
