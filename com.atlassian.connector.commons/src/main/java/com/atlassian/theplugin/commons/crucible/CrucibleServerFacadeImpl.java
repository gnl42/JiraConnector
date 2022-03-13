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

package com.atlassian.theplugin.commons.crucible;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.crucible.CrucibleServerFacade2;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.CrucibleSession;
import com.atlassian.theplugin.commons.crucible.api.PathAndRevision;
import com.atlassian.theplugin.commons.crucible.api.UploadItem;
import com.atlassian.theplugin.commons.crucible.api.model.BasicProject;
import com.atlassian.theplugin.commons.crucible.api.model.BasicReview;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleUserCache;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleVersionInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldDef;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.NewReviewItem;
import com.atlassian.theplugin.commons.crucible.api.model.PatchAnchorData;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.commons.crucible.api.model.Repository;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.rest.CrucibleSessionImpl;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import com.atlassian.theplugin.commons.util.Logger;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.commons.util.UrlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CrucibleServerFacadeImpl implements CrucibleServerFacade2 {
	private final Map<String, CrucibleSession> sessions = new HashMap<String, CrucibleSession>();

	private CrucibleUserCache userCache;

	private HttpSessionCallback callback;

	private final Logger logger;

	public CrucibleServerFacadeImpl(@NotNull Logger logger, CrucibleUserCache userCache,
			@NotNull HttpSessionCallback callback) {
		this.logger = logger;
		this.userCache = userCache;
		this.callback = callback;
	}

	public void setUserCache(CrucibleUserCache newCache) {
		userCache = newCache;
	}

	public ServerType getServerType() {
		return ServerType.CRUCIBLE_SERVER;
	}

	public synchronized CrucibleSession getSession(ConnectionCfg server) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		String key = server.getUrl() + server.getUsername() + server.getPassword();
		CrucibleSession session = sessions.get(key);
		if (session == null) {
			try {
				session = new CrucibleSessionImpl(server, callback, logger);
				// workaround for ACC-31
				if (!session.isLoggedIn()) {
					session.login();
				}
				sessions.put(key, session);
			} catch (RemoteApiMalformedUrlException e) {
				if (server.getPassword().length() > 0 || !UrlUtil.isUrlValid(server.getUrl())) {
					throw e;
				} else {
					// this is probably never thrown
					// todo remove it
					throw new ServerPasswordNotProvidedException(e);
				}
			}
		}
		return session;
	}

	private void fixUserName(ConnectionCfg server, Comment comment) {
		User u = comment.getAuthor();
		if (u.getDisplayName() == null || u.getDisplayName().length() == 0) {
			User newU = userCache.getUser(this, server, u.getUsername(), true);
			if (newU != null) {
				comment.setAuthor(newU);
			}
		}
	}

	@Nullable
	public String getDisplayName(@NotNull final ConnectionCfg server, @NotNull String username) {
		final User user = userCache.getUser(this, server, username, true);
		return user != null ? user.getDisplayName() : null;
	}

	// this method (and the method above is broken wrt to its design
	// @todo eliminate user cache from here, do not swollow exception, etc.
	@Nullable
	public User getUser(@NotNull final ConnectionCfg server, String username) {
		return userCache.getUser(this, server, username, true);
	}


    /* @todo optimize to get single project instead loading all from crucible 2.0 only
    https://extranet.atlassian.com/crucible/rest-service/projects-v1/CR?expand=allowedReviewers */
	public BasicProject getProject(@NotNull final ConnectionCfg server, @NotNull final String projectKey)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		final List<BasicProject> projects = getProjects(server);
		for (BasicProject project : projects) {
			if (project.getKey().equals(projectKey)) {
				return project;
			}
		}
		return null;
	}

	public boolean checkContentUrlAvailable(final ConnectionCfg server) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.checkContentUrlAvailable();
	}

	public Review createReviewFromUpload(@NotNull final ConnectionCfg server, @NotNull final Review review,
			@NotNull final Collection<UploadItem> uploadItems) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return getFullReview(session.createReviewFromUpload(review, uploadItems), session);
	}

	/**
	 * For testing Only
	 */
//	public void testServerConnection(String url, String userName, String password) throws RemoteApiException {
	// ConnectionCfg serverData = new ConnectionCfg("unknown", new ServerId(), userName, password, url);
//		testServerConnection(serverData);
//	}

    public void testServerConnection(ConnectionCfg connectionCfg) throws RemoteApiException {
		final CrucibleSession session = new CrucibleSessionImpl(connectionCfg, callback, logger);
		session.login();
		session.isLoggedIn();
		try {
			session.getServerVersion();
		} catch (RemoteApiException e) {
			// getServerVersion tries to login again due to https://studio.atlassian.com/browse/ACC-31
			// if it fails it will throw RemoteApiLoginException which doesn't have Cause
			if (e.getCause() != null && e.getCause().getMessage() != null
					&& e.getCause().getMessage().startsWith("HTTP 500")) {
				throw new CrucibleLoginException("Atlassian Connector for IntelliJ IDEA detected a Crucible version older\n"
						+ "than 1.6. Unfortunately, the plugin will not\n" + "work with this version of Crucible."
                        + "\nDetailed error message is\n" + e.getCause().getMessage() + "\n");
			}

			throw e;
		}
		session.logout();
    }

