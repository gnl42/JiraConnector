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

package com.atlassian.connector.eclipse.internal.fisheye.core.client;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.fisheye.FishEyeServerFacade2;
import com.atlassian.connector.eclipse.internal.fisheye.core.FishEyeCorePlugin;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
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
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;

/**
 * Bridge between Mylyn and the ACC API's
 * 
 * @author Shawn Minto (original design)
 * @author Thomas Ehrnhoefer
 * @author Wojciech Seliga (adaptation for FishEye)
 */
public class FishEyeClient {

	private final FishEyeClientData clientData;

	private AbstractWebLocation location;

	private ConnectionCfg serverData;

	private final FishEyeServerFacade2 fishEyeServer;

	public FishEyeClient(AbstractWebLocation location, ConnectionCfg serverData, FishEyeServerFacade2 fishEyeServer,
			FishEyeClientData data) {
		this.location = location;
		this.clientData = data;
		this.serverData = serverData;
		this.fishEyeServer = fishEyeServer;
	}

	public ConnectionCfg getServerData() {
		return serverData;
	}

	public void setCrucibleServerCfg(ConnectionCfg crucibleServerCfg) {
		this.serverData = crucibleServerCfg;
	}

	public <T> T execute(FishEyeRemoteOperation<T> op) throws CoreException {
		IProgressMonitor monitor = op.getMonitor();
		TaskRepository taskRepository = op.getTaskRepository();
		try {

			if (taskRepository.getCredentials(AuthenticationType.REPOSITORY).getPassword().length() < 1) {
				try {
					location.requestCredentials(AuthenticationType.REPOSITORY, null, monitor);
				} catch (UnsupportedRequestException e) {
					// ignore
				}
			}

			monitor.beginTask("Connecting to FishEye", IProgressMonitor.UNKNOWN);
			updateServer();
			return op.run(fishEyeServer, serverData, op.getMonitor());
		} catch (CrucibleLoginException e) {
			return executeRetry(op, monitor, e);
		} catch (RemoteApiLoginException e) {
			if (e.getCause() instanceof IOException) {
				throw new CoreException(new Status(IStatus.ERROR, FishEyeCorePlugin.PLUGIN_ID, e.getMessage(), e));
			}
			return executeRetry(op, monitor, e);
		} catch (ServerPasswordNotProvidedException e) {
			return executeRetry(op, monitor, e);
		} catch (RemoteApiException e) {
			throw new CoreException(new Status(IStatus.ERROR, FishEyeCorePlugin.PLUGIN_ID, e.getMessage(), e));
		} finally {
			monitor.done();
		}
	}

	private <T> T executeRetry(FishEyeRemoteOperation<T> op, IProgressMonitor monitor, Exception e)
			throws CoreException {
		try {
			location.requestCredentials(AuthenticationType.REPOSITORY, null, monitor);
		} catch (UnsupportedRequestException ex) {
			throw new CoreException(new Status(IStatus.ERROR, FishEyeCorePlugin.PLUGIN_ID,
					RepositoryStatus.ERROR_REPOSITORY_LOGIN, e.getMessage(), e));
		}
		return execute(op);
	}

	@Nullable
	public String getUserName() {
		AuthenticationCredentials credentials = location.getCredentials(AuthenticationType.REPOSITORY);
		if (credentials != null) {
			return credentials.getUserName();
		} else {
			return null;
		}
	}

	private void updateServer() {
		AuthenticationCredentials credentials = location.getCredentials(AuthenticationType.REPOSITORY);
		if (credentials != null) {
			serverData = new ConnectionCfg(serverData.getId(), serverData.getUrl(), credentials.getUserName(),
					credentials.getPassword());
		}
	}

	public void validate(IProgressMonitor monitor, TaskRepository taskRepository) throws CoreException {
		execute(new FishEyeRemoteOperation<Void>(monitor, taskRepository) {
			@Override
			public Void run(FishEyeServerFacade2 server, ConnectionCfg aServerData, IProgressMonitor monitor)
					throws RemoteApiException, ServerPasswordNotProvidedException {
				server.testServerConnection(aServerData);
				return null;
			}
		});
	}

	public boolean hasRepositoryData() {
		return clientData != null && clientData.hasData();
	}

	public FishEyeClientData getClientData() {
		return clientData;
	}

	public void updateRepositoryData(IProgressMonitor monitor, TaskRepository taskRepository) throws CoreException {
		execute(new FishEyeRemoteOperation<Void>(monitor, taskRepository) {
			@Override
			public Void run(FishEyeServerFacade2 server, ConnectionCfg aServerData, IProgressMonitor monitor)
					throws RemoteApiException, ServerPasswordNotProvidedException {

				monitor.subTask("Retrieving FishEye repositories");
				Collection<String> repositories = server.getRepositories(aServerData);
				clientData.setRepositories(repositories);
				return null;
			}
		});
	}

	// needed so that the ui location can replace the default one
	public void updateLocation(AbstractWebLocation newLocation) {
		this.location = newLocation;
	}

}
