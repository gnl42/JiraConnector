package com.atlassian.connector.eclipse.monitor.server.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.mylyn.monitor.core.UserInteractionEvent;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;

import com.atlassian.connector.eclipse.monitor.server.HibernateUtil;

/**
 * Servlet implementation class UploadServlet
 */
public class UploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(UploadServlet.class);
	
	/**
     * Default constructor. 
     */
    public UploadServlet() {
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.warn("GET is not supported");
		response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!ServletFileUpload.isMultipartContent(request)) {
			log.warn("Multipart content is required");
			response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}
		
		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		
		List<?> files = null;
		try {
			files = upload.parseRequest(request);
		} catch(FileUploadException e) {
			log.error("Upload failed", e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		
		if (files != null) {
			loadFiles(files);
		}
	}
	
	private void loadFiles(final List<?> files) {
		final Session[] session = new Session[1];
		
		try {
			session[0] = HibernateUtil.getSessionFactory().openSession();
		
			for (Object fileObj : files) {
				UsageDataUtil.processFile((FileItem) fileObj, new UsageDataUtil.UserInteractionEventCallback() {
					public boolean visit(UserInteractionEvent uie) {
						// put as much data as we can into db, don't care if something is dropped
						Transaction tx = null;
						try {
							tx = session[0].beginTransaction();
							session[0].persist(uie);
							tx.commit();
						} catch(Exception e) {
							if (tx != null) {
								try {
									tx.rollback();
								} catch(Exception e1) {
									// ignore
								}
							}
							log.warn("Failed to store UserInteractionEvent", e);
						}
						return true;
					}
				});
			}
		} catch (Exception e) {
			log.error("Exception while storing UserInteractionEvent", e);
		} finally {
			if (session[0] != null) {
				try {
					session[0].close();
				} catch(Exception e) {
					// ignore
				}
			}
		}		
	}
	
}
