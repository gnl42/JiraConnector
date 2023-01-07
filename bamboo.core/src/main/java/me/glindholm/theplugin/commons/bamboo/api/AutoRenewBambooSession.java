/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License..
 */

package me.glindholm.theplugin.commons.bamboo.api;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.jdom2.JDOMException;

import me.glindholm.connector.commons.api.ConnectionCfg;
import me.glindholm.theplugin.commons.bamboo.BambooBuild;
import me.glindholm.theplugin.commons.bamboo.BambooJobImpl;
import me.glindholm.theplugin.commons.bamboo.BambooPlan;
import me.glindholm.theplugin.commons.bamboo.BambooProject;
import me.glindholm.theplugin.commons.bamboo.BuildDetails;
import me.glindholm.theplugin.commons.bamboo.BuildIssue;
import me.glindholm.theplugin.commons.cfg.SubscribedPlan;
import me.glindholm.theplugin.commons.exception.ServerPasswordNotProvidedException;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiException;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiLoginException;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiSessionExpiredException;
import me.glindholm.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import me.glindholm.theplugin.commons.util.Logger;

public class AutoRenewBambooSession implements BambooSession {
    private final BambooSession delegate;
    private String userName;
    private char[] password;

    public AutoRenewBambooSession(final ConnectionCfg serverCfg, final HttpSessionCallback callback, final Logger logger) throws RemoteApiException {
        final Exception e = new JDOMException();
        delegate = new BambooSessionImpl(serverCfg, callback, logger);
    }

    AutoRenewBambooSession(final BambooSession bambooSession) throws RemoteApiException {
        delegate = bambooSession;
    }

    @Override
    public void addCommentToBuild(@NonNull final String planKey, final int buildNumber, final String buildComment) throws RemoteApiException {
        try {
            delegate.addCommentToBuild(planKey, buildNumber, buildComment);
        } catch (final RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            delegate.addCommentToBuild(planKey, buildNumber, buildComment);
        }
    }

    @Override
    public void executeBuild(@NonNull final String buildKey) throws RemoteApiException {
        try {
            delegate.executeBuild(buildKey);
        } catch (final RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            delegate.executeBuild(buildKey);
        }
    }

    @Override
    public void addLabelToBuild(@NonNull final String planKey, final int buildNumber, final String buildLabel) throws RemoteApiException {
        try {
            delegate.addLabelToBuild(planKey, buildNumber, buildLabel);
        } catch (final RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            delegate.addLabelToBuild(planKey, buildNumber, buildLabel);
        }
    }

    @Override
    @NonNull
    public BuildDetails getBuildResultDetails(@NonNull final String planKey, final int buildNumber) throws RemoteApiException {
        try {
            return delegate.getBuildResultDetails(planKey, buildNumber);
        } catch (final RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            return delegate.getBuildResultDetails(planKey, buildNumber);
        }
    }

    @Override
    @NonNull
    public List<String> getFavouriteUserPlans() throws RemoteApiException {
        try {
            return delegate.getFavouriteUserPlans();
        } catch (final RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            return delegate.getFavouriteUserPlans();
        }
    }

    @Override
    @NonNull
    public BambooBuild getLatestBuildForPlan(@NonNull final String planKey, final int timezoneOffset) throws RemoteApiException {
        try {
            return delegate.getLatestBuildForPlan(planKey, timezoneOffset);
        } catch (final RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            return delegate.getLatestBuildForPlan(planKey, timezoneOffset);
        }
    }

    @Override
    @NonNull
    public BambooBuild getBuildForPlanAndNumber(@NonNull final String planKey, final int buildNumber, final int timezoneOffset) throws RemoteApiException {
        try {
            return delegate.getBuildForPlanAndNumber(planKey, buildNumber, timezoneOffset);
        } catch (final RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            return delegate.getBuildForPlanAndNumber(planKey, buildNumber, timezoneOffset);
        }
    }

    @Override
    public boolean isLoggedIn() throws RemoteApiLoginException {
        return delegate.isLoggedIn();
    }

