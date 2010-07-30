package com.atlassian.jira.restjavaclient;

import com.atlassian.jira.restjavaclient.domain.Issue;
import com.atlassian.jira.restjavaclient.domain.User;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class JerseyJiraRestClient implements JiraRestClient {

    private ApacheHttpClient client;
    private final URI baseUri;

    public JerseyJiraRestClient(URI serverUri) {
        this.baseUri = UriBuilder.fromUri(serverUri).path("/rest/api/latest").build();
        DefaultApacheHttpClientConfig config = new DefaultApacheHttpClientConfig();
        config.getState().setCredentials(null, null, -1, "admin", "admin");
        // @todo check with Justus why 404 is returned instead of 401 when no credentials are provided automagically
        config.getProperties().put(ApacheHttpClientConfig.PROPERTY_PREEMPTIVE_AUTHENTICATION, true);
        client = ApacheHttpClient.create(config);

    }

    public void login() {
    }

    public Issue getIssue(IssueArgs args) {
        final WebResource issueResource = client.resource(baseUri).path("issue").path(args.getKey());
        final JSONObject s = issueResource.get(JSONObject.class);
        try {
            return new Issue(new URI(s.getString("self")), s.getString("key"));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public User getUser() {
        return null;
    }
}
