/*
 * Copyright (C) 2010-2014 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.glindholm.jira.rest.client.api;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import io.atlassian.util.concurrent.Promise;
import me.glindholm.jira.rest.client.api.domain.BasicIssue;
import me.glindholm.jira.rest.client.api.domain.BulkOperationResult;
import me.glindholm.jira.rest.client.api.domain.CimFieldInfo;
import me.glindholm.jira.rest.client.api.domain.Comment;
import me.glindholm.jira.rest.client.api.domain.Issue;
import me.glindholm.jira.rest.client.api.domain.IssueType;
import me.glindholm.jira.rest.client.api.domain.Page;
import me.glindholm.jira.rest.client.api.domain.Remotelink;
import me.glindholm.jira.rest.client.api.domain.Transition;
import me.glindholm.jira.rest.client.api.domain.Votes;
import me.glindholm.jira.rest.client.api.domain.Watchers;
import me.glindholm.jira.rest.client.api.domain.input.AttachmentInput;
import me.glindholm.jira.rest.client.api.domain.input.IssueInput;
import me.glindholm.jira.rest.client.api.domain.input.LinkIssuesInput;
import me.glindholm.jira.rest.client.api.domain.input.TransitionInput;
import me.glindholm.jira.rest.client.api.domain.input.WorklogInput;

/**
 * The me.glindholm.jira.rest.client.api handling issue resources.
 *
 * @since 0.1
 */
public interface IssueRestClient {

    /**
     * Creates new issue.
     *
     * @param issue populated with data to create new issue
     * @return basicIssue with generated <code>issueKey</code>
     * @throws URISyntaxException
     * @throws RestClientException in case of problems (connectivity, malformed messages, invalid
     *                             argument, etc.)
     * @since me.glindholm.jira.rest.client.api 1.0, server 5.0
     */
    Promise<BasicIssue> createIssue(IssueInput issue) throws URISyntaxException;

    /**
     * Update an existing issue.
     *
     * @param issueKey issue key (like TST-1, or JRA-9)
     * @param issue    populated with fields to set (no other verbs) in issue
     * @return Void
     * @throws URISyntaxException
     * @throws RestClientException in case of problems (connectivity, malformed messages, invalid
     *                             argument, etc.)
     * @since me.glindholm.jira.rest.client.api 3.0, server 5.0
     */
    Promise<Void> updateIssue(String issueKey, IssueInput issue) throws URISyntaxException;

    /**
     * Creates new issues in batch.
     *
     * @param issues populated with data to create new issue
     * @return BulkOperationResult&lt;BasicIssues&gt; with generated <code>issueKey</code> and errors
     *         for failed issues
     * @throws URISyntaxException
     * @throws RestClientException in case of problems (connectivity, malformed messages, invalid
     *                             argument, etc.)
     * @since me.glindholm.jira.rest.client.api 2.0, server 6.0
     */

    Promise<BulkOperationResult<BasicIssue>> createIssues(List<IssueInput> issues) throws URISyntaxException;

    Promise<Page<IssueType>> getCreateIssueMetaProjectIssueTypes(@NonNull String projectIdOrKey, @Nullable Long startAt, @Nullable Integer maxResults)
            throws URISyntaxException;

    Promise<Page<CimFieldInfo>> getCreateIssueMetaFields(@NonNull String projectIdOrKey, @NonNull String issueTypeId, @Nullable Long startAt,
            @Nullable Integer maxResults) throws URISyntaxException;

    /**
     * Retrieves issue with selected issue key.
     *
     * @param issueKey issue key (like TST-1, or JRA-9)
     * @return issue with given <code>issueKey</code>
     * @throws URISyntaxException
     * @throws RestClientException in case of problems (connectivity, malformed messages, invalid
     *                             argument, etc.)
     */
    Promise<Issue> getIssue(String issueKey) throws URISyntaxException;

    /**
     * Retrieves issue with selected issue key, with specified additional expandos.
     *
     * @param issueKey issue key (like TST-1, or JRA-9)
     * @param expand   additional expands. Currently CHANGELOG is the only supported expand that is not
     *                 expanded by default.
     * @return issue with given <code>issueKey</code>
     * @throws URISyntaxException
     * @throws RestClientException in case of problems (connectivity, malformed messages, invalid
     *                             argument, etc.)
     * @since 0.6
     */
    Promise<Issue> getIssue(String issueKey, List<Expandos> expand) throws URISyntaxException;

    /**
     * Retrieves remotelinks for an issue
     *
     * @param issueIdorKey
     * @return
     * @throws URISyntaxException
     *
     * @since 6.2.0
     */
    Promise<List<Remotelink>> getRemotelinks(String issueIdorKey) throws URISyntaxException;

