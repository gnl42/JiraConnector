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
import com.atlassian.theplugin.commons.bamboo.BambooBuildInfo;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.IRepositoryManager;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.osgi.util.NLS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

		@Override
		public boolean belongsTo(Object family) {
			return family == BambooConstants.FAMILY_REFRESH_OPERATION;
		}

	}

	private class RefreshBuildsForAllRepositoriesJob extends Job {

		private final Map<TaskRepository, Collection<BambooBuild>> builds;

		private MultiStatus result;

		private final boolean manualRefresh;

		private boolean isRunning = false;

		public RefreshBuildsForAllRepositoriesJob(boolean manualRefresh) {
			super("Refresh Builds");
			this.builds = new HashMap<TaskRepository, Collection<BambooBuild>>();
			this.manualRefresh = manualRefresh;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			isRunning = true;
			if (repositoryManager == null) {
				StatusHandler.log(new Status(IStatus.ERROR, BambooCorePlugin.PLUGIN_ID, "No repository manager found."));
				return Status.OK_STATUS;
			}
			BambooClientManager clientManager = BambooCorePlugin.getRepositoryConnector().getClientManager();
			Set<TaskRepository> repositories = repositoryManager.getRepositories(BambooCorePlugin.CONNECTOR_KIND);
			result = new MultiStatus(BambooCorePlugin.PLUGIN_ID, 0, "Retrieval of Bamboo builds failed", null);
			for (TaskRepository repository : repositories) {
				//ignore disconnected repositories
				if (!repository.isOffline()) {
					BambooClient client = clientManager.getClient(repository);
					try {
						this.builds.put(repository, client.getBuilds(monitor, repository));
					} catch (CoreException e) {
						Status status = new Status(IStatus.ERROR, BambooCorePlugin.PLUGIN_ID, NLS.bind(
								"Update of builds from {0} failed", repository.getRepositoryLabel()), e);
						result.add(status);
						StatusHandler.log(status);
					}
				}
			}
			firstScheduledSynchronizationDone = true;
			handleFinishedRefreshAllBuildsJob(builds, manualRefresh);
			isRunning = false;
			return Status.OK_STATUS;
		}

		public Map<TaskRepository, Collection<BambooBuild>> getBuilds() {
			return builds;
		}

		public IStatus getStatus() {
			return result;
		};

		@Override
		public boolean belongsTo(Object family) {
			return manualRefresh && family == BambooConstants.FAMILY_REFRESH_OPERATION;
		}

		public boolean isRunning() {
			return isRunning;
		}
	}

	private final Map<TaskRepository, Collection<BambooBuild>> subscribedBuilds;

	private final List<BuildsChangedListener> buildChangedListeners;

	private RefreshBuildsForAllRepositoriesJob scheduledRefreshBuildsForAllRepositoriesJob;

	private IRepositoryManager repositoryManager;

	private RefreshBuildsForAllRepositoriesJob forcedRefreshBuildsForAllRepositoriesJob;

	private boolean firstScheduledSynchronizationDone = false;

	public BuildPlanManager() {
		subscribedBuilds = new HashMap<TaskRepository, Collection<BambooBuild>>();
		buildChangedListeners = new CopyOnWriteArrayList<BuildsChangedListener>();
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
			Map<TaskRepository, Collection<BambooBuild>> changedBuilds2, List<String> errorLog) {
		Collection<BambooBuild> currentBuilds = subscribedBuilds.get(taskRepository);
		//if it is a new repository, add it
		if (currentBuilds == null) {
			currentBuilds = new ArrayList<BambooBuild>();
		}
		List<BambooBuild> changedBuilds = new ArrayList<BambooBuild>();
		HashSet<BambooBuild> failedToRemove = new HashSet<BambooBuild>();
		HashMap<String, BambooBuild> cachedToAdd = new HashMap<String, BambooBuild>();
		//find changed and removed builds
		for (BambooBuild oldBuild : currentBuilds) {
			for (BambooBuild newBuild : newBuilds) {
				if (newBuild.getErrorMessage() != null && newBuild.getStatus() == BuildStatus.UNKNOWN) {
					//if retrieval failed, use old (cached) build
					failedToRemove.add(newBuild);
					if (!cachedToAdd.containsKey(oldBuild.getBuildKey())) {
						cachedToAdd.put(oldBuild.getBuildKey(), createCachedBuild(oldBuild, newBuild));
						errorLog.add(oldBuild.getBuildKey() + " - " + newBuild.getErrorMessage());
					}
				} else if (BambooUtil.isSameBuildPlan(newBuild, oldBuild)) {
					//if build keys do not match, but builds are of the same build plan, it is a changed build
					if (newBuild.getBuildKey().equals(oldBuild.getBuildKey())) {
						changedBuilds.add(newBuild);
					}
					break;
				}
			}
		}
		newBuilds.removeAll(failedToRemove);
		newBuilds.addAll(cachedToAdd.values());
		//set newbuilds as current builds and add the added/removed/changed to the maps
		subscribedBuilds.put(taskRepository, newBuilds);
		changedBuilds2.put(taskRepository, changedBuilds);
	}

	private BambooBuild createCachedBuild(BambooBuild oldBuild, BambooBuild newBuild) {
		return new BambooBuildInfo(oldBuild.getBuildKey(), oldBuild.getBuildName(), oldBuild.getServer(),
				oldBuild.getPollingTime(), oldBuild.getProjectName(), oldBuild.getEnabled(), oldBuild.getBuildNumber(),
				oldBuild.getStatus(), oldBuild.getBuildReason(), oldBuild.getBuildStartedDate(), null, null,
				oldBuild.getTestsPassed(), oldBuild.getTestsFailed(), oldBuild.getBuildCompletedDate(),
				newBuild.getErrorMessage(), oldBuild.getBuildRelativeBuildDate(),
				oldBuild.getBuildDurationDescription(), oldBuild.getCommiters());
	}

	private void processRefreshedBuildsOneRepository(Collection<BambooBuild> newBuilds, TaskRepository taskRepository) {
		Map<TaskRepository, Collection<BambooBuild>> oldBuilds = new HashMap<TaskRepository, Collection<BambooBuild>>(
				subscribedBuilds);
		Map<TaskRepository, Collection<BambooBuild>> changedBuilds = new HashMap<TaskRepository, Collection<BambooBuild>>();
		List<String> errorLog = new ArrayList<String>();
		synchronized (subscribedBuilds) {
			getRefreshedBuildsDiff(newBuilds, taskRepository, changedBuilds, errorLog);
		}

		notifyListeners(oldBuilds, changedBuilds, errorLog, true);
	}

	private void notifyListeners(Map<TaskRepository, Collection<BambooBuild>> oldBuilds,
			Map<TaskRepository, Collection<BambooBuild>> changedBuilds, List<String> errorLog, boolean forcedRefresh) {
		BuildsChangedEvent event = new BuildsChangedEvent(changedBuilds, subscribedBuilds, oldBuilds, errorLog);

		//notify listeners
		for (BuildsChangedListener listener : buildChangedListeners) {
			listener.buildsUpdated(event);
		}
		//send failed refreshes to error log
		if (forcedRefresh && errorLog.size() > 0) {
			MultiStatus refreshStatus = new MultiStatus(BambooCorePlugin.PLUGIN_ID, 0, "Error while refreshing builds",
					null);
			for (String error : errorLog) {
				refreshStatus.add(new Status(IStatus.WARNING, BambooCorePlugin.PLUGIN_ID, error));
			}
			StatusHandler.log(refreshStatus);
		}
	}

	public void repositoryRemoved(TaskRepository repository) {
		Map<TaskRepository, Collection<BambooBuild>> oldBuilds = new HashMap<TaskRepository, Collection<BambooBuild>>(
				subscribedBuilds);
		synchronized (subscribedBuilds) {
			Collection<BambooBuild> buildsToRemove = subscribedBuilds.get(repository);
			if (buildsToRemove != null) {
				subscribedBuilds.remove(repository);
			}
		}
		notifyListeners(oldBuilds, null, null, false);
	}

	private void processRefreshedBuildsAllRepositories(Map<TaskRepository, Collection<BambooBuild>> newBuilds,
			boolean forcedRefresh) {
		Map<TaskRepository, Collection<BambooBuild>> oldBuilds = new HashMap<TaskRepository, Collection<BambooBuild>>(
				subscribedBuilds);
		Map<TaskRepository, Collection<BambooBuild>> changedBuilds = new HashMap<TaskRepository, Collection<BambooBuild>>();
		List<String> errorLog = new ArrayList<String>();
		synchronized (subscribedBuilds) {
			for (TaskRepository repository : newBuilds.keySet()) {
				getRefreshedBuildsDiff(newBuilds.get(repository), repository, changedBuilds, errorLog);
			}
		}

		notifyListeners(oldBuilds, changedBuilds, errorLog, forcedRefresh);
	}

	public void initializeScheduler(IRepositoryManager manager) {
		this.repositoryManager = manager;
		scheduledRefreshBuildsForAllRepositoriesJob = new RefreshBuildsForAllRepositoriesJob(false);
		scheduledRefreshBuildsForAllRepositoriesJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				scheduledRefreshBuildsForAllRepositoriesJob.schedule(BambooCorePlugin.getSyncIntervalMinutes() * 60000);
			}
		});
		scheduledRefreshBuildsForAllRepositoriesJob.schedule(); //first iteration without delay
	}

	public void reInitializeScheduler() {
		if (this.repositoryManager != null && !scheduledRefreshBuildsForAllRepositoriesJob.isRunning()) {
			if (scheduledRefreshBuildsForAllRepositoriesJob.cancel()) {
				scheduledRefreshBuildsForAllRepositoriesJob.schedule(BambooCorePlugin.getSyncIntervalMinutes() * 60000);
			}
		}
	}

	public void handleFinishedRefreshAllBuildsJob(Map<TaskRepository, Collection<BambooBuild>> builds,
			boolean forcedRefresh) {
		//compare new builds with current builds
		processRefreshedBuildsAllRepositories(builds, forcedRefresh);
	}

	public void refreshAllBuilds() {
		//only trigger if refreshBuildsForAllRepositoriesJob exists and is not running
		if (forcedRefreshBuildsForAllRepositoriesJob == null) {
			forcedRefreshBuildsForAllRepositoriesJob = new RefreshBuildsForAllRepositoriesJob(true);
			forcedRefreshBuildsForAllRepositoriesJob.addJobChangeListener(new JobChangeAdapter() {
				@Override
				public void done(IJobChangeEvent event) {
					forcedRefreshBuildsForAllRepositoriesJob = null;
				}
			});
			forcedRefreshBuildsForAllRepositoriesJob.schedule();
		}
	}

	public boolean isFirstScheduledSynchronizationDone() {
		return firstScheduledSynchronizationDone;
	}
}
