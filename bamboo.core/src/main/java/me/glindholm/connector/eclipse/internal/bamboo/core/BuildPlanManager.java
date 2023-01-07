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

package me.glindholm.connector.eclipse.internal.bamboo.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.IRepositoryManager;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.osgi.util.NLS;

import me.glindholm.connector.eclipse.internal.bamboo.core.client.BambooClient;
import me.glindholm.theplugin.commons.bamboo.BambooBuild;
import me.glindholm.theplugin.commons.bamboo.BambooBuildInfo;
import me.glindholm.theplugin.commons.bamboo.BuildStatus;

/**
 * Manages subscribed build plans, receives notification on changed
 * subscriptions and sends out notification to BuildChangedListeners
 *
 * @author Thomas Ehrnhoefer
 */
public final class BuildPlanManager {

    private class RefreshBuildsJob extends Job {

        private final ArrayList<BambooBuild> builds;

        private final TaskRepository taskRepository;

        public RefreshBuildsJob(final String name, final TaskRepository repository) {
            super(name);
            builds = new ArrayList<>();
            taskRepository = repository;
        }

        @Override
        protected IStatus run(final IProgressMonitor monitor) {
            if (!taskRepository.isOffline()) {
                final BambooClientManager clientManager = BambooCorePlugin.getRepositoryConnector().getClientManager();
                try {
                    builds.addAll(clientManager.getClient(taskRepository).getBuilds(monitor, taskRepository, true));
                } catch (final CoreException e) {
                    return new Status(IStatus.ERROR, BambooCorePlugin.PLUGIN_ID,
                            NLS.bind("Update of builds from {0} failed", taskRepository.getRepositoryLabel()), e);
                }
            }
            return new Status(IStatus.OK, BambooCorePlugin.PLUGIN_ID, "Successfully retrieved Builds.");
        }

        public List<BambooBuild> getBuilds() {
            return builds;
        }

        @Override
        public boolean belongsTo(final Object family) {
            return family == BambooConstants.FAMILY_REFRESH_OPERATION;
        }

    }

    private class RefreshBuildsForAllRepositoriesJob extends Job {

        private final Map<TaskRepository, Collection<BambooBuild>> builds;

        private final boolean manualRefresh;

        private boolean isRunning;

        public RefreshBuildsForAllRepositoriesJob(final boolean manualRefresh) {
            super("Refresh Builds");
            builds = new HashMap<>();
            this.manualRefresh = manualRefresh;
        }

        @Override
        protected IStatus run(final IProgressMonitor monitor) {
            if (repositoryManager == null) {
                StatusHandler.log(new Status(IStatus.ERROR, BambooCorePlugin.PLUGIN_ID, "No repository manager found."));
                return Status.OK_STATUS;
            }

            isRunning = true;
            try {
                final BambooClientManager clientManager = BambooCorePlugin.getRepositoryConnector().getClientManager();
                final Set<TaskRepository> repositories = repositoryManager.getRepositories(BambooCorePlugin.CONNECTOR_KIND);
                boolean allSuccessful = true;

                for (final TaskRepository repository : repositories) {
                    if (monitor.isCanceled()) {
                        return Status.CANCEL_STATUS;
                    }

                    // ignore disconnected repositories
                    if (!repository.isOffline()) {
                        final BambooClient client = clientManager.getClient(repository);
                        try {
                            builds.put(repository, client.getBuilds(monitor, repository, manualRefresh || !firstScheduledSynchronizationDone));
                        } catch (final OperationCanceledException | CoreException e) {
                            final Status status = new Status(IStatus.ERROR, BambooCorePlugin.PLUGIN_ID,
                                    NLS.bind("Update of builds from {0} failed", repository.getRepositoryLabel()), e);
                            StatusHandler.log(status);
                            allSuccessful = false;
                        }
                    }
                }
                firstScheduledSynchronizationDone = true;

                if (monitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }

                // compare new builds with current builds
                processRefreshedBuildsAllRepositories(builds, manualRefresh, allSuccessful);
            } finally {
                isRunning = false;
            }
            return Status.OK_STATUS;
        }

        @Override
        public boolean belongsTo(final Object family) {
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

    protected RefreshBuildsForAllRepositoriesJob forcedRefreshBuildsForAllRepositoriesJob;

    private boolean firstScheduledSynchronizationDone;

    public BuildPlanManager() {
        subscribedBuilds = new HashMap<>();
        buildChangedListeners = new CopyOnWriteArrayList<>();
    }

    public void addBuildsChangedListener(final BuildsChangedListener listener) {
        buildChangedListeners.add(listener);
    }

    public void removeBuildsChangedListener(final BuildsChangedListener listener) {
        buildChangedListeners.remove(listener);
    }

    public BambooBuild[] getSubscribedBuilds(final TaskRepository repository) {
        if (subscribedBuilds.containsKey(repository)) {
            return subscribedBuilds.get(repository).toArray(new BambooBuild[subscribedBuilds.get(repository).size()]);
        } else {
            return null;
        }
    }

    public Map<TaskRepository, Collection<BambooBuild>> getSubscribedBuilds() {
        return subscribedBuilds;
    }

    public Job buildSubscriptionsChanged(final TaskRepository repository) {
        final RefreshBuildsJob job = new RefreshBuildsJob("Refresh builds", repository);
        job.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void done(final IJobChangeEvent event) {
                if (event.getResult().isOK()) {
                    final List<BambooBuild> builds = ((RefreshBuildsJob) event.getJob()).getBuilds();
                    // compare new builds with current builds
                    processRefreshedBuildsOneRepository(builds, repository);
                }
            }
        });
        job.schedule();
        return job;
    }

