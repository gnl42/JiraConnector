/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.service.soap;

import java.io.File;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.rpc.ServiceException;

import org.apache.axis.AxisFault;
import org.apache.axis.configuration.FileProvider;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.eclipse.mylyn.internal.jira.core.model.Attachment;
import org.eclipse.mylyn.internal.jira.core.model.Comment;
import org.eclipse.mylyn.internal.jira.core.model.Component;
import org.eclipse.mylyn.internal.jira.core.model.CustomField;
import org.eclipse.mylyn.internal.jira.core.model.Group;
import org.eclipse.mylyn.internal.jira.core.model.Issue;
import org.eclipse.mylyn.internal.jira.core.model.IssueType;
import org.eclipse.mylyn.internal.jira.core.model.JiraVersion;
import org.eclipse.mylyn.internal.jira.core.model.NamedFilter;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.Query;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.model.ServerInfo;
import org.eclipse.mylyn.internal.jira.core.model.Status;
import org.eclipse.mylyn.internal.jira.core.model.User;
import org.eclipse.mylyn.internal.jira.core.model.Version;
import org.eclipse.mylyn.internal.jira.core.model.WebServerInfo;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.jira.core.model.filter.IssueCollector;
import org.eclipse.mylyn.internal.jira.core.model.filter.SingleIssueCollector;
import org.eclipse.mylyn.internal.jira.core.model.filter.SmartQuery;
import org.eclipse.mylyn.internal.jira.core.service.AbstractJiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraAuthenticationException;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.jira.core.service.JiraInsufficientPermissionException;
import org.eclipse.mylyn.internal.jira.core.service.JiraServiceUnavailableException;
import org.eclipse.mylyn.internal.jira.core.service.web.JiraWebIssueService;
import org.eclipse.mylyn.internal.jira.core.service.web.rss.RssJiraFilterService;
import org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteField;
import org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteIssue;
import org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteNamedObject;
import org.eclipse.mylyn.internal.jira.core.wsdl.soap.JiraSoapService;
import org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;
import org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;
import org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException;
import org.eclipse.mylyn.tasks.core.RepositoryOperation;
import org.eclipse.mylyn.tasks.core.RepositoryTaskAttribute;
import org.eclipse.mylyn.web.core.AbstractWebLocation;
import org.eclipse.mylyn.web.core.AuthenticationCredentials;
import org.eclipse.mylyn.web.core.AuthenticationType;
import org.eclipse.mylyn.web.core.AbstractWebLocation.ResultType;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

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
public class JiraRpcClient extends AbstractJiraClient {

	private static final String ERROR_RPC_NOT_ENABLED = "JIRA RPC services are not enabled. Please contact your JIRA administrator.";

	private static final String REMOTE_ERROR_BAD_ID = "White spaces are required between publicId and systemId.";

	private static final String REMOTE_ERROR_BAD_ENVELOPE_TAG = "Bad envelope tag:  html";

	private static final String REMOTE_ERROR_CONTENT_NOT_ALLOWED_IN_PROLOG = "Content is not allowed in prolog.";

	private static final String SOAP_SERVICE_URL = "/rpc/soap/jirasoapservice-v2";

	/**
	 * Default session timeout for a JIRA instance. The default value is 10 minutes.
	 */
	private static final long DEFAULT_SESSION_TIMEOUT = 1000L * 60L * 10L;

	private JiraSoapService soapService = null;

	private Lock soapServiceLock = new ReentrantLock();

	private JiraWebIssueService issueService = null;

	private RssJiraFilterService filterService = null;

	private LoginToken loginToken;

	public JiraRpcClient(AbstractWebLocation location, boolean useCompression) {
		super(location, useCompression);

		this.loginToken = new LoginToken(location, DEFAULT_SESSION_TIMEOUT);
		this.filterService = new RssJiraFilterService(this);
		this.issueService = new JiraWebIssueService(this);
	}

