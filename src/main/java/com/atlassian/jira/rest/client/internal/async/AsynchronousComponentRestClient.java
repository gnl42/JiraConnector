package com.atlassian.jira.rest.client.internal.async;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.ComponentRestClient;
import com.atlassian.jira.rest.client.domain.Component;
import com.atlassian.jira.rest.client.domain.input.ComponentInput;
import com.atlassian.jira.rest.client.internal.domain.input.ComponentInputWithProjectKey;
import com.atlassian.jira.rest.client.internal.json.ComponentJsonParser;
import com.atlassian.jira.rest.client.internal.json.JsonObjectParser;
import com.atlassian.jira.rest.client.internal.json.gen.ComponentInputWithProjectKeyJsonGenerator;
import com.atlassian.util.concurrent.Promise;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.annotation.Nullable;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * TODO: Document this class / interface here
 *
 * @since v6.0
 */
public class AsynchronousComponentRestClient extends AbstractAsynchronousRestClient implements ComponentRestClient {

    private final ComponentJsonParser componentJsonParser = new ComponentJsonParser();
    private final URI componentUri;

    public AsynchronousComponentRestClient(final URI baseUri, final HttpClient client) {
        super(client);
        componentUri = UriBuilder.fromUri(baseUri).path("component").build();
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
    public Promise<Component> updateComponent(URI componentUri, ComponentInput componentInput) {
        final ComponentInputWithProjectKey helper = new ComponentInputWithProjectKey(null, componentInput);
        return putAndParse(componentUri, helper, new ComponentInputWithProjectKeyJsonGenerator(), componentJsonParser);
    }

    @Override
    public Promise<Void> removeComponent(URI componentUri, @Nullable URI moveIssueToComponentUri) {
        final UriBuilder uriBuilder = UriBuilder.fromUri(componentUri);
        if (moveIssueToComponentUri != null) {
            uriBuilder.queryParam("moveIssuesTo", moveIssueToComponentUri);
        }
        return delete(uriBuilder.build());
    }

    @Override
    public Promise<Integer> getComponentRelatedIssuesCount(URI componentUri) {
        final URI relatedIssueCountsUri = UriBuilder.fromUri(componentUri).path("relatedIssueCounts").build();
        return getAndParse(relatedIssueCountsUri, new JsonObjectParser<Integer>() {
            @Override
            public Integer parse(JSONObject json) throws JSONException {
                return json.getInt("issueCount");
            }
        });
    }
}