    @Override
    public String getBuildLogs(@NonNull final String planKey, final int buildNumber) throws RemoteApiException {
        try {
            return delegate.getBuildLogs(planKey, buildNumber);
        } catch (final RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            return delegate.getBuildLogs(planKey, buildNumber);
        }
    }

    @Override
    public List<BambooBuild> getRecentBuildsForPlan(@NonNull final String planKey, final int timezoneOffset) throws RemoteApiException {
        try {
            return delegate.getRecentBuildsForPlan(planKey, timezoneOffset);
        } catch (final RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            return delegate.getRecentBuildsForPlan(planKey, timezoneOffset);
        }
    }

    @Override
    public List<BambooBuild> getRecentBuildsForUser(final int timezoneOffset) throws RemoteApiException {
        try {
            return delegate.getRecentBuildsForUser(timezoneOffset);
        } catch (final RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            return delegate.getRecentBuildsForUser(timezoneOffset);
        }
    }

    @Override
    @NonNull
    public List<BuildIssue> getIssuesForBuild(@NonNull final String planKey, final int buildNumber) throws RemoteApiException {
        try {
            return delegate.getIssuesForBuild(planKey, buildNumber);
        } catch (final RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            return delegate.getIssuesForBuild(planKey, buildNumber);
        }
    }

    @Override
    @NonNull
    public BambooPlan getPlanDetails(@NonNull final String planKey) throws RemoteApiException {
        try {
            return delegate.getPlanDetails(planKey);
        } catch (final RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            return delegate.getPlanDetails(planKey);
        }
    }

    @Override
    @NonNull
    public BambooBuild getLatestBuildForPlanNew(@NonNull final String planKey, @Nullable final String masterPlanKey, final boolean isPlanEnabled,
            final int timezoneOffset) throws RemoteApiException {
        try {
            return delegate.getLatestBuildForPlanNew(planKey, masterPlanKey, isPlanEnabled, timezoneOffset);
        } catch (final RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            return delegate.getLatestBuildForPlanNew(planKey, masterPlanKey, isPlanEnabled, timezoneOffset);
        }

    }

    @Override
    @NonNull
    public List<BambooPlan> getPlanList() throws ServerPasswordNotProvidedException, RemoteApiException {
        try {
            return delegate.getPlanList();
        } catch (final RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            return delegate.getPlanList();
        }
    }

    @Override
    @NonNull
    public List<BambooProject> listProjectNames() throws RemoteApiException {
        try {
            return delegate.listProjectNames();
        } catch (final RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            return delegate.listProjectNames();
        }
    }

    @Override
    public void login(final String name, final char[] aPassword) throws RemoteApiLoginException {
        userName = name;
        password = new char[aPassword.length];
        System.arraycopy(aPassword, 0, password, 0, aPassword.length);
        delegate.login(name, aPassword);
    }

    @Override
    public void logout() {
        delegate.logout();
    }

    @Override
    public int getBamboBuildNumber() throws RemoteApiException {
        try {
            return delegate.getBamboBuildNumber();
        } catch (final RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            return delegate.getBamboBuildNumber();
        }
    }

    @Override
    @NonNull
    public List<BambooBuild> getSubscribedPlansResults(final Collection<SubscribedPlan> plans, final boolean isUseFavourities, final int timezoneOffset)
            throws RemoteApiException {
        try {
            return delegate.getSubscribedPlansResults(plans, isUseFavourities, timezoneOffset);
        } catch (final RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            return delegate.getSubscribedPlansResults(plans, isUseFavourities, timezoneOffset);
        }
    }

    @Override
    public List<BambooJobImpl> getJobsForPlan(final String planKey) throws RemoteApiException {
        try {
            return delegate.getJobsForPlan(planKey);
        } catch (final RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            return delegate.getJobsForPlan(planKey);
        }
    }

    @Override
    @NonNull
    public List<String> getBranchKeys(final String planKey, final boolean useFavourites, final boolean myBranchesOnly) throws RemoteApiException {
        try {
            return delegate.getBranchKeys(planKey, useFavourites, myBranchesOnly);
        } catch (final RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            return delegate.getBranchKeys(planKey, useFavourites, myBranchesOnly);
        }
    }
}
