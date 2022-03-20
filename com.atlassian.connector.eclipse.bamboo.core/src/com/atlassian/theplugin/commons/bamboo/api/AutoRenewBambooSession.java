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

package com.atlassian.theplugin.commons.bamboo.api;

import java.util.Collection;
import java.util.List;

import org.jdom2.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BambooJobImpl;
import com.atlassian.theplugin.commons.bamboo.BambooPlan;
import com.atlassian.theplugin.commons.bamboo.BambooProject;
import com.atlassian.theplugin.commons.bamboo.BuildDetails;
import com.atlassian.theplugin.commons.bamboo.BuildIssue;
import com.atlassian.theplugin.commons.cfg.SubscribedPlan;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiSessionExpiredException;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import com.atlassian.theplugin.commons.util.Logger;

public class AutoRenewBambooSession implements BambooSession {
    private final BambooSession delegate;
    private String userName;
    private char[] password;

    public AutoRenewBambooSession(ConnectionCfg serverCfg, HttpSessionCallback callback, Logger logger)
            throws RemoteApiException {
        Exception e = new JDOMException();
        this.delegate = new BambooSessionImpl(serverCfg, callback, logger);
    }

    AutoRenewBambooSession(BambooSession bambooSession) throws RemoteApiException {
        this.delegate = bambooSession;
    }

    public void addCommentToBuild(@NotNull String planKey, int buildNumber, String buildComment) throws RemoteApiException {
        try {
            delegate.addCommentToBuild(planKey, buildNumber, buildComment);
        } catch (RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            delegate.addCommentToBuild(planKey, buildNumber, buildComment);
        }
    }

    public void executeBuild(@NotNull String buildKey) throws RemoteApiException {
        try {
            delegate.executeBuild(buildKey);
        } catch (RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            delegate.executeBuild(buildKey);
        }
    }

    public void addLabelToBuild(@NotNull String planKey, int buildNumber, String buildLabel) throws RemoteApiException {
        try {
            delegate.addLabelToBuild(planKey, buildNumber, buildLabel);
        } catch (RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            delegate.addLabelToBuild(planKey, buildNumber, buildLabel);
        }
    }

    @NotNull
    public BuildDetails getBuildResultDetails(@NotNull String planKey, int buildNumber) throws RemoteApiException {
        try {
            return delegate.getBuildResultDetails(planKey, buildNumber);
        } catch (RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            return delegate.getBuildResultDetails(planKey, buildNumber);
        }
    }

    @NotNull
    public List<String> getFavouriteUserPlans() throws RemoteApiException {
        try {
            return delegate.getFavouriteUserPlans();
        } catch (RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            return delegate.getFavouriteUserPlans();
        }
    }

    @NotNull
    public BambooBuild getLatestBuildForPlan(@NotNull final String planKey, final int timezoneOffset)
            throws RemoteApiException {
        try {
            return delegate.getLatestBuildForPlan(planKey, timezoneOffset);
        } catch (RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            return delegate.getLatestBuildForPlan(planKey, timezoneOffset);
        }
    }

    @NotNull
    public BambooBuild getBuildForPlanAndNumber(@NotNull String planKey, final int buildNumber, final int timezoneOffset)
            throws RemoteApiException {
        try {
            return delegate.getBuildForPlanAndNumber(planKey, buildNumber, timezoneOffset);
        } catch (RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            return delegate.getBuildForPlanAndNumber(planKey, buildNumber, timezoneOffset);
        }
    }

    public boolean isLoggedIn() throws RemoteApiLoginException {
        return delegate.isLoggedIn();
    }

    public String getBuildLogs(@NotNull String planKey, int buildNumber) throws RemoteApiException {
        try {
            return delegate.getBuildLogs(planKey, buildNumber);
        } catch (RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            return delegate.getBuildLogs(planKey, buildNumber);
        }
    }

    public Collection<BambooBuild> getRecentBuildsForPlan(@NotNull final String planKey, final int timezoneOffset)
            throws RemoteApiException {
        try {
            return delegate.getRecentBuildsForPlan(planKey, timezoneOffset);
        } catch (RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            return delegate.getRecentBuildsForPlan(planKey, timezoneOffset);
        }
    }

    public Collection<BambooBuild> getRecentBuildsForUser(final int timezoneOffset)
            throws RemoteApiException {
        try {
            return delegate.getRecentBuildsForUser(timezoneOffset);
        } catch (RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            return delegate.getRecentBuildsForUser(timezoneOffset);
        }
    }

    @NotNull
    public Collection<BuildIssue> getIssuesForBuild(@NotNull String planKey, int buildNumber) throws RemoteApiException {
        try {
            return delegate.getIssuesForBuild(planKey, buildNumber);
        } catch (RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            return delegate.getIssuesForBuild(planKey, buildNumber);
        }
    }

    @NotNull
    public BambooPlan getPlanDetails(@NotNull final String planKey) throws RemoteApiException {
        try {
            return delegate.getPlanDetails(planKey);
        } catch (RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            return delegate.getPlanDetails(planKey);
        }
    }

    @NotNull
    public BambooBuild getLatestBuildForPlanNew(@NotNull final String planKey, @Nullable String masterPlanKey, final boolean isPlanEnabled,
            final int timezoneOffset) throws RemoteApiException {
        try {
            return delegate.getLatestBuildForPlanNew(planKey, masterPlanKey, isPlanEnabled, timezoneOffset);
        } catch (RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            return delegate.getLatestBuildForPlanNew(planKey, masterPlanKey, isPlanEnabled, timezoneOffset);
        }

    }

    @NotNull
    public Collection<BambooPlan> getPlanList() throws ServerPasswordNotProvidedException, RemoteApiException {
        try {
            return delegate.getPlanList();
        } catch (RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            return delegate.getPlanList();
        }
    }

    @NotNull
    public List<BambooProject> listProjectNames() throws RemoteApiException {
        try {
            return delegate.listProjectNames();
        } catch (RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            return delegate.listProjectNames();
        }
    }

    public void login(String name, char[] aPassword) throws RemoteApiLoginException {
        this.userName = name;
        this.password = new char[aPassword.length];
        System.arraycopy(aPassword, 0, password, 0, aPassword.length);
        delegate.login(name, aPassword);
    }

    public void logout() {
        delegate.logout();
    }

    public int getBamboBuildNumber() throws RemoteApiException {
        try {
            return delegate.getBamboBuildNumber();
        } catch (RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            return delegate.getBamboBuildNumber();
        }
    }

    @NotNull
    public Collection<BambooBuild> getSubscribedPlansResults(Collection<SubscribedPlan> plans, boolean isUseFavourities,
            int timezoneOffset) throws RemoteApiException {
        try {
            return delegate.getSubscribedPlansResults(plans, isUseFavourities, timezoneOffset);
        } catch (RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            return delegate.getSubscribedPlansResults(plans, isUseFavourities, timezoneOffset);
        }
    }

    public List<BambooJobImpl> getJobsForPlan(String planKey) throws RemoteApiException {
        try {
            return delegate.getJobsForPlan(planKey);
        } catch (RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            return delegate.getJobsForPlan(planKey);
        }
    }

    @NotNull
    public Collection<String> getBranchKeys(String planKey, boolean useFavourites, boolean myBranchesOnly) throws RemoteApiException {
        try {
            return delegate.getBranchKeys(planKey, useFavourites, myBranchesOnly);
        } catch (RemoteApiSessionExpiredException e) {
            delegate.login(userName, password);
            return delegate.getBranchKeys(planKey, useFavourites, myBranchesOnly);
        }
    }
}
