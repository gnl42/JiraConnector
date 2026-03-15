/*
 * Copyright (C) 2012 Atlassian
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
package me.glindholm.jira.rest.client.internal.async;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import me.glindholm.jira.rest.client.api.IssueRestClient;
import me.glindholm.jira.rest.client.api.MetadataRestClient;
import me.glindholm.jira.rest.client.api.RestClientException;
import me.glindholm.jira.rest.client.api.SessionRestClient;
import me.glindholm.jira.rest.client.api.domain.BasicIssue;
import me.glindholm.jira.rest.client.api.domain.BulkOperationResult;
import me.glindholm.jira.rest.client.api.domain.CimFieldInfo;
import me.glindholm.jira.rest.client.api.domain.Comment;
import me.glindholm.jira.rest.client.api.domain.Issue;
import me.glindholm.jira.rest.client.api.domain.IssueType;
import me.glindholm.jira.rest.client.api.domain.Page;
import me.glindholm.jira.rest.client.api.domain.Remotelink;
import me.glindholm.jira.rest.client.api.domain.ServerInfo;
import me.glindholm.jira.rest.client.api.domain.Transition;
import me.glindholm.jira.rest.client.api.domain.Votes;
import me.glindholm.jira.rest.client.api.domain.Watchers;
import me.glindholm.jira.rest.client.api.domain.input.AttachmentInput;
import me.glindholm.jira.rest.client.api.domain.input.FieldInput;
import me.glindholm.jira.rest.client.api.domain.input.IssueInput;
import me.glindholm.jira.rest.client.api.domain.input.LinkIssuesInput;
import me.glindholm.jira.rest.client.api.domain.input.TransitionInput;
import me.glindholm.jira.rest.client.api.domain.input.WorklogInput;
import me.glindholm.jira.rest.client.internal.ServerVersionConstants;
import me.glindholm.jira.rest.client.internal.json.BasicIssueJsonParser;
import me.glindholm.jira.rest.client.internal.json.BasicIssuesJsonParser;
import me.glindholm.jira.rest.client.internal.json.CreateIssueMetaFieldsParser;
import me.glindholm.jira.rest.client.internal.json.CreateIssueMetaProjectIssueTypesParser;
import me.glindholm.jira.rest.client.internal.json.CreateIssueMetadataJsonParser;
import me.glindholm.jira.rest.client.internal.json.IssueJsonParser;
import me.glindholm.jira.rest.client.internal.json.JsonObjectParser;
import me.glindholm.jira.rest.client.internal.json.JsonParseUtil;
import me.glindholm.jira.rest.client.internal.json.RemotelinksJsonParser;
import me.glindholm.jira.rest.client.internal.json.TransitionJsonParser;
import me.glindholm.jira.rest.client.internal.json.TransitionJsonParserV5;
import me.glindholm.jira.rest.client.internal.json.VotesJsonParser;
import me.glindholm.jira.rest.client.internal.json.WatchersJsonParserBuilder;
import me.glindholm.jira.rest.client.internal.json.gen.CommentJsonGenerator;
import me.glindholm.jira.rest.client.internal.json.gen.IssueInputJsonGenerator;
import me.glindholm.jira.rest.client.internal.json.gen.IssueUpdateJsonGenerator;
import me.glindholm.jira.rest.client.internal.json.gen.IssuesInputJsonGenerator;
import me.glindholm.jira.rest.client.internal.json.gen.LinkIssuesInputGenerator;
import me.glindholm.jira.rest.client.internal.json.gen.WorklogInputJsonGenerator;

/**
 * Asynchronous implementation of IssueRestClient.
 *
 * @since v2.0
 */
public class AsynchronousIssueRestClient extends AbstractAsynchronousRestClient implements IssueRestClient {

