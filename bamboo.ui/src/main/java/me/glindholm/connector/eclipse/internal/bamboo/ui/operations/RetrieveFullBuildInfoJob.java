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

package me.glindholm.connector.eclipse.internal.bamboo.ui.operations;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.mylyn.tasks.core.TaskRepository;

import me.glindholm.connector.eclipse.internal.bamboo.core.client.BambooClient;
import me.glindholm.connector.eclipse.internal.bamboo.ui.BambooUiPlugin;
import me.glindholm.connector.eclipse.internal.core.client.BambooClientFactory;
import me.glindholm.theplugin.commons.bamboo.BambooBuild;
import me.glindholm.theplugin.commons.bamboo.BuildDetails;

public class RetrieveFullBuildInfoJob extends Job {

    private IStatus status;

    private String buildLog;

    private BuildDetails buildDetails;

    private final BambooBuild build;

    private final TaskRepository repository;

    public RetrieveFullBuildInfoJob(final BambooBuild build, final TaskRepository repository) {
        super("Retrieve full build details");
        this.build = build;
        this.repository = repository;
    }

    @Override
    protected IStatus run(final IProgressMonitor monitor) {
        final BambooClient client = BambooClientFactory.getDefault().getBambooClient(repository);
        IStatus buildLogStatus = Status.OK_STATUS;
        try {
            buildLog = client.getBuildLogs(monitor, repository, build);
        } catch (CoreException | UnsupportedOperationException e) {
            buildLogStatus = new Status(IStatus.ERROR, BambooUiPlugin.ID_PLUGIN, "Failed to retrieve build logs for build " + build.getPlanKey(), e);
        }
        IStatus buildDetailsStatus = Status.OK_STATUS;
        try {
            buildDetails = client.getBuildDetails(monitor, repository, build);
        } catch (CoreException | UnsupportedOperationException e) {
            buildDetailsStatus = new Status(IStatus.ERROR, BambooUiPlugin.ID_PLUGIN, "Failed to retrieve build details for build " + build.getPlanKey(), e);
        }
        status = new MultiStatus(BambooUiPlugin.ID_PLUGIN, 0, new IStatus[] { buildLogStatus, buildDetailsStatus },
                "Retrieval of full build information failed", null);
        return Status.OK_STATUS;
    }

    public IStatus getStatus() {
        return status;
    }

    public String getBuildLog() {
        return buildLog;
    }

    public BuildDetails getBuildDetails() {
        return buildDetails;
    }

}
