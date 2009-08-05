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

import com.atlassian.connector.commons.api.BambooServerFacade2;
import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.eclipse.internal.bamboo.core.BambooCorePlugin;
import com.atlassian.connector.eclipse.internal.bamboo.core.BambooUtil;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BambooPlan;
import com.atlassian.theplugin.commons.bamboo.BuildDetails;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.net.UnsupportedRequestException;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;

import java.util.Collection;

/**
 * Bridge between Mylyn and the ACC API's
 * 
 * @author Shawn Minto
 * @author Wojciech Seliga
 */
public class BambooClient {

	private BambooClientData clientData;

	private final AbstractWebLocation location;

	private ConnectionCfg serverCfg;

	private final BambooServerFacade2 server;

	public BambooClient(AbstractWebLocation location, ConnectionCfg serverCfg, BambooServerFacade2 server,
			BambooClientData data) {
		this.location = location;
		this.clientData = data;
		this.serverCfg = serverCfg;
		this.server = server;
	}

	public ConnectionCfg getServerCfg() {
		return serverCfg;
	}

	public void validate(IProgressMonitor monitor, TaskRepository taskRepository) throws CoreException {
		execute(new BambooRemoteOperation<Object>(monitor, taskRepository) {

			@Override
			public Object run(BambooServerFacade2 server, ConnectionCfg serverCfg, IProgressMonitor monitor)
					throws RemoteApiException, ServerPasswordNotProvidedException {
				server.testServerConnection(serverCfg);
				return null;
			}
		}, true);
	}

	private <T> T execute(BambooRemoteOperation<T> op) throws CoreException {
		return execute(op, true);
	}

	private <T> T execute(BambooRemoteOperation<T> op, boolean promptForCredentials) throws CoreException {
		IProgressMonitor monitor = op.getMonitor();
		TaskRepository taskRepository = op.getTaskRepository();

		try {
			if (taskRepository.getCredentials(AuthenticationType.REPOSITORY).getPassword().length() < 1
					&& promptForCredentials) {
				try {
					location.requestCredentials(AuthenticationType.REPOSITORY, null, monitor);
				} catch (UnsupportedRequestException e) {
					// ignore
				}
			}

			monitor.beginTask("Connecting to Bamboo", IProgressMonitor.UNKNOWN);
			updateServer(taskRepository);
			return op.run(server, serverCfg, op.getMonitor());
		} catch (CrucibleLoginException e) {
			throw new CoreException(new Status(IStatus.ERROR, BambooCorePlugin.PLUGIN_ID,
					RepositoryStatus.ERROR_REPOSITORY_LOGIN, e.getMessage(), e));
		} catch (RemoteApiException e) {
			throw new CoreException(new Status(IStatus.ERROR, BambooCorePlugin.PLUGIN_ID, e.getMessage(), e));
		} catch (ServerPasswordNotProvidedException e) {
			throw new CoreException(new Status(IStatus.ERROR, BambooCorePlugin.PLUGIN_ID,
					RepositoryStatus.ERROR_REPOSITORY_LOGIN, e.getMessage(), e));
		} catch (OperationCanceledException e) {
			throw new CoreException(new Status(IStatus.CANCEL, BambooCorePlugin.PLUGIN_ID,
					RepositoryStatus.OPERATION_CANCELLED, "Operation Canceled", e));
		} finally {
			monitor.done();
		}
	}

	private void updateServer(TaskRepository taskrepository) {
		AuthenticationCredentials credentials = taskrepository.getCredentials(AuthenticationType.REPOSITORY);
		if (credentials != null) {
			String newUserName = taskrepository.getCredentials(AuthenticationType.REPOSITORY).getUserName();
			String newPassword = taskrepository.getCredentials(AuthenticationType.REPOSITORY).getPassword();
			serverCfg = new ConnectionCfg(serverCfg.getId(), newUserName, newPassword, serverCfg.getUrl());
		}
	}

	public boolean hasRepositoryData() {
		return clientData != null && clientData.hasData();
	}

	public BambooClientData getClientData() {
		return clientData;
	}

