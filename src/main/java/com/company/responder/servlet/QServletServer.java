package com.company.responder.servlet;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.jpos.q2.QBeanSupport;

public class QServletServer extends QBeanSupport {
	
	private final static Logger log = Logger.getLogger(QServletServer.class);

	public QServletServer() {
		super();
	}

	@Override
	protected void startService() throws Exception {
		
        int port = Integer.valueOf(this.getConfiguration().get("port"));
        String contextPath = this.getConfiguration().get("context-path");
        
        Server server = new Server(port);
        ServletContextHandler servletContextHandler = new ServletContextHandler(server, contextPath, true, false);
        servletContextHandler.addServlet(FileServlet.class, "/file-servlet");
        server.start();
        server.join();
        log.info("Servlet Server started. Context-path: " + contextPath + ". Port: " + port);
	}
}