    private void getRefreshedBuildsDiff(final Collection<BambooBuild> newBuilds, final TaskRepository taskRepository,
            final Map<TaskRepository, Collection<BambooBuild>> changedBuilds2, final List<String> errorLog) {
        Collection<BambooBuild> currentBuilds = subscribedBuilds.get(taskRepository);
        // if it is a new repository, add it
        if (currentBuilds == null) {
            currentBuilds = new ArrayList<>();
        }
        final List<BambooBuild> changedBuilds = new ArrayList<>();
        final HashSet<BambooBuild> failedToRemove = new HashSet<>();
        final HashMap<String, BambooBuild> cachedToAdd = new HashMap<>();

        for (final BambooBuild newBuild : newBuilds) {
            // find same old build
            BambooBuild correspondingOldBuild = null;
            for (final BambooBuild oldBuild : currentBuilds) {
                if (BambooUtil.isSameBuildPlan(newBuild, oldBuild)) {
                    correspondingOldBuild = oldBuild;
                    break;
                }
            }

            // process failed build retrieval
            if (newBuild.getErrorMessage() != null && newBuild.getStatus() == BuildStatus.UNKNOWN) {
                final TaskRepository bambooRepo = repositoryManager.getRepository(BambooCorePlugin.CONNECTOR_KIND, newBuild.getServerUrl());

                final String repoName = bambooRepo != null ? bambooRepo.getRepositoryLabel() : newBuild.getServer().getUrl();

                // log error
                errorLog.add(newBuild.getPlanKey() + " - " + newBuild.getErrorMessage() + "[" + repoName + "]");
                // of there is an old build, used cached information for the failed new build
                // retrieval
                if (correspondingOldBuild != null) {
                    if (!cachedToAdd.containsKey(correspondingOldBuild.getPlanKey())) {
                        final BambooBuild buildToCache = createCachedBuild(correspondingOldBuild, newBuild);
                        if (buildToCache != null) {
                            cachedToAdd.put(correspondingOldBuild.getPlanKey(), buildToCache);
                            failedToRemove.add(newBuild);
                        }
                    }
                }
                // process successful build retrieval (only if there was a corresponding old
                // build)
            } else if (correspondingOldBuild != null) {
                if (BambooUtil.isSameBuildPlan(newBuild, correspondingOldBuild)) {
                    // if build keys do not match, but builds are of the same build plan, it is a
                    // changed build
                    if (newBuild.getPlanKey().equals(correspondingOldBuild.getPlanKey())) {
                        changedBuilds.add(newBuild);
                    }
                }
            }
        }
        newBuilds.removeAll(failedToRemove);
        newBuilds.addAll(cachedToAdd.values());
        // set newbuilds as current builds and add the added/removed/changed to the maps
        subscribedBuilds.put(taskRepository, newBuilds);
        changedBuilds2.put(taskRepository, changedBuilds);
    }

    @Nullable
    private BambooBuild createCachedBuild(final BambooBuild oldBuild, final BambooBuild newBuild) {
        try {
            return new BambooBuildInfo(oldBuild.getPlanKey(), oldBuild.getPlanName(), oldBuild.getMasterPlanKey(), oldBuild.getServer(),
                    oldBuild.getPollingTime(), oldBuild.getProjectName(), oldBuild.getEnabled(), oldBuild.getNumber(), oldBuild.getStatus(),
                    oldBuild.getPlanState(), oldBuild.getReason(), oldBuild.getStartDate(), null, null, oldBuild.getTestsPassed(), oldBuild.getTestsFailed(),
                    oldBuild.getCompletionDate(), newBuild.getErrorMessage(), oldBuild.getException(), oldBuild.getRelativeBuildDate(),
                    oldBuild.getDurationDescription(), oldBuild.getCommiters());
        } catch (final UnsupportedOperationException e) {
            return null;
        }
    }

    private void processRefreshedBuildsOneRepository(final Collection<BambooBuild> newBuilds, final TaskRepository taskRepository) {
        final Map<TaskRepository, Collection<BambooBuild>> oldBuilds = new HashMap<>(subscribedBuilds);
        final Map<TaskRepository, Collection<BambooBuild>> changedBuilds = new HashMap<>();
        final List<String> errorLog = new ArrayList<>();
        synchronized (subscribedBuilds) {
            getRefreshedBuildsDiff(newBuilds, taskRepository, changedBuilds, errorLog);
        }

        notifyListeners(oldBuilds, changedBuilds, errorLog, true, true);
    }

