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

package com.atlassian.theplugin.commons.crucible.api;

import com.atlassian.theplugin.commons.crucible.api.model.BasicProject;
import com.atlassian.theplugin.commons.crucible.api.model.BasicReview;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleVersionInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldDef;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
import com.atlassian.theplugin.commons.crucible.api.model.ExtendedCrucibleProject;
import com.atlassian.theplugin.commons.crucible.api.model.NewReviewItem;
import com.atlassian.theplugin.commons.crucible.api.model.PatchAnchorData;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.commons.crucible.api.model.Repository;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.RevisionData;
import com.atlassian.theplugin.commons.crucible.api.model.State;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.changes.Changes;
import com.atlassian.theplugin.commons.remoteapi.ProductSession;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface CrucibleSession extends ProductSession {
	void login() throws RemoteApiLoginException;

	void logout();

	CrucibleVersionInfo getServerVersion() throws RemoteApiException;

	@Nullable
	BasicReview createReview(Review review) throws RemoteApiException;

	@Nullable
	BasicReview createSnippetReview(Review review, String snippet, String filename) throws RemoteApiException;

	@Nullable
	BasicReview createReviewFromPatch(Review review, String patch, PatchAnchorData anchorData) throws RemoteApiException;

	@Nullable
	BasicReview createReviewFromPatch(Review review, String patch) throws RemoteApiException;

	@Nullable
	BasicReview createReviewFromRevision(Review review, List<String> revisions) throws RemoteApiException;

	List<CrucibleAction> getAvailableActions(PermId permId) throws RemoteApiException;

	List<CrucibleAction> getAvailableTransitions(PermId permId) throws RemoteApiException;

	@Nullable
	BasicReview addRevisionsToReview(PermId permId, String repository, Collection<String> revisions) throws RemoteApiException;

	BasicReview addRevisionsToReviewItems(PermId permId, Collection<RevisionData> revisions)
			throws RemoteApiException;

	@Nullable
	BasicReview addFileRevisionsToReview(PermId permId, String repository, List<PathAndRevision> revisions)
            throws RemoteApiException;

	void addFileToReview(PermId permId, NewReviewItem newReviewItem) throws RemoteApiException;

	@Nullable
	BasicReview addPatchToReview(PermId permId, String repository, String patch) throws RemoteApiException;

	void addReviewers(PermId permId, Set<String> userNames) throws RemoteApiException;

	void removeReviewer(PermId permId, String userNames) throws RemoteApiException;

	BasicReview changeReviewState(PermId permId, CrucibleAction action) throws RemoteApiException;

	@Nullable
	BasicReview closeReview(PermId permId, String summary) throws RemoteApiException;

	void completeReview(PermId permId, boolean complete) throws RemoteApiException;

	/**
	 *
	 * @param states
	 *            <code>null</code> for all reviews (in every state)
	 * @return
	 * @throws RemoteApiException
	 */
	List<BasicReview> getReviewsInStates(List<State> states) throws RemoteApiException;

	List<BasicReview> getAllReviews() throws RemoteApiException;

	List<BasicReview> getReviewsForFilter(PredefinedFilter filter) throws RemoteApiException;

	List<BasicReview> getReviewsForCustomFilter(CustomFilter filter) throws RemoteApiException;


	List<BasicReview> getAllReviewsForFile(String repoName, String path) throws RemoteApiException;

	Review getReview(PermId permId) throws RemoteApiException;

	List<Reviewer> getReviewers(PermId arg1) throws RemoteApiException;

	List<User> getUsers() throws RemoteApiException;

	List<BasicProject> getProjects() throws RemoteApiException;

	ExtendedCrucibleProject getProject(String key) throws RemoteApiException;

	List<Repository> getRepositories() throws RemoteApiException;

	Repository getRepository(String repoName) throws RemoteApiException;

	Set<CrucibleFileInfo> getFiles(PermId id) throws RemoteApiException;

	List<VersionedComment> getVersionedComments(Review review, CrucibleFileInfo reviewItem) throws RemoteApiException;

	Comment addGeneralComment(Review review, Comment comment) throws RemoteApiException;

	VersionedComment addVersionedComment(Review review, PermId riId, VersionedComment comment) throws RemoteApiException;

	void removeComment(PermId id, Comment comment) throws RemoteApiException;

	void updateComment(PermId id, Comment comment) throws RemoteApiException;

	void publishComment(PermId reviewId, PermId commentId) throws RemoteApiException;

	@Nullable
	Comment addReply(Review review, Comment reply) throws RemoteApiException;

	void updateReply(PermId id, PermId cId, PermId rId, Comment comment) throws RemoteApiException;

	List<CustomFieldDef> getMetrics(int version) throws RemoteApiException;

	boolean isLoggedIn() throws RemoteApiLoginException;

//	CrucibleFileInfo addItemToReview(Review review, NewReviewItem item) throws RemoteApiException;

	@Nullable
	BasicReview createReviewFromUpload(Review review, Collection<UploadItem> uploadItems) throws RemoteApiException;

	byte[] getFileContent(String contentUrl, boolean ignoreBase) throws RemoteApiException;

	byte[] getFileContent(String contentUrl) throws RemoteApiException;

	boolean checkContentUrlAvailable();

	void addItemsToReview(PermId permId, Collection<UploadItem> uploadItems) throws RemoteApiException;

    void markCommentRead(PermId reviewId, PermId commentId) throws RemoteApiException;

    void markCommentLeaveRead(PermId reviewId, PermId commentId) throws RemoteApiException;

    void markAllCommentsRead(PermId reviewId) throws RemoteApiException;

	@NotNull
	Changes getChanges(@NotNull String repository, @Nullable String oldestCsid, boolean includeOldest,
			@Nullable String newestCsid, boolean includeNewest, @Nullable Integer max) throws RemoteApiException;

    List<BasicReview> getReviewsForIssue(@NotNull String jiraIssueKey, @NotNull int maxReturn)
            throws RemoteApiException;

}