    private static final EnumSet<Expandos> DEFAULT_EXPANDS = EnumSet.of(Expandos.NAMES, Expandos.SCHEMA, Expandos.TRANSITIONS);
    private static final Function<IssueRestClient.Expandos, String> EXPANDO_TO_PARAM = from -> from.name().toLowerCase();
    private final SessionRestClient sessionRestClient;
    private final MetadataRestClient metadataRestClient;

    private final IssueJsonParser issueParser = new IssueJsonParser();
    private final BasicIssueJsonParser basicIssueParser = new BasicIssueJsonParser();
    private final JsonObjectParser<Watchers> watchersParser = WatchersJsonParserBuilder.createWatchersParser();
    private final TransitionJsonParser transitionJsonParser = new TransitionJsonParser();
    private final JsonObjectParser<Transition> transitionJsonParserV5 = new TransitionJsonParserV5();
    private final VotesJsonParser votesJsonParser = new VotesJsonParser();
    private final CreateIssueMetadataJsonParser createIssueMetadataJsonParser = new CreateIssueMetadataJsonParser();
    private final RemotelinksJsonParser remotelinksParser = new RemotelinksJsonParser();

    private static final String FILE_BODY_TYPE = "file";
    private final URI baseUri;
    private ServerInfo serverInfo;

    public AsynchronousIssueRestClient(final URI baseUri, final DisposableHttpClient client, final SessionRestClient sessionRestClient,
            final MetadataRestClient metadataRestClient) {
        super(client);
        this.baseUri = baseUri;
        this.sessionRestClient = sessionRestClient;
        this.metadataRestClient = metadataRestClient;
    }

    private synchronized ServerInfo getVersionInfo() throws URISyntaxException {
        if (serverInfo == null) {
            serverInfo = metadataRestClient.getServerInfo().join();
        }
        return serverInfo;
    }

    @Override
    public CompletableFuture<BasicIssue> createIssue(final IssueInput issue) throws URISyntaxException {
        final UriBuilder uriBuilder = new UriBuilder(baseUri).appendPath("issue");
        return postAndParse(uriBuilder.build(), issue, new IssueInputJsonGenerator(), basicIssueParser);
    }

    @Override
    public CompletableFuture<Void> updateIssue(final String issueKey, final IssueInput issue) throws URISyntaxException {
        final UriBuilder uriBuilder = new UriBuilder(baseUri).appendPath("issue").appendPath(issueKey);
        return put(uriBuilder.build(), issue, new IssueInputJsonGenerator());
    }

    @Override
    public CompletableFuture<BulkOperationResult<BasicIssue>> createIssues(final List<IssueInput> issues) throws URISyntaxException {
        final UriBuilder uriBuilder = new UriBuilder(baseUri).appendPath("issue/bulk");
        return postAndParse(uriBuilder.build(), issues, new IssuesInputJsonGenerator(), new BasicIssuesJsonParser());
    }

    @Override
    public CompletableFuture<Page<IssueType>> getCreateIssueMetaProjectIssueTypes(@NonNull final String projectIdOrKey, @Nullable final Long startAt,
            @Nullable final Integer maxResults) throws URISyntaxException {
        final UriBuilder uriBuilder = new UriBuilder(baseUri).appendPath("issue/createmeta/" + projectIdOrKey + "/issuetypes");
        addPagingParameters(uriBuilder, startAt, maxResults);
        return getAndParse(uriBuilder.build(), new CreateIssueMetaProjectIssueTypesParser());
    }

    @Override
    public CompletableFuture<Page<CimFieldInfo>> getCreateIssueMetaFields(@NonNull final String projectIdOrKey, @NonNull final String issueTypeId,
            @Nullable final Long startAt, @Nullable final Integer maxResults) throws URISyntaxException {
        final UriBuilder uriBuilder = new UriBuilder(baseUri).appendPath("issue/createmeta/" + projectIdOrKey + "/issuetypes/" + issueTypeId);
        addPagingParameters(uriBuilder, startAt, maxResults);
        return getAndParse(uriBuilder.build(), new CreateIssueMetaFieldsParser());
    }