//	public CrucibleVersionInfo getServerVersion(CrucibleServerCfg server)
//			throws RemoteApiException, ServerPasswordNotProvidedException {
//		CrucibleSession session = getSession(server);
//		return session.getServerVersion();
//	}

	@Nullable
	private Review getFullReview(@Nullable BasicReview basicReview, CrucibleSession session) throws RemoteApiException {
		if (basicReview == null) {
			return null;
		}
		return session.getReview(basicReview.getPermId());
	}

	/**
	 * Creates new review in Crucible
	 *
	 * @param server connection configuration
	 * @param review data for new review to create (some fields have to be set e.g. projectKey)
	 * @return created revew date
	 * @throws com.atlassian.theplugin.commons.crucible.api.CrucibleException
	 *          in case of createReview error or CrucibleLoginException in case of login error
	 */
	public Review createReview(ConnectionCfg server, Review review) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return getFullReview(session.createReview(review), session);
	}

	public Review createReviewFromRevision(ConnectionCfg server, Review review, List<String> revisions)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return getFullReview(session.createReviewFromRevision(review, revisions), session);
	}

	public Review addRevisionsToReview(ConnectionCfg server, PermId permId, String repository, Collection<String> revisions)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		Review review = null;
		if (!revisions.isEmpty()) {
			review = getFullReview(session.addRevisionsToReview(permId, repository, revisions), session);
		}
		return review;
	}

    public Review addFileRevisionsToReview(ConnectionCfg server, PermId permId, String repository,
                                           List<PathAndRevision> revisions)
            throws RemoteApiException, ServerPasswordNotProvidedException {

        CrucibleSession session = getSession(server);
        Review review = null;
        if (!revisions.isEmpty()) {
			review = getFullReview(session.addFileRevisionsToReview(permId, repository, revisions), session);
        }
        return review;
    }

	public void addFileToReview(ConnectionCfg server, PermId permId, NewReviewItem newReviewItem) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		session.addFileToReview(permId, newReviewItem);
	}

	public CrucibleVersionInfo getServerVersion(ConnectionCfg server) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getServerVersion();
	}

    public List<BasicReview> getAllReviewsForFile(ConnectionCfg server, String repoName, String filePath)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        return session.getAllReviewsForFile(repoName, filePath);
    }

    public List<BasicReview> getReviewsForIssue(ConnectionCfg server, String jiraIssueKey, int maxReturn)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        return session.getReviewsForIssue(jiraIssueKey, maxReturn);
    }

	public Review addPatchToReview(ConnectionCfg server, PermId permId, String repository, String patch)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		final CrucibleSession session = getSession(server);
		return getFullReview(session.addPatchToReview(permId, repository, patch), session);
	}

	public Review addItemsToReview(ConnectionCfg server, PermId permId, Collection<UploadItem> items)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		session.addItemsToReview(permId, items);
		return session.getReview(permId);
	}

	public void addReviewers(ConnectionCfg server, PermId permId, Set<String> userNames) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		session.addReviewers(permId, userNames);
	}

	public void removeReviewer(ConnectionCfg server, PermId permId, String userName) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		session.removeReviewer(permId, userName);
	}

	private boolean contains(Set<Reviewer> reviewers, String username) {
		for (Reviewer reviewer : reviewers) {
			if (reviewer.getUsername().equals(username)) {
				return true;
			}
		}
		return false;
	}

	public void setReviewers(@NotNull final ConnectionCfg server, @NotNull final PermId permId,
			@NotNull final Collection<String> aUsernames) throws RemoteApiException, ServerPasswordNotProvidedException {
		final Set<String> reviewersForAdd = MiscUtil.buildHashSet();
		final Set<String> reviewersForRemove = MiscUtil.buildHashSet();
		final Review review = getReview(server, permId);
		// removing potential duplicates
		final Set<String> usernames = MiscUtil.buildHashSet(aUsernames);

		for (String username : usernames) {
			if (!contains(review.getReviewers(), username)) {
				reviewersForAdd.add(username);
			}
		}

		for (Reviewer reviewer : review.getReviewers()) {
			if (!usernames.contains(reviewer.getUsername())) {
				reviewersForRemove.add(reviewer.getUsername());
			}
		}

		if (!reviewersForAdd.isEmpty()) {
			addReviewers(server, permId, reviewersForAdd);
		}
		if (!reviewersForRemove.isEmpty()) {
			for (String reviewer : reviewersForRemove) {
				removeReviewer(server, permId, reviewer);
			}
		}
	}


	public Review closeReview(ConnectionCfg server, PermId permId, String summary) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return getFullReview(session.closeReview(permId, summary), session);
	}


	public void completeReview(ConnectionCfg server, PermId permId, boolean complete) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		session.completeReview(permId, complete);
	}

	/**
	 * Creates new review in Crucible
	 *
	 *
	 * @param server connection configuration
	 * @param review data for new review to create (some fields have to be set e.g. projectKey)
	 * @param patch  patch to assign with the review
	 * @return created revew date
	 * @throws com.atlassian.theplugin.commons.crucible.api.CrucibleException
	 *          in case of createReview error or CrucibleLoginException in case of login error
	 */
	public Review createReviewFromPatch(ConnectionCfg server, Review review, String patch)
			throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return getFullReview(session.createReviewFromPatch(review, patch), session);
	}

		/**
	 * Creates new review in Crucible
	 *
	 *
	 * @param server connection configuration
	 * @param review data for new review to create (some fields have to be set e.g. projectKey)
	 * @param patch  patch to assign with the review
	 * @param anchorData
	 * @return created revew date
	 * @throws com.atlassian.theplugin.commons.crucible.api.CrucibleException
	 *          in case of createReview error or CrucibleLoginException in case of login error
	 */
	public Review createReviewFromPatch(ConnectionCfg server, Review review, String patch, PatchAnchorData anchorData)
			throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return getFullReview(session.createReviewFromPatch(review, patch, anchorData), session);
	}
	public Set<CrucibleFileInfo> getFiles(ConnectionCfg server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getFiles(permId);
	}

	//	public List<Comment> getComments(CrucibleServerCfg server, PermId permId)
