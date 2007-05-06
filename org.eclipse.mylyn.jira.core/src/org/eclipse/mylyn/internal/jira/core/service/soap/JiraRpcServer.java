/*******************************************************************************
 * Copyright (c) 2007 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.internal.jira.core.service.soap;

import java.io.File;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.rpc.ServiceException;

import org.apache.axis.AxisFault;
import org.apache.axis.configuration.FileProvider;
import org.eclipse.mylar.internal.jira.core.model.Attachment;
import org.eclipse.mylar.internal.jira.core.model.Comment;
import org.eclipse.mylar.internal.jira.core.model.Component;
import org.eclipse.mylar.internal.jira.core.model.Group;
import org.eclipse.mylar.internal.jira.core.model.Issue;
import org.eclipse.mylar.internal.jira.core.model.IssueType;
import org.eclipse.mylar.internal.jira.core.model.NamedFilter;
import org.eclipse.mylar.internal.jira.core.model.Priority;
import org.eclipse.mylar.internal.jira.core.model.Project;
import org.eclipse.mylar.internal.jira.core.model.Query;
import org.eclipse.mylar.internal.jira.core.model.Resolution;
import org.eclipse.mylar.internal.jira.core.model.ServerInfo;
import org.eclipse.mylar.internal.jira.core.model.Status;
import org.eclipse.mylar.internal.jira.core.model.User;
import org.eclipse.mylar.internal.jira.core.model.Version;
import org.eclipse.mylar.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylar.internal.jira.core.model.filter.IssueCollector;
import org.eclipse.mylar.internal.jira.core.model.filter.SingleIssueCollector;
import org.eclipse.mylar.internal.jira.core.model.filter.SmartQuery;
import org.eclipse.mylar.internal.jira.core.service.AbstractJiraServer;
import org.eclipse.mylar.internal.jira.core.service.JiraAuthenticationException;
import org.eclipse.mylar.internal.jira.core.service.JiraException;
import org.eclipse.mylar.internal.jira.core.service.JiraInsufficientPermissionException;
import org.eclipse.mylar.internal.jira.core.service.JiraServiceUnavailableException;
import org.eclipse.mylar.internal.jira.core.service.web.JiraWebIssueService;
import org.eclipse.mylar.internal.jira.core.service.web.rss.RssJiraFilterService;
import org.eclipse.mylar.internal.jira.core.wsdl.beans.RemoteIssue;
import org.eclipse.mylar.internal.jira.core.wsdl.soap.JiraSoapService;
import org.eclipse.mylar.internal.jira.core.wsdl.soap.RemoteAuthenticationException;
import org.eclipse.mylar.internal.jira.core.wsdl.soap.RemoteException;
import org.eclipse.mylar.internal.jira.core.wsdl.soap.RemotePermissionException;
import org.w3c.dom.Element;

// This class does not represent the data in a JIRA installation. It is merely
// a helper to get any data that is missing in the cached JiraInstallation
// object
// TODO do we want the ability to have a non cached JiraInstallation? Might be
// good
// if they had an RMI interface
// Make JiraInstallation an interface, implement a CachedInstallation which
// requires
// a concrete installation. The cached one can then forward on any requests it
// has
// not yet cached. Also need the ability to flush and fully re-load the cached
// installation
/**
 * @author Brock Janiczak
 * @author Steffen Pingel
 */
public class JiraRpcServer extends AbstractJiraServer {
	
	private static final String SOAP_SERVICE_URL = "/rpc/soap/jirasoapservice-v2";

	/**
	 * Default session timeout for a JIRA instance. The default value is 10
	 * minutes.
	 */
	private static final long DEFAULT_SESSION_TIMEOUT = 1000L * 60L * 10L;

	private JiraSoapService soapService = null;

	private Lock soapServiceLock = new ReentrantLock();
	
	private JiraWebIssueService issueService = null;

	private RssJiraFilterService filterService = null;

	private LoginToken loginToken;

