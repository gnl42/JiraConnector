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
import com.atlassian.connector.eclipse.internal.core.client.AbstractConnectorClient;
import com.atlassian.connector.eclipse.internal.core.client.HttpSessionCallbackImpl;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Bridge between Mylyn and the ACC API's
 * 
 * @author Wojciech Seliga
 */
public class FishEyeClient extends AbstractConnectorClient<FishEyeServerFacade2> implements IUpdateRepositoryData,
		IClientDataProvider {

	private final FishEyeClientData clientData;

	public FishEyeClient(AbstractWebLocation location, ConnectionCfg connectionCfg, FishEyeServerFacade2 facade,
			FishEyeClientData data, HttpSessionCallbackImpl callback) {
		super(location, connectionCfg, facade, callback);
		this.clientData = data;
	}

	@Override
	@Nullable
	public String getUsername() {
		AuthenticationCredentials credentials = location.getCredentials(AuthenticationType.REPOSITORY);
		if (credentials != null) {
			return credentials.getUserName();
		} else {
			return null;
		}
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

}
