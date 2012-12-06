package com.atlassian.jira.rest.client.internal.async;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.UserRestClient;
import com.atlassian.jira.rest.client.domain.User;
import com.atlassian.jira.rest.client.internal.json.UserJsonParser;
import com.atlassian.util.concurrent.Promise;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * TODO: Document this class / interface here
 *
 * @since v6.0
 */
public class AsynchronousUserRestClient extends AbstractAsynchronousRestClient implements UserRestClient {

    private static final String USER_URI_PREFIX = "user";
    private final UserJsonParser userJsonParser = new UserJsonParser();

    private final URI baseUri;

    public AsynchronousUserRestClient(final URI baseUri, final HttpClient client) {
        super(client);
        this.baseUri = baseUri;
    }

    @Override
    public Promise<User> getUser(final String username) {
        final URI userUri = UriBuilder.fromUri(baseUri).path(USER_URI_PREFIX)
                .queryParam("username", username).queryParam("expand", "groups").build();
        return getUser(userUri);
    }

    @Override
    public Promise<User> getUser(final URI userUri) {
        return getAndParse(userUri, userJsonParser);
    }
}
