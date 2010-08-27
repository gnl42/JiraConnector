package com.atlassian.connector.eclipse.monitor.server.servlet;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.eclipse.mylyn.monitor.core.UserInteractionEvent;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.connector.eclipse.monitor.core.UsageDataUtil2;
import com.atlassian.connector.eclipse.monitor.server.HibernateUtil;
import com.atlassian.connector.eclipse.monitor.server.model.DailyStatistic;

/**
 * Servlet implementation class UploadServlet
 */
public class UploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String DESCRIPTION_URL = "descriptionUrl";
	private static final Logger log = LoggerFactory.getLogger(UploadServlet.class);
	
	private String descriptionUrl;
	
	/**
     * Default constructor. 
     */
    public UploadServlet() {
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
    	super.init(config);
    	
    	descriptionUrl = config.getInitParameter(DESCRIPTION_URL);
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.warn(String.format("GET is not supported, redirecting to %s", descriptionUrl));
		
		if (descriptionUrl == null) {
			response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		} else {
			response.sendRedirect(descriptionUrl);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug(String.format("Upload request from %s", request.getRemoteAddr()));
		
		if (!ServletFileUpload.isMultipartContent(request)) {
			log.warn("Multipart content is required");
			response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}
		
		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		
		List<?> files = null;
		try {
			log.debug("Parsing request");
			files = upload.parseRequest(request);
		} catch(FileUploadException e) {
			log.error("Upload failed", e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		
		if (files != null) {
			Session session = null;
			try {
				session = HibernateUtil.getSessionFactory().openSession();
				updateDailyStats(session, request.getServletPath().endsWith("-2") ? loadFiles2(session, files) : loadFiles(session, files));
			} finally {
				if (session != null) {
					try {
						session.close();
					} catch(Exception e) {
						// ignore
					}
				}
 			}
		}
		
		log.debug(String.format("Finished processing request from %s", request.getRemoteAddr()));
	}
	
	private void updateDailyStats(Session session, DailyStatistic stat) {
		Transaction tx = session.beginTransaction();
		
		Date today = Calendar.getInstance().getTime();
		DailyStatistic daily = (DailyStatistic) session.get(DailyStatistic.class, today);
		if (daily == null) {
			daily = new DailyStatistic(today, 1, stat.getEntriesSucceeded(), stat.getEntriesFailed(), 
					stat.getEntriesConflicting());
		} else {
			daily.update(stat);
		}
		session.saveOrUpdate(daily);
		tx.commit();
	}

	private DailyStatistic loadFiles(final Session session, final List<?> files) {
		final int[] succeeded = new int[] {0};
		final int[] failed = new int[] {0};
		final int[] conflicts = new int[] {0};
		
		log.debug("Loading files");
		
		try {
			for (Object fileObj : files) {
				UsageDataUtil.processFile((FileItem) fileObj, new UsageDataUtil.UserInteractionEventCallback() {
					public boolean visit(UserInteractionEvent uie) {
						// put as much data as we can into db, don't care if something is dropped
						Transaction tx = null;
						try {
							tx = session.beginTransaction();
							session.persist(uie);
							tx.commit();
							
							++succeeded[0];
						} catch(Exception e) {
							if (tx != null) {
								try {
									tx.rollback();
								} catch(Exception e1) {
									// ignore
								}
							}
							
							if (e instanceof ConstraintViolationException) {
								// tried to put data again (happens sometimes)
								++conflicts[0];
							} else {
								log.warn("Failed to store UserInteractionEvent", e);
								++failed[0];
							}
						}
						return true;
					}
				});
			}
		} catch (Exception e) {
			log.error("Exception while storing UserInteractionEvent", e);
		}
		
		log.debug("Files loaded");
		
		return new DailyStatistic(null, 1, succeeded[0], failed[0], conflicts[0]);
	}
	
	private DailyStatistic loadFiles2(final Session session, final List<?> files) {
		final int[] succeeded = new int[] {0};
		final int[] failed = new int[] {0};
		final int[] conflicts = new int[] {0};
		
		log.debug("Loading files");
		
		try {
			for (Object fileObj : files) {
				UsageDataUtil2.processFile((FileItem) fileObj, new UsageDataUtil2.UserInteractionEventCallback() {
					public boolean visit(
							com.atlassian.connector.eclipse.monitor.core.UserInteractionEvent uie) {
						// put as much data as we can into db, don't care if something is dropped
						Transaction tx = null;
						try {
							tx = session.beginTransaction();
							session.persist(uie);
							tx.commit();
							
							++succeeded[0];
						} catch(Exception e) {
							if (tx != null) {
								try {
									tx.rollback();
								} catch(Exception e1) {
									// ignore
								}
							}
							
							if (e instanceof ConstraintViolationException) {
								// tried to put data again (happens sometimes)
								++conflicts[0];
							} else {
								log.warn("Failed to store UserInteractionEvent", e);
								++failed[0];
							}
						}
						return true;
					}
				});
			}
		} catch (Exception e) {
			log.error("Exception while storing UserInteractionEvent", e);
		}
		
		log.debug("Files loaded");
		
		return new DailyStatistic(null, 1, succeeded[0], failed[0], conflicts[0]);
	}
	
}
