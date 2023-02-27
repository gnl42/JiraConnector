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
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.TaskRepository;

import me.glindholm.connector.eclipse.internal.bamboo.core.client.BambooClient;
import me.glindholm.connector.eclipse.internal.bamboo.ui.BambooUiPlugin;
import me.glindholm.connector.eclipse.internal.core.client.BambooClientFactory;
import me.glindholm.theplugin.commons.bamboo.BambooBuild;
import me.glindholm.theplugin.commons.bamboo.BuildDetails;

public class RetrieveTestResultsJob extends Job {
    private final BambooBuild build;

    private final TaskRepository repository;

    private BuildDetails testResults;

    public RetrieveTestResultsJob(final BambooBuild build, final TaskRepository repository) {
        super("Retrieving test results");
        this.build = build;
        this.repository = repository;
    }

    @Override
    protected IStatus run(final IProgressMonitor monitor) {
        final BambooClient client = BambooClientFactory.getDefault().getBambooClient(repository);
        try {
            testResults = client.getBuildDetails(monitor, repository, build);
        } catch (CoreException | UnsupportedOperationException e) {
            StatusHandler.log(new Status(IStatus.ERROR, BambooUiPlugin.ID_PLUGIN, "Failed to retrieve test results for build " + build.getPlanKey(), e));
        }
        return Status.OK_STATUS;
    }

    public BuildDetails getBuildDetails() {
        return testResults;
    }
}