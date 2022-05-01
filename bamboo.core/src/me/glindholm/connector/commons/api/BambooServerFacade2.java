/*******************************************************************************
 * Copyright (c) 2008 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package me.glindholm.connector.commons.api;

import me.glindholm.connector.commons.api.ConnectionCfg;
import me.glindholm.theplugin.commons.bamboo.BambooBuild;
import me.glindholm.theplugin.commons.bamboo.BambooPlan;
import me.glindholm.theplugin.commons.bamboo.BambooProject;
import me.glindholm.theplugin.commons.bamboo.BuildDetails;
import me.glindholm.theplugin.commons.bamboo.BuildIssue;
import me.glindholm.theplugin.commons.bamboo.api.BambooSession;
import me.glindholm.theplugin.commons.cfg.SubscribedPlan;
import me.glindholm.theplugin.commons.exception.ServerPasswordNotProvidedException;
import me.glindholm.theplugin.commons.remoteapi.ProductServerFacade;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiException;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Strange name, but this class should one day disappear.
 * me.glindholm.theplugin.commons.bamboo.api.BambooSession should ultimately replacy it.
 */
public interface BambooServerFacade2 extends ProductServerFacade {

	/**
	 * This method is to make migration to BambooSession (used directly) easier
	 */
	BambooSession getSession(ConnectionCfg server) throws RemoteApiException;

	Collection<BambooProject> getProjectList(ConnectionCfg bambooServer) throws ServerPasswordNotProvidedException,
		RemoteApiException;

	Collection<BambooPlan> getPlanList(ConnectionCfg bambooServer) throws ServerPasswordNotProvidedException,
		RemoteApiException;

	Collection<BambooBuild> getSubscribedPlansResults(
            ConnectionCfg bambooServer, final Collection<SubscribedPlan> plans,
			boolean isUseFavourities, boolean isShowBranches, boolean myBranchesOly, int timezoneOffset)
            throws ServerPasswordNotProvidedException, RemoteApiException;

	BuildDetails getBuildDetails(ConnectionCfg bambooServer, @Nonnull String planKey, int buildNumber)
		throws ServerPasswordNotProvidedException, RemoteApiException;

	void addLabelToBuild(ConnectionCfg bambooServer, @Nonnull String planKey, int buildNumber, String buildComment)
		throws ServerPasswordNotProvidedException, RemoteApiException;

	void addCommentToBuild(ConnectionCfg bambooServer, @Nonnull String planKey, int buildNumber, String buildComment)
		throws ServerPasswordNotProvidedException, RemoteApiException;

	void executeBuild(ConnectionCfg bambooServer, @Nonnull String planKey) throws ServerPasswordNotProvidedException,
		RemoteApiException;

	String getBuildLogs(ConnectionCfg bambooServer, @Nonnull String planKey, int buildNumber)
		throws ServerPasswordNotProvidedException, RemoteApiException;

	/**
	 * List build history for provided plan.
	 * <p/>
	 * Returns last X builds on provided plan including information about failed attempt. X may differ depending on build
	 * frequence in selected plan.
	 * <p/>
	 * Throws ServerPasswordNotProvidedException when invoked for Server that has not had the password set, when the server
	 * returns a meaningful exception response.
	 *
	 * @param bambooServer
	 *            Bamboo server information
	 * @param planKey
	 *            key of the plan to query
	 * @param timezoneOffset
	 * @return last X builds for selected plan
	 * @throws me.glindholm.theplugin.commons.exception.ServerPasswordNotProvidedException
	 *             when invoked for Server that has not had the password set yet
	 */
	Collection<BambooBuild> getRecentBuildsForPlans(ConnectionCfg bambooServer, String planKey, final int timezoneOffset)
		throws ServerPasswordNotProvidedException;

	/**
	 * List build history for current user.
	 * <p>
	 * <p/>
	 * Returns last builds for selected user including information about failed attempt.
	 * <p>
	 * <p/>
	 * Throws ServerPasswordNotProvidedException when invoked for Server that has not had the password set, when the server
	 * returns a meaningful exception response.
	 *
	 * @param bambooServer
	 *            Bamboo server information
	 * @return last builds for the user (as configred in <code>bambooServer</code>)
	 * @throws me.glindholm.theplugin.commons.exception.ServerPasswordNotProvidedException
	 *             when invoked for Server that has not had the password set yet
	 */
	Collection<BambooBuild> getRecentBuildsForUser(ConnectionCfg bambooServer, final int timezoneOffset)
		throws ServerPasswordNotProvidedException;

	BambooBuild getBuildForPlanAndNumber(ConnectionCfg bambooServer, @Nonnull String planKey, final int buildNumber,
			final int timezoneOffset) throws ServerPasswordNotProvidedException, RemoteApiException;

    Collection<BuildIssue> getIssuesForBuild(ConnectionCfg bambooServer, @Nonnull String planKey, int buildNumber)
            throws ServerPasswordNotProvidedException, RemoteApiException;
}
