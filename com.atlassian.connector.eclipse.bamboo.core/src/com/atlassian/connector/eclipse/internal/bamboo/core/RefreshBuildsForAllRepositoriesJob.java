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

package com.atlassian.connector.eclipse.internal.bamboo.core;

import com.atlassian.connector.eclipse.internal.bamboo.core.client.BambooClient;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.mylyn.tasks.core.IRepositoryManager;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.osgi.util.NLS;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RefreshBuildsForAllRepositoriesJob extends Job {

	private final Map<TaskRepository, Collection<BambooBuild>> builds2;

	private final IRepositoryManager repositoryManager;

	public RefreshBuildsForAllRepositoriesJob(String name, IRepositoryManager repositoryManager) {
		super(name);
		this.builds2 = new HashMap<TaskRepository, Collection<BambooBuild>>();
		this.repositoryManager = repositoryManager;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		BambooClientManager clientManager = BambooCorePlugin.getRepositoryConnector().getClientManager();
		Set<TaskRepository> repositories = repositoryManager.getRepositories(BambooCorePlugin.CONNECTOR_KIND);
		MultiStatus result = new MultiStatus(BambooCorePlugin.PLUGIN_ID, 0, "Retrieval of Bamboo builds failed", null);
		for (TaskRepository repository : repositories) {
			BambooClient client = clientManager.getClient(repository);
			try {
				this.builds2.put(repository, client.getBuilds(monitor, repository));
			} catch (CoreException e) {
				result.add(new Status(IStatus.ERROR, BambooCorePlugin.PLUGIN_ID, NLS.bind(
						"Update of builds from {0} failed", repository.getRepositoryLabel()), e));
			}
		}
		return result;
	}

	public Map<TaskRepository, Collection<BambooBuild>> getBuilds() {
		return builds2;
	}
}