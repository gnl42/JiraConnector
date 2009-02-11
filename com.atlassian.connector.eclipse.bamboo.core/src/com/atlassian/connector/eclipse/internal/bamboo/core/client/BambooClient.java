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

package com.atlassian.connector.eclipse.internal.bamboo.core.client;

import com.atlassian.connector.eclipse.internal.bamboo.core.BambooCorePlugin;
import com.atlassian.connector.eclipse.internal.bamboo.core.BambooUtil;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BambooPlan;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacade;
import com.atlassian.theplugin.commons.bamboo.BuildDetails;
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
import org.eclipse.mylyn.commons.net.UnsupportedRequestException;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;

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

	public void validate(IProgressMonitor monitor, TaskRepository taskRepository) throws CoreException {
		execute(new RemoteOperation<Object>(monitor) {
			@Override
			public Object run(IProgressMonitor monitor) throws CrucibleLoginException, RemoteApiException {
				server.testServerConnection(serverCfg);
				return null;
			}
		}, taskRepository);
	}

	private <T> T execute(RemoteOperation<T> op, TaskRepository taskRepository) throws CoreException {
		IProgressMonitor monitor = op.getMonitor();
		try {
			monitor.beginTask("Connecting to Bamboo", IProgressMonitor.UNKNOWN);
			if (taskRepository.getCredentials(AuthenticationType.REPOSITORY).getPassword().length() < 1) {
				try {
					location.requestCredentials(AuthenticationType.REPOSITORY, null, monitor);
				} catch (UnsupportedRequestException e) {
					// ignore
				}
			}
			updateServer(taskRepository);
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

	private void updateServer(TaskRepository taskrepository) {
		AuthenticationCredentials credentials = location.getCredentials(AuthenticationType.REPOSITORY);
		if (credentials != null) {
			String newUserName = credentials.getUserName();
			String newPassword = credentials.getPassword();
			serverCfg.setUsername(newUserName);
			serverCfg.setPassword(newPassword);
		}
		serverCfg.setPlans(BambooUtil.getSubscribedPlans(taskrepository));
		serverCfg.setUseFavourites(false);
	}

	public boolean hasRepositoryData() {
		return clientData != null && clientData.hasData();
	}

	public BambooClientData getClientData() {
		return clientData;
	}

	public BambooClientData updateRepositoryData(IProgressMonitor monitor, TaskRepository taskRepository)
			throws CoreException {
		this.clientData = execute(new RemoteOperation<BambooClientData>(monitor) {
			@Override
			public BambooClientData run(IProgressMonitor monitor) throws CrucibleLoginException, RemoteApiException,
					ServerPasswordNotProvidedException {
				monitor.subTask("Retrieving plans");
				BambooClientData newClientData = new BambooClientData();
				Collection<BambooPlan> projects = server.getPlanList(serverCfg);
				newClientData.setPlans(projects);
				return newClientData;
			}
		}, taskRepository);
		return clientData;
	}

	public Collection<BambooBuild> getBuilds(IProgressMonitor monitor, TaskRepository taskRepository)
			throws CoreException {
		return execute(new RemoteOperation<Collection<BambooBuild>>(monitor) {
			@Override
			public Collection<BambooBuild> run(IProgressMonitor monitor) throws CrucibleLoginException,
					RemoteApiException, ServerPasswordNotProvidedException {
				monitor.subTask("Retrieving builds");
				return server.getSubscribedPlansResults(serverCfg);
			}
		}, taskRepository);
	}

	public BuildDetails getBuildDetails(IProgressMonitor monitor, TaskRepository taskRepository, final BambooBuild build)
			throws CoreException {
		return execute(new RemoteOperation<BuildDetails>(monitor) {
			@Override
			public BuildDetails run(IProgressMonitor monitor) throws CrucibleLoginException, RemoteApiException,
					ServerPasswordNotProvidedException {
				monitor.subTask("Retrieving build details");
				BuildDetails buildDetails = server.getBuildDetails(serverCfg, build.getBuildKey(),
						build.getBuildNumber());
				return buildDetails;
			}

		}, taskRepository);
	}

	public byte[] getBuildLogs(IProgressMonitor monitor, TaskRepository taskRepository, final BambooBuild build)
			throws CoreException {
		return execute(new RemoteOperation<byte[]>(monitor) {
			@Override
			public byte[] run(IProgressMonitor monitor) throws CrucibleLoginException, RemoteApiException,
					ServerPasswordNotProvidedException {
				monitor.subTask("Retrieving build details");
				return server.getBuildLogs(serverCfg, build.getBuildKey(), build.getBuildNumber());
			}

		}, taskRepository);
	}

	public void addLabelToBuild(IProgressMonitor monitor, TaskRepository repository, final BambooBuild build,
			final String label) throws CoreException {
		execute(new RemoteOperation<Object>(monitor) {

			@Override
			public Object run(IProgressMonitor monitor) throws CrucibleLoginException, RemoteApiException,
					ServerPasswordNotProvidedException {
				monitor.subTask("Adding label to build");
				server.addLabelToBuild(serverCfg, build.getBuildKey(), build.getBuildNumber(), label);
				return null;
			}

		}, repository);
	}

	public void addCommentToBuild(IProgressMonitor monitor, TaskRepository repository, final BambooBuild build,
			final String comment) throws CoreException {
		execute(new RemoteOperation<Object>(monitor) {

			@Override
			public Object run(IProgressMonitor monitor) throws CrucibleLoginException, RemoteApiException,
					ServerPasswordNotProvidedException {
				monitor.subTask("Adding comment to build");
				server.addCommentToBuild(serverCfg, build.getBuildKey(), build.getBuildNumber(), comment);
				return null;
			}

		}, repository);
	}

	public void runBuild(IProgressMonitor monitor, TaskRepository repository, final BambooBuild build)
			throws CoreException {
		execute(new RemoteOperation<Object>(monitor) {
			@Override
			public Object run(IProgressMonitor monitor) throws CrucibleLoginException, RemoteApiException,
					ServerPasswordNotProvidedException {
				monitor.subTask("Run Build");
				server.executeBuild(serverCfg, build.getBuildKey());
				return null;
			}
		}, repository);
	}

}