    private void notifyListeners(final Map<TaskRepository, Collection<BambooBuild>> oldBuilds, final Map<TaskRepository, Collection<BambooBuild>> changedBuilds,
            final List<String> errorLog, final boolean forcedRefresh, final boolean allUpdated) {
        final boolean failed = (errorLog == null ? false : errorLog.size() > 0) || !allUpdated;
        final BuildsChangedEvent event = new BuildsChangedEvent(changedBuilds, subscribedBuilds, oldBuilds, errorLog, forcedRefresh, failed);

        // notify listeners
        for (final BuildsChangedListener listener : buildChangedListeners) {
            listener.buildsUpdated(event);
        }
        // send failed refreshes to error log
        if (failed && errorLog != null && errorLog.size() > 0) {
            if (forcedRefresh) {
                final MultiStatus refreshStatus = new MultiStatus(BambooCorePlugin.PLUGIN_ID, 0, "Error while refreshing builds", null);
                for (final String error : errorLog) {
                    refreshStatus.add(new Status(IStatus.WARNING, BambooCorePlugin.PLUGIN_ID, error));
                }
                StatusHandler.log(refreshStatus);
            }
        }

    }

    public void repositoryRemoved(final TaskRepository repository) {
        final Map<TaskRepository, Collection<BambooBuild>> oldBuilds = new HashMap<>(subscribedBuilds);
        synchronized (subscribedBuilds) {
            final Collection<BambooBuild> buildsToRemove = subscribedBuilds.get(repository);
            if (buildsToRemove != null) {
                subscribedBuilds.remove(repository);
            }
        }
        notifyListeners(oldBuilds, null, null, false, true);
    }

    private void processRefreshedBuildsAllRepositories(final Map<TaskRepository, Collection<BambooBuild>> newBuilds, final boolean forcedRefresh,
            final boolean allUpdated) {
        final Map<TaskRepository, Collection<BambooBuild>> oldBuilds = new HashMap<>(subscribedBuilds);
        final Map<TaskRepository, Collection<BambooBuild>> changedBuilds = new HashMap<>();
        final List<String> errorLog = new ArrayList<>();
        synchronized (subscribedBuilds) {
            for (final TaskRepository repository : newBuilds.keySet()) {
                getRefreshedBuildsDiff(newBuilds.get(repository), repository, changedBuilds, errorLog);
            }
        }

        notifyListeners(oldBuilds, changedBuilds, errorLog, forcedRefresh, allUpdated);
    }

    public Job initializeScheduler(final IRepositoryManager manager) {
        repositoryManager = manager;
        scheduledRefreshBuildsForAllRepositoriesJob = new RefreshBuildsForAllRepositoriesJob(false);
        scheduledRefreshBuildsForAllRepositoriesJob.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void done(final IJobChangeEvent event) {
                // prohibit immediate reschedule
                if (BambooCorePlugin.isAutoRefresh() && BambooCorePlugin.getRefreshIntervalMinutes() > 0) {
                    scheduledRefreshBuildsForAllRepositoriesJob.schedule(BambooCorePlugin.getRefreshIntervalMinutes() * 60000);
                }
            }
        });
        if (BambooCorePlugin.isAutoRefresh()) {
            scheduledRefreshBuildsForAllRepositoriesJob.schedule(); // first iteration without delay
        }
        return scheduledRefreshBuildsForAllRepositoriesJob;
    }

    public Job reInitializeScheduler() {
        if (repositoryManager != null && !scheduledRefreshBuildsForAllRepositoriesJob.isRunning()) {
            if (scheduledRefreshBuildsForAllRepositoriesJob.cancel() && BambooCorePlugin.isAutoRefresh()) {
                scheduledRefreshBuildsForAllRepositoriesJob.schedule(BambooCorePlugin.getRefreshIntervalMinutes() * 60000);
            }
        }
        return scheduledRefreshBuildsForAllRepositoriesJob;
    }

    public Job refreshAllBuilds() {
        // only trigger if refreshBuildsForAllRepositoriesJob exists and is not running
        if (forcedRefreshBuildsForAllRepositoriesJob == null) {
            forcedRefreshBuildsForAllRepositoriesJob = new RefreshBuildsForAllRepositoriesJob(true);
            forcedRefreshBuildsForAllRepositoriesJob.addJobChangeListener(new JobChangeAdapter() {
                @Override
                public void done(final IJobChangeEvent event) {
                    forcedRefreshBuildsForAllRepositoriesJob = null;
                }
            });
            forcedRefreshBuildsForAllRepositoriesJob.schedule();
        }
        return forcedRefreshBuildsForAllRepositoriesJob;
    }

    public boolean isFirstScheduledSynchronizationDone() {
        return firstScheduledSynchronizationDone;
    }
}
