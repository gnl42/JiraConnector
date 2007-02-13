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
import java.net.URL;

import org.apache.axis.AxisFault;
import org.apache.axis.configuration.FileProvider;
import org.eclipse.mylar.internal.jira.core.model.Comment;
import org.eclipse.mylar.internal.jira.core.model.Component;
import org.eclipse.mylar.internal.jira.core.model.Group;
import org.eclipse.mylar.internal.jira.core.model.Issue;
import org.eclipse.mylar.internal.jira.core.model.IssueType;
import org.eclipse.mylar.internal.jira.core.model.NamedFilter;
import org.eclipse.mylar.internal.jira.core.model.Priority;
import org.eclipse.mylar.internal.jira.core.model.Project;
import org.eclipse.mylar.internal.jira.core.model.Resolution;
import org.eclipse.mylar.internal.jira.core.model.ServerInfo;
import org.eclipse.mylar.internal.jira.core.model.Status;
import org.eclipse.mylar.internal.jira.core.model.User;
import org.eclipse.mylar.internal.jira.core.model.Version;
import org.eclipse.mylar.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylar.internal.jira.core.model.filter.IssueCollector;
import org.eclipse.mylar.internal.jira.core.model.filter.SingleIssueCollector;
import org.eclipse.mylar.internal.jira.core.service.AuthenticationException;
import org.eclipse.mylar.internal.jira.core.service.InsufficientPermissionException;
import org.eclipse.mylar.internal.jira.core.service.JiraServer;
import org.eclipse.mylar.internal.jira.core.service.JiraService;
import org.eclipse.mylar.internal.jira.core.service.ServiceUnavailableException;
import org.eclipse.mylar.internal.jira.core.service.web.JiraWebIssueService;
import org.eclipse.mylar.internal.jira.core.service.web.rss.RssJiraFilterService;
import org.eclipse.mylar.internal.jira.core.wsdl.soap.JiraSoapService;
import org.eclipse.mylar.internal.jira.core.wsdl.soap.JiraSoapServiceServiceLocator;
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
 * @author	Brock Janiczak
 */
public class SoapJiraService implements JiraService {
	/**
	 * Default session timeout for a jira instance. The default value is 10
	 * minutes.
	 */
	private static final long DEFAULT_SESSION_TIMEOUT = 1000L * 60L * 60L * 10L;

	private JiraSoapService jirasoapserviceV2 = null;

	private JiraWebIssueService issueService = null;

	private RssJiraFilterService filterService = null;

	private final JiraServer server;

	private LoginToken loginToken;