	public JiraRpcServer(String baseURL, boolean useCompression, String username, String password,
			Proxy proxy, String httpUser, String httpPassword) {
		super(baseURL, useCompression, username, password, proxy, httpUser, httpPassword);

		filterService = new RssJiraFilterService(this);
		issueService = new JiraWebIssueService(this);

		if (username == null) {
			loginToken = new AnonymousLoginToken();
		} else {
			loginToken = new StandardLoginToken(username, password, DEFAULT_SESSION_TIMEOUT);
		}
	}
	
	private JiraSoapService getSoapService() throws JiraException {
		soapServiceLock.lock();
		try {
			if (soapService == null) {
				GZipJiraSoapServiceServiceLocator locator = new GZipJiraSoapServiceServiceLocator(new FileProvider(this
						.getClass().getClassLoader().getResourceAsStream("client-config.wsdd")));
				locator.setHttpUser(getHttpUser());
				locator.setHttpPassword(getHttpPassword());
				locator.setProxy(getProxy());
				locator.setCompression(useCompression());

				try {
					soapService = locator.getJirasoapserviceV2(new URL(getBaseURL() + SOAP_SERVICE_URL));
				} catch (ServiceException e) {
					throw new JiraException(e);
				} catch (MalformedURLException e) {
					throw new JiraException(e);
				}

				if (soapService == null) {
					throw new JiraException("Initialization of JIRA Soap service failed");
				}
			}
			return soapService;
		} finally {
			soapServiceLock.unlock();
		}
	}

	public User getUser(final String username) throws JiraException {
		return call(new RemoteRunnable<User>() {
			@Override
			public User run() throws java.rmi.RemoteException, JiraException {
				return Converter.convert(getSoapService().getUser(loginToken.getCurrentValue(), username));
			}
		});
	}

	public Component[] getComponentsRemote(final String projectKey) throws JiraException {
		return call(new RemoteRunnable<Component[]>() {
			@Override
			public Component[] run() throws java.rmi.RemoteException, JiraException {
				return Converter.convert(getSoapService().getComponents(loginToken.getCurrentValue(), projectKey));
			}
		});
	}

	public void login() throws JiraException {
		loginToken.expire();
		loginToken.getCurrentValue();
	}

	public Group getGroup(final String name) throws JiraException {
		return call(new RemoteRunnable<Group>() {
			@Override
			public Group run() throws java.rmi.RemoteException, JiraException {
				return Converter.convert(getSoapService().getGroup(loginToken.getCurrentValue(), name));
			}
		});
	}

	public ServerInfo getServerInfoRemote() throws JiraException {
		return call(new RemoteRunnable<ServerInfo>() {
			@Override
			public ServerInfo run() throws java.rmi.RemoteException, JiraException {
				return Converter.convert(getSoapService().getServerInfo(loginToken.getCurrentValue()));
			}
		});
	}

	public Issue getIssueByKey(String issueKey) throws JiraException {
		SingleIssueCollector collector = new SingleIssueCollector();
		filterService.quickSearch(issueKey, collector);
		return collector.getIssue();
	}

	public Issue getIssueById(String issueId) throws JiraException {
		String issueKey = getKeyFromId(issueId);
		return getIssueByKey(issueKey);
	}

	public String getKeyFromId(final String issueId) throws JiraException {
		return call(new RemoteRunnable<String>() {
			@Override
			public String run() throws java.rmi.RemoteException, JiraException {
				RemoteIssue issue = getSoapService().getIssueById(loginToken.getCurrentValue(), issueId);
				return (issue != null) ? issue.getKey() : null; 
			}
		});
	}
	
	public Issue createIssue(Issue issue) throws JiraException {
		return issueService.createIssue(issue);
	}

	public Project[] getProjectsRemote() throws JiraException {
		return call(new RemoteRunnable<Project[]>() {
			@Override
			public Project[] run() throws java.rmi.RemoteException, JiraException {
				return Converter.convert(getSoapService().getProjects(loginToken.getCurrentValue()));
			}
		});
	}