	public BambooClientData updateRepositoryData(IProgressMonitor monitor, TaskRepository taskRepository)
			throws CoreException {
		this.clientData = execute(new BambooRemoteOperation<BambooClientData>(monitor, taskRepository) {
			@Override
			public BambooClientData run(BambooServerFacade2 server, ConnectionCfg serverCfg, IProgressMonitor monitor)
					throws RemoteApiException, ServerPasswordNotProvidedException {
				monitor.subTask("Retrieving plans");
				BambooClientData newClientData = new BambooClientData();
				Collection<BambooPlan> projects = server.getPlanList(serverCfg);
				newClientData.setPlans(projects);
				return newClientData;
			}
		});
		return clientData;
	}

	public Collection<BambooBuild> getBuilds(IProgressMonitor monitor, final TaskRepository taskRepository,
			boolean promptForCredentials) throws CoreException {
		return execute(new BambooRemoteOperation<Collection<BambooBuild>>(monitor, taskRepository) {
			@Override
			public Collection<BambooBuild> run(BambooServerFacade2 server, ConnectionCfg serverCfg,
					IProgressMonitor monitor) throws RemoteApiException, ServerPasswordNotProvidedException {
				monitor.subTask("Retrieving builds");
				return server.getSubscribedPlansResults(serverCfg, BambooUtil.getSubscribedPlans(taskRepository),
						false, 0);
			}
		}, promptForCredentials);
	}

	public BuildDetails getBuildDetails(IProgressMonitor monitor, TaskRepository taskRepository, final BambooBuild build)
			throws CoreException {
		return execute(new BambooRemoteOperation<BuildDetails>(monitor, taskRepository) {
			@Override
			public BuildDetails run(BambooServerFacade2 server, ConnectionCfg serverCfg, IProgressMonitor monitor)
					throws RemoteApiException, ServerPasswordNotProvidedException {
				monitor.subTask("Retrieving build details");
				BuildDetails buildDetails = server.getBuildDetails(serverCfg, build.getPlanKey(), build.getNumber());
				return buildDetails;
			}

		});
	}

	public String getBuildLogs(IProgressMonitor monitor, TaskRepository taskRepository, final BambooBuild build)
			throws CoreException {
		return execute(new BambooRemoteOperation<String>(monitor, taskRepository) {
			@Override
			public String run(BambooServerFacade2 server, ConnectionCfg serverCfg, IProgressMonitor monitor)
					throws RemoteApiException, ServerPasswordNotProvidedException {
				monitor.subTask("Retrieving build details");
				return server.getBuildLogs(serverCfg, build.getPlanKey(), build.getNumber());
			}

		});
	}

	public void addLabelToBuild(IProgressMonitor monitor, TaskRepository repository, final BambooBuild build,
			final String label) throws CoreException {
		execute(new BambooRemoteOperation<Object>(monitor, repository) {
			@Override
			public Object run(BambooServerFacade2 server, ConnectionCfg serverCfg, IProgressMonitor monitor)
					throws RemoteApiException, ServerPasswordNotProvidedException {
				monitor.subTask("Adding label to build");
				server.addLabelToBuild(serverCfg, build.getPlanKey(), build.getNumber(), label);
				return null;
			}
		});
	}

	public void addCommentToBuild(IProgressMonitor monitor, TaskRepository repository, final BambooBuild build,
			final String comment) throws CoreException {
		execute(new BambooRemoteOperation<Object>(monitor, repository) {
			@Override
			public Object run(BambooServerFacade2 server, ConnectionCfg serverCfg, IProgressMonitor monitor)
					throws RemoteApiException, ServerPasswordNotProvidedException {
				monitor.subTask("Adding comment to build");
				server.addCommentToBuild(serverCfg, build.getPlanKey(), build.getNumber(), comment);
				return null;
			}
		});
	}

	public void runBuild(IProgressMonitor monitor, TaskRepository repository, final BambooBuild build)
			throws CoreException {
		execute(new BambooRemoteOperation<Object>(monitor, repository) {
			@Override
			public Object run(BambooServerFacade2 server, ConnectionCfg serverCfg, IProgressMonitor monitor)
					throws RemoteApiException, ServerPasswordNotProvidedException {
				monitor.subTask("Run Build");
				server.executeBuild(serverCfg, build.getPlanKey());
				return null;
			}
		});
	}

}