    @Override
    public CompletableFuture<Issue> getIssue(final String issueKey) throws URISyntaxException {
        return getIssue(issueKey, Collections.emptyList());
    }

    @Override
    public CompletableFuture<Issue> getIssue(final String issueKey, final List<Expandos> expand) throws URISyntaxException {
        final UriBuilder uriBuilder = new UriBuilder(baseUri);
        final List<Expandos> expands = Stream.of(DEFAULT_EXPANDS, expand).collect(ArrayList::new, List::addAll, List::addAll);
        uriBuilder.appendPath("issue").appendPath(issueKey).addParameter("expand",
                StreamSupport.stream(expands.spliterator(), false).map(EXPANDO_TO_PARAM).collect(Collectors.joining(",")));
        return getAndParse(uriBuilder.build(), issueParser);
    }

    @Override
    public CompletableFuture<Void> deleteIssue(final String issueKey, final boolean deleteSubtasks) throws URISyntaxException {
        return delete(new UriBuilder(baseUri).appendPath("issue").appendPath(issueKey).addParameter("deleteSubtasks", String.valueOf(deleteSubtasks)).build());
    }

    @Override
    public CompletableFuture<Watchers> getWatchers(final URI watchersUri) {
        return getAndParse(watchersUri, watchersParser);
    }

    @Override
    public CompletableFuture<Votes> getVotes(final URI votesUri) {
        return getAndParse(votesUri, votesJsonParser);
    }

    @Override
    public CompletableFuture<List<Transition>> getTransitions(final URI transitionsUri) {
        return callAndParse(client().execute(client().newRequest(transitionsUri).GET().build()),
                (ResponseHandler<List<Transition>>) (statusCode, body) -> {
                    final JSONObject jsonObject = new JSONObject(body);
                    if (jsonObject.has("transitions")) {
                        return JsonParseUtil.parseJsonArray(jsonObject.getJSONArray("transitions"), transitionJsonParserV5);
                    } else {
                        final List<Transition> transitions = new ArrayList<>(jsonObject.length());
                        @SuppressWarnings("unchecked")
                        final Iterator<String> iterator = jsonObject.keys();
                        while (iterator.hasNext()) {
                            final String key = iterator.next();
                            try {
                                final int id = Integer.parseInt(key);
                                final Transition transition = transitionJsonParser.parse(jsonObject.getJSONObject(key), id);
                                transitions.add(transition);
                            } catch (URISyntaxException | JSONException e) {
                                throw new RestClientException(e);
                            } catch (final NumberFormatException e) {
                                throw new RestClientException("Transition id should be an integer, but found [" + key + "]", e);
                            }
                        }
                        return transitions;
                    }
                });
    }

    @Override
    public CompletableFuture<List<Transition>> getTransitions(final Issue issue) throws URISyntaxException {
        if (issue.getTransitionsUri() != null) {
            return getTransitions(issue.getTransitionsUri());
        } else {
            final UriBuilder transitionsUri = new UriBuilder(issue.getSelf());
            return getTransitions(transitionsUri.appendPath("transitions").addParameter("expand", "transitions.fields").build());
        }
    }

    @Override
    public CompletableFuture<Void> transition(final URI transitionsUri, final TransitionInput transitionInput) throws URISyntaxException {
        final int buildNumber = getVersionInfo().getBuildNumber();
        try {
            final JSONObject jsonObject = new JSONObject();
            if (buildNumber >= ServerVersionConstants.BN_JIRA_5) {
                jsonObject.put("transition", new JSONObject().put("id", transitionInput.getId()));
            } else {
                jsonObject.put("transition", transitionInput.getId());
            }
            if (transitionInput.getComment() != null) {
                if (buildNumber >= ServerVersionConstants.BN_JIRA_5) {
                    jsonObject.put("update", new JSONObject().put("comment", new JSONArray()
                            .put(new JSONObject().put("add", new CommentJsonGenerator(getVersionInfo()).generate(transitionInput.getComment())))));
                } else {
                    jsonObject.put("comment", new CommentJsonGenerator(getVersionInfo()).generate(transitionInput.getComment()));
                }
            }
            final List<FieldInput> fields = transitionInput.getFields();
            final JSONObject fieldsJs = new IssueUpdateJsonGenerator().generate(fields);
            if (fieldsJs.keys().hasNext()) {
                jsonObject.put("fields", fieldsJs);
            }
            return post(transitionsUri, jsonObject);
        } catch (final JSONException ex) {
            throw new RestClientException(ex);
        }
    }

