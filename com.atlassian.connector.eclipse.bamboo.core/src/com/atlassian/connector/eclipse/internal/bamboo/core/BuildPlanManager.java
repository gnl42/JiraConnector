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

import com.atlassian.theplugin.commons.bamboo.BambooBuild;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.osgi.util.NLS;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages subscribed build plans, receives notification on changed subscriptions and sends out notification to
 * BuildChangedListeners
 * 
 * @author Thomas Ehrnhoefer
 */
public final class BuildPlanManager {

	private class RefreshBuildsJob extends Job {

		private final ArrayList<BambooBuild> builds;

		private TaskRepository taskReporisory;

		public RefreshBuildsJob(String name) {
			super(name);
			this.builds = new ArrayList<BambooBuild>();
		}

		public RefreshBuildsJob(String name, TaskRepository repository) {
			super(name);
			this.builds = new ArrayList<BambooBuild>();
			this.taskReporisory = repository;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			BambooClientManager clientManager = BambooCorePlugin.getRepositoryConnector().getClientManager();
			try {
				this.builds.addAll(clientManager.getClient(taskReporisory).getBuilds(monitor));
			} catch (CoreException e) {
				return new Status(IStatus.ERROR, BambooCorePlugin.PLUGIN_ID, NLS.bind(
						"Update of builds from {0} failed", taskReporisory.getRepositoryLabel()), e);
			}
			return new Status(IStatus.OK, BambooCorePlugin.PLUGIN_ID, "Successfully retrieved Builds.");
		}

		public List<BambooBuild> getBuilds() {
			return builds;
		}

	}

	private static BuildPlanManager uniqueInstance;

	private final List<BambooBuild> subscribedBuilds;

	private final List<BuildsChangedListener> buildChangedListeners;

	private BuildPlanManager() {
		subscribedBuilds = new CopyOnWriteArrayList<BambooBuild>();
		buildChangedListeners = new CopyOnWriteArrayList<BuildsChangedListener>();
	}

	public static BuildPlanManager getInstance() {
		if (uniqueInstance == null) {
			uniqueInstance = new BuildPlanManager();
		}
		return uniqueInstance;
	}

	public void addBuildsChangedListener(BuildsChangedListener listener) {
		buildChangedListeners.add(listener);
	}

	public void removeBuildsChangedListener(BuildsChangedListener listener) {
		buildChangedListeners.remove(listener);
	}

	public BambooBuild[] getSubscribedBuilds() {
		return subscribedBuilds.toArray(new BambooBuild[subscribedBuilds.size()]);
	}

	public void buildSubscriptionsChanged(TaskRepository repository) {
		RefreshBuildsJob job = new RefreshBuildsJob("Refresh builds", repository);
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				if (event.getResult().isOK()) {
					List<BambooBuild> builds = ((RefreshBuildsJob) event.getJob()).getBuilds();
					//compare new builds with current builds
					processRefreshedBuilds(builds);
				}
				super.done(event);
			}
		});
		job.schedule();

	}

	private void processRefreshedBuilds(List<BambooBuild> newBuilds) {
		List<BambooBuild> addedBuilds = new ArrayList<BambooBuild>();
		List<BambooBuild> changedBuilds = new ArrayList<BambooBuild>();
		List<BambooBuild> removedBuilds = new ArrayList<BambooBuild>();

		synchronized (subscribedBuilds) {
			for (BambooBuild oldBuild : subscribedBuilds) {
				boolean found = false;
				for (BambooBuild newBuild : newBuilds) {
					if (BambooUtil.isSameBuildPlan(newBuild, oldBuild)) {
						found = true;
						//if build keys do not match, but builds are of the same build plan, it is a changed build
						if (newBuild.getBuildKey().equals(oldBuild.getBuildKey())) {
							changedBuilds.add(newBuild);
						}
						break;
					}
				}
				//if current build is not found in newBuilds, its subscription has been removed
				if (!found) {
					removedBuilds.add(oldBuild);
				}
			}
			for (BambooBuild newBuild : newBuilds) {
				boolean found = false;
				for (BambooBuild oldBuild : subscribedBuilds) {
					if (newBuild.getBuildKey().equals(oldBuild.getBuildKey())) {
						found = true;
						break;
					}
				}
				if (!found) {
					addedBuilds.add(newBuild);
				}
			}
		}
		BuildsChangedEvent event = new BuildsChangedEvent(addedBuilds, removedBuilds, changedBuilds, newBuilds);

		//notify listeners
		for (BuildsChangedListener listener : buildChangedListeners) {
			if (addedBuilds.size() > 0) {
				listener.buildsAdded(event);
			}
			if (changedBuilds.size() > 0) {
				listener.buildsChanged(event);
			}
			if (removedBuilds.size() > 0) {
				listener.buildsRemoved(event);
			}
		}
	}
}