	public SoapJiraService(JiraServer server) {
		this.server = server;

		try {
			// JiraSoapServiceServiceLocator s = new
			// JiraSoapServiceServiceLocator();
			JiraSoapServiceServiceLocator s = new GZipJiraSoapServiceServiceLocator(new FileProvider(this.getClass()
					.getClassLoader().getResourceAsStream("client-config.wsdd")));
			jirasoapserviceV2 = s.getJirasoapserviceV2(new URL(server.getBaseURL() + "/rpc/soap/jirasoapservice-v2")); //$NON-NLS-1$
			filterService = new RssJiraFilterService(server);
			issueService = new JiraWebIssueService(server);

			if (server.getCurrentUserName() == null) {
				loginToken = new AnonymousLoginToken();
			} else {
				loginToken = new StandardLoginToken(server.getCurrentUserName(), server.getCurrentUserPassword(),
						DEFAULT_SESSION_TIMEOUT, jirasoapserviceV2);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraService#getUser(java.lang.String)
	 */
	public User getUser(String username) throws AuthenticationException, InsufficientPermissionException,
			ServiceUnavailableException {
		try {
			return Converter.convert(jirasoapserviceV2.getUser(loginToken.getCurrentValue(), username));
		} catch (RemoteAuthenticationException e) {
			throw new AuthenticationException(e.getMessage());
		} catch (RemotePermissionException e) {
			throw new InsufficientPermissionException(e.getMessage());
		} catch (java.rmi.RemoteException e) {
			throw new ServiceUnavailableException(unwrapRemoteException(e));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraService#getComponents(java.lang.String)
	 */
	public Component[] getComponents(String projectKey) throws InsufficientPermissionException,
			AuthenticationException, ServiceUnavailableException {

		try {
			return Converter.convert(jirasoapserviceV2.getComponents(loginToken.getCurrentValue(), projectKey));
		} catch (RemotePermissionException e) {
			throw new InsufficientPermissionException(e.getMessage());
		} catch (RemoteAuthenticationException e) {
			throw new AuthenticationException(e.getMessage());
		} catch (RemoteException e) {
			throw new ServiceUnavailableException(e.getMessage());
		} catch (java.rmi.RemoteException e) {
			throw new ServiceUnavailableException(unwrapRemoteException(e));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraService#login(java.lang.String,
	 *      java.lang.String)
	 */
	public String login(String username, String password) throws AuthenticationException, ServiceUnavailableException {
		try {
			return jirasoapserviceV2.login(username, password);
		} catch (RemoteException e) {
			// TODO check this
			throw new AuthenticationException(e.getMessage());
		} catch (java.rmi.RemoteException e) {
			throw new ServiceUnavailableException(unwrapRemoteException(e));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraService#getGroup(java.lang.String)
	 */
	public Group getGroup(String name) throws InsufficientPermissionException, AuthenticationException,
			ServiceUnavailableException {
		try {
			return Converter.convert(jirasoapserviceV2.getGroup(loginToken.getCurrentValue(), name));
		} catch (RemotePermissionException e) {
			throw new InsufficientPermissionException(e.getMessage());
		} catch (RemoteAuthenticationException e) {
			throw new AuthenticationException(e.getMessage());
		} catch (RemoteException e) {
			throw new ServiceUnavailableException(e.getMessage());
		} catch (java.rmi.RemoteException e) {
			throw new ServiceUnavailableException(unwrapRemoteException(e));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraService#getServerInfo()
	 */
	public ServerInfo getServerInfo() throws ServiceUnavailableException {
		try {
			return Converter.convert(jirasoapserviceV2.getServerInfo(loginToken.getCurrentValue()));
		} catch (java.rmi.RemoteException e) {
			throw new ServiceUnavailableException(unwrapRemoteException(e));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraService#getIssue(java.lang.String)
	 */
	public Issue getIssue(String issueKey) {
		SingleIssueCollector collector = new SingleIssueCollector();
		filterService.quickSearch(issueKey, collector);
		return collector.getIssue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraService#createIssue(com.gbst.jira.core.model.Issue)
	 */
	public Issue createIssue(Issue issue) {
		return issueService.createIssue(issue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraService#getProjects()
	 */
	public Project[] getProjects() throws InsufficientPermissionException, AuthenticationException,
			ServiceUnavailableException {
		try {
			return Converter.convert(jirasoapserviceV2.getProjects(loginToken.getCurrentValue()));
		} catch (RemotePermissionException e) {
			throw new InsufficientPermissionException(e.getMessage());
		} catch (RemoteAuthenticationException e) {
			throw new AuthenticationException(e.getMessage());
		} catch (RemoteException e) {
			throw new ServiceUnavailableException(e.getMessage());
		} catch (java.rmi.RemoteException e) {
			throw new ServiceUnavailableException(unwrapRemoteException(e));
		}
	}

	public Project[] getProjectsNoSchemes() throws InsufficientPermissionException, AuthenticationException,
			ServiceUnavailableException {
		try {
			return Converter.convert(jirasoapserviceV2.getProjectsNoSchemes(loginToken.getCurrentValue()));
		} catch (RemotePermissionException e) {
			throw new InsufficientPermissionException(e.getMessage());
		} catch (RemoteAuthenticationException e) {
			throw new AuthenticationException(e.getMessage());
		} catch (RemoteException e) {
			throw new ServiceUnavailableException(e.getMessage());
		} catch (java.rmi.RemoteException e) {
			throw new ServiceUnavailableException(unwrapRemoteException(e));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraService#getStatuses()
	 */
	public Status[] getStatuses() throws InsufficientPermissionException, AuthenticationException,
			ServiceUnavailableException {
		try {
			return Converter.convert(jirasoapserviceV2.getStatuses(loginToken.getCurrentValue()));
		} catch (RemotePermissionException e) {
			throw new InsufficientPermissionException(e.getMessage());
		} catch (RemoteAuthenticationException e) {
			throw new AuthenticationException(e.getMessage());
		} catch (java.rmi.RemoteException e) {
			throw new ServiceUnavailableException(unwrapRemoteException(e));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraService#getIssueTypes()
	 */
	public IssueType[] getIssueTypes() throws InsufficientPermissionException, AuthenticationException,
			ServiceUnavailableException {
		try {
			return Converter.convert(jirasoapserviceV2.getIssueTypes(loginToken.getCurrentValue()));
		} catch (RemotePermissionException e) {
			throw new InsufficientPermissionException(e.getMessage());
		} catch (RemoteAuthenticationException e) {
			throw new AuthenticationException(e.getMessage());
		} catch (java.rmi.RemoteException e) {
			throw new ServiceUnavailableException(unwrapRemoteException(e));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraService#getSubTaskIssueTypes()
	 */
	public IssueType[] getSubTaskIssueTypes() throws InsufficientPermissionException, AuthenticationException,
			ServiceUnavailableException {
		if (server.getServerInfo().getVersion().compareTo("3.2") < 0) {
			return new IssueType[0];
		}

		try {
			return Converter.convert(jirasoapserviceV2.getSubTaskIssueTypes(loginToken.getCurrentValue()));
		} catch (RemotePermissionException e) {
			throw new InsufficientPermissionException(e.getMessage());
		} catch (RemoteAuthenticationException e) {
			throw new AuthenticationException(e.getMessage());
		} catch (java.rmi.RemoteException e) {
			throw new ServiceUnavailableException(unwrapRemoteException(e));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraService#getPriorities()
	 */
	public Priority[] getPriorities() throws InsufficientPermissionException, AuthenticationException,
			ServiceUnavailableException {
		try {
			return Converter.convert(jirasoapserviceV2.getPriorities(loginToken.getCurrentValue()));
		} catch (RemotePermissionException e) {
			throw new InsufficientPermissionException(e.getMessage());
		} catch (RemoteAuthenticationException e) {
			throw new AuthenticationException(e.getMessage());
		} catch (java.rmi.RemoteException e) {
			throw new ServiceUnavailableException(unwrapRemoteException(e));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraService#getResolutions()
	 */
	public Resolution[] getResolutions() throws InsufficientPermissionException, AuthenticationException,
			ServiceUnavailableException {
		try {
			return Converter.convert(jirasoapserviceV2.getResolutions(loginToken.getCurrentValue()));
		} catch (RemotePermissionException e) {
			throw new InsufficientPermissionException(e.getMessage());
		} catch (RemoteAuthenticationException e) {
			throw new AuthenticationException(e.getMessage());
		} catch (java.rmi.RemoteException e) {
			throw new ServiceUnavailableException(unwrapRemoteException(e));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraService#getComments(java.lang.String)
	 */
	public Comment[] getComments(String issueKey) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraService#getVersions(java.lang.String)
	 */
	public Version[] getVersions(String componentKey) throws InsufficientPermissionException, AuthenticationException,
			ServiceUnavailableException {
		try {
			return Converter.convert(jirasoapserviceV2.getVersions(loginToken.getCurrentValue(), componentKey));
		} catch (RemotePermissionException e) {
			throw new InsufficientPermissionException(e.getMessage());
		} catch (RemoteAuthenticationException e) {
			throw new AuthenticationException(e.getMessage());
		} catch (RemoteException e) {
			throw new ServiceUnavailableException(e.getMessage());
		} catch (java.rmi.RemoteException e) {
			throw new ServiceUnavailableException(unwrapRemoteException(e));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraService#logout()
	 */
	public boolean logout() throws ServiceUnavailableException {
		try {
			if (loginToken.isValidToken()) {
				return jirasoapserviceV2.logout(loginToken.getCurrentValue());
			}
			return true;
		} catch (java.rmi.RemoteException e) {
			// Do nothing. A failed logout is ok
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraService#getSavedFilters()
	 */
	public NamedFilter[] getSavedFilters() {
		try {
			return Converter.convert(jirasoapserviceV2.getSavedFilters(loginToken.getCurrentValue()));
		} catch (RemotePermissionException e) {
			throw new InsufficientPermissionException(e.getMessage());
		} catch (RemoteAuthenticationException e) {
			throw new AuthenticationException(e.getMessage());
		} catch (RemoteException e) {
			throw new ServiceUnavailableException(e.getMessage());
		} catch (java.rmi.RemoteException e) {
			throw new ServiceUnavailableException(unwrapRemoteException(e));
		}
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.internal.jira.core.service.JiraService#findIssues(org.eclipse.mylar.internal.jira.core.model.filter.FilterDefinition,
	 *      org.eclipse.mylar.internal.jira.core.model.filter.IssueCollector)
	 */
	public void findIssues(FilterDefinition filterDefinition, IssueCollector collector) {
		filterService.findIssues(filterDefinition, collector);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.internal.jira.core.service.JiraService#executeNamedFilter(org.eclipse.mylar.internal.jira.core.model.NamedFilter,
	 *      org.eclipse.mylar.internal.jira.core.model.filter.IssueCollector)
	 */
	public void executeNamedFilter(NamedFilter filter, IssueCollector collector) {
		filterService.executeNamedFilter(filter, collector);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.internal.jira.core.service.JiraService#quickSearch(java.lang.String,
	 *      org.eclipse.mylar.internal.jira.core.model.filter.IssueCollector)
	 */
	public void quickSearch(String searchString, IssueCollector collector) {
		filterService.quickSearch(searchString, collector);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.internal.jira.core.service.JiraService#addCommentToIssue(org.eclipse.mylar.internal.jira.core.model.Issue,
	 *      java.lang.String)
	 */
	public void addCommentToIssue(Issue issue, String comment) {
		issueService.addCommentToIssue(issue, comment);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.internal.jira.core.service.JiraService#updateIssue(org.eclipse.mylar.internal.jira.core.model.Issue,
	 *      java.lang.String)
	 */
	public void updateIssue(Issue issue, String comment) {
		issueService.updateIssue(issue, comment);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.internal.jira.core.service.JiraService#assignIssueTo(org.eclipse.mylar.internal.jira.core.model.Issue,
	 *      int, java.lang.String, java.lang.String)
	 */
	public void assignIssueTo(Issue issue, int assigneeType, String user, String comment) {
		issueService.assignIssueTo(issue, assigneeType, user, comment);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.internal.jira.core.service.JiraService#advanceIssueWorkflow(org.eclipse.mylar.internal.jira.core.model.Issue,
	 *      java.lang.String, org.eclipse.mylar.internal.jira.core.model.Resolution,
	 *      org.eclipse.mylar.internal.jira.core.model.Version[], java.lang.String, int,
	 *      java.lang.String)
	 */
	public void advanceIssueWorkflow(Issue issue, String action, Resolution resolution, Version[] fixVersions,
			String comment, int assigneeType, String user) {
		issueService.advanceIssueWorkflow(issue, action, resolution, fixVersions, comment, assigneeType, user);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.internal.jira.core.service.JiraService#advanceIssueWorkflow(org.eclipse.mylar.internal.jira.core.model.Issue,
	 *      java.lang.String)
	 */
	public void advanceIssueWorkflow(Issue issue, String action) {
		issueService.advanceIssueWorkflow(issue, action);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.internal.jira.core.service.JiraService#startIssue(org.eclipse.mylar.internal.jira.core.model.Issue,
	 *      java.lang.String, java.lang.String)
	 */
	public void startIssue(Issue issue, String comment, String user) {
		issueService.startIssue(issue, comment, user);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.internal.jira.core.service.JiraService#stopIssue(org.eclipse.mylar.internal.jira.core.model.Issue,
	 *      java.lang.String, java.lang.String)
	 */
	public void stopIssue(Issue issue, String comment, String user) {
		issueService.stopIssue(issue, comment, user);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.internal.jira.core.service.JiraService#resolveIssue(org.eclipse.mylar.internal.jira.core.model.Issue,
	 *      org.eclipse.mylar.internal.jira.core.model.Resolution,
	 *      org.eclipse.mylar.internal.jira.core.model.Version[], java.lang.String, int,
	 *      java.lang.String)
	 */
	public void resolveIssue(Issue issue, Resolution resolution, Version[] fixVersions, String comment,
			int assigneeType, String user) {
		issueService.resolveIssue(issue, resolution, fixVersions, comment, assigneeType, user);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.internal.jira.core.service.JiraService#reopenIssue(org.eclipse.mylar.internal.jira.core.model.Issue,
	 *      java.lang.String, int, java.lang.String)
	 */
	public void reopenIssue(Issue issue, String comment, int assigneeType, String user) {
		issueService.reopenIssue(issue, comment, assigneeType, user);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.internal.jira.core.service.JiraService#closeIssue(org.eclipse.mylar.internal.jira.core.model.Issue,
	 *      org.eclipse.mylar.internal.jira.core.model.Resolution,
	 *      org.eclipse.mylar.internal.jira.core.model.Version[], java.lang.String, int,
	 *      java.lang.String)
	 */
	public void closeIssue(Issue issue, Resolution resolution, Version[] fixVersions, String comment, int assigneeType,
			String user) {
		issueService.closeIssue(issue, resolution, fixVersions, comment, assigneeType, user);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.internal.jira.core.service.JiraService#attachFile(org.eclipse.mylar.internal.jira.core.model.Issue,
	 *      java.lang.String, java.lang.String, byte[], java.lang.String)
	 */
	public void attachFile(Issue issue, String comment, String filename, byte[] contents, String contentType) {
		issueService.attachFile(issue, comment, filename, contents, contentType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.internal.jira.core.service.JiraService#attachFile(org.eclipse.mylar.internal.jira.core.model.Issue,
	 *      java.lang.String, java.io.File, java.lang.String)
	 */
	public void attachFile(Issue issue, String comment, File file, String contentType) {
		issueService.attachFile(issue, comment, file, contentType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.internal.jira.core.service.JiraService#watchIssue(org.eclipse.mylar.internal.jira.core.model.Issue)
	 */
	public void watchIssue(Issue issue) {
		issueService.watchIssue(issue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.internal.jira.core.service.JiraService#unwatchIssue(org.eclipse.mylar.internal.jira.core.model.Issue)
	 */
	public void unwatchIssue(Issue issue) {
		issueService.unwatchIssue(issue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.internal.jira.core.service.JiraService#voteIssue(org.eclipse.mylar.internal.jira.core.model.Issue)
	 */
	public void voteIssue(Issue issue) {
		issueService.voteIssue(issue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.internal.jira.core.service.JiraService#unvoteIssue(org.eclipse.mylar.internal.jira.core.model.Issue)
	 */
	public void unvoteIssue(Issue issue) {
		issueService.unvoteIssue(issue);
	}

	private static interface LoginToken {
		/**
		 * Gets the current value of the login token. If the token has expired a
		 * new one may be requested.
		 * 
		 * @return Current login token
		 */
		public String getCurrentValue();

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

	private static class StandardLoginToken implements LoginToken {
		private final String username;

		private final String password;

		private final long timeout;

		private final JiraSoapService jiraSoapService;

		private String token;
		
		private long lastAccessed;
		
		public StandardLoginToken(String username, String password, long timeout, JiraSoapService jiraSoapService) {
			this.username = username;
			this.password = password;
			this.timeout = timeout;
			this.jiraSoapService = jiraSoapService;
			this.lastAccessed = -1L;
		}

		public String getCurrentValue() {
			if ((System.currentTimeMillis() - lastAccessed) >= timeout || token == null) {
				if (token != null) {
					try {
						jiraSoapService.logout(this.token);
					} catch (Exception e) {
						// do nothing
					}
				}

				try {
					this.token = jiraSoapService.login(username, password);
					this.lastAccessed = System.currentTimeMillis();
				} catch (RemoteAuthenticationException e) {
					this.token = null;
					this.lastAccessed = -1L;
					throw new AuthenticationException(e.getMessage());
				} catch (java.rmi.RemoteException e) {
					this.token = null;
					this.lastAccessed = -1L;
					throw new ServiceUnavailableException(unwrapRemoteException(e));
				}
			}

			return this.token;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.mylar.internal.jira.core.service.CachedRpcJiraServer.LoginToken#expire()
		 */
		public void expire() {
			if (token != null) {
				try {
					jiraSoapService.logout(this.token);
				} catch (java.rmi.RemoteException e) {
					// Do nothing
				}
				token = null;
				lastAccessed = -1;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.mylar.internal.jira.core.service.soap.SoapJiraService.LoginToken#isValidToken()
		 */
		public boolean isValidToken() {
			return token != null && (System.currentTimeMillis() - lastAccessed) < timeout;
		}
	}

	private static class AnonymousLoginToken implements LoginToken {

		public String getCurrentValue() {
			return "";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.mylar.internal.jira.core.service.CachedRpcJiraServer.LoginToken#expire()
		 */
		public void expire() {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.mylar.internal.jira.core.service.soap.SoapJiraService.LoginToken#isValidToken()
		 */
		public boolean isValidToken() {
			return false;
		}
	}
}