	public Project[] getProjectsRemoteNoSchemes() throws JiraException {
		return call(new RemoteRunnable<Project[]>() {
			@Override
			public Project[] run() throws java.rmi.RemoteException, JiraException {
				return Converter.convert(getSoapService().getProjectsNoSchemes(loginToken.getCurrentValue()));
			}
		});
	}

	public Status[] getStatusesRemote() throws JiraException {
		return call(new RemoteRunnable<Status[]>() {
			@Override
			public Status[] run() throws java.rmi.RemoteException, JiraException {
				return Converter.convert(getSoapService().getStatuses(loginToken.getCurrentValue()));
			}
		});
	}

	public IssueType[] getIssueTypesRemote() throws JiraException {
		return call(new RemoteRunnable<IssueType[]>() {
			@Override
			public IssueType[] run() throws java.rmi.RemoteException, JiraException {
				return Converter.convert(getSoapService().getIssueTypes(loginToken.getCurrentValue()));
			}
		});
	}

	public IssueType[] getSubTaskIssueTypesRemote() throws JiraException {
		return call(new RemoteRunnable<IssueType[]>() {
			@Override
			public IssueType[] run() throws java.rmi.RemoteException, JiraException {
				return Converter.convert(getSoapService().getSubTaskIssueTypes(loginToken.getCurrentValue()));
			}
		});
	}

	public Priority[] getPrioritiesRemote() throws JiraException {
		return call(new RemoteRunnable<Priority[]>() {
			@Override
			public Priority[] run() throws java.rmi.RemoteException, JiraException {
				return Converter.convert(getSoapService().getPriorities(loginToken.getCurrentValue()));
			}
		});
	}

	public Resolution[] getResolutionsRemote() throws JiraException {
		return call(new RemoteRunnable<Resolution[]>() {
			@Override
			public Resolution[] run() throws java.rmi.RemoteException, JiraException {
				return Converter.convert(getSoapService().getResolutions(loginToken.getCurrentValue()));
			}
		});
	}

	public Comment[] getCommentsRemote(String issueKey) throws JiraException {
		return call(new RemoteRunnable<Comment[]>() {
			@Override
			public Comment[] run() throws java.rmi.RemoteException, JiraException {
				// TODO implement
				// return
				// Converter.convert(jirasoapserviceV2.getComments(loginToken.getCurrentValue(),
				// issueKey));
				return null;
			}
		});
	}

	public Version[] getVersionsRemote(final String componentKey) throws JiraException {
		return call(new RemoteRunnable<Version[]>() {
			@Override
			public Version[] run() throws java.rmi.RemoteException, JiraException {
				return Converter.convert(getSoapService().getVersions(loginToken.getCurrentValue(), componentKey));
			}
		});
	}

	public void logout() {
		loginToken.expire();
	}

	public NamedFilter[] getNamedFilters() throws JiraException {
		return call(new RemoteRunnable<NamedFilter[]>() {
			@Override
			public NamedFilter[] run() throws java.rmi.RemoteException, JiraException {
				return Converter.convert(getSoapService().getSavedFilters(loginToken.getCurrentValue()));
			}
		});
	}

	/**
	 * Remote exceptions sometimes have a cause and sometimes don't. If the
	 * exception is some sort of connection failure it will be an AxisFault with
	 * no message that wraps a ConnectionException. If the exception was
	 * triggered by a server side error (404 or 500) there will be no cause and
	 * the AxisFault will have the message.
	 * 
	 * @param e
	 *            Exception to extract message from
	 * @return Message from the exception
	 */
	/* default */static String unwrapRemoteException(java.rmi.RemoteException e) {
		if (e instanceof AxisFault) {
			AxisFault fault = (AxisFault) e;
			Element httpErrorElement = fault
					.lookupFaultDetail(org.apache.axis.Constants.QNAME_FAULTDETAIL_HTTPERRORCODE);
			if (httpErrorElement != null) {
				int responseCode = Integer.parseInt(httpErrorElement.getFirstChild().getTextContent());
				switch (responseCode) {
				case HttpURLConnection.HTTP_INTERNAL_ERROR:
					return "Internal Server Error.  Please contact your Jira administrator.";
				case HttpURLConnection.HTTP_UNAVAILABLE:
					return "Jira RPC interface is not enabled.  Please contact your Jira administrator.";
				case HttpURLConnection.HTTP_NOT_FOUND:
					return "Web service endpoint not found.  Please check the URL.";
				case HttpURLConnection.HTTP_MOVED_PERM:
					return "The location of the Jira server has moved.  Please check the URL.";
				}
			}
		}

		if (e.getCause() != null) {
			Throwable cause = e.getCause();
			if (cause instanceof ConnectException) {
				return "Unable to connect to server.  Please check the URL.";
			}
			return e.getCause().getLocalizedMessage();
		}
		return e.getLocalizedMessage();
	}

