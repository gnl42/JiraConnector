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
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jdt.annotation.Nullable;

import com.atlassian.httpclient.api.HttpClient;

import io.atlassian.util.concurrent.Promise;
import me.glindholm.jira.rest.client.api.ComponentRestClient;
import me.glindholm.jira.rest.client.api.domain.Component;
import me.glindholm.jira.rest.client.api.domain.input.ComponentInput;
import me.glindholm.jira.rest.client.internal.domain.input.ComponentInputWithProjectKey;
import me.glindholm.jira.rest.client.internal.json.ComponentJsonParser;
import me.glindholm.jira.rest.client.internal.json.gen.ComponentInputWithProjectKeyJsonGenerator;

/**
 * Asynchronous implementation of ComponentRestClient.
 *
 * @since v2.0
 */
public class AsynchronousComponentRestClient extends AbstractAsynchronousRestClient implements ComponentRestClient {

    private final ComponentJsonParser componentJsonParser = new ComponentJsonParser();
    private final URI componentUri;

    public AsynchronousComponentRestClient(final URI baseUri, final HttpClient client) throws URISyntaxException {
        super(client);
        componentUri = new URIBuilder(baseUri).appendPath("component").build();
    }

    @Override
    public Promise<Component> getComponent(final URI componentUri) {
        return getAndParse(componentUri, componentJsonParser);
    }

    @Override
    public Promise<Component> createComponent(final String projectKey, final ComponentInput componentInput) {
        final ComponentInputWithProjectKey helper = new ComponentInputWithProjectKey(projectKey, componentInput);
        return postAndParse(componentUri, helper, new ComponentInputWithProjectKeyJsonGenerator(), componentJsonParser);
    }

    @Override
    public Promise<Component> updateComponent(final URI componentUri, final ComponentInput componentInput) {
        final ComponentInputWithProjectKey helper = new ComponentInputWithProjectKey(null, componentInput);
        return putAndParse(componentUri, helper, new ComponentInputWithProjectKeyJsonGenerator(), componentJsonParser);
    }

    @Override
    public Promise<Void> removeComponent(final URI componentUri, @Nullable final URI moveIssueToComponentUri) throws URISyntaxException {
        final URIBuilder uriBuilder = new URIBuilder(componentUri);
        if (moveIssueToComponentUri != null) {
            uriBuilder.addParameter("moveIssuesTo", String.valueOf(moveIssueToComponentUri));
        }
        return delete(uriBuilder.build());
    }

    @Override
    public Promise<Integer> getComponentRelatedIssuesCount(final URI componentUri) throws URISyntaxException {
        final URI relatedIssueCountsUri = new URIBuilder(componentUri).appendPath("relatedIssueCounts").build();
        return getAndParse(relatedIssueCountsUri, json -> ((JSONObject) json).getInt("issueCount"));
    }
}
