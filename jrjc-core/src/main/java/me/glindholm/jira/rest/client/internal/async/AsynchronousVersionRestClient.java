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

import org.apache.hc.core5.net.URIBuilder;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jdt.annotation.Nullable;

import com.atlassian.httpclient.api.HttpClient;

import io.atlassian.util.concurrent.Promise;
import me.glindholm.jira.rest.client.api.VersionRestClient;
import me.glindholm.jira.rest.client.api.domain.Version;
import me.glindholm.jira.rest.client.api.domain.VersionRelatedIssuesCount;
import me.glindholm.jira.rest.client.api.domain.input.VersionInput;
import me.glindholm.jira.rest.client.api.domain.input.VersionPosition;
import me.glindholm.jira.rest.client.internal.json.JsonObjectParser;
import me.glindholm.jira.rest.client.internal.json.VersionJsonParser;
import me.glindholm.jira.rest.client.internal.json.VersionRelatedIssueCountJsonParser;
import me.glindholm.jira.rest.client.internal.json.gen.JsonGenerator;
import me.glindholm.jira.rest.client.internal.json.gen.VersionInputJsonGenerator;
import me.glindholm.jira.rest.client.internal.json.gen.VersionPositionInputGenerator;

/**
 * Asynchronous implementation of VersionRestClient.
 *
 * @since v2.0
 */
public class AsynchronousVersionRestClient extends AbstractAsynchronousRestClient implements VersionRestClient {

    private final URI versionRootUri;

    public AsynchronousVersionRestClient(final URI baseUri, final HttpClient client) throws URISyntaxException {
        super(client);
        versionRootUri = new URIBuilder(baseUri).appendPath("version").build();
    }

    @Override
    public Promise<Version> getVersion(final URI versionUri) {
        return getAndParse(versionUri, new VersionJsonParser());
    }

    @Override
    public Promise<Version> createVersion(final VersionInput versionInput) {
        return postAndParse(versionRootUri, versionInput, new VersionInputJsonGenerator(), new VersionJsonParser());
    }

    @Override
    public Promise<Version> updateVersion(final URI versionUri, final VersionInput versionInput) {
        return putAndParse(versionUri, versionInput, new VersionInputJsonGenerator(), new VersionJsonParser());
    }

    @Override
    public Promise<Void> removeVersion(final URI versionUri, final @Nullable URI moveFixIssuesToVersionUri, final @Nullable URI moveAffectedIssuesToVersionUri)
            throws URISyntaxException {
        final URIBuilder uriBuilder = new URIBuilder(versionUri);
        if (moveFixIssuesToVersionUri != null) {
            uriBuilder.addParameter("moveFixIssuesTo", String.valueOf(moveFixIssuesToVersionUri));
        }
        if (moveAffectedIssuesToVersionUri != null) {
            uriBuilder.addParameter("moveAffectedIssuesTo", String.valueOf(moveAffectedIssuesToVersionUri));
        }
        return delete(uriBuilder.build());
    }

    @Override
    public Promise<VersionRelatedIssuesCount> getVersionRelatedIssuesCount(final URI versionUri) throws URISyntaxException {
        final URI relatedIssueCountsUri = new URIBuilder(versionUri).appendPath("relatedIssueCounts").build();
        return getAndParse(relatedIssueCountsUri, new VersionRelatedIssueCountJsonParser());
    }

    @Override
    public Promise<Integer> getNumUnresolvedIssues(final URI versionUri) throws URISyntaxException {
        final URI unresolvedIssueCountUri = new URIBuilder(versionUri).appendPath("unresolvedIssueCount").build();
        return getAndParse(unresolvedIssueCountUri, new JsonObjectParser<Integer>() {
            @Override
            public Integer parse(final JSONObject json) throws JSONException {
                return json.getInt("issuesUnresolvedCount");
            }
        });
    }

    @Override
    public Promise<Version> moveVersionAfter(final URI versionUri, final URI afterVersionUri) throws URISyntaxException {
        final URI moveUri = getMoveVersionUri(versionUri);

        return postAndParse(moveUri, afterVersionUri, new JsonGenerator<URI>() {
            @Override
            public JSONObject generate(final URI uri) throws JSONException {
                final JSONObject res = new JSONObject();
                res.put("after", uri);
                return res;
            }
        }, new VersionJsonParser());
    }

    @Override
    public Promise<Version> moveVersion(final URI versionUri, final VersionPosition versionPosition) throws URISyntaxException {
        final URI moveUri = getMoveVersionUri(versionUri);
        return postAndParse(moveUri, versionPosition, new VersionPositionInputGenerator(), new VersionJsonParser());
    }

    private URI getMoveVersionUri(final URI versionUri) throws URISyntaxException {
        return new URIBuilder(versionUri).appendPath("move").build();
    }

}
