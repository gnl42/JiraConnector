/*******************************************************************************
 * Copyright (c) 2008 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.bamboo.core.client;

import com.atlassian.connector.eclipse.internal.bamboo.core.BambooCorePlugin;
import com.atlassian.theplugin.commons.bamboo.BambooPlan;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacade;
import com.atlassian.theplugin.commons.cfg.BambooServerCfg;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;

import java.util.Collection;

/**
 * Bridge between Mylyn and the ACC API's
 * 
 * @author Shawn Minto
 */
public class BambooClient {

	private BambooClientData clientData;

	private final AbstractWebLocation location;

	private final BambooServerCfg serverCfg;

	private final BambooServerFacade server;

	private abstract static class RemoteOperation<T> {

		private final IProgressMonitor fMonitor;

		public RemoteOperation(IProgressMonitor monitor) {
			this.fMonitor = Policy.monitorFor(monitor);
		}

		public IProgressMonitor getMonitor() {
			return fMonitor;
		}

		public abstract T run(IProgressMonitor monitor) throws CrucibleLoginException, RemoteApiException,
				ServerPasswordNotProvidedException;

	}

	public BambooClient(AbstractWebLocation location, BambooServerCfg serverCfg, BambooServerFacade server,
			BambooClientData data) {
		this.location = location;
		this.clientData = data;
		this.serverCfg = serverCfg;
		this.server = server;
	}

	public void validate(IProgressMonitor monitor) throws CoreException {
		execute(new RemoteOperation<Object>(monitor) {
			@Override
			public Object run(IProgressMonitor monitor) throws CrucibleLoginException, RemoteApiException {
				server.testServerConnection(serverCfg);
				return null;
			}
		});
	}

	public <T> T execute(RemoteOperation<T> op) throws CoreException {
		IProgressMonitor monitor = op.getMonitor();
		try {
			monitor.beginTask("Connecting to Bamboo server", IProgressMonitor.UNKNOWN);
			updateCredentials();
			return op.run(op.getMonitor());
		} catch (CrucibleLoginException e) {
			throw new CoreException(new Status(IStatus.ERROR, BambooCorePlugin.PLUGIN_ID,
					RepositoryStatus.ERROR_REPOSITORY_LOGIN, e.getMessage(), e));
		} catch (RemoteApiException e) {
			throw new CoreException(new Status(IStatus.ERROR, BambooCorePlugin.PLUGIN_ID, e.getMessage(), e));
		} catch (ServerPasswordNotProvidedException e) {
			throw new CoreException(new Status(IStatus.ERROR, BambooCorePlugin.PLUGIN_ID,
					RepositoryStatus.ERROR_REPOSITORY_LOGIN, e.getMessage(), e));
		} finally {
			monitor.done();
		}
	}

	private void updateCredentials() {
		AuthenticationCredentials credentials = location.getCredentials(AuthenticationType.REPOSITORY);
		if (credentials != null) {
			String newUserName = credentials.getUserName();
			String newPassword = credentials.getPassword();
			serverCfg.setUsername(newUserName);
			serverCfg.setPassword(newPassword);
		}
	}

	public boolean hasRepositoryData() {
		return clientData != null && clientData.hasData();
	}

	public BambooClientData getClientData() {
		return clientData;
	}

	public BambooClientData updateRepositoryData(IProgressMonitor monitor) throws CoreException {
		this.clientData = execute(new RemoteOperation<BambooClientData>(monitor) {
			@Override
			public BambooClientData run(IProgressMonitor monitor) throws CrucibleLoginException, RemoteApiException,
					ServerPasswordNotProvidedException {
				monitor.subTask("Retrieving Bamboo plans");
				BambooClientData newClientData = new BambooClientData();
				Collection<BambooPlan> projects = server.getPlanList(serverCfg);
				newClientData.setPlans(projects);
				return newClientData;
			}
		});
		return clientData;
	}
}
