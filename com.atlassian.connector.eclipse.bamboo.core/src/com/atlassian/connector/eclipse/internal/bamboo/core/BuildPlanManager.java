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
import org.eclipse.mylyn.tasks.core.IRepositoryManager;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.osgi.util.NLS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages subscribed build plans, receives notification on changed subscriptions and sends out notification to
 * BuildChangedListeners
 * 
 * @author Thomas Ehrnhoefer
 */
public final class BuildPlanManager {

	private static final int REVIEW_SYNCHRONISATION_DELAY_MS = 1200000;

	private class RefreshBuildsJob extends Job {

		private final ArrayList<BambooBuild> builds;

		private TaskRepository taskRepository;

		public RefreshBuildsJob(String name) {
			super(name);
			this.builds = new ArrayList<BambooBuild>();
		}

		public RefreshBuildsJob(String name, TaskRepository repository) {
			super(name);
			this.builds = new ArrayList<BambooBuild>();
			this.taskRepository = repository;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			BambooClientManager clientManager = BambooCorePlugin.getRepositoryConnector().getClientManager();
			try {
				this.builds.addAll(clientManager.getClient(taskRepository).getBuilds(monitor, taskRepository));
			} catch (CoreException e) {
				return new Status(IStatus.ERROR, BambooCorePlugin.PLUGIN_ID, NLS.bind(
						"Update of builds from {0} failed", taskRepository.getRepositoryLabel()), e);
			}
			return new Status(IStatus.OK, BambooCorePlugin.PLUGIN_ID, "Successfully retrieved Builds.");
		}

		public List<BambooBuild> getBuilds() {
			return builds;
		}

	}

	private static BuildPlanManager uniqueInstance;

	private final Map<TaskRepository, Collection<BambooBuild>> subscribedBuilds;

	private final List<BuildsChangedListener> buildChangedListeners;

	private RefreshBuildsForAllRepositoriesJob refreshBuildsForAllRepositoriesJob;

