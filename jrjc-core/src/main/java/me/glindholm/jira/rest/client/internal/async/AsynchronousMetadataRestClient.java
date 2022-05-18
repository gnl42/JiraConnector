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

import com.atlassian.httpclient.api.HttpClient;

import io.atlassian.util.concurrent.Promise;
import me.glindholm.jira.rest.client.api.MetadataRestClient;
import me.glindholm.jira.rest.client.api.domain.Field;
import me.glindholm.jira.rest.client.api.domain.IssueType;
import me.glindholm.jira.rest.client.api.domain.IssuelinksType;
import me.glindholm.jira.rest.client.api.domain.Priority;
import me.glindholm.jira.rest.client.api.domain.Resolution;
import me.glindholm.jira.rest.client.api.domain.ServerInfo;
import me.glindholm.jira.rest.client.api.domain.Status;
import me.glindholm.jira.rest.client.internal.json.FieldJsonParser;
import me.glindholm.jira.rest.client.internal.json.GenericJsonArrayParser;
import me.glindholm.jira.rest.client.internal.json.IssueLinkTypesJsonParser;
import me.glindholm.jira.rest.client.internal.json.IssueTypeJsonParser;
import me.glindholm.jira.rest.client.internal.json.JsonArrayParser;
import me.glindholm.jira.rest.client.internal.json.PriorityJsonParser;
import me.glindholm.jira.rest.client.internal.json.ResolutionJsonParser;
import me.glindholm.jira.rest.client.internal.json.ServerInfoJsonParser;
import me.glindholm.jira.rest.client.internal.json.StatusJsonParser;

/**
 * Asynchronous implementation of MetadataRestClient.
 *
 * @since v2.0
 */
public class AsynchronousMetadataRestClient extends AbstractAsynchronousRestClient implements MetadataRestClient {

    private static final String SERVER_INFO_RESOURCE = "/serverInfo";
    private final ServerInfoJsonParser serverInfoJsonParser = new ServerInfoJsonParser();
    private final IssueTypeJsonParser issueTypeJsonParser = new IssueTypeJsonParser();
    private final GenericJsonArrayParser<IssueType> issueTypesJsonParser = GenericJsonArrayParser.create(issueTypeJsonParser);
    private final StatusJsonParser statusJsonParser = new StatusJsonParser();
    private final GenericJsonArrayParser<Status> statusesJsonParser = GenericJsonArrayParser.create(statusJsonParser);
    private final PriorityJsonParser priorityJsonParser = new PriorityJsonParser();
    private final GenericJsonArrayParser<Priority> prioritiesJsonParser = GenericJsonArrayParser.create(priorityJsonParser);
    private final ResolutionJsonParser resolutionJsonParser = new ResolutionJsonParser();
    private final GenericJsonArrayParser<Resolution> resolutionsJsonParser = GenericJsonArrayParser.create(resolutionJsonParser);
    private final IssueLinkTypesJsonParser issueLinkTypesJsonParser = new IssueLinkTypesJsonParser();
    private final JsonArrayParser<Iterable<Field>> fieldsJsonParser = FieldJsonParser.createFieldsArrayParser();
    private final URI baseUri;

    public AsynchronousMetadataRestClient(final URI baseUri, HttpClient httpClient) {
        super(httpClient);
        this.baseUri = baseUri;

    }

    @Override
    public Promise<IssueType> getIssueType(final URI uri) {
        return getAndParse(uri, issueTypeJsonParser);
    }

    @Override
    public Promise<Iterable<IssueType>> getIssueTypes() throws URISyntaxException {
        final URI uri = new URIBuilder(baseUri).appendPath("issuetype").build();
        return getAndParse(uri, issueTypesJsonParser);
    }

    @Override
    public Promise<Iterable<IssuelinksType>> getIssueLinkTypes() throws URISyntaxException {
        final URI uri = new URIBuilder(baseUri).appendPath("issueLinkType").build();
        return getAndParse(uri, issueLinkTypesJsonParser);
    }

    @Override
    public Promise<Status> getStatus(URI uri) {
        return getAndParse(uri, statusJsonParser);
    }

    @Override
    public Promise<Iterable<Status>> getStatuses() throws URISyntaxException {
        final URI uri = new URIBuilder(baseUri).appendPath("status").build();
        return getAndParse(uri, statusesJsonParser);
    }

    @Override
    public Promise<Priority> getPriority(URI uri) {
        return getAndParse(uri, priorityJsonParser);
    }

    @Override
    public Promise<Iterable<Priority>> getPriorities() throws URISyntaxException {
        final URI uri = new URIBuilder(baseUri).appendPath("priority").build();
        return getAndParse(uri, prioritiesJsonParser);
    }

    @Override
    public Promise<Resolution> getResolution(URI uri) {
        return getAndParse(uri, resolutionJsonParser);
    }

    @Override
    public Promise<Iterable<Resolution>> getResolutions() throws URISyntaxException {
        final URI uri = new URIBuilder(baseUri).appendPath("resolution").build();
        return getAndParse(uri, resolutionsJsonParser);
    }

    @Override
    public Promise<ServerInfo> getServerInfo() throws URISyntaxException {
        final URI serverInfoUri = new URIBuilder(baseUri).appendPath(SERVER_INFO_RESOURCE).build();
        return getAndParse(serverInfoUri, serverInfoJsonParser);
    }

    @Override
    public Promise<Iterable<Field>> getFields() throws URISyntaxException {
        final URI uri = new URIBuilder(baseUri).appendPath("field").build();
        return getAndParse(uri, fieldsJsonParser);
    }
}
