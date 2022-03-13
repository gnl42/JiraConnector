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

package com.atlassian.connector.commons.crucible;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.theplugin.commons.crucible.api.CrucibleSession;
import com.atlassian.theplugin.commons.crucible.api.PathAndRevision;
import com.atlassian.theplugin.commons.crucible.api.UploadItem;
import com.atlassian.theplugin.commons.crucible.api.model.BasicProject;
import com.atlassian.theplugin.commons.crucible.api.model.BasicReview;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleVersionInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldDef;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
import com.atlassian.theplugin.commons.crucible.api.model.NewReviewItem;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.commons.crucible.api.model.Repository;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.ProductServerFacade;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Strange name, but this class should one day disappear.
 * com.atlassian.theplugin.commons.crucible.api.CrucibleSession should ultimately replacy it.
 */
public interface CrucibleServerFacade2 extends ProductServerFacade {
	// CrucibleVersionInfo getServerVersion(CrucibleServerCfg server)
	// throws RemoteApiException, ServerPasswordNotProvidedException;

	CrucibleSession getSession(ConnectionCfg server) throws RemoteApiException, ServerPasswordNotProvidedException;

	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
	Review createReview(ConnectionCfg server, Review review) throws RemoteApiException, ServerPasswordNotProvidedException;

	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
	Review createReviewFromRevision(ConnectionCfg server, Review review, List<String> revisions) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
	Review addRevisionsToReview(ConnectionCfg server, PermId permId, String repository, Collection<String> revisions)
		throws RemoteApiException, ServerPasswordNotProvidedException;

	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
    Review addFileRevisionsToReview(ConnectionCfg server, PermId permId, String repository, List<PathAndRevision> revisions)
            throws RemoteApiException, ServerPasswordNotProvidedException;

	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
    void addFileToReview(ConnectionCfg server, PermId permId, NewReviewItem newReviewItem) throws RemoteApiException,
            ServerPasswordNotProvidedException;

	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
	Review addPatchToReview(ConnectionCfg server, PermId permId, String repository, String patch) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
	List<Reviewer> getReviewers(ConnectionCfg server, PermId permId) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
	void addReviewers(ConnectionCfg server, PermId permId, Set<String> userName) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
	void removeReviewer(ConnectionCfg server, PermId permId, String userName) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	/**
	 * Convenience method for setting reviewers for a review. Please keep in mind that it involves at least 3 remote calls to
	 * Crucible server: getReview(), addReviewers() and N times removeReviewer(). This method is not atomic, so it may fail and
	 * leave reviewers in partially updated state After this method is complete, reviewers for selected review will be equal to
	 * this as given by <code>usernames</code>. Reviewers which are in <code>usernames</code> and are also present in the review
	 * itself are left intact - i.e. the method does gurantee to leave them intact even if some problems occur during execution.
	 *
	 * @param server
	 *            Crucible server to connect to
	 * @param permId
	 *            id of review
	 * @param usernames
	 *            usernames of reviewers
	 * @throws RemoteApiException
	 *             in case of some connection problems or malformed responses
	 * @throws ServerPasswordNotProvidedException
	 *             when password was not provided
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
	void setReviewers(@NotNull ConnectionCfg server, @NotNull PermId permId, @NotNull Collection<String> usernames)
		throws RemoteApiException, ServerPasswordNotProvidedException;



	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
	Review closeReview(ConnectionCfg server, PermId permId, String summary) throws RemoteApiException,
		ServerPasswordNotProvidedException;



	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
	void completeReview(ConnectionCfg server, PermId permId, boolean complete) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	// /**
	// * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	// * {@link CrucibleSession} directly.
	// */
	// @Deprecated
	// List<BasicReview> getAllReviews(ConnectionCfg server) throws RemoteApiException, ServerPasswordNotProvidedException;

	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
	List<BasicReview> getReviewsForFilter(ConnectionCfg server, PredefinedFilter filter) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
	List<BasicReview> getReviewsForCustomFilter(ConnectionCfg server, CustomFilter filter) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
	Review getReview(ConnectionCfg server, PermId permId) throws RemoteApiException, ServerPasswordNotProvidedException;

	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
		/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
	Review createReviewFromPatch(ConnectionCfg server, Review review, String patch) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
	Set<CrucibleFileInfo> getFiles(ConnectionCfg server, PermId permId) throws RemoteApiException,
		ServerPasswordNotProvidedException;


	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
	List<VersionedComment> getVersionedComments(ConnectionCfg server, Review review, CrucibleFileInfo reviewItem)
		throws RemoteApiException, ServerPasswordNotProvidedException;

	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
	Comment addGeneralComment(ConnectionCfg server, Review review, Comment comment) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
	VersionedComment addVersionedComment(ConnectionCfg server, Review review, PermId riId, VersionedComment comment)
		throws RemoteApiException, ServerPasswordNotProvidedException;

	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
	void updateComment(ConnectionCfg server, PermId id, Comment comment) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
	void publishComment(ConnectionCfg server, PermId reviewId, PermId commentId) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
	void publishAllCommentsForReview(ConnectionCfg server, PermId reviewId) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
	Comment addReply(ConnectionCfg server, Comment reply)
		throws RemoteApiException, ServerPasswordNotProvidedException;

	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
	void removeComment(ConnectionCfg server, PermId id, Comment comment) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
	List<User> getUsers(ConnectionCfg server) throws RemoteApiException, ServerPasswordNotProvidedException;

	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
	List<BasicProject> getProjects(ConnectionCfg server) throws RemoteApiException, ServerPasswordNotProvidedException;

	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
	List<Repository> getRepositories(ConnectionCfg server) throws RemoteApiException, ServerPasswordNotProvidedException;

	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
	Repository getRepository(ConnectionCfg server, String repoName) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
	List<CustomFieldDef> getMetrics(ConnectionCfg server, int version) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
	@Nullable
	String getDisplayName(@NotNull final ConnectionCfg server, @NotNull String username);

	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
	boolean checkContentUrlAvailable(ConnectionCfg server) throws RemoteApiException, ServerPasswordNotProvidedException;

	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
	Review createReviewFromUpload(ConnectionCfg server, Review review, Collection<UploadItem> uploadItems)
		throws RemoteApiException, ServerPasswordNotProvidedException;

	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
	Review addItemsToReview(ConnectionCfg server, PermId permId, Collection<UploadItem> items) throws RemoteApiException,
		ServerPasswordNotProvidedException;


	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
    void markCommentRead(@NotNull ConnectionCfg server, PermId reviewId, PermId commentId)  throws RemoteApiException,
			ServerPasswordNotProvidedException;

	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
    void markCommentLeaveUnread(@NotNull ConnectionCfg server, PermId reviewId, PermId commentId)
            throws RemoteApiException, ServerPasswordNotProvidedException;

	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
    void markAllCommentsRead(@NotNull ConnectionCfg server, PermId reviewId)  throws RemoteApiException,
			ServerPasswordNotProvidedException;

	/**
	 * @deprecated We are going remove {@link CrucibleServerFacade2}, so getSession here is for new code that should use
	 *             {@link CrucibleSession} directly.
	 */
	@Deprecated
	CrucibleVersionInfo getServerVersion(ConnectionCfg server) throws RemoteApiException, ServerPasswordNotProvidedException;

    List<BasicReview> getAllReviewsForFile(ConnectionCfg server, String repoName, String filePath)
            throws RemoteApiException, ServerPasswordNotProvidedException;

     List<BasicReview> getReviewsForIssue(ConnectionCfg server, String jiraIssueKey, int maxReturn)
             throws RemoteApiException, ServerPasswordNotProvidedException;
}