	private BuildPlanManager() {
		subscribedBuilds = new HashMap<TaskRepository, Collection<BambooBuild>>();
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

	public BambooBuild[] getSubscribedBuilds(TaskRepository repository) {
		if (subscribedBuilds.containsKey(repository)) {
			return subscribedBuilds.get(repository).toArray(new BambooBuild[subscribedBuilds.get(repository).size()]);
		} else {
			return null;
		}
	}

	public Map<TaskRepository, Collection<BambooBuild>> getSubscribedBuilds() {
		return subscribedBuilds;
	}

	public void buildSubscriptionsChanged(final TaskRepository repository) {
		RefreshBuildsJob job = new RefreshBuildsJob("Refresh builds", repository);
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				if (event.getResult().isOK()) {
					List<BambooBuild> builds = ((RefreshBuildsJob) event.getJob()).getBuilds();
					//compare new builds with current builds
					processRefreshedBuildsOneRepository(builds, repository);
				}
			}
		});
		job.schedule();

	}

	private void getRefreshedBuildsDiff(Collection<BambooBuild> newBuilds, TaskRepository taskRepository,
			Map<TaskRepository, Collection<BambooBuild>> addedBuilds2,
			Map<TaskRepository, Collection<BambooBuild>> removedBuilds2,
			Map<TaskRepository, Collection<BambooBuild>> changedBuilds2) {
		Collection<BambooBuild> currentBuilds = subscribedBuilds.get(taskRepository);
		//if it is a new repository, add it
		if (currentBuilds == null) {
			currentBuilds = new ArrayList<BambooBuild>();
		}
		List<BambooBuild> addedBuilds = new ArrayList<BambooBuild>();
		List<BambooBuild> changedBuilds = new ArrayList<BambooBuild>();
		List<BambooBuild> removedBuilds = new ArrayList<BambooBuild>();
		//find changed and removed builds
		for (BambooBuild oldBuild : currentBuilds) {
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
		//find newly added builds
		for (BambooBuild newBuild : newBuilds) {
			boolean found = false;
			for (BambooBuild oldBuild : currentBuilds) {
				if (newBuild.getBuildKey().equals(oldBuild.getBuildKey())) {
					found = true;
					break;
				}
			}
			if (!found) {
				addedBuilds.add(newBuild);
			}
		}
		//set newbuilds as current builds and add the added/removed/changed to the maps
		subscribedBuilds.put(taskRepository, newBuilds);
		addedBuilds2.put(taskRepository, addedBuilds);
		removedBuilds2.put(taskRepository, removedBuilds);
		changedBuilds2.put(taskRepository, changedBuilds);
	}

	private void processRefreshedBuildsOneRepository(Collection<BambooBuild> newBuilds, TaskRepository taskRepository) {
		Map<TaskRepository, Collection<BambooBuild>> oldBuilds = new HashMap<TaskRepository, Collection<BambooBuild>>(
				subscribedBuilds);
		Map<TaskRepository, Collection<BambooBuild>> addedBuilds = new HashMap<TaskRepository, Collection<BambooBuild>>();
		Map<TaskRepository, Collection<BambooBuild>> removedBuilds = new HashMap<TaskRepository, Collection<BambooBuild>>();
		Map<TaskRepository, Collection<BambooBuild>> changedBuilds = new HashMap<TaskRepository, Collection<BambooBuild>>();
		synchronized (subscribedBuilds) {
			getRefreshedBuildsDiff(newBuilds, taskRepository, addedBuilds, removedBuilds, changedBuilds);
		}

		BuildsChangedEvent event = new BuildsChangedEvent(addedBuilds, removedBuilds, changedBuilds, subscribedBuilds,
				oldBuilds);

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

	public void repositoryRemoved(TaskRepository repository) {
		Map<TaskRepository, Collection<BambooBuild>> oldBuilds = new HashMap<TaskRepository, Collection<BambooBuild>>(
				subscribedBuilds);
		Map<TaskRepository, Collection<BambooBuild>> removedBuilds = new HashMap<TaskRepository, Collection<BambooBuild>>();
		synchronized (subscribedBuilds) {
			Collection<BambooBuild> buildsToRemove = subscribedBuilds.get(repository);
			if (buildsToRemove != null) {
				removedBuilds.put(repository, buildsToRemove);
				subscribedBuilds.remove(repository);
			}
		}
		Map<TaskRepository, Collection<BambooBuild>> blank = new HashMap<TaskRepository, Collection<BambooBuild>>();
		BuildsChangedEvent event = new BuildsChangedEvent(blank, removedBuilds, blank, subscribedBuilds, oldBuilds);

		//notify listeners
		for (BuildsChangedListener listener : buildChangedListeners) {
			if (removedBuilds.size() > 0) {
				listener.buildsRemoved(event);
			}
		}
	}

	private void processRefreshedBuildsAllRepositories(Map<TaskRepository, Collection<BambooBuild>> newBuilds) {
		Map<TaskRepository, Collection<BambooBuild>> oldBuilds = new HashMap<TaskRepository, Collection<BambooBuild>>(
				subscribedBuilds);
		Map<TaskRepository, Collection<BambooBuild>> addedBuilds = new HashMap<TaskRepository, Collection<BambooBuild>>();
		Map<TaskRepository, Collection<BambooBuild>> removedBuilds = new HashMap<TaskRepository, Collection<BambooBuild>>();
		Map<TaskRepository, Collection<BambooBuild>> changedBuilds = new HashMap<TaskRepository, Collection<BambooBuild>>();

		synchronized (subscribedBuilds) {
			for (TaskRepository repository : newBuilds.keySet()) {
				getRefreshedBuildsDiff(newBuilds.get(repository), repository, addedBuilds, removedBuilds, changedBuilds);
			}
		}

		BuildsChangedEvent event = new BuildsChangedEvent(addedBuilds, removedBuilds, changedBuilds, subscribedBuilds,
				oldBuilds);

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

	public void initializeScheduler(IRepositoryManager repositoryManager) {
		refreshBuildsForAllRepositoriesJob = new RefreshBuildsForAllRepositoriesJob("Refresh Builds", repositoryManager);
		refreshBuildsForAllRepositoriesJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				if (event.getResult().isOK()) {
					handleFinishedRefreshAllBuildsJob(event);
				}
				refreshBuildsForAllRepositoriesJob.schedule(REVIEW_SYNCHRONISATION_DELAY_MS);
			}
		});
		refreshBuildsForAllRepositoriesJob.schedule(); //first iteration without delay
	}

	public void handleFinishedRefreshAllBuildsJob(IJobChangeEvent event) {
		RefreshBuildsForAllRepositoriesJob job = (RefreshBuildsForAllRepositoriesJob) event.getJob();
		Map<TaskRepository, Collection<BambooBuild>> builds = job.getBuilds();
		//compare new builds with current builds
		processRefreshedBuildsAllRepositories(builds);
	}
}
