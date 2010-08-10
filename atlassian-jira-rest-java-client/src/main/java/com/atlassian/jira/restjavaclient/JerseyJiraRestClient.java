package com.atlassian.jira.restjavaclient;

import com.atlassian.jira.restjavaclient.domain.Issue;
import com.atlassian.jira.restjavaclient.domain.User;
import com.atlassian.jira.restjavaclient.json.IssueJsonParser;
import com.google.common.base.Joiner;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.annotation.Nullable;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class JerseyJiraRestClient implements JiraRestClient {

	private ApacheHttpClient client;
	private final URI baseUri;
	private IssueJsonParser issueParser = new IssueJsonParser();

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


	@Nullable
	String getExpandoString(IssueArgs args) {
		Collection<String> expandos = new ArrayList<String>();
		expandos.add("fields"); // this IMO always vital;
		StringBuilder sb = new StringBuilder();
		if (args.withAttachments()) {
			expandos.add("attachments");
		}
		if (args.withComments()) {
			expandos.add("comments");
		}
		if (args.withWorklogs()) {
			expandos.add("worklogs");
		}
		if (args.withWatchers()) {
			expandos.add("watchers.list");
		}
		if (expandos.size() == 0) {
			return null;
		}
		return Joiner.on(',').join(expandos);
	}



	public Issue getIssue(final IssueArgs args, ProgressMonitor progressMonitor) {
		final UriBuilder uriBuilder = UriBuilder.fromUri(baseUri);
		uriBuilder.path("issue").path(args.getKey());
		final String expandoString = getExpandoString(args);
		if (expandoString != null) {
			uriBuilder.queryParam("expand", expandoString);
		}

		final WebResource issueResource = client.resource(uriBuilder.build());

		final JSONObject s = issueResource.get(JSONObject.class);

		try {
//            System.out.println(s.toString(4));
			return issueParser.parseIssue(args, s);
		} catch (JSONException e) {
			throw new RestClientException(e);
		}
	}




	public User getUser() {
		return null;
	}

}