    @Override
    public CompletableFuture<Void> transition(final Issue issue, final TransitionInput transitionInput) throws URISyntaxException {
        if (issue.getTransitionsUri() != null) {
            return transition(issue.getTransitionsUri(), transitionInput);
        } else {
            final UriBuilder uriBuilder = new UriBuilder(issue.getSelf());
            uriBuilder.appendPath("transitions");
            return transition(uriBuilder.build(), transitionInput);
        }
    }

    @Override
    public CompletableFuture<Void> vote(final URI votesUri) {
        return post(votesUri);
    }

    @Override
    public CompletableFuture<Void> unvote(final URI votesUri) {
        return delete(votesUri);
    }

    @Override
    public CompletableFuture<Void> watch(final URI watchersUri) {
        return post(watchersUri);
    }

    @Override
    public CompletableFuture<Void> unwatch(final URI watchersUri) throws URISyntaxException {
        return removeWatcher(watchersUri, getLoggedUsername());
    }

    @Override
    public CompletableFuture<Void> addWatcher(final URI watchersUri, final String username) {
        return post(watchersUri, JSONObject.quote(username));
    }

    @Override
    public CompletableFuture<Void> removeWatcher(final URI watchersUri, final String username) throws URISyntaxException {
        final UriBuilder uriBuilder = new UriBuilder(watchersUri);
        if (getVersionInfo().getBuildNumber() >= ServerVersionConstants.BN_JIRA_4_4) {
            uriBuilder.addParameter("username", username);
        } else {
            uriBuilder.appendPath(username).build();
        }
        return delete(uriBuilder.build());
    }

    @Override
    public CompletableFuture<Void> linkIssue(final LinkIssuesInput linkIssuesInput) throws URISyntaxException {
        final URI uri = new UriBuilder(baseUri).appendPath("issueLink").build();
        return post(uri, linkIssuesInput, new LinkIssuesInputGenerator(getVersionInfo()));
    }

    @Override
    public CompletableFuture<List<Remotelink>> getRemotelinks(final String issueIdorKey) throws URISyntaxException {
        final UriBuilder uriBuilder = new UriBuilder(baseUri);
        uriBuilder.appendPath("issue").appendPath(issueIdorKey).appendPath("remotelink");
        return getAndParse(uriBuilder.build(), remotelinksParser);
    }

    @Override
    public CompletableFuture<Void> addAttachment(final URI attachmentsUri, final InputStream inputStream, final String filename) {
        try {
            final byte[] bytes = inputStream.readAllBytes();
            return postMultipart(attachmentsUri, filename, bytes, "application/octet-stream");
        } catch (final IOException e) {
            throw new RestClientException(e);
        }
    }

    @Override
    public CompletableFuture<Void> addAttachments(final URI attachmentsUri, final AttachmentInput... attachments) {
        return postMultipartBatch(attachmentsUri, Stream.of(attachments)
                .collect(Collectors.toMap(AttachmentInput::getFilename, a -> {
                    try {
                        return a.getInputStream().readAllBytes();
                    } catch (final IOException e) {
                        throw new RestClientException(e);
                    }
                })));
    }

    @Override
    public CompletableFuture<Void> addAttachments(final URI attachmentsUri, final File... files) {
        return postMultipartBatch(attachmentsUri, Stream.of(files)
                .collect(Collectors.toMap(File::getName, f -> {
                    try {
                        return Files.readAllBytes(f.toPath());
                    } catch (final IOException e) {
                        throw new RestClientException(e);
                    }
                })));
    }

