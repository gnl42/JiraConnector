package com.atlassian.jira.rest.client.internal.async;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.RestClientException;
import com.atlassian.jira.rest.client.SessionRestClient;
import com.atlassian.jira.rest.client.domain.Session;
import com.atlassian.jira.rest.client.internal.json.SessionJsonParser;
import com.atlassian.util.concurrent.Promise;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * TODO: Document this class / interface here
 *
 * @since v6.0
 */
public class AsynchronousSessionRestClient extends AbstractAsynchronousRestClient implements SessionRestClient {

    private final SessionJsonParser sessionJsonParser = new SessionJsonParser();
    private final URI serverUri;

    public AsynchronousSessionRestClient(final URI serverUri, final HttpClient client) {
        super(client);
        this.serverUri = serverUri;
    }

    @Override
    public Promise<Session> getCurrentSession() throws RestClientException {
        return getAndParse(UriBuilder.fromUri(serverUri).path("rest/auth/latest/session").build(), sessionJsonParser);
    }

}
