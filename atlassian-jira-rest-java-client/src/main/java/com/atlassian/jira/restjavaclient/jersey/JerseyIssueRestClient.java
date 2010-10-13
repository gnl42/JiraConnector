/*
 * Copyright (C) 2010 Atlassian
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

package com.atlassian.jira.restjavaclient.jersey;

import com.atlassian.jira.restjavaclient.IssueRestClient;
import com.atlassian.jira.restjavaclient.ProgressMonitor;
import com.atlassian.jira.restjavaclient.RestClientException;
import com.atlassian.jira.restjavaclient.SessionRestClient;
import com.atlassian.jira.restjavaclient.domain.FieldInput;
import com.atlassian.jira.restjavaclient.domain.Issue;
import com.atlassian.jira.restjavaclient.domain.Session;
import com.atlassian.jira.restjavaclient.domain.Transition;
import com.atlassian.jira.restjavaclient.domain.TransitionInput;
import com.atlassian.jira.restjavaclient.domain.Votes;
import com.atlassian.jira.restjavaclient.domain.Watchers;
import com.atlassian.jira.restjavaclient.json.IssueJsonParser;
import com.atlassian.jira.restjavaclient.json.JsonParser;
import com.atlassian.jira.restjavaclient.json.TransitionJsonParser;
import com.atlassian.jira.restjavaclient.json.VotesJsonParser;
import com.atlassian.jira.restjavaclient.json.WatchersJsonParserBuilder;
import com.atlassian.jira.restjavaclient.json.gen.CommentJsonGenerator;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.apache.ApacheHttpClient;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Callable;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class JerseyIssueRestClient extends AbstractJerseyRestClient implements IssueRestClient {

	private final SessionRestClient sessionRestClient;

	private final IssueJsonParser issueParser = new IssueJsonParser();
	private final JsonParser<Watchers> watchersParser = WatchersJsonParserBuilder.createWatchersParser();
	private final TransitionJsonParser transitionJsonParser = new TransitionJsonParser();
	private final CommentJsonGenerator commentJsonGenerator = new CommentJsonGenerator();
	private final VotesJsonParser votesJsonParser = new VotesJsonParser();

	public JerseyIssueRestClient(URI baseUri, ApacheHttpClient client, SessionRestClient sessionRestClient) {
		super(baseUri, client);
		this.sessionRestClient = sessionRestClient;
	}

	@Override
	public Watchers getWatchers(Issue issue, ProgressMonitor progressMonitor) {
		return getAndParse(issue.getWatchers().getSelf(), watchersParser, progressMonitor);
	}


	@Override
	public Votes getVotes(Issue issue, ProgressMonitor progressMonitor) {
		return getAndParse(issue.getVotes().getSelf(), votesJsonParser, progressMonitor);
	}

	@Override
	public Issue getIssue(final String issueKey, ProgressMonitor progressMonitor) {
		final UriBuilder uriBuilder = UriBuilder.fromUri(baseUri);
		uriBuilder.path("issue").path(issueKey);
//		final String expandoString = getExpandoString(args);
//		if (expandoString != null) {
//			uriBuilder.queryParam("expand", expandoString);
//		}
		return invoke(new Callable<Issue>() {
			@Override
			public Issue call() throws Exception {
				final WebResource issueResource = client.resource(uriBuilder.build());
				final JSONObject s = issueResource.get(JSONObject.class);
				return issueParser.parse(s);
			}
		});
	}

	@Override
	public Iterable<Transition> getTransitions(final Issue issue, ProgressMonitor progressMonitor) {
		return invoke(new Callable<Iterable<Transition>>() {
			@Override
			public Iterable<Transition> call() throws Exception {
				final WebResource transitionsResource = client.resource(issue.getTransitionsUri());
				final JSONObject jsonObject = transitionsResource.get(JSONObject.class);
				final Collection<Transition> transitions = new ArrayList<Transition>(jsonObject.length());
				@SuppressWarnings("unchecked")
				final Iterator<String> iterator = jsonObject.keys();
				while (iterator.hasNext()) {
					final String key = iterator.next();
					try {
						final int id = Integer.parseInt(key);
						final Transition transition = transitionJsonParser.parse(jsonObject.getJSONObject(key), id);
						transitions.add(transition);
					} catch (JSONException e) {
						throw new RestClientException(e);
					} catch (NumberFormatException e) {
						throw new RestClientException("Transition id should be an integer, but found [" + key + "]", e);
					}
				}
				return transitions;
			}
		});

	}

	@Override
	public void transition(final Issue issue, final TransitionInput transitionInput, ProgressMonitor progressMonitor) {
		invoke(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				final URI uri = issue.getTransitionsUri();
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("transition", transitionInput.getId());
				if (transitionInput.getComment() != null) {
					jsonObject.put("comment", commentJsonGenerator.generate(transitionInput.getComment()));
				}
				JSONObject fieldsJs = new JSONObject();
				final Iterable<FieldInput> fields = transitionInput.getFields();
				if (fields.iterator().hasNext()) {
					for (FieldInput fieldInput : fields) {
						fieldsJs.put(fieldInput.getId(), fieldInput.getValue());
					}
				}
				if (fieldsJs.keys().hasNext()) {
					jsonObject.put("fields", fieldsJs);
				}
				final WebResource issueResource = client.resource(uri);
				issueResource.post(jsonObject);
				return null;
			}
		});
	}


	@Override
	public void vote(final Issue issue, ProgressMonitor progressMonitor) {
		invoke(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				final WebResource votesResource = client.resource(getVotesUri(issue));
				votesResource.post();
				return null;
			}
		});
	}

	private URI getVotesUri(Issue issue) {
		return UriBuilder.fromUri(issue.getSelf()).path("votes").build();
	}

	@Override
	public void unvote(final Issue issue, ProgressMonitor progressMonitor) {
		invoke(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				final WebResource votesResource = client.resource(getVotesUri(issue));
				votesResource.delete();
				return null;
			}
		});

	}

	@Override
	public void addWatcher(final Issue issue, @Nullable final String username, ProgressMonitor progressMonitor) {
		invoke(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				final WebResource.Builder builder = client.resource(issue.getWatchers().getSelf()).type(MediaType.APPLICATION_JSON_TYPE);
				if (username != null) {
					builder.post(JSONObject.quote(username));
				} else {
					builder.post();
				}
				return null;
			}
		});

	}

	private String getLoggedUsername(ProgressMonitor progressMonitor) {
		final Session session = sessionRestClient.getCurrentSession(progressMonitor);
		return session.getUsername();
	}

	@Override
	public void removeWatcher(final Issue issue, final String username, ProgressMonitor progressMonitor) {
		invoke(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				final URI uri = UriBuilder.fromUri(issue.getWatchers().getSelf()).path(username).build();
				final WebResource watchersResource = client.resource(uri);
				watchersResource.delete();
				return null;
			}
		});

	}


	@Override
	public void watch(final Issue issue, ProgressMonitor progressMonitor) {
		addWatcher(issue, null, progressMonitor);
	}

	@Override
	public void unwatch(final Issue issue, ProgressMonitor progressMonitor) {
		removeWatcher(issue, getLoggedUsername(progressMonitor), progressMonitor);
	}

}
