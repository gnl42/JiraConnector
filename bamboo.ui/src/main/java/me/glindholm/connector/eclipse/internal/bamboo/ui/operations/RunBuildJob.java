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

public class RunBuildJob extends Job {
    private final BambooBuild build;

    private final TaskRepository repository;

    public RunBuildJob(final BambooBuild build, final TaskRepository repository) {
        super("Run build");
        this.build = build;
        this.repository = repository;
    }

    @Override
    protected IStatus run(final IProgressMonitor monitor) {
        final BambooClient client = BambooClientFactory.getDefault().getBambooClient(repository);
        try {
            client.runBuild(monitor, repository, build);
        } catch (final CoreException e) {
            final Status status = new Status(IStatus.ERROR, BambooUiPlugin.ID_PLUGIN, "Failed to run build " + build.getPlanKey(), e);
            StatusHandler.log(status);
            return status;
        }
        return Status.OK_STATUS;
    }

}