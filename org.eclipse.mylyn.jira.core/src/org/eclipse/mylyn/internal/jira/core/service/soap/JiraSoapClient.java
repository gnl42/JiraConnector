/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.service.soap;

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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.internal.jira.core.model.Comment;
import org.eclipse.mylyn.internal.jira.core.model.Component;
import org.eclipse.mylyn.internal.jira.core.model.CustomField;
import org.eclipse.mylyn.internal.jira.core.model.Group;
import org.eclipse.mylyn.internal.jira.core.model.IssueType;
import org.eclipse.mylyn.internal.jira.core.model.JiraStatus;
import org.eclipse.mylyn.internal.jira.core.model.NamedFilter;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.model.ServerInfo;
import org.eclipse.mylyn.internal.jira.core.model.User;
import org.eclipse.mylyn.internal.jira.core.model.Version;
import org.eclipse.mylyn.internal.jira.core.service.JiraAuthenticationException;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.jira.core.service.JiraInsufficientPermissionException;
import org.eclipse.mylyn.internal.jira.core.service.JiraServiceUnavailableException;
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
import org.eclipse.mylyn.web.core.Policy;
import org.eclipse.mylyn.web.core.WebUtil;
import org.eclipse.mylyn.web.core.AbstractWebLocation.ResultType;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * @author Brock Janiczak
 * @author Steffen Pingel
 */
public class JiraSoapClient {

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

	private final Lock soapServiceLock = new ReentrantLock();

	private final LoginToken loginToken;

	private final JiraClient jiraClient;

	public JiraSoapClient(JiraClient jiraClient) {
		this.jiraClient = jiraClient;
		this.loginToken = new LoginToken(jiraClient.getLocation(), DEFAULT_SESSION_TIMEOUT);
	}

