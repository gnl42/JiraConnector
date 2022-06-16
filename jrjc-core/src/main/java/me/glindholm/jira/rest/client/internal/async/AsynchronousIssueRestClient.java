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
import static org.apache.http.entity.ContentType.DEFAULT_BINARY;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.hc.core5.net.URIBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.atlassian.httpclient.apache.httpcomponents.MultiPartEntityBuilder;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Message;
import com.atlassian.httpclient.api.ResponsePromise;

import io.atlassian.util.concurrent.Promise;
import me.glindholm.jira.rest.client.api.GetCreateIssueMetadataOptions;
import me.glindholm.jira.rest.client.api.IssueRestClient;
import me.glindholm.jira.rest.client.api.MetadataRestClient;
import me.glindholm.jira.rest.client.api.RestClientException;
import me.glindholm.jira.rest.client.api.SessionRestClient;
import me.glindholm.jira.rest.client.api.domain.BasicIssue;
import me.glindholm.jira.rest.client.api.domain.BulkOperationResult;
import me.glindholm.jira.rest.client.api.domain.CimFieldInfo;
import me.glindholm.jira.rest.client.api.domain.CimProject;
import me.glindholm.jira.rest.client.api.domain.Comment;
import me.glindholm.jira.rest.client.api.domain.Issue;
import me.glindholm.jira.rest.client.api.domain.IssueType;
import me.glindholm.jira.rest.client.api.domain.Page;
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
    private static final String FILE_BODY_TYPE = "file";
    private final URI baseUri;
    private ServerInfo serverInfo;

    public AsynchronousIssueRestClient(final URI baseUri, final HttpClient client, final SessionRestClient sessionRestClient,
            final MetadataRestClient metadataRestClient) {
        super(client);
        this.baseUri = baseUri;
        this.sessionRestClient = sessionRestClient;
        this.metadataRestClient = metadataRestClient;
    }

    private synchronized ServerInfo getVersionInfo() throws URISyntaxException {
        if (serverInfo == null) {
            serverInfo = metadataRestClient.getServerInfo().claim();
        }
        return serverInfo;
    }

    @Override
    public Promise<BasicIssue> createIssue(final IssueInput issue) throws URISyntaxException {
        final URIBuilder uriBuilder = new URIBuilder(baseUri).appendPath("issue");
        return postAndParse(uriBuilder.build(), issue, new IssueInputJsonGenerator(), basicIssueParser);
    }

    @Override
    public Promise<Void> updateIssue(final String issueKey, final IssueInput issue) throws URISyntaxException {
        final URIBuilder uriBuilder = new URIBuilder(baseUri).appendPath("issue").appendPath(issueKey);
        return put(uriBuilder.build(), issue, new IssueInputJsonGenerator());
    }

    @Override
    public Promise<BulkOperationResult<BasicIssue>> createIssues(List<IssueInput> issues) throws URISyntaxException {
        final URIBuilder uriBuilder = new URIBuilder(baseUri).appendPath("issue/bulk");

        return postAndParse(uriBuilder.build(), issues, new IssuesInputJsonGenerator(), new BasicIssuesJsonParser());
    }

    @Override
    public Promise<List<CimProject>> getCreateIssueMetadata(@Nullable GetCreateIssueMetadataOptions options) throws URISyntaxException {
        final URIBuilder uriBuilder = new URIBuilder(baseUri).appendPath("issue/createmeta");

        if (options != null) {
            if (options.projectIds != null) {
                uriBuilder.addParameter("projectIds", options.projectIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
            }

            if (options.projectKeys != null) {
                uriBuilder.addParameter("projectKeys", options.projectKeys.stream().map(String::valueOf).collect(Collectors.joining(",")));
            }

            if (options.issueTypeIds != null) {
                uriBuilder.addParameter("issuetypeIds", options.issueTypeIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
            }

            final List<String> issueTypeNames = options.issueTypeNames;
            if (issueTypeNames != null) {
                for (final String name : issueTypeNames) {
                    uriBuilder.addParameter("issuetypeNames", name);
                }
            }

            final Set<String> expandos = options.expandos;
            if (expandos != null && expandos.iterator().hasNext()) {
                uriBuilder.addParameter("expand", String.join(",", expandos));
            }
        }

        return getAndParse(uriBuilder.build(), createIssueMetadataJsonParser);
    }

    @Override
    public Promise<Page<IssueType>> getCreateIssueMetaProjectIssueTypes(@Nonnull final String projectIdOrKey, @Nullable final Long startAt,
            @Nullable final Integer maxResults) throws URISyntaxException {
        final URIBuilder uriBuilder = new URIBuilder(baseUri).appendPath("issue/createmeta/" + projectIdOrKey + "/issuetypes");
        addPagingParameters(uriBuilder, startAt, maxResults);

        return getAndParse(uriBuilder.build(), new CreateIssueMetaProjectIssueTypesParser());
    }

    @Override
    public Promise<Page<CimFieldInfo>> getCreateIssueMetaFields(@Nonnull final String projectIdOrKey, @Nonnull final String issueTypeId,
            @Nullable final Long startAt, @Nullable final Integer maxResults) throws URISyntaxException {
        final URIBuilder uriBuilder = new URIBuilder(baseUri).appendPath("issue/createmeta/" + projectIdOrKey + "/issuetypes/" + issueTypeId);
        addPagingParameters(uriBuilder, startAt, maxResults);

        return getAndParse(uriBuilder.build(), new CreateIssueMetaFieldsParser());
    }

    @Override
    public Promise<Issue> getIssue(final String issueKey) throws URISyntaxException {
        return getIssue(issueKey, Collections.emptyList());
    }

    @Override
    public Promise<Issue> getIssue(final String issueKey, final List<Expandos> expand) throws URISyntaxException {
        final URIBuilder uriBuilder = new URIBuilder(baseUri);
        final List<Expandos> expands = Stream.of(DEFAULT_EXPANDS, expand).collect(ArrayList::new, List::addAll, List::addAll);
        //        Lists.concat(DEFAULT_EXPANDS, expand);
        uriBuilder.appendPath("issue").appendPath(issueKey).addParameter("expand",
                StreamSupport.stream(expands.spliterator(), false).map(EXPANDO_TO_PARAM).collect(Collectors.joining(",")));
        return getAndParse(uriBuilder.build(), issueParser);
    }

    @Override
    public Promise<Void> deleteIssue(String issueKey, boolean deleteSubtasks) throws URISyntaxException {
        return delete(new URIBuilder(baseUri).appendPath("issue").appendPath(issueKey).addParameter("deleteSubtasks", String.valueOf(deleteSubtasks)).build());
    }

    @Override
    public Promise<Watchers> getWatchers(final URI watchersUri) {
        return getAndParse(watchersUri, watchersParser);
    }

    @Override
    public Promise<Votes> getVotes(final URI votesUri) {
        return getAndParse(votesUri, votesJsonParser);
    }

    @Override
    public Promise<List<Transition>> getTransitions(final URI transitionsUri) {
        return callAndParse(client().newRequest(transitionsUri).get(), (ResponseHandler<List<Transition>>) response -> {
            final JSONObject jsonObject = new JSONObject(response.getEntity());
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
                    } catch (NumberFormatException e) {
                        throw new RestClientException("Transition id should be an integer, but found [" + key + "]", e);
                    }
                }
                return transitions;
            }
        });
    }

    @Override
    public Promise<List<Transition>> getTransitions(final Issue issue) throws URISyntaxException {
        if (issue.getTransitionsUri() != null) {
            return getTransitions(issue.getTransitionsUri());
        } else {
            final URIBuilder transitionsUri = new URIBuilder(issue.getSelf());
            return getTransitions(transitionsUri.appendPath("transitions").addParameter("expand", "transitions.fields").build());
        }
    }

    @Override
    public Promise<Void> transition(final URI transitionsUri, final TransitionInput transitionInput) throws URISyntaxException {
        final int buildNumber = getVersionInfo().getBuildNumber();
        try {
            JSONObject jsonObject = new JSONObject();
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
            if (fieldsJs.keys().hasNext()) {
                jsonObject.put("fields", fieldsJs);
            }
            return post(transitionsUri, jsonObject);
        } catch (JSONException ex) {
            throw new RestClientException(ex);
        }
    }

    @Override
    public Promise<Void> transition(final Issue issue, final TransitionInput transitionInput) throws URISyntaxException {
        if (issue.getTransitionsUri() != null) {
            return transition(issue.getTransitionsUri(), transitionInput);
        } else {
            final URIBuilder uriBuilder = new URIBuilder(issue.getSelf());
            uriBuilder.appendPath("transitions");
            return transition(uriBuilder.build(), transitionInput);
        }
    }

    @Override
    public Promise<Void> vote(final URI votesUri) {
        return post(votesUri);
    }

    @Override
    public Promise<Void> unvote(final URI votesUri) {
        return delete(votesUri);
    }

    @Override
    public Promise<Void> watch(final URI watchersUri) {
        return post(watchersUri);
    }

    @Override
    public Promise<Void> unwatch(final URI watchersUri) throws URISyntaxException {
        return removeWatcher(watchersUri, getLoggedUsername());
    }

    @Override
    public Promise<Void> addWatcher(final URI watchersUri, final String username) {
        return post(watchersUri, JSONObject.quote(username));
    }

    @Override
    public Promise<Void> removeWatcher(final URI watchersUri, final String username) throws URISyntaxException {
        final URIBuilder uriBuilder = new URIBuilder(watchersUri);
        if (getVersionInfo().getBuildNumber() >= ServerVersionConstants.BN_JIRA_4_4) {
            uriBuilder.addParameter("username", username);
        } else {
            uriBuilder.appendPath(username).build();
        }
        return delete(uriBuilder.build());
    }

    @Override
    public Promise<Void> linkIssue(final LinkIssuesInput linkIssuesInput) throws URISyntaxException {
        final URI uri = new URIBuilder(baseUri).appendPath("issueLink").build();
        return post(uri, linkIssuesInput, new LinkIssuesInputGenerator(getVersionInfo()));
    }

    @Override
    public Promise<Void> addAttachment(final URI attachmentsUri, final InputStream inputStream, final String filename) {
        final MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create().setLaxMode().setCharset(UTF_8).addBinaryBody(FILE_BODY_TYPE, inputStream,
                DEFAULT_BINARY, filename);
        return postAttachments(attachmentsUri, entityBuilder);
    }

    @Override
    public Promise<Void> addAttachments(final URI attachmentsUri, final AttachmentInput... attachments) {
        final MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create().setLaxMode().setCharset(UTF_8);
        for (final AttachmentInput attachmentInput : attachments) {
            entityBuilder.addBinaryBody(FILE_BODY_TYPE, attachmentInput.getInputStream(), DEFAULT_BINARY, attachmentInput.getFilename());
        }
        return postAttachments(attachmentsUri, entityBuilder);
    }

    @Override
    public Promise<Void> addAttachments(final URI attachmentsUri, final File... files) {
        final MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create().setLaxMode().setCharset(UTF_8);
        for (final File file : files) {
            entityBuilder.addBinaryBody(FILE_BODY_TYPE, file);
        }
        return postAttachments(attachmentsUri, entityBuilder);
    }

    @Override
    public Promise<Void> addComment(final URI commentsUri, final Comment comment) throws URISyntaxException {
        return post(commentsUri, comment, new CommentJsonGenerator(getVersionInfo()));
    }

    @Override
    public Promise<InputStream> getAttachment(URI attachmentUri) {
        return callAndParse(client().newRequest(attachmentUri).get(), Message::getEntityStream);
    }

    @Override
    public Promise<Void> addWorklog(URI worklogUri, WorklogInput worklogInput) throws URISyntaxException {
        final URIBuilder uriBuilder = new URIBuilder(worklogUri).addParameter("adjustEstimate", worklogInput.getAdjustEstimate().restValue);

        switch (worklogInput.getAdjustEstimate()) {
        case NEW:
            uriBuilder.addParameter("newEstimate", nullToEmpty(worklogInput.getAdjustEstimateValue()));
            break;
        case MANUAL:
            uriBuilder.addParameter("reduceBy", nullToEmpty(worklogInput.getAdjustEstimateValue()));
            break;
        case AUTO: // FIXME What should we do?
            break;
        case LEAVE: // FIXME What should we do?
            break;
        default: // FIXME What should we do?
            break;
        }

        return post(uriBuilder.build(), worklogInput, new WorklogInputJsonGenerator());
    }

    private static String nullToEmpty(final String value) {
        return value == null ? "" : value;
    }

    private void addPagingParameters(URIBuilder uriBuilder, @Nullable Long startAt, @Nullable Integer maxResults) {
        if (startAt != null) {
            uriBuilder.addParameter("startAt", String.valueOf(startAt));
        }
        if (maxResults != null) {
            uriBuilder.addParameter("maxResults", String.valueOf(maxResults));
        }
    }

    private Promise<Void> postAttachments(final URI attachmentsUri, final MultipartEntityBuilder multipartEntityBuilder) {
        final ResponsePromise responsePromise = client().newRequest(attachmentsUri).setEntity(new MultiPartEntityBuilder(multipartEntityBuilder.build()))
                .setHeader("X-Atlassian-Token", "nocheck").post();
        return call(responsePromise);
    }

    private String getLoggedUsername() throws RestClientException, URISyntaxException {
        return sessionRestClient.getCurrentSession().claim().getUsername();
    }
}