	public void search(Query query, IssueCollector collector) throws JiraException {
		if (query instanceof SmartQuery) {
			quickSearch(((SmartQuery) query).getKeywords(), collector);
		} else if (query instanceof FilterDefinition) {
			findIssues((FilterDefinition) query, collector);
		} else if (query instanceof NamedFilter) {
			executeNamedFilter((NamedFilter) query, collector);
		} else {
			throw new IllegalArgumentException("Unknown query type: " + query.getClass());
		}
	}

	public void findIssues(FilterDefinition filterDefinition, IssueCollector collector) throws JiraException {
		filterService.findIssues(filterDefinition, collector);
	}

	public void executeNamedFilter(NamedFilter filter, IssueCollector collector) throws JiraException {
		filterService.executeNamedFilter(filter, collector);
	}

	public void quickSearch(String searchString, IssueCollector collector) throws JiraException {
		filterService.quickSearch(searchString, collector);

	}

	public void addCommentToIssue(Issue issue, String comment) throws JiraException {
		issueService.addCommentToIssue(issue, comment);
	}

	public void updateIssue(Issue issue, String comment) throws JiraException {
		issueService.updateIssue(issue, comment);
	}

	public void assignIssueTo(Issue issue, int assigneeType, String user, String comment) throws JiraException {
		issueService.assignIssueTo(issue, assigneeType, user, comment);
	}

	public void advanceIssueWorkflow(Issue issue, String action, Resolution resolution, Version[] fixVersions,
			String comment, int assigneeType, String user) throws JiraException {
		issueService.advanceIssueWorkflow(issue, action, resolution, fixVersions, comment, assigneeType, user);
	}

	public void advanceIssueWorkflow(Issue issue, String action) throws JiraException {
		issueService.advanceIssueWorkflow(issue, action);
	}

	public void startIssue(Issue issue) throws JiraException {
		issueService.startIssue(issue);
	}

	public void stopIssue(Issue issue) throws JiraException {
		issueService.stopIssue(issue);
	}

	public void resolveIssue(Issue issue, Resolution resolution, Version[] fixVersions, String comment,
			int assigneeType, String user) throws JiraException {
		issueService.resolveIssue(issue, resolution, fixVersions, comment, assigneeType, user);
	}

	public void reopenIssue(Issue issue, String comment, int assigneeType, String user) throws JiraException {
		issueService.reopenIssue(issue, comment, assigneeType, user);
	}

	public void closeIssue(Issue issue, Resolution resolution, Version[] fixVersions, String comment, int assigneeType,
			String user) throws JiraException {
		issueService.closeIssue(issue, resolution, fixVersions, comment, assigneeType, user);
	}

	public void attachFile(Issue issue, String comment, String filename, byte[] contents, String contentType) throws JiraException {
		issueService.attachFile(issue, comment, filename, contents, contentType);
	}

	public void attachFile(Issue issue, String comment, File file, String contentType) throws JiraException {
		issueService.attachFile(issue, comment, file, contentType);
	}


	public byte[] retrieveFile(Issue issue, Attachment attachment) throws JiraException {
		byte[] result = new byte[(int) attachment.getSize()];
		issueService.retrieveFile(issue, attachment, result);
		return result;
	}

