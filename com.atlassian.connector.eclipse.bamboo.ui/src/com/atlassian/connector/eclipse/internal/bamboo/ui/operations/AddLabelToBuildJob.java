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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.TaskRepository;

public class AddLabelToBuildJob extends Job {
	private final BambooBuild build;

	private final TaskRepository repository;

	private final String label;

	public AddLabelToBuildJob(BambooBuild build, TaskRepository repository, String label) {
		super("Add Label to build");
		this.build = build;
		this.repository = repository;
		this.label = label;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		BambooClient client = BambooCorePlugin.getRepositoryConnector().getClientManager().getClient(repository);
		try {
			client.addLabelToBuild(monitor, repository, build, label);
		} catch (CoreException e) {
			StatusHandler.log(new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID, "Failed to add label to build "
					+ build.getPlanKey()));
		}
		return Status.OK_STATUS;
	}
}