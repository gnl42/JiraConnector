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
 * limitations under the License.
 */

package com.atlassian.theplugin.commons.bamboo.api;

import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BambooJobImpl;
import com.atlassian.theplugin.commons.bamboo.BambooPlan;
import com.atlassian.theplugin.commons.bamboo.BambooProject;
import com.atlassian.theplugin.commons.bamboo.BuildDetails;
import com.atlassian.theplugin.commons.bamboo.BuildIssue;
import com.atlassian.theplugin.commons.cfg.SubscribedPlan;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.ProductSession;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * @author Marek Went
 * @author Wojciech Seliga
 * @author Jacek Jaroczynski
 */
public interface BambooSession extends ProductSession {

	int getBamboBuildNumber() throws RemoteApiException;

	@NotNull
	List<BambooProject> listProjectNames() throws RemoteApiException;

	@NotNull
	Collection<BambooPlan> getPlanList() throws ServerPasswordNotProvidedException, RemoteApiException;

	/**
	 * This result of this method does not contain properly set information about whether given build is enabled
	 * or not. There are two reasons for that:<ul>
	 * <li>remote API does not return this information and additional call is needed to retrieve all plan information
	 * to actually know whether given plan is enabled or not</li>
	 * <li>I tend to think that information whether given build is enabled or not does not make sense. "Enableness"
	 * belongs to plans rather than to builds. When build was executed it must have been enabled! So even though
	 * the plan may be disabled now it has nothing to do with the build for this plan which was executed before that!</li>
	 * </ul>
	 * <p/>
	 * Avoid calling this method, use rather {@link #getLatestBuildForPlan(String, boolean, int)}. In the future the whole
	 * concept may be probably rethought.
	 *
	 * @param planKey id of the plan
	 * @return last build for selected plan
	 * @throws RemoteApiException in case of some communication problem or malformed response
	 */
	@NotNull
	BambooBuild getLatestBuildForPlan(@NotNull String planKey, final int timezoneOffset) throws RemoteApiException;

	@NotNull
	BambooBuild getBuildForPlanAndNumber(@NotNull String planKey, final int buildNumber, final int timezoneOffset)
			throws RemoteApiException;

	@NotNull
	List<String> getFavouriteUserPlans() throws RemoteApiException;

	@NotNull
	Collection<BambooBuild> getSubscribedPlansResults(final Collection<SubscribedPlan> plans, boolean isUseFavourities,
			int timezoneOffset) throws RemoteApiException;

	@NotNull
	BuildDetails getBuildResultDetails(@NotNull String planKey, int buildNumber) throws RemoteApiException;

	void addLabelToBuild(@NotNull String planKey, int buildNumber, String buildLabel) throws RemoteApiException;

	/**
	 * Adds comment to selected build.
	 *
	 * @param planKey	  plan identifier
	 * @param buildNumber  build number
	 * @param buildComment the comment to add.
	 * @throws RemoteApiException in case of some communication problem
	 */
	void addCommentToBuild(@NotNull String planKey, int buildNumber, String buildComment) throws RemoteApiException;

	void executeBuild(@NotNull String planKey) throws RemoteApiException;

	String getBuildLogs(@NotNull String planKey, int buildNumber) throws RemoteApiException;

	Collection<BambooBuild> getRecentBuildsForPlan(@NotNull String planKey, int timezoneOffset) throws RemoteApiException;

	Collection<BambooBuild> getRecentBuildsForUser(final int timezoneOffset) throws RemoteApiException;

	@NotNull
	BambooBuild getLatestBuildForPlanNew(@NotNull String planKey, @Nullable String masterPlanKey, boolean isPlanEnabled,
			int timezoneOffset) throws RemoteApiException;

    @NotNull
    Collection<BuildIssue> getIssuesForBuild(@NotNull String planKey, int buildNumber) throws RemoteApiException;

	@NotNull
	BambooPlan getPlanDetails(@NotNull String planKey) throws RemoteApiException;

	List<BambooJobImpl> getJobsForPlan(String planKey) throws RemoteApiException;

    @NotNull
    Collection<String> getBranchKeys(String planKey, boolean useFavourites, boolean myBranchesOnly) throws RemoteApiException;
}