    @Override
    public CompletableFuture<Void> addComment(final URI commentsUri, final Comment comment) throws URISyntaxException {
        return post(commentsUri, comment, new CommentJsonGenerator(getVersionInfo()));
    }

    @Override
    public CompletableFuture<InputStream> getAttachment(final URI attachmentUri) {
        final HttpRequest request = client().newRequest(attachmentUri).GET().build();
        return client().executeForBytes(request).thenApply(response -> new java.io.ByteArrayInputStream(response.body()));
    }

    @Override
    public CompletableFuture<Void> addWorklog(final URI worklogUri, final WorklogInput worklogInput) throws URISyntaxException {
        final UriBuilder uriBuilder = new UriBuilder(worklogUri).addParameter("adjustEstimate", worklogInput.getAdjustEstimate().restValue);

        switch (worklogInput.getAdjustEstimate()) {
        case NEW:
            uriBuilder.addParameter("newEstimate", nullToEmpty(worklogInput.getAdjustEstimateValue()));
            break;
        case MANUAL:
            uriBuilder.addParameter("reduceBy", nullToEmpty(worklogInput.getAdjustEstimateValue()));
            break;
        default:
            break;
        }

        return post(uriBuilder.build(), worklogInput, new WorklogInputJsonGenerator());
    }

    private static String nullToEmpty(final String value) {
        return value == null ? "" : value;
    }

    private void addPagingParameters(final UriBuilder uriBuilder, @Nullable final Long startAt, @Nullable final Integer maxResults) {
        if (startAt != null) {
            uriBuilder.addParameter("startAt", String.valueOf(startAt));
        }
        if (maxResults != null) {
            uriBuilder.addParameter("maxResults", String.valueOf(maxResults));
        }
    }

    private CompletableFuture<Void> postMultipart(final URI uri, final String filename, final byte[] data, final String contentType) {
        final String boundary = "----JiraClientBoundary" + UUID.randomUUID().toString().replace("-", "");
        final byte[] body = buildMultipartBody(boundary, FILE_BODY_TYPE, filename, data, contentType);
        final HttpRequest request = client().newRequest(uri)
                .header("X-Atlassian-Token", "nocheck")
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();
        return call(client().execute(request));
    }

    private CompletableFuture<Void> postMultipartBatch(final URI uri, final java.util.Map<String, byte[]> files) {
        final String boundary = "----JiraClientBoundary" + UUID.randomUUID().toString().replace("-", "");
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            for (final java.util.Map.Entry<String, byte[]> entry : files.entrySet()) {
                final byte[] part = buildMultipartBody(boundary, FILE_BODY_TYPE, entry.getKey(), entry.getValue(), "application/octet-stream");
                baos.write(part);
            }
            // Final boundary
            baos.write(("--" + boundary + "--\r\n").getBytes(UTF_8));
        } catch (final IOException e) {
            throw new RestClientException(e);
        }
        final HttpRequest request = client().newRequest(uri)
                .header("X-Atlassian-Token", "nocheck")
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(baos.toByteArray()))
                .build();
        return call(client().execute(request));
    }

    private static byte[] buildMultipartBody(final String boundary, final String fieldName, final String filename, final byte[] data, final String contentType) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(("--" + boundary + "\r\n").getBytes(UTF_8));
            baos.write(("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + filename + "\"\r\n").getBytes(UTF_8));
            baos.write(("Content-Type: " + contentType + "\r\n\r\n").getBytes(UTF_8));
            baos.write(data);
            baos.write("\r\n".getBytes(UTF_8));
        } catch (final IOException e) {
            throw new RestClientException(e);
        }
        return baos.toByteArray();
    }

    private String getLoggedUsername() throws RestClientException, URISyntaxException {
        return sessionRestClient.getCurrentSession().join().getUsername();
    }
}
