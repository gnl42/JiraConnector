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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.jdt.annotation.Nullable;

import me.glindholm.jira.rest.client.api.VersionRestClient;
import me.glindholm.jira.rest.client.api.domain.Version;
import me.glindholm.jira.rest.client.api.domain.VersionRelatedIssuesCount;
import me.glindholm.jira.rest.client.api.domain.input.VersionInput;
import me.glindholm.jira.rest.client.api.domain.input.VersionPosition;
import me.glindholm.jira.rest.client.internal.json.VersionJsonParser;
import me.glindholm.jira.rest.client.internal.json.VersionRelatedIssueCountJsonParser;
import me.glindholm.jira.rest.client.internal.json.gen.VersionInputJsonGenerator;
import me.glindholm.jira.rest.client.internal.json.gen.VersionPositionInputGenerator;
import me.glindholm.jira.rest.client.shim.jettison.json.JSONObject;

/**
 * Asynchronous implementation of VersionRestClient.
 *
 * @since v2.0
 */
public class AsynchronousVersionRestClient extends AbstractAsynchronousRestClient implements VersionRestClient {

    private final URI versionRootUri;

    public AsynchronousVersionRestClient(final URI baseUri, final DisposableHttpClient client) throws URISyntaxException {
        super(client);
        versionRootUri = new UriBuilder(baseUri).appendPath("version").build();
    }

    @Override
    public CompletableFuture<Version> getVersion(final URI versionUri) {
        return getAndParse(versionUri, new VersionJsonParser());
    }

    @Override
    public CompletableFuture<Version> createVersion(final VersionInput versionInput) {
        return postAndParse(versionRootUri, versionInput, new VersionInputJsonGenerator(), new VersionJsonParser());
    }

    @Override
    public CompletableFuture<Version> updateVersion(final URI versionUri, final VersionInput versionInput) {
        return putAndParse(versionUri, versionInput, new VersionInputJsonGenerator(), new VersionJsonParser());
    }

    @Override
    public CompletableFuture<Void> removeVersion(final URI versionUri, final @Nullable URI moveFixIssuesToVersionUri, final @Nullable URI moveAffectedIssuesToVersionUri)
            throws URISyntaxException {
        final UriBuilder uriBuilder = new UriBuilder(versionUri);
        if (moveFixIssuesToVersionUri != null) {
            uriBuilder.addParameter("moveFixIssuesTo", String.valueOf(moveFixIssuesToVersionUri));
        }
        if (moveAffectedIssuesToVersionUri != null) {
            uriBuilder.addParameter("moveAffectedIssuesTo", String.valueOf(moveAffectedIssuesToVersionUri));
        }
        return delete(uriBuilder.build());
    }

    @Override
    public CompletableFuture<VersionRelatedIssuesCount> getVersionRelatedIssuesCount(final URI versionUri) throws URISyntaxException {
        final URI relatedIssueCountsUri = new UriBuilder(versionUri).appendPath("relatedIssueCounts").build();
        return getAndParse(relatedIssueCountsUri, new VersionRelatedIssueCountJsonParser());
    }

    @Override
    public CompletableFuture<Integer> getNumUnresolvedIssues(final URI versionUri) throws URISyntaxException {
        final URI unresolvedIssueCountUri = new UriBuilder(versionUri).appendPath("unresolvedIssueCount").build();
        return getAndParse(unresolvedIssueCountUri, json -> ((JSONObject) json).getInt("issuesUnresolvedCount"));
    }

    @Override
    public CompletableFuture<Version> moveVersionAfter(final URI versionUri, final URI afterVersionUri) throws URISyntaxException {
        final URI moveUri = getMoveVersionUri(versionUri);
        return postAndParse(moveUri, afterVersionUri, uri -> {
            final JSONObject res = new JSONObject();
            res.put("after", uri);
            return res;
        }, new VersionJsonParser());
    }

    @Override
    public CompletableFuture<Version> moveVersion(final URI versionUri, final VersionPosition versionPosition) throws URISyntaxException {
        final URI moveUri = getMoveVersionUri(versionUri);
        return postAndParse(moveUri, versionPosition, new VersionPositionInputGenerator(), new VersionJsonParser());
    }

    private URI getMoveVersionUri(final URI versionUri) throws URISyntaxException {
        return new UriBuilder(versionUri).appendPath("move").build();
    }
}