    /**
     * Deletes issue with given issueKey. You can set {@code deleteSubtasks} to delete issue with
     * subtasks. If issue have subtasks and {@code deleteSubtasks} is set to false, then issue won't be
     * deleted.
     *
     * @param issueKey       issue key (like TST-1, or JRA-9)
     * @param deleteSubtasks Determines if subtask of issue should be also deleted. If false, and issue
     *                       has subtasks, then it won't be deleted.
     * @return Void
     * @throws URISyntaxException
     * @since 2.0
     */
    Promise<Void> deleteIssue(String issueKey, boolean deleteSubtasks) throws URISyntaxException;

    /**
     * Retrieves complete information (if the caller has permission) about watchers for selected issue.
     *
     * @param watchersUri URI of watchers resource for selected issue. Usually obtained by calling
     *                    <code>Issue.getWatchers().getSelf()</code>
     * @return detailed information about watchers watching selected issue.
     * @throws RestClientException in case of problems (connectivity, malformed messages, invalid
     *                             argument, etc.)
     * @see me.glindholm.jira.rest.client.api.domain.Issue#getWatchers()
     */
    Promise<Watchers> getWatchers(URI watchersUri);

    /**
     * Retrieves complete information (if the caller has permission) about voters for selected issue.
     *
     * @param votesUri URI of voters resource for selected issue. Usually obtained by calling
     *                 <code>Issue.getVotesUri()</code>
     * @return detailed information about voters of selected issue
     * @throws RestClientException in case of problems (connectivity, malformed messages, invalid
     *                             argument, etc.)
     * @see me.glindholm.jira.rest.client.api.domain.Issue#getVotesUri()
     */
    Promise<Votes> getVotes(URI votesUri);

    /**
     * Retrieves complete information (if the caller has permission) about transitions available for the
     * selected issue in its current state.
     *
     * @param transitionsUri URI of transitions resource of selected issue. Usually obtained by calling
     *                       <code>Issue.getTransitionsUri()</code>
     * @return transitions about transitions available for the selected issue in its current state.
     * @throws RestClientException in case of problems (connectivity, malformed messages, invalid
     *                             argument, etc.)
     */
    Promise<List<Transition>> getTransitions(URI transitionsUri);

    /**
     * Retrieves complete information (if the caller has permission) about transitions available for the
     * selected issue in its current state.
     *
     * @param issue issue
     * @return transitions about transitions available for the selected issue in its current state.
     * @throws URISyntaxException
     * @throws RestClientException in case of problems (connectivity, malformed messages, invalid
     *                             argument, etc.)
     * @since v0.5
     */
    Promise<List<Transition>> getTransitions(Issue issue) throws URISyntaxException;

    /**
     * Performs selected transition on selected issue.
     *
     * @param transitionsUri  URI of transitions resource of selected issue. Usually obtained by calling
     *                        <code>Issue.getTransitionsUri()</code>
     * @param transitionInput data for this transition (fields modified, the comment, etc.)
     * @throws URISyntaxException
     * @throws RestClientException in case of problems (connectivity, malformed messages, invalid
     *                             argument, etc.)
     */
    Promise<Void> transition(URI transitionsUri, TransitionInput transitionInput) throws URISyntaxException;

    /**
     * Performs selected transition on selected issue.
     *
     * @param issue           selected issue
     * @param transitionInput data for this transition (fields modified, the comment, etc.)
     * @throws URISyntaxException
     * @throws RestClientException in case of problems (connectivity, malformed messages, invalid
     *                             argument, etc.)
     * @since v0.5
     */
    Promise<Void> transition(Issue issue, TransitionInput transitionInput) throws URISyntaxException;

    /**
     * Casts your vote on the selected issue. Casting a vote on already votes issue by the caller,
     * causes the exception.
     *
     * @param votesUri URI of votes resource for selected issue. Usually obtained by calling
     *                 <code>Issue.getVotesUri()</code>
     * @throws RestClientException in case of problems (connectivity, malformed messages, invalid
     *                             argument, etc.)
     */
    Promise<Void> vote(URI votesUri);

    /**
     * Removes your vote from the selected issue. Removing a vote from the issue without your vote
     * causes the exception.
     *
     * @param votesUri URI of votes resource for selected issue. Usually obtained by calling
     *                 <code>Issue.getVotesUri()</code>
     * @throws RestClientException in case of problems (connectivity, malformed messages, invalid
     *                             argument, etc.)
     */
    Promise<Void> unvote(URI votesUri);

    /**
     * Starts watching selected issue
     *
     * @param watchersUri URI of watchers resource for selected issue. Usually obtained by calling
     *                    <code>Issue.getWatchers().getSelf()</code>
     * @throws RestClientException in case of problems (connectivity, malformed messages, invalid
     *                             argument, etc.)
     */
    Promise<Void> watch(URI watchersUri);

