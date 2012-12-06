package com.atlassian.jira.rest.client.internal.async;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.MetadataRestClient;
import com.atlassian.jira.rest.client.domain.*;
import com.atlassian.jira.rest.client.internal.json.*;
import com.atlassian.util.concurrent.Promise;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * TODO: Document this class / interface here
 *
 * @since v6.0
 */
public class AsynchronousMetadataRestClient extends AbstractAsynchronousRestClient implements MetadataRestClient {

    private static final String SERVER_INFO_RESOURCE = "/serverInfo";
    private final ServerInfoJsonParser serverInfoJsonParser = new ServerInfoJsonParser();
    private final IssueTypeJsonParser issueTypeJsonParser = new IssueTypeJsonParser();
    private final GenericJsonArrayParser<IssueType> issueTypesJsonParser = GenericJsonArrayParser.create(issueTypeJsonParser);
    private final StatusJsonParser statusJsonParser = new StatusJsonParser();
    private final PriorityJsonParser priorityJsonParser = new PriorityJsonParser();
    private final GenericJsonArrayParser<Priority> prioritiesJsonParser = GenericJsonArrayParser.create(priorityJsonParser);
    private final ResolutionJsonParser resolutionJsonParser = new ResolutionJsonParser();
    private final GenericJsonArrayParser<Resolution> resolutionsJsonParser = GenericJsonArrayParser.create(resolutionJsonParser);
    private final IssueLinkTypesJsonParser issueLinkTypesJsonParser = new IssueLinkTypesJsonParser();
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
    public Promise<Iterable<IssueType>> getIssueTypes() {
        final URI uri = UriBuilder.fromUri(baseUri).path("issuetype").build();
        return getAndParse(uri, issueTypesJsonParser);
    }

    @Override
    public Promise<Iterable<IssuelinksType>> getIssueLinkTypes() {
        final URI uri = UriBuilder.fromUri(baseUri).path("issueLinkType").build();
        return getAndParse(uri, issueLinkTypesJsonParser);
    }

    @Override
    public Promise<Status> getStatus(URI uri) {
        return getAndParse(uri, statusJsonParser);
    }

    @Override
    public Promise<Priority> getPriority(URI uri) {
        return getAndParse(uri, priorityJsonParser);
    }

    @Override
    public Promise<Iterable<Priority>> getPriorities() {
        final URI uri = UriBuilder.fromUri(baseUri).path("priority").build();
        return getAndParse(uri, prioritiesJsonParser);
    }

    @Override
    public Promise<Resolution> getResolution(URI uri) {
        return getAndParse(uri, resolutionJsonParser);
    }

    @Override
    public Promise<Iterable<Resolution>> getResolutions() {
        final URI uri = UriBuilder.fromUri(baseUri).path("resolution").build();
        return getAndParse(uri, resolutionsJsonParser);
    }

    @Override
    public Promise<ServerInfo> getServerInfo() {
        final URI serverInfoUri = UriBuilder.fromUri(baseUri).path(SERVER_INFO_RESOURCE).build();
        return getAndParse(serverInfoUri, serverInfoJsonParser);
    }
}