	private JiraSoapService getSoapService() throws JiraException {
		soapServiceLock.lock();
		try {
			if (soapService == null) {
				GZipJiraSoapServiceServiceLocator locator = new GZipJiraSoapServiceServiceLocator(new FileProvider(
						this.getClass().getClassLoader().getResourceAsStream("client-config.wsdd")));
				locator.setLocation(getLocation());
				locator.setCompression(useCompression());

				try {
					soapService = locator.getJirasoapserviceV2(new URL(getBaseUrl() + SOAP_SERVICE_URL));
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
			public User run() throws java.rmi.RemoteException, JiraException {
				return Converter.convert(getSoapService().getUser(loginToken.getCurrentValue(), username));
			}
		});
	}

	@Override
	public Component[] getComponentsRemote(final String projectKey) throws JiraException {
		return call(new RemoteRunnable<Component[]>() {
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
			public Group run() throws java.rmi.RemoteException, JiraException {
				return Converter.convert(getSoapService().getGroup(loginToken.getCurrentValue(), name));
			}
		});
	}

	@Override
	public ServerInfo getServerInfoRemote() throws JiraException {
		// get server information through SOAP
		ServerInfo serverInfo = call(new RemoteRunnable<ServerInfo>() {
			public ServerInfo run() throws java.rmi.RemoteException, JiraException {
				return Converter.convert(getSoapService().getServerInfo(loginToken.getCurrentValue()));
			}
		});
		
		// get character encoding through web
		WebServerInfo webServerInfo = issueService.getWebServerInfo();
		serverInfo.setCharacterEncoding(webServerInfo.getCharacterEncoding());
		serverInfo.setWebBaseUrl(webServerInfo.getBaseUrl());
		
		return serverInfo;
	}

	public Issue getIssueByKey(String issueKey) throws JiraException {
		SingleIssueCollector collector = new SingleIssueCollector();
		filterService.getIssueByKey(issueKey, collector);
		return collector.getIssue();
	}

	public Issue getIssueById(String issueId) throws JiraException {
		String issueKey = getKeyFromId(issueId);
		return getIssueByKey(issueKey);
	}

	public String getKeyFromId(final String issueId) throws JiraException {
		return call(new RemoteRunnable<String>() {
			public String run() throws java.rmi.RemoteException, JiraException {
				RemoteIssue issue = getSoapService().getIssueById(loginToken.getCurrentValue(), issueId);
				return (issue != null) ? issue.getKey() : null;
			}
		});
	}

	// TODO need to cache those
	public RepositoryOperation[] getAvailableOperations(final String taskKey) throws JiraException {
		return call(new RemoteRunnable<RepositoryOperation[]>() {
			public RepositoryOperation[] run() throws java.rmi.RemoteException, JiraException {
				RemoteNamedObject[] actions = getSoapService().getAvailableActions(loginToken.getCurrentValue(),
						taskKey);
				if (actions == null) {
					return new RepositoryOperation[0];
				}

				RepositoryOperation[] operations = new RepositoryOperation[actions.length];
				for (int i = 0; i < actions.length; i++) {
					RemoteNamedObject action = actions[i];
					operations[i] = new RepositoryOperation(action.getId(), action.getName());
				}
				return operations;
			}
		});
	}

	// TODO need to cache those
	public String[] getActionFields(final String taskKey, final String actionId) throws JiraException {
		return call(new RemoteRunnable<String[]>() {
			public String[] run() throws java.rmi.RemoteException, JiraException {
				RemoteField[] remoteFields = getSoapService().getFieldsForAction(loginToken.getCurrentValue(), taskKey,
						actionId);
				if (remoteFields == null) {
					return new String[0];
				}

				String[] fields = new String[remoteFields.length];
				for (int i = 0; i < remoteFields.length; i++) {
					fields[i] = remoteFields[i].getId();
				}
				return fields;
			}
		});
	}

	// TODO need to cache those
	public RepositoryTaskAttribute[] getEditableAttributes(final String taskKey) throws JiraException {
		return call(new RemoteRunnable<RepositoryTaskAttribute[]>() {
			public RepositoryTaskAttribute[] run() throws java.rmi.RemoteException, JiraException {
				RemoteField[] fields = getSoapService().getFieldsForEdit(loginToken.getCurrentValue(), taskKey);
				if (fields == null) {
					return new RepositoryTaskAttribute[0];
				}

				// work around for bug 205015
				int add = 0;
				String version = getServerInfo().getVersion();
				if (new JiraVersion(version).compareTo(JiraVersion.JIRA_3_12) < 0) {
					add += 2;
				}
				
				RepositoryTaskAttribute[] attributes = new RepositoryTaskAttribute[fields.length + add];
				for (int i = 0; i < fields.length; i++) {
					RemoteField field = fields[i];
					attributes[i] = new RepositoryTaskAttribute(field.getId(), field.getName(), false);
				}
				
				if (add > 0) {
					// might also need to add: Reporter and Summary (http://jira.atlassian.com/browse/JRA-13703)
					attributes[attributes.length - 2] = new RepositoryTaskAttribute("duedate", "Due Date", false);
					attributes[attributes.length - 1] = new RepositoryTaskAttribute("fixVersions", "Fix Version/s", false);
				}
				
				return attributes;
			}
		});
	}

	// TODO need to cache those
	public CustomField[] getCustomAttributes() throws JiraException {
		return call(new RemoteRunnable<CustomField[]>() {
			public CustomField[] run() throws java.rmi.RemoteException, JiraException {
				RemoteField[] remoteFields = getSoapService().getCustomFields(loginToken.getCurrentValue());
				CustomField[] fields = new CustomField[remoteFields.length];
				for (int i = 0; i < remoteFields.length; i++) {
					RemoteField remoteField = remoteFields[i];
					fields[i] = new CustomField(remoteField.getId(), null, remoteField.getName(),
							Collections.<String> emptyList());
				}
				return fields;
			}
		});
	}

	public Issue createIssue(Issue issue) throws JiraException {
		String issueKey = issueService.createIssue(issue);
		return getIssueByKey(issueKey);
	}

	public Issue createSubTask(Issue issue) throws JiraException {
		String issueKey = issueService.createSubTask(issue);
		return getIssueByKey(issueKey);
	}

	public RemoteIssue getRemoteIssueByKey(final String key) throws JiraException {
		return call(new RemoteRunnable<RemoteIssue>() {
			public RemoteIssue run() throws java.rmi.RemoteException, JiraException {
				return getSoapService().getIssue(loginToken.getCurrentValue(), key);
			}
		});		
	}
	
	@Override
	public Project[] getProjectsRemote() throws JiraException {
		return call(new RemoteRunnable<Project[]>() {
			public Project[] run() throws java.rmi.RemoteException, JiraException {
				return Converter.convert(getSoapService().getProjects(loginToken.getCurrentValue()));
			}
		});
	}

	@Override
	public Project[] getProjectsRemoteNoSchemes() throws JiraException {
		return call(new RemoteRunnable<Project[]>() {
			public Project[] run() throws java.rmi.RemoteException, JiraException {
				return Converter.convert(getSoapService().getProjectsNoSchemes(loginToken.getCurrentValue()));
			}
		});
	}

	@Override
	public Status[] getStatusesRemote() throws JiraException {
		return call(new RemoteRunnable<Status[]>() {
			public Status[] run() throws java.rmi.RemoteException, JiraException {
				return Converter.convert(getSoapService().getStatuses(loginToken.getCurrentValue()));
			}
		});
	}

	@Override
	public IssueType[] getIssueTypesRemote() throws JiraException {
		return call(new RemoteRunnable<IssueType[]>() {
			public IssueType[] run() throws java.rmi.RemoteException, JiraException {
				return Converter.convert(getSoapService().getIssueTypes(loginToken.getCurrentValue()));
			}
		});
	}

	@Override
	public IssueType[] getSubTaskIssueTypesRemote() throws JiraException {
		return call(new RemoteRunnable<IssueType[]>() {
			public IssueType[] run() throws java.rmi.RemoteException, JiraException {
				return Converter.convert(getSoapService().getSubTaskIssueTypes(loginToken.getCurrentValue()));
			}
		});
	}

	@Override
	public Priority[] getPrioritiesRemote() throws JiraException {
		return call(new RemoteRunnable<Priority[]>() {
			public Priority[] run() throws java.rmi.RemoteException, JiraException {
				return Converter.convert(getSoapService().getPriorities(loginToken.getCurrentValue()));
			}
		});
	}

	@Override
	public Resolution[] getResolutionsRemote() throws JiraException {
		return call(new RemoteRunnable<Resolution[]>() {
			public Resolution[] run() throws java.rmi.RemoteException, JiraException {
				return Converter.convert(getSoapService().getResolutions(loginToken.getCurrentValue()));
			}
		});
	}

	public Comment[] getCommentsRemote(String issueKey) throws JiraException {
		return call(new RemoteRunnable<Comment[]>() {
			public Comment[] run() throws java.rmi.RemoteException, JiraException {
				// TODO implement
				// return
				// Converter.convert(jirasoapserviceV2.getComments(loginToken.getCurrentValue(),
				// issueKey));
				return null;
			}
		});
	}

	@Override
	public Version[] getVersionsRemote(final String componentKey) throws JiraException {
		return call(new RemoteRunnable<Version[]>() {
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
			public NamedFilter[] run() throws java.rmi.RemoteException, JiraException {
				return Converter.convert(getSoapService().getSavedFilters(loginToken.getCurrentValue()));
			}
		});
	}

	/**
	 * Remote exceptions sometimes have a cause and sometimes don't. If the exception is some sort of connection failure
	 * it will be an AxisFault with no message that wraps a ConnectionException. If the exception was triggered by a
	 * server side error (404 or 500) there will be no cause and the AxisFault will have the message.
	 * 
	 * @param e
	 *            Exception to extract message from
	 * @return Message from the exception
	 */
	/* default */static String unwrapRemoteException(java.rmi.RemoteException e) {
		if (e instanceof AxisFault) {
			AxisFault fault = (AxisFault) e;
			Element httpErrorElement = fault.lookupFaultDetail(org.apache.axis.Constants.QNAME_FAULTDETAIL_HTTPERRORCODE);
			if (httpErrorElement != null) {
				int responseCode = Integer.parseInt(httpErrorElement.getFirstChild().getTextContent());
				switch (responseCode) {
				case HttpURLConnection.HTTP_INTERNAL_ERROR:
					return "Internal Server Error. Please contact your JIRA administrator.";
				case HttpURLConnection.HTTP_UNAVAILABLE:
					return ERROR_RPC_NOT_ENABLED;
				case HttpURLConnection.HTTP_NOT_FOUND:
					return "No JIRA repository found at location.";
				case HttpURLConnection.HTTP_MOVED_PERM:
					return "The location of the Jira server has moved.";
				}
			}
		}

		if (e.getCause() != null) {
			Throwable cause = e.getCause();
			if (cause instanceof UnknownHostException) {
				return "Unknown host.";
			} else if (cause instanceof ConnectException) {
				return "Unable to connect to server.";
			} else if (cause instanceof SAXException) {
				if (REMOTE_ERROR_BAD_ENVELOPE_TAG.equals(cause.getMessage())
						|| REMOTE_ERROR_BAD_ID.equals(cause.getMessage())
						|| REMOTE_ERROR_CONTENT_NOT_ALLOWED_IN_PROLOG.equals(cause.getMessage())) {
					return ERROR_RPC_NOT_ENABLED;
				}
			}
			return e.getCause().getLocalizedMessage();
		}
		
		if (e instanceof AxisFault) {
			return "Server error: " + e.getLocalizedMessage();
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

//	public void advanceIssueWorkflow(Issue issue, String action, Resolution resolution, Version[] fixVersions,
//			String comment, int assigneeType, String user) throws JiraException {
//		issueService.advanceIssueWorkflow(issue, action, resolution, fixVersions, comment, assigneeType, user);
//	}

	public void advanceIssueWorkflow(Issue issue, String actionKey, String comment) throws JiraException {
		String[] fields = getActionFields(issue.getKey(), actionKey);
		issueService.advanceIssueWorkflow(issue, actionKey, comment, fields);
	}

	public void attachFile(Issue issue, String comment, PartSource partSource, String contentType) throws JiraException {
		issueService.attachFile(issue, comment, partSource, contentType);
	}

	public void attachFile(Issue issue, String comment, String filename, byte[] contents, String contentType)
			throws JiraException {
		issueService.attachFile(issue, comment, filename, contents, contentType);
	}

	public void attachFile(Issue issue, String comment, String filename, File file, String contentType)
			throws JiraException {
		issueService.attachFile(issue, comment, filename, file, contentType);
	}

	public byte[] retrieveFile(Issue issue, Attachment attachment) throws JiraException {
		byte[] result = new byte[(int) attachment.getSize()];
		issueService.retrieveFile(issue, attachment, result);
		return result;
	}

	public void retrieveFile(Issue issue, Attachment attachment, OutputStream out) throws JiraException {
		issueService.retrieveFile(issue, attachment, out);
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

	public void deleteIssue(Issue issue) throws JiraException {
		issueService.deleteIssue(issue);
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
		while (true) {
			try {
				return call(runnable, true);
			} catch (JiraAuthenticationException e) {
				if (getLocation().requestCredentials(AuthenticationType.REPOSITORY, null) == ResultType.NOT_SUPPORTED) {
					throw e;
				}
			}
		}
	}

	private interface RemoteRunnable<T> {

		T run() throws java.rmi.RemoteException, JiraException;

	}

	private class LoginToken {

		private final long timeout;

		private String token;

		private long lastAccessed;

		private final AbstractWebLocation location;

		private AuthenticationCredentials credentials;

		public LoginToken(AbstractWebLocation location, long timeout) {
			this.location = location;
			this.timeout = timeout;
			this.lastAccessed = -1L;
		}

		public synchronized String getCurrentValue() throws JiraException {
			AuthenticationCredentials newCredentials = location.getCredentials(AuthenticationType.REPOSITORY);
			if (newCredentials == null) {
				expire();
				return "";
			} else if (!newCredentials.equals(credentials)) {
				expire();
				credentials = newCredentials;
			}
			
			if ((System.currentTimeMillis() - lastAccessed) >= timeout || token == null) {
				expire();

				this.token = call(new RemoteRunnable<String>() {
					public String run() throws java.rmi.RemoteException, JiraException {
						return getSoapService().login(credentials.getUserName(), credentials.getPassword());
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
			return "[credentials=" + credentials + ", timeout=" + timeout + ", valid="
					+ isValidToken() + ", expires=" + expiresIn + "]";
		}
		
	}

}