	public void retrieveFile(Issue issue, Attachment attachment, File file) throws JiraException {
		issueService.retrieveFile(issue, attachment, file);
	}

	public void watchIssue(Issue issue) throws JiraException {
		issueService.watchIssue(issue);
	}

	public void unwatchIssue(Issue issue) throws JiraException {
		issueService.unwatchIssue(issue);
	}

	public void voteIssue(Issue issue) throws JiraException {
		issueService.voteIssue(issue);
	}

	public void unvoteIssue(Issue issue) throws JiraException {
		issueService.unvoteIssue(issue);
	}

	private <T> T call(RemoteRunnable<T> runnable, boolean retry) throws JiraException {
		// retry in case login token is expired
		for (int i = 0; i < 2; i++) {
			try {
				return runnable.run();
			} catch (RemotePermissionException e) {
				throw new JiraInsufficientPermissionException(e.getMessage());
			} catch (RemoteAuthenticationException e) {
				if (!retry || i > 0) {
					throw new JiraAuthenticationException(e.getMessage());
				}
				loginToken.expire();
			} catch (RemoteException e) {
				throw new JiraServiceUnavailableException(e.getMessage());
			} catch (java.rmi.RemoteException e) {
				throw new JiraServiceUnavailableException(unwrapRemoteException(e));
			}
		}
		throw new RuntimeException("Invalid section of code reached");
	}

	private <T> T call(RemoteRunnable<T> runnable) throws JiraException {
		return call(runnable, true);
	}

	private static interface LoginToken {

		/**
		 * Gets the current value of the login token. If the token has expired a
		 * new one may be requested.
		 * 
		 * @return current login token
		 */
		public String getCurrentValue() throws JiraException;

		/**
		 * Manually expire the current session token
		 */
		public void expire();

		/**
		 * Determines if there is a currently valid token being stored. This
		 * method can be used to check if the token has been set and has not
		 * probably expired. Usually, this method will only be used by the
		 * logout service to determine if it needs to do anything.
		 * 
		 * @return <code>true</code> if the token is probably valid
		 */
		public boolean isValidToken();
	}

	private class StandardLoginToken implements LoginToken {

		private final String username;

		private final String password;

		private final long timeout;

		private String token;

		private long lastAccessed;

		public StandardLoginToken(String username, String password, long timeout) {
			this.username = username;
			this.password = password;
			this.timeout = timeout;
			this.lastAccessed = -1L;
		}

		public synchronized String getCurrentValue() throws JiraException {
			if ((System.currentTimeMillis() - lastAccessed) >= timeout || token == null) {
				expire();

				this.token = call(new RemoteRunnable<String>() {
					@Override
					public String run() throws java.rmi.RemoteException, JiraException {
						return getSoapService().login(username, password);
					}
				}, false);

				this.lastAccessed = System.currentTimeMillis();
			}

			return this.token;
		}

		public synchronized void expire() {
			if (token != null) {
				try {
					getSoapService().logout(this.token);
				} catch (java.rmi.RemoteException e) {
					// ignore
				} catch (JiraException e) {
					// ignore
				}
				token = null;
				lastAccessed = -1;
			}
		}

		public synchronized boolean isValidToken() {
			return token != null && (System.currentTimeMillis() - lastAccessed) < timeout;
		}

		@Override
		public String toString() {
			long expiresIn = (timeout - (System.currentTimeMillis() - lastAccessed)) / 1000;
			return "[username=" + username + ", password=" + password + ", timeout=" + timeout + ", valid="
					+ isValidToken() + ", expires=" + expiresIn + "]";
		}

	}

	private class AnonymousLoginToken implements LoginToken {

		public String getCurrentValue() {
			return "";
		}

		public void expire() {
		}

		public boolean isValidToken() {
			return false;
		}
	}

	private abstract class RemoteRunnable<T> {

		public abstract T run() throws java.rmi.RemoteException, JiraException;

	}

}