    /**
     * Stops watching selected issue
     *
     * @param watchersUri URI of watchers resource for selected issue. Usually obtained by calling
     *                    <code>Issue.getWatchers().getSelf()</code>
     * @throws URISyntaxException
     * @throws RestClientException in case of problems (connectivity, malformed messages, invalid
     *                             argument, etc.)
     */
    Promise<Void> unwatch(URI watchersUri) throws URISyntaxException;

    /**
     * Adds selected person as a watcher for selected issue. You need to have permissions to do that
     * (otherwise the exception is thrown).
     *
     * @param watchersUri URI of watchers resource for selected issue. Usually obtained by calling
     *                    <code>Issue.getWatchers().getSelf()</code>
     * @param username    user to add as a watcher
     * @throws RestClientException in case of problems (connectivity, malformed messages, invalid
     *                             argument, etc.)
     */
    Promise<Void> addWatcher(final URI watchersUri, final String username);

    /**
     * Removes selected person from the watchers list for selected issue. You need to have permissions
     * to do that (otherwise the exception is thrown).
     *
     * @param watchersUri URI of watchers resource for selected issue. Usually obtained by calling
     *                    <code>Issue.getWatchers().getSelf()</code>
     * @param username    user to remove from the watcher list
     * @throws URISyntaxException
     * @throws RestClientException in case of problems (connectivity, malformed messages, invalid
     *                             argument, etc.)
     */
    Promise<Void> removeWatcher(final URI watchersUri, final String username) throws URISyntaxException;

    /**
     * Creates link between two issues and adds a comment (optional) to the source issues.
     *
     * @param linkIssuesInput details for the link and the comment (optional) to be created
     * @throws URISyntaxException
     * @throws RestClientException in case of problems (connectivity, malformed messages, invalid
     *                             argument, permissions, etc.)
     * @since me.glindholm.jira.rest.client.api 0.2, server 4.3
     */
    Promise<Void> linkIssue(LinkIssuesInput linkIssuesInput) throws URISyntaxException;

    /**
     * Uploads attachments to JIRA (adding it to selected issue)
     *
     * @param attachmentsUri where to upload the attachment. You can get this URI by examining issue
     *                       resource first
     * @param in             stream from which to read data to upload
     * @param filename       file name to use for the uploaded attachment
     * @since me.glindholm.jira.rest.client.api 0.2, server 4.3
     */
    Promise<Void> addAttachment(URI attachmentsUri, InputStream in, String filename);

    /**
     * Uploads attachments to JIRA (adding it to selected issue)
     *
     * @param attachmentsUri where to upload the attachments. You can get this URI by examining issue
     *                       resource first
     * @param attachments    attachments to upload
     * @since me.glindholm.jira.rest.client.api 0.2, server 4.3
     */
    Promise<Void> addAttachments(URI attachmentsUri, AttachmentInput... attachments);

    /**
     * Uploads attachments to JIRA (adding it to selected issue)
     *
     * @param attachmentsUri where to upload the attachments. You can get this URI by examining issue
     *                       resource first
     * @param files          files to upload
     * @since me.glindholm.jira.rest.client.api 0.2, server 4.3
     */
    Promise<Void> addAttachments(URI attachmentsUri, File... files);

    /**
     * Adds a comment to JIRA (adding it to selected issue)
     *
     * @param commentsUri where to add comment
     * @param comment     the {@link Comment} to add
     * @throws URISyntaxException
     * @since me.glindholm.jira.rest.client.api 1.0, server 5.0
     */
    Promise<Void> addComment(URI commentsUri, Comment comment) throws URISyntaxException;

    /**
     * Retrieves the content of given attachment.
     *
     * @param attachmentUri URI for the attachment to retrieve
     * @return stream from which the caller may read the attachment content (bytes). The caller is
     *         responsible for closing the stream.
     */
    Promise<InputStream> getAttachment(URI attachmentUri);

    /**
     * Adds new worklog entry to issue.
     *
     * @param worklogUri   URI for worklog in issue
     * @param worklogInput worklog input object to create
     * @throws URISyntaxException
     */
    Promise<Void> addWorklog(URI worklogUri, WorklogInput worklogInput) throws URISyntaxException;

    /**
     * Expandos supported by {@link IssueRestClient#getIssue(String, List)}
     */
    public enum Expandos {
        CHANGELOG("changelog"), OPERATIONS("operations"), SCHEMA("schema"), NAMES("names"), TRANSITIONS("transitions"), RENDERED_FIELDS("renderedFields"),
        EDITMETA("editmeta"), VERSIONED_REPRESENTATIONS("versionedRepresentations");

        private final String value;

        Expandos(final String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