	private JiraSoapService getSoapService() throws JiraException {
		soapServiceLock.lock();
		try {
			if (soapService == null) {
				JiraSoapServiceLocator locator = new JiraSoapServiceLocator(new FileProvider(this.getClass()
						.getClassLoader()
						.getResourceAsStream("client-config.wsdd")));
				locator.setLocation(jiraClient.getLocation());
				locator.setCompression(jiraClient.useCompression());

				try {
					soapService = locator.getJirasoapserviceV2(new URL(jiraClient.getBaseUrl() + SOAP_SERVICE_URL));
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

	public User getUser(IProgressMonitor monitor, final String username) throws JiraException {
		return call(monitor, new RemoteRunnable<User>() {
			public User run() throws java.rmi.RemoteException, JiraException {
				return JiraSoapConverter.convert(getSoapService().getUser(loginToken.getCurrentValue(), username));
			}
		});
	}

	public Component[] getComponents(final String projectKey, IProgressMonitor monitor) throws JiraException {
		return call(monitor, new RemoteRunnable<Component[]>() {
			public Component[] run() throws java.rmi.RemoteException, JiraException {
				return JiraSoapConverter.convert(getSoapService().getComponents(loginToken.getCurrentValue(),
						projectKey));
			}
		});
	}

	public void login(IProgressMonitor monitor) throws JiraException {
		loginToken.expire();
		call(monitor, new RemoteRunnable<Object>() {
			public Object run() throws java.rmi.RemoteException, JiraException {
				return loginToken.getCurrentValue();
			}
		});
	}

	public Group getGroup(final String name, IProgressMonitor monitor) throws JiraException {
		return call(monitor, new RemoteRunnable<Group>() {
			public Group run() throws java.rmi.RemoteException, JiraException {
				return JiraSoapConverter.convert(getSoapService().getGroup(loginToken.getCurrentValue(), name));
			}
		});
	}

	public ServerInfo getServerInfo(IProgressMonitor monitor) throws JiraException {
		ServerInfo serverInfo = call(monitor, new RemoteRunnable<ServerInfo>() {
			public ServerInfo run() throws java.rmi.RemoteException, JiraException {
				return JiraSoapConverter.convert(getSoapService().getServerInfo(loginToken.getCurrentValue()));
			}
		});
		return serverInfo;
	}

	/**
	 * It is recommended to use {@link #getIssueByKey(String)} instead.
	 */
	public String getKeyFromId(final String issueId, IProgressMonitor monitor) throws JiraException {
		return call(monitor, new RemoteRunnable<String>() {
			public String run() throws java.rmi.RemoteException, JiraException {
				RemoteIssue issue = getSoapService().getIssueById(loginToken.getCurrentValue(), issueId);
				return (issue != null) ? issue.getKey() : null;
			}
		});
	}

	public RepositoryOperation[] getAvailableOperations(final String taskKey, IProgressMonitor monitor)
			throws JiraException {
		return call(monitor, new RemoteRunnable<RepositoryOperation[]>() {
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

	public String[] getActionFields(final String taskKey, final String actionId, IProgressMonitor monitor)
			throws JiraException {
		return call(monitor, new RemoteRunnable<String[]>() {
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

	public RepositoryTaskAttribute[] getEditableAttributes(final String taskKey, final boolean workAroundBug205015,
			IProgressMonitor monitor) throws JiraException {
		return call(monitor, new RemoteRunnable<RepositoryTaskAttribute[]>() {
			public RepositoryTaskAttribute[] run() throws java.rmi.RemoteException, JiraException {
				RemoteField[] fields = getSoapService().getFieldsForEdit(loginToken.getCurrentValue(), taskKey);
				if (fields == null) {
					return new RepositoryTaskAttribute[0];
				}

				int add = 0;
				if (workAroundBug205015) {
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
					attributes[attributes.length - 1] = new RepositoryTaskAttribute("fixVersions", "Fix Version/s",
							false);
				}

				return attributes;
			}
		});
	}

	public CustomField[] getCustomAttributes(IProgressMonitor monitor) throws JiraException {
		return call(monitor, new RemoteRunnable<CustomField[]>() {
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

	public RemoteIssue getIssueByKey(final String key, IProgressMonitor monitor) throws JiraException {
		return call(monitor, new RemoteRunnable<RemoteIssue>() {
			public RemoteIssue run() throws java.rmi.RemoteException, JiraException {
				return getSoapService().getIssue(loginToken.getCurrentValue(), key);
			}
		});
	}

	public Project[] getProjects(IProgressMonitor monitor) throws JiraException {
		return call(monitor, new RemoteRunnable<Project[]>() {
			public Project[] run() throws java.rmi.RemoteException, JiraException {
				return JiraSoapConverter.convert(getSoapService().getProjects(loginToken.getCurrentValue()));
			}
		});
	}

	public Project[] getProjectsNoSchemes(IProgressMonitor monitor) throws JiraException {
		return call(monitor, new RemoteRunnable<Project[]>() {
			public Project[] run() throws java.rmi.RemoteException, JiraException {
				return JiraSoapConverter.convert(getSoapService().getProjectsNoSchemes(loginToken.getCurrentValue()));
			}
		});
	}

	public JiraStatus[] getStatuses(IProgressMonitor monitor) throws JiraException {
		return call(monitor, new RemoteRunnable<JiraStatus[]>() {
			public JiraStatus[] run() throws java.rmi.RemoteException, JiraException {
				return JiraSoapConverter.convert(getSoapService().getStatuses(loginToken.getCurrentValue()));
			}
		});
	}

	public IssueType[] getIssueTypes(IProgressMonitor monitor) throws JiraException {
		return call(monitor, new RemoteRunnable<IssueType[]>() {
			public IssueType[] run() throws java.rmi.RemoteException, JiraException {
				return JiraSoapConverter.convert(getSoapService().getIssueTypes(loginToken.getCurrentValue()));
			}
		});
	}

	public IssueType[] getSubTaskIssueTypes(IProgressMonitor monitor) throws JiraException {
		return call(monitor, new RemoteRunnable<IssueType[]>() {
			public IssueType[] run() throws java.rmi.RemoteException, JiraException {
				return JiraSoapConverter.convert(getSoapService().getSubTaskIssueTypes(loginToken.getCurrentValue()));
			}
		});
	}

	public Priority[] getPriorities(IProgressMonitor monitor) throws JiraException {
		return call(monitor, new RemoteRunnable<Priority[]>() {
			public Priority[] run() throws java.rmi.RemoteException, JiraException {
				return JiraSoapConverter.convert(getSoapService().getPriorities(loginToken.getCurrentValue()));
			}
		});
	}

	public Resolution[] getResolutions(IProgressMonitor monitor) throws JiraException {
		return call(monitor, new RemoteRunnable<Resolution[]>() {
			public Resolution[] run() throws java.rmi.RemoteException, JiraException {
				return JiraSoapConverter.convert(getSoapService().getResolutions(loginToken.getCurrentValue()));
			}
		});
	}

	public Comment[] getComments(String issueKey, IProgressMonitor monitor) throws JiraException {
		return call(monitor, new RemoteRunnable<Comment[]>() {
			public Comment[] run() throws java.rmi.RemoteException, JiraException {
				// TODO implement
				// return
				// Converter.convert(jirasoapserviceV2.getComments(loginToken.getCurrentValue(),
				// issueKey));
				return null;
			}
		});
	}

	public Version[] getVersions(final String componentKey, IProgressMonitor monitor) throws JiraException {
		return call(monitor, new RemoteRunnable<Version[]>() {
			public Version[] run() throws java.rmi.RemoteException, JiraException {
				return JiraSoapConverter.convert(getSoapService().getVersions(loginToken.getCurrentValue(),
						componentKey));
			}
		});
	}

	public void logout(IProgressMonitor monitor) {
		loginToken.expire();
	}

	public NamedFilter[] getNamedFilters(IProgressMonitor monitor) throws JiraException {
		return call(monitor, new RemoteRunnable<NamedFilter[]>() {
			public NamedFilter[] run() throws java.rmi.RemoteException, JiraException {
				return JiraSoapConverter.convert(getSoapService().getSavedFilters(loginToken.getCurrentValue()));
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

	private <T> T callWithRetry(RemoteRunnable<T> runnable, IProgressMonitor monitor) throws JiraException {
		try {
			return callOnce(runnable, monitor);
		} catch (JiraAuthenticationException e) {
			loginToken.expire();
			return callOnce(runnable, monitor);
		}
	}

	private <T> T callOnce(final RemoteRunnable<T> runnable, IProgressMonitor monitor) throws JiraException,
			JiraInsufficientPermissionException, JiraAuthenticationException, JiraServiceUnavailableException {
		try {
			monitor = Policy.monitorFor(monitor);

			final JiraRequest request = new JiraRequest(monitor);
			return WebUtil.poll(monitor, new WebUtil.AbortableCallable<T>() {

				public void abort() {
					request.cancel();
				}

				public T call() throws Exception {
					try {
						JiraRequest.setCurrentRequest(request);
						return runnable.run();
					} finally {
						request.done();
					}
				}

			});
		} catch (RemotePermissionException e) {
			throw new JiraInsufficientPermissionException(e.getMessage());
		} catch (RemoteAuthenticationException e) {
			throw new JiraAuthenticationException(e.getMessage());
		} catch (RemoteException e) {
			throw new JiraServiceUnavailableException(e.getMessage());
		} catch (java.rmi.RemoteException e) {
			throw new JiraServiceUnavailableException(unwrapRemoteException(e));
		} catch (JiraException e) {
			throw e;
		} catch (RuntimeException e) {
			throw e;
		} catch (Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private <T> T call(IProgressMonitor monitor, RemoteRunnable<T> runnable) throws JiraException {
		while (true) {
			try {
				return callWithRetry(runnable, monitor);
			} catch (JiraAuthenticationException e) {
				if (jiraClient.getLocation().requestCredentials(AuthenticationType.REPOSITORY, null) == ResultType.NOT_SUPPORTED) {
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

		public synchronized String getCurrentValue() throws JiraException, RemoteAuthenticationException,
				RemoteException, java.rmi.RemoteException {
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
			return "[credentials=" + credentials + ", timeout=" + timeout + ", valid=" + isValidToken() + ", expires="
					+ expiresIn + "]";
		}

	}

}