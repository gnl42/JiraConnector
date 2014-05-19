package com.atlassian.jira.rest.client.internal.async;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.api.MyPermissionsRestClient;
import com.atlassian.jira.rest.client.api.domain.Permissions;
import com.atlassian.jira.rest.client.internal.json.PermissionsJsonParser;
import com.atlassian.util.concurrent.Promise;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class AsynchronousMyPermissionsRestClient extends AbstractAsynchronousRestClient implements MyPermissionsRestClient {
    private static final String URI_PREFIX = "mypermissions";
    private final URI baseUri;
    private final PermissionsJsonParser permissionsJsonParser = new PermissionsJsonParser();

    protected AsynchronousMyPermissionsRestClient(URI baseUri, HttpClient client) {
        super(client);
        this.baseUri = baseUri;
    }

    @Override
    public Promise<Permissions> getMyPermissions(Object...issueOrProjectKeyOrIds) {
        UriBuilder uriBuilder = UriBuilder.fromUri(baseUri).path(URI_PREFIX);
        for (Object issueOrProjectKeyOrId : issueOrProjectKeyOrIds) {
            uriBuilder.queryParam(issueOrProjectKeyOrId.toString(), "");
        }
        return getAndParse(uriBuilder.build(), permissionsJsonParser);
    }
}