//			throws RemoteApiException, ServerPasswordNotProvidedException {
//		CrucibleSession session = getSession(server);
//		return session.getComments(permId);
//	}

	public List<VersionedComment> getVersionedComments(ConnectionCfg server, Review review, CrucibleFileInfo reviewItem)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getVersionedComments(review, reviewItem);
	}

	public Comment addGeneralComment(ConnectionCfg server, Review review, Comment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		GeneralComment newComment = (GeneralComment) session.addGeneralComment(review, comment);
		if (newComment != null) {
			fixUserName(server, newComment);
		}
		return newComment;
	}

	public VersionedComment addVersionedComment(ConnectionCfg server, Review review, PermId riId, VersionedComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		VersionedComment newComment = session.addVersionedComment(review, riId, comment);
		if (newComment != null) {
			fixUserName(server, newComment);
		}
		return newComment;
	}

	public void updateComment(ConnectionCfg server, PermId id, Comment comment) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		session.updateComment(id, comment);
	}

	public void publishComment(ConnectionCfg server, PermId reviewId, PermId commentId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		session.publishComment(reviewId, commentId);
	}

	public void publishAllCommentsForReview(ConnectionCfg server, PermId reviewId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		session.publishComment(reviewId, null);
	}

	public Comment addReply(ConnectionCfg server, Comment reply)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		GeneralComment newReply = (GeneralComment) session.addReply(reply.getReview(), reply);
		if (newReply != null) {
			fixUserName(server, newReply);
		}
		return newReply;
	}

	public void removeComment(ConnectionCfg server, PermId id, Comment comment) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		session.removeComment(id, comment);
	}

	public List<User> getUsers(ConnectionCfg server) throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getUsers();
	}

	/**
	 * Retrieves list of projects defined on Crucible server
	 *
	 * @param server connection configuration
	 * @return
	 * @throws RemoteApiException
	 * @throws ServerPasswordNotProvidedException
	 *
	 */
	public List<BasicProject> getProjects(ConnectionCfg server) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getProjects();
	}

	/**
	 * Retrieves list of repositories defined on Crucible server
	 *
	 * @param server connection configuration
	 * @return
	 * @throws RemoteApiException
	 * @throws com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException
	 *
	 */
	public List<Repository> getRepositories(ConnectionCfg server) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getRepositories();
	}

	public Repository getRepository(ConnectionCfg server, String repoName) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getRepository(repoName);
	}

	public List<CustomFieldDef> getMetrics(ConnectionCfg server, int version) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getMetrics(version);
	}

	public List<BasicReview> getReviewsForFilter(ConnectionCfg server, PredefinedFilter filter) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getReviewsForFilter(filter);
	}

	public List<BasicReview> getReviewsForCustomFilter(ConnectionCfg server, CustomFilter filter) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getReviewsForCustomFilter(filter);
	}

	public Review getReview(ConnectionCfg server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getReview(permId);
	}

	public List<Reviewer> getReviewers(ConnectionCfg server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getReviewers(permId);
	}

//	public List<Review> getAllReviewsForFile(CrucibleServerCfg server, String repoName, String path)
//			throws RemoteApiException, ServerPasswordNotProvidedException {
//		CrucibleSession session = getSession(server);
//		return session.getAllReviewsForFile(repoName, path, true);
//	}

	public void setCallback(HttpSessionCallback callback) {
		this.callback = callback;
	}

    public void markCommentRead(@NotNull ConnectionCfg server, PermId reviewId, PermId commentId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        session.markCommentRead(reviewId, commentId);
    }

    public void markCommentLeaveUnread(@NotNull ConnectionCfg server, PermId reviewId, PermId commentId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        session.markCommentLeaveRead(reviewId, commentId);
    }

    public void markAllCommentsRead(@NotNull ConnectionCfg server, PermId reviewId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        session.markAllCommentsRead(reviewId);
    }
}
