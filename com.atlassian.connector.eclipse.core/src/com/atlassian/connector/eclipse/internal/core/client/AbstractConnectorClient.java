/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.core.client;

import static com.atlassian.connector.eclipse.internal.core.AtlassianCorePlugin.PLUGIN_ID;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.ProductServerFacade;
import com.atlassian.theplugin.commons.remoteapi.ProductSession;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.net.UnsupportedRequestException;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public abstract class AbstractConnectorClient<F extends ProductServerFacade, S extends ProductSession> {
	protected final F facade;

	protected AbstractWebLocation location;

	protected ConnectionCfg connectionCfg;

	private final HttpSessionCallbackImpl callback;

	public AbstractConnectorClient(AbstractWebLocation location, ConnectionCfg connectionCfg, F facade,
			HttpSessionCallbackImpl callback) {
		this.location = location;
		this.connectionCfg = connectionCfg;
		this.facade = facade;
		this.callback = callback;
	}

	public String getUsername() {
		AuthenticationCredentials credentials = location.getCredentials(AuthenticationType.REPOSITORY);
		if (credentials != null) {
			return credentials.getUserName();
		} else {
			return null;
		}
	}

	public <T> T execute(RemoteOperation<T, F> op) throws CoreException {
		return execute(op, true);
	}

	public <T> T execute(RemoteSessionOperation<T, S> op) throws CoreException {
		return execute(op, true);
	}

	private <T> T executeRetry(RemoteSessionOperation<T, S> op, IProgressMonitor monitor, Exception e)
			throws CoreException {
		try {
			location.requestCredentials(AuthenticationType.REPOSITORY, null, monitor);
		} catch (UnsupportedRequestException ex) {
			throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, RepositoryStatus.ERROR_REPOSITORY_LOGIN,
					e.getMessage(), e));
		}
		return execute(op);
	}

	public final <T> T execute(RemoteSessionOperation<T, S> op, boolean promptForCredentials) throws CoreException {
		IProgressMonitor monitor = op.getMonitor();
		TaskRepository taskRepository = op.getTaskRepository();
		try {
			AuthenticationCredentials creds = taskRepository.getCredentials(AuthenticationType.REPOSITORY);
			if (creds != null && creds.getPassword().length() < 1 && promptForCredentials) {
				try {
					location.requestCredentials(AuthenticationType.REPOSITORY, null, monitor);
				} catch (UnsupportedRequestException e) {
					// ignore
				}
			}

			monitor.beginTask("Connecting to " + facade.getServerType().getShortName(), IProgressMonitor.UNKNOWN);
			updateServer();
			// @todo refactor this part in 10 years or so - as this is hack which workarounds facade ill design
			callback.updateHostConfiguration(location, connectionCfg);
			return op.run(getSession(connectionCfg), op.getMonitor());
		} catch (CrucibleLoginException e) {
			return executeRetry(op, monitor, e);
		} catch (RemoteApiLoginException e) {
			if (e.getCause() instanceof IOException) {
				throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, e.getMessage(), e));
			}
			return executeRetry(op, monitor, e);
		} catch (ServerPasswordNotProvidedException e) {
			return executeRetry(op, monitor, e);
		} catch (RemoteApiException e) {
			throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, e.getMessage(), e));
		} finally {
			monitor.done();
		}
	}

	@NotNull
	protected abstract S getSession(ConnectionCfg connectionCfg) throws RemoteApiException,
			ServerPasswordNotProvidedException;

	public final <T> T execute(RemoteOperation<T, F> op, boolean promptForCredentials) throws CoreException {
		IProgressMonitor monitor = op.getMonitor();
		TaskRepository taskRepository = op.getTaskRepository();
		try {
			AuthenticationCredentials creds = taskRepository.getCredentials(AuthenticationType.REPOSITORY);
			if (creds != null && creds.getPassword().length() < 1 && promptForCredentials) {
				try {
					location.requestCredentials(AuthenticationType.REPOSITORY, null, monitor);
				} catch (UnsupportedRequestException e) {
					// ignore
				}
			}

			monitor.beginTask("Connecting to " + facade.getServerType().getShortName(), IProgressMonitor.UNKNOWN);
			updateServer();
			// @todo refactor this part in 10 years or so - as this is hack which workarounds facade ill design
			callback.updateHostConfiguration(location, connectionCfg);
			return op.run(facade, connectionCfg, op.getMonitor());
		} catch (CrucibleLoginException e) {
			return executeRetry(op, monitor, e);
		} catch (RemoteApiLoginException e) {
			if (e.getCause() instanceof IOException) {
				throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, e.getMessage(), e));
			}
			return executeRetry(op, monitor, e);
		} catch (ServerPasswordNotProvidedException e) {
			return executeRetry(op, monitor, e);
		} catch (RemoteApiException e) {
			throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, e.getMessage(), e));
		} finally {
			monitor.done();
		}
	}

	private <T> T executeRetry(RemoteOperation<T, F> op, IProgressMonitor monitor, Exception e) throws CoreException {
		try {
			location.requestCredentials(AuthenticationType.REPOSITORY, null, monitor);
		} catch (UnsupportedRequestException ex) {
			throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, RepositoryStatus.ERROR_REPOSITORY_LOGIN,
					e.getMessage(), e));
		}
		return execute(op);
	}

	protected void updateServer() {
		AuthenticationCredentials credentials = location.getCredentials(AuthenticationType.REPOSITORY);
		if (credentials != null) {
			connectionCfg = new ConnectionCfg(connectionCfg.getId(), connectionCfg.getUrl(), credentials.getUserName(),
					credentials.getPassword());
		}
	}

	public void validate(IProgressMonitor monitor, TaskRepository taskRepository) throws CoreException {
		execute(new RemoteOperation<Void, F>(monitor, taskRepository) {
			@Override
			public Void run(F server, ConnectionCfg serverCfg, IProgressMonitor monitor) throws CrucibleLoginException,
					RemoteApiException, ServerPasswordNotProvidedException {
				server.testServerConnection(serverCfg);
				return null;
			}
		});
	}

	// needed so that the ui location can replace the default one
	public void updateLocation(AbstractWebLocation newLocation) {
		this.location = newLocation;
	}

	public ConnectionCfg getServerData() {
		return connectionCfg;
	}

}
