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

package com.atlassian.connector.eclipse.internal.bamboo.ui.operations;

import com.atlassian.connector.eclipse.internal.bamboo.core.BambooCorePlugin;
import com.atlassian.connector.eclipse.internal.bamboo.core.client.BambooClient;
import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooUiPlugin;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BuildDetails;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.mylyn.tasks.core.TaskRepository;

public class RetrieveFullBuildInfoJob extends Job {

	private IStatus status;

	private String buildLog;

	private BuildDetails buildDetails;

	private final BambooBuild build;

	private final TaskRepository repository;

	public RetrieveFullBuildInfoJob(BambooBuild build, TaskRepository repository) {
		super("Retrieve full build details");
		this.build = build;
		this.repository = repository;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		BambooClient client = BambooCorePlugin.getRepositoryConnector().getClientManager().getClient(repository);
		IStatus buildLogStatus = Status.OK_STATUS;
		try {
			buildLog = client.getBuildLogs(monitor, repository, build);
		} catch (CoreException e) {
			buildLogStatus = new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID,
					"Failed to retrieve build logs for build " + build.getPlanKey(), e);
		}
		IStatus buildDetailsStatus = Status.OK_STATUS;
		try {
			buildDetails = client.getBuildDetails(monitor, repository, build);
		} catch (CoreException e) {
			buildDetailsStatus = new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID,
					"Failed to retrieve build details for build " + build.getPlanKey(), e);
		}
		status = new MultiStatus(BambooUiPlugin.PLUGIN_ID, 0, new IStatus[] { buildLogStatus, buildDetailsStatus },
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
