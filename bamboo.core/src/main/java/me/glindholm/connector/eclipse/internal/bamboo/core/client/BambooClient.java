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

package me.glindholm.connector.eclipse.internal.bamboo.core.client;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.tasks.core.TaskRepository;

import me.glindholm.connector.commons.api.BambooServerFacade2;
import me.glindholm.connector.commons.api.ConnectionCfg;
import me.glindholm.connector.eclipse.internal.bamboo.core.BambooUtil;
import me.glindholm.connector.eclipse.internal.bamboo.core.PlanBranches;
import me.glindholm.connector.eclipse.internal.core.client.AbstractConnectorClient;
import me.glindholm.connector.eclipse.internal.core.client.HttpSessionCallbackImpl;
import me.glindholm.theplugin.commons.bamboo.BambooBuild;
import me.glindholm.theplugin.commons.bamboo.BambooPlan;
import me.glindholm.theplugin.commons.bamboo.BuildDetails;
import me.glindholm.theplugin.commons.bamboo.api.BambooSession;
import me.glindholm.theplugin.commons.exception.ServerPasswordNotProvidedException;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiException;

/**
 * Bridge between Mylyn and the ACC API's
 *
 * @author Shawn Minto
 * @author Wojciech Seliga
 */
public class BambooClient extends AbstractConnectorClient<BambooServerFacade2, BambooSession> {

    private BambooClientData clientData;

    public BambooClient(AbstractWebLocation location, ConnectionCfg serverCfg, BambooServerFacade2 server,
            BambooClientData data, HttpSessionCallbackImpl callback) {
        super(location, serverCfg, server, callback);
        this.clientData = data;
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
            public BambooClientData run(BambooServerFacade2 server, ConnectionCfg connectionCfg,
                    IProgressMonitor monitor) throws RemoteApiException, ServerPasswordNotProvidedException {
                monitor.subTask("Retrieving plans");
                BambooClientData newClientData = new BambooClientData();
                Collection<BambooPlan> projects = server.getPlanList(connectionCfg);
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
                        BambooUtil.isUseFavourites(taskRepository),
                        !BambooUtil.getPlanBranches(taskRepository).equals(PlanBranches.NO),
                        BambooUtil.getPlanBranches(taskRepository).equals(PlanBranches.MINE), 0);
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

    public BambooBuild getBuildForPlanAndNumber(IProgressMonitor monitor, final TaskRepository repository,
            final String planKey, final int buildNumber, final int timezoneOffset) throws CoreException {
        return execute(new BambooRemoteOperation<BambooBuild>(monitor, repository) {
            @Override
            public BambooBuild run(BambooServerFacade2 server, ConnectionCfg serverCfg, IProgressMonitor monitor)
                    throws RemoteApiException, ServerPasswordNotProvidedException {
                monitor.subTask("Retrieving build details");
                return server.getBuildForPlanAndNumber(serverCfg, planKey, buildNumber, timezoneOffset);
            }
        });
    }

    @Override
    protected BambooSession getSession(ConnectionCfg connectionCfg) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        return facade.getSession(connectionCfg);
    }
}
