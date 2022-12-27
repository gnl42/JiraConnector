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

package me.glindholm.theplugin.commons.bamboo.api;

import me.glindholm.theplugin.commons.bamboo.BambooBuild;
import me.glindholm.theplugin.commons.bamboo.BambooJobImpl;
import me.glindholm.theplugin.commons.bamboo.BambooPlan;
import me.glindholm.theplugin.commons.bamboo.BambooProject;
import me.glindholm.theplugin.commons.bamboo.BuildDetails;
import me.glindholm.theplugin.commons.bamboo.BuildIssue;
import me.glindholm.theplugin.commons.cfg.SubscribedPlan;
import me.glindholm.theplugin.commons.exception.ServerPasswordNotProvidedException;
import me.glindholm.theplugin.commons.remoteapi.ProductSession;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * @author Marek Went
 * @author Wojciech Seliga
 * @author Jacek Jaroczynski
 */
public interface BambooSession extends ProductSession {

	int getBamboBuildNumber() throws RemoteApiException;

	@Nonnull
	List<BambooProject> listProjectNames() throws RemoteApiException;

	@Nonnull
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
	@Nonnull
	BambooBuild getLatestBuildForPlan(@Nonnull String planKey, final int timezoneOffset) throws RemoteApiException;

	@Nonnull
	BambooBuild getBuildForPlanAndNumber(@Nonnull String planKey, final int buildNumber, final int timezoneOffset)
			throws RemoteApiException;

	@Nonnull
	List<String> getFavouriteUserPlans() throws RemoteApiException;

	@Nonnull
	Collection<BambooBuild> getSubscribedPlansResults(final Collection<SubscribedPlan> plans, boolean isUseFavourities,
			int timezoneOffset) throws RemoteApiException;

	@Nonnull
	BuildDetails getBuildResultDetails(@Nonnull String planKey, int buildNumber) throws RemoteApiException;

	void addLabelToBuild(@Nonnull String planKey, int buildNumber, String buildLabel) throws RemoteApiException;

	/**
	 * Adds comment to selected build.
	 *
	 * @param planKey	  plan identifier
	 * @param buildNumber  build number
	 * @param buildComment the comment to add.
	 * @throws RemoteApiException in case of some communication problem
	 */
	void addCommentToBuild(@Nonnull String planKey, int buildNumber, String buildComment) throws RemoteApiException;

	void executeBuild(@Nonnull String planKey) throws RemoteApiException;

	String getBuildLogs(@Nonnull String planKey, int buildNumber) throws RemoteApiException;

	Collection<BambooBuild> getRecentBuildsForPlan(@Nonnull String planKey, int timezoneOffset) throws RemoteApiException;

	Collection<BambooBuild> getRecentBuildsForUser(final int timezoneOffset) throws RemoteApiException;

	@Nonnull
	BambooBuild getLatestBuildForPlanNew(@Nonnull String planKey, @Nullable String masterPlanKey, boolean isPlanEnabled,
			int timezoneOffset) throws RemoteApiException;

    @Nonnull
    Collection<BuildIssue> getIssuesForBuild(@Nonnull String planKey, int buildNumber) throws RemoteApiException;

	@Nonnull
	BambooPlan getPlanDetails(@Nonnull String planKey) throws RemoteApiException;

	List<BambooJobImpl> getJobsForPlan(String planKey) throws RemoteApiException;

    @Nonnull
    Collection<String> getBranchKeys(String planKey, boolean useFavourites, boolean myBranchesOnly) throws RemoteApiException;
}
