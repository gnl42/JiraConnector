/*******************************************************************************
 * Copyright (c) 2004, 2009 Brock Janiczak and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brock Janiczak - initial API and implementation
 *     Tasktop Technologies - improvements
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.service.soap;

import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.rpc.ServiceException;

import org.apache.axis.AxisFault;
import org.apache.axis.configuration.FileProvider;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.eclipse.mylyn.internal.jira.core.model.Comment;
import org.eclipse.mylyn.internal.jira.core.model.Component;
import org.eclipse.mylyn.internal.jira.core.model.CustomField;
import org.eclipse.mylyn.internal.jira.core.model.Group;
import org.eclipse.mylyn.internal.jira.core.model.IssueField;
import org.eclipse.mylyn.internal.jira.core.model.IssueType;
import org.eclipse.mylyn.internal.jira.core.model.JiraAction;
import org.eclipse.mylyn.internal.jira.core.model.JiraStatus;
import org.eclipse.mylyn.internal.jira.core.model.JiraVersion;
import org.eclipse.mylyn.internal.jira.core.model.JiraWorkLog;
import org.eclipse.mylyn.internal.jira.core.model.NamedFilter;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.ProjectRole;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.model.SecurityLevel;
import org.eclipse.mylyn.internal.jira.core.model.ServerInfo;
import org.eclipse.mylyn.internal.jira.core.model.User;
import org.eclipse.mylyn.internal.jira.core.model.Version;
import org.eclipse.mylyn.internal.jira.core.service.JiraAuthenticationException;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.jira.core.service.JiraInsufficientPermissionException;
import org.eclipse.mylyn.internal.jira.core.service.JiraServiceUnavailableException;
import org.eclipse.mylyn.internal.jira.core.service.JiraTimeFormat;
import org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteField;
import org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteIssue;
import org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteNamedObject;
import org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProjectRole;
import org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProjectRoleActors;
import org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteSecurityLevel;
import org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteServerInfo;
import org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteWorklog;
import org.eclipse.mylyn.internal.jira.core.wsdl.soap.JiraSoapService;
import org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;
import org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;
import org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException;
import org.eclipse.mylyn.internal.provisional.commons.soap.AbstractSoapClient;
import org.eclipse.osgi.util.NLS;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * @author Brock Janiczak
 * @author Steffen Pingel
 * @author Thomas Ehrnhoefer
 */
public class JiraSoapClient extends AbstractSoapClient {

	private static final String CONFIG_FILE = "org/eclipse/mylyn/internal/jira/core/service/soap/client-config.wsdd"; //$NON-NLS-1$

	private static final String ERROR_RPC_NOT_ENABLED = "JIRA RPC services are not enabled. Please contact your JIRA administrator."; //$NON-NLS-1$

	private static final String REMOTE_ERROR_BAD_ID = "White spaces are required between publicId and systemId."; //$NON-NLS-1$

	private static final String REMOTE_ERROR_BAD_ENVELOPE_TAG = "Bad envelope tag:  html"; //$NON-NLS-1$

	private static final String REMOTE_ERROR_CONTENT_NOT_ALLOWED_IN_PROLOG = "Content is not allowed in prolog."; //$NON-NLS-1$

	private static final String REMOTE_ERROR_PROCESSING_INSTRUCTIONS = "Processing instructions are not allowed within SOAP messages"; //$NON-NLS-1$

	private static final String SOAP_SERVICE_URL = "/rpc/soap/jirasoapservice-v2"; //$NON-NLS-1$

	/**
	 * Default session timeout for a JIRA instance. The default value is 10 minutes.
	 */
	private static final long DEFAULT_SESSION_TIMEOUT = 1000L * 60L * 10L;

	private JiraSoapService soapService;

	private final Lock soapServiceLock = new ReentrantLock();

	private final LoginToken loginToken;

	private final JiraClient jiraClient;

	private boolean reauthenticate;

	public JiraSoapClient(JiraClient jiraClient) {
		this.jiraClient = jiraClient;
		this.loginToken = new LoginToken(jiraClient.getLocation(), DEFAULT_SESSION_TIMEOUT);
	}

	public JiraSoapService getSoapService() throws JiraException {
		soapServiceLock.lock();
		try {
			if (soapService == null) {
				JiraSoapServiceLocator locator = new JiraSoapServiceLocator(//
						new FileProvider(getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)), //
						jiraClient);
				try {
					soapService = locator.getJirasoapserviceV2(new URL(jiraClient.getBaseUrl() + SOAP_SERVICE_URL));
				} catch (ServiceException e) {
					throw new JiraException(e);
				} catch (MalformedURLException e) {
					throw new JiraException(e);
				}

				if (soapService == null) {
					throw new JiraException("Initialization of JIRA Soap service failed"); //$NON-NLS-1$
				}
			}
			return soapService;
		} finally {
			soapServiceLock.unlock();
		}
	}

	public User getUser(IProgressMonitor monitor, final String username) throws JiraException {
		return call(monitor, new Callable<User>() {
			public User call() throws java.rmi.RemoteException, JiraException {
				return JiraSoapConverter.convert(getSoapService().getUser(loginToken.getCurrentValue(), username));
			}
		});
	}

	public Component[] getComponents(final String projectKey, IProgressMonitor monitor) throws JiraException {
		return call(monitor, new Callable<Component[]>() {
			public Component[] call() throws java.rmi.RemoteException, JiraException {
				return JiraSoapConverter.convert(getSoapService().getComponents(loginToken.getCurrentValue(),
						projectKey));
			}
		});
	}

	public String login(IProgressMonitor monitor) throws JiraException {
		loginToken.expire();
		return call(monitor, new Callable<String>() {
			public String call() throws java.rmi.RemoteException, JiraException {
				return loginToken.getCurrentValue();
			}
		});
	}

	public Group getGroup(final String name, IProgressMonitor monitor) throws JiraException {
		return call(monitor, new Callable<Group>() {
			public Group call() throws java.rmi.RemoteException, JiraException {
				return JiraSoapConverter.convert(getSoapService().getGroup(loginToken.getCurrentValue(), name));
			}
		});
	}

	public ServerInfo getServerInfo(IProgressMonitor monitor) throws JiraException {
		ServerInfo serverInfo = call(monitor, new Callable<ServerInfo>() {
			public ServerInfo call() throws java.rmi.RemoteException, JiraException {
				ServerInfo serverInfo = new ServerInfo();

				// TODO consider logging proxy information
				serverInfo.getStatistics().mark();
				try {
					InetAddress.getByName(WebUtil.getHost(getLocation().getUrl()));
					serverInfo.getStatistics().record("Resolving name took {0}"); //$NON-NLS-1$
				} catch (Exception e) {
					// ignore
				}

				serverInfo.getStatistics().mark();
				String session = loginToken.getCurrentValue();
				serverInfo.getStatistics().record("Login via SOAP took {0}"); //$NON-NLS-1$

				serverInfo.getStatistics().mark();
				RemoteServerInfo remoteServerInfo = getSoapService().getServerInfo(session);
				serverInfo.getStatistics().record("Retrieval of server info took {0}"); //$NON-NLS-1$

				return JiraSoapConverter.convert(remoteServerInfo, serverInfo);
			}
		});
		return serverInfo;
	}

	/**
	 * It is recommended to use {@link #getIssueByKey(String)} instead.
	 */
	public String getKeyFromId(final String issueId, IProgressMonitor monitor) throws JiraException {
		return call(monitor, new Callable<String>() {
			public String call() throws java.rmi.RemoteException, JiraException {
				RemoteIssue issue = getSoapService().getIssueById(loginToken.getCurrentValue(), issueId);
				return (issue != null) ? issue.getKey() : null;
			}
		});
	}

	public JiraAction[] getAvailableActions(final String taskKey, IProgressMonitor monitor) throws JiraException {
		return call(monitor, new Callable<JiraAction[]>() {
			public JiraAction[] call() throws java.rmi.RemoteException, JiraException {
				RemoteNamedObject[] actions = getSoapService().getAvailableActions(loginToken.getCurrentValue(),
						taskKey);
				if (actions == null) {
					return new JiraAction[0];
				}

				JiraAction[] operations = new JiraAction[actions.length];
				for (int i = 0; i < actions.length; i++) {
					RemoteNamedObject action = actions[i];
					operations[i] = new JiraAction(action.getId(), action.getName());
				}
				return operations;
			}
		});
	}

	public String[] getActionFields(final String taskKey, final String actionId, IProgressMonitor monitor)
			throws JiraException {
		return call(monitor, new Callable<String[]>() {
			public String[] call() throws java.rmi.RemoteException, JiraException {
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

	public IssueField[] getEditableAttributes(final String taskKey, final boolean workAroundBug205015,
			IProgressMonitor monitor) throws JiraException {
		return call(monitor, new Callable<IssueField[]>() {
			public IssueField[] call() throws java.rmi.RemoteException, JiraException {
				RemoteField[] fields = getSoapService().getFieldsForEdit(loginToken.getCurrentValue(), taskKey);
				if (fields == null) {
					return new IssueField[0];
				}

				int add = 0;
				if (workAroundBug205015) {
					add += 2;
				}

				IssueField[] attributes = new IssueField[fields.length + add];
				for (int i = 0; i < fields.length; i++) {
					RemoteField field = fields[i];
					attributes[i] = new IssueField(field.getId(), field.getName());
				}

				if (add > 0) {
					// might also need to add: Reporter and Summary (http://jira.atlassian.com/browse/JRA-13703)
					attributes[attributes.length - 2] = new IssueField("duedate", Messages.JiraSoapClient_Due_Date); //$NON-NLS-1$
					attributes[attributes.length - 1] = new IssueField(
							"fixVersions", Messages.JiraSoapClient_Fix_Version_s); //$NON-NLS-1$
				}

				return attributes;
			}
		});
	}

	public CustomField[] getCustomAttributes(IProgressMonitor monitor) throws JiraException {
		return call(monitor, new Callable<CustomField[]>() {
			public CustomField[] call() throws java.rmi.RemoteException, JiraException {
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

	public RemoteIssue getIssueByKey(final String key, IProgressMonitor monitor) throws JiraException {
		return call(monitor, new Callable<RemoteIssue>() {
			public RemoteIssue call() throws java.rmi.RemoteException, JiraException {
				return getSoapService().getIssue(loginToken.getCurrentValue(), key);
			}
		});
	}

	public Project[] getProjects(IProgressMonitor monitor) throws JiraException {
		return call(monitor, new Callable<Project[]>() {
			public Project[] call() throws java.rmi.RemoteException, JiraException {
				return JiraSoapConverter.convert(getSoapService().getProjectsNoSchemes(loginToken.getCurrentValue()));
			}
		});
	}

	public ProjectRole[] getProjectRoles(final IProgressMonitor monitor) throws JiraException {
		return call(monitor, new Callable<ProjectRole[]>() {
			public ProjectRole[] call() throws java.rmi.RemoteException, JiraException {
				String version = jiraClient.getCache().getServerInfo(monitor).getVersion();
				boolean hasApi = (new JiraVersion(version).compareTo(JiraVersion.JIRA_3_7) >= 0);
				if (hasApi) {
					RemoteProjectRole[] remoteProjectRoles = getSoapService().getProjectRoles(
							loginToken.getCurrentValue());
					return JiraSoapConverter.convert(remoteProjectRoles);
				} else {
					return null;
				}
			}
		});
	}

	public JiraStatus[] getStatuses(IProgressMonitor monitor) throws JiraException {
		return call(monitor, new Callable<JiraStatus[]>() {
			public JiraStatus[] call() throws java.rmi.RemoteException, JiraException {
				return JiraSoapConverter.convert(getSoapService().getStatuses(loginToken.getCurrentValue()));
			}
		});
	}

	public IssueType[] getIssueTypes(IProgressMonitor monitor) throws JiraException {
		return call(monitor, new Callable<IssueType[]>() {
			public IssueType[] call() throws java.rmi.RemoteException, JiraException {
				return JiraSoapConverter.convert(getSoapService().getIssueTypes(loginToken.getCurrentValue()));
			}
		});
	}

	public IssueType[] getSubTaskIssueTypes(IProgressMonitor monitor) throws JiraException {
		return call(monitor, new Callable<IssueType[]>() {
			public IssueType[] call() throws java.rmi.RemoteException, JiraException {
				return JiraSoapConverter.convert(getSoapService().getSubTaskIssueTypes(loginToken.getCurrentValue()));
			}
		});
	}

	public Priority[] getPriorities(IProgressMonitor monitor) throws JiraException {
		return call(monitor, new Callable<Priority[]>() {
			public Priority[] call() throws java.rmi.RemoteException, JiraException {
				return JiraSoapConverter.convert(getSoapService().getPriorities(loginToken.getCurrentValue()));
			}
		});
	}

	public Resolution[] getResolutions(IProgressMonitor monitor) throws JiraException {
		return call(monitor, new Callable<Resolution[]>() {
			public Resolution[] call() throws java.rmi.RemoteException, JiraException {
				return JiraSoapConverter.convert(getSoapService().getResolutions(loginToken.getCurrentValue()));
			}
		});
	}

	public Comment[] getComments(final String issueKey, IProgressMonitor monitor) throws JiraException {
		return call(monitor, new Callable<Comment[]>() {
			public Comment[] call() throws java.rmi.RemoteException, JiraException {
				return JiraSoapConverter.convert(getSoapService().getComments(loginToken.getCurrentValue(), issueKey));
			}
		});
	}

	public Version[] getVersions(final String componentKey, IProgressMonitor monitor) throws JiraException {
		return call(monitor, new Callable<Version[]>() {
			public Version[] call() throws java.rmi.RemoteException, JiraException {
				return JiraSoapConverter.convert(getSoapService().getVersions(loginToken.getCurrentValue(),
						componentKey));
			}
		});
	}

	public JiraWorkLog[] getWorkLogs(final String issueKey, final IProgressMonitor monitor) throws JiraException {
		return call(monitor, new Callable<JiraWorkLog[]>() {
			public JiraWorkLog[] call() throws java.rmi.RemoteException, JiraException {
				String version = jiraClient.getCache().getServerInfo(monitor).getVersion();
				boolean hasApi = (new JiraVersion(version).compareTo(JiraVersion.JIRA_3_10) >= 0);
				if (hasApi) {
					return JiraSoapConverter.convert(getSoapService().getWorklogs(loginToken.getCurrentValue(),
							issueKey));
				} else {
					return null;
				}
			}
		});
	}

	public void logout(IProgressMonitor monitor) throws JiraException {
		callOnce(monitor, new Callable<Object>() {
			public Object call() throws java.rmi.RemoteException, JiraException {
				loginToken.expire();
				return null;
			}
		});
	}

	public NamedFilter[] getNamedFilters(IProgressMonitor monitor) throws JiraException {
		return call(monitor, new Callable<NamedFilter[]>() {
			public NamedFilter[] call() throws java.rmi.RemoteException, JiraException {
				return JiraSoapConverter.convert(getSoapService().getSavedFilters(loginToken.getCurrentValue()));
			}
		});
	}

	public IssueType[] getIssueTypes(final String projectId, IProgressMonitor monitor) throws JiraException {
		return call(monitor, new Callable<IssueType[]>() {
			public IssueType[] call() throws java.rmi.RemoteException, JiraException {
				getSoapService().getIssueTypes(loginToken.getCurrentValue());
				return JiraSoapConverter.convert(getSoapService().getIssueTypesForProject(loginToken.getCurrentValue(),
						projectId));
			}
		});
	}

	public IssueType[] getSubTaskIssueTypes(final String projectId, IProgressMonitor monitor) throws JiraException {
		return call(monitor, new Callable<IssueType[]>() {
			public IssueType[] call() throws java.rmi.RemoteException, JiraException {
				return JiraSoapConverter.convert(getSoapService().getSubTaskIssueTypesForProject(
						loginToken.getCurrentValue(), projectId));
			}
		});
	}

	public void purgeSession() {
		this.reauthenticate = true;
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
					return Messages.JiraSoapClient_Internal_Server_Error;
				case HttpURLConnection.HTTP_UNAVAILABLE:
					return ERROR_RPC_NOT_ENABLED;
				case HttpURLConnection.HTTP_NOT_FOUND:
					return Messages.JiraSoapClient_No_JIRA_repository_found_at_location;
				case HttpURLConnection.HTTP_MOVED_PERM:
					return Messages.JiraSoapClient_The_location_of_the_Jira_server_has_moved;
				}
			}
		}

		if (e.getCause() != null) {
			Throwable cause = e.getCause();
			if (cause instanceof UnknownHostException) {
				return Messages.JiraSoapClient_Unknown_host;
			} else if (cause instanceof ConnectException) {
				return Messages.JiraSoapClient_Unable_to_connect_to_server;
			} else if (cause instanceof SAXException) {
				if (REMOTE_ERROR_BAD_ENVELOPE_TAG.equalsIgnoreCase(cause.getMessage())
						|| REMOTE_ERROR_BAD_ID.equals(cause.getMessage())
						|| REMOTE_ERROR_CONTENT_NOT_ALLOWED_IN_PROLOG.equals(cause.getMessage())
						|| REMOTE_ERROR_PROCESSING_INSTRUCTIONS.equals(cause.getMessage())) {
					return ERROR_RPC_NOT_ENABLED;
				}
			}
			return e.getCause().getLocalizedMessage();
		}

		if (e instanceof AxisFault) {
			return Messages.JiraSoapClient_Server_error_ + e.getLocalizedMessage();
		}

		return e.getLocalizedMessage();
	}

	@Override
	protected <T> T call(IProgressMonitor monitor, Callable<T> runnable) throws JiraException {
		try {
			return super.call(monitor, runnable);
		} catch (JiraException e) {
			throw e;
		} catch (RuntimeException e) {
			throw e;
		} catch (Error e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected <T> T callOnce(IProgressMonitor monitor, Callable<T> runnable) throws JiraException {
		try {
			return super.callOnce(monitor, runnable);
		} catch (RemotePermissionException e) {
			throw new JiraInsufficientPermissionException(e.getMessage());
		} catch (RemoteAuthenticationException e) {
			throw new JiraAuthenticationException(e.getMessage());
		} catch (RemoteException e) {
			String message = e.getMessage();
			if (message == null) {
				message = NLS.bind("Service unavailabe: {0}", e.getFaultReason()); //$NON-NLS-1$
			}
			throw new JiraServiceUnavailableException(message);
		} catch (java.rmi.RemoteException e) {
			throw new JiraServiceUnavailableException(unwrapRemoteException(e));
		} catch (JiraException e) {
			throw e;
		} catch (RuntimeException e) {
			throw e;
		} catch (Error e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected AbstractWebLocation getLocation() {
		return jiraClient.getLocation();
	}

	@Override
	protected boolean isAuthenticationException(Exception e) {
		return e instanceof JiraAuthenticationException;
	}

	@Override
	protected boolean doLogin(IProgressMonitor monitor) {
		loginToken.expire();
		return true;
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

		public synchronized String getCurrentValue() throws JiraException, RemoteAuthenticationException,
				RemoteException, java.rmi.RemoteException {
			AuthenticationCredentials newCredentials = location.getCredentials(AuthenticationType.REPOSITORY);
			if (newCredentials == null) {
				// anonymous login
				expire();
				return ""; //$NON-NLS-1$
			} else if (!newCredentials.equals(credentials) || reauthenticate) {
				// credentials have changed since last request
				reauthenticate = false;
				expire();
				credentials = newCredentials;
			}

			if ((System.currentTimeMillis() - lastAccessed) >= timeout || token == null) {
				expire();

				this.token = getSoapService().login(credentials.getUserName(), credentials.getPassword());
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
			return "[credentials=" + credentials + ", timeout=" + timeout + ", valid=" + isValidToken() + ", expires=" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ 
					+ expiresIn + "]"; //$NON-NLS-1$
		}

	}

	public SecurityLevel[] getAvailableSecurityLevels(final String projectKey, IProgressMonitor monitor)
			throws JiraException {
		return call(monitor, new Callable<SecurityLevel[]>() {
			public SecurityLevel[] call() throws java.rmi.RemoteException, JiraException {
				RemoteSecurityLevel[] remoteSecurityLevels = getSoapService().getSecurityLevels(
						loginToken.getCurrentValue(), projectKey);
				if (remoteSecurityLevels == null) {
					return new SecurityLevel[0];
				}
				SecurityLevel[] securityLevels = JiraSoapConverter.convert(remoteSecurityLevels);
				return securityLevels;
			}
		});
	}

	public JiraWorkLog addWorkLog(final String issueKey, final JiraWorkLog log, IProgressMonitor monitor)
			throws JiraException {
		return call(monitor, new Callable<JiraWorkLog>() {
			public JiraWorkLog call() throws java.rmi.RemoteException, JiraException {
				JiraTimeFormat formatter = new JiraTimeFormat(jiraClient.getConfiguration().getWorkDaysPerWeek(),
						jiraClient.getConfiguration().getWorkHoursPerDay());
				RemoteWorklog remoteLog = JiraSoapConverter.convert(log, formatter);
				if (log.isAutoAdjustEstimate()) {
					remoteLog = getSoapService().addWorklogAndAutoAdjustRemainingEstimate(loginToken.getCurrentValue(),
							issueKey, remoteLog);
				} else {
					remoteLog = getSoapService().addWorklogAndRetainRemainingEstimate(loginToken.getCurrentValue(),
							issueKey, remoteLog);
				}
				return (remoteLog != null) ? JiraSoapConverter.convert(remoteLog) : null;
			}
		});
	}

	public void addComment(final String issueKey, final Comment comment, IProgressMonitor monitor) throws JiraException {
		call(monitor, new Callable<Object>() {
			public Object call() throws java.rmi.RemoteException, JiraException {
				getSoapService().addComment(loginToken.getCurrentValue(), issueKey, JiraSoapConverter.convert(comment));
				return null;
			}
		});
	}

	public User[] getProjectRoleUsers(final Project project, final ProjectRole projectRole, IProgressMonitor monitor)
			throws JiraException {
		return call(monitor, new Callable<User[]>() {
			public User[] call() throws Exception {
				RemoteProjectRoleActors actors = getSoapService().getProjectRoleActors(loginToken.getCurrentValue(),
						JiraSoapConverter.convert(projectRole), JiraSoapConverter.convert(project));

				return JiraSoapConverter.convert(actors.getUsers());
			}
		});
	}

}