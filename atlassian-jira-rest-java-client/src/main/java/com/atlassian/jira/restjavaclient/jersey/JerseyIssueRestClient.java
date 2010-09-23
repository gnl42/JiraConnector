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

import com.atlassian.jira.restjavaclient.IssueArgs;
import com.atlassian.jira.restjavaclient.IssueRestClient;
import com.atlassian.jira.restjavaclient.ProgressMonitor;
import com.atlassian.jira.restjavaclient.RestClientException;
import com.atlassian.jira.restjavaclient.domain.FieldInput;
import com.atlassian.jira.restjavaclient.domain.Issue;
import com.atlassian.jira.restjavaclient.domain.Transition;
import com.atlassian.jira.restjavaclient.domain.TransitionInput;
import com.atlassian.jira.restjavaclient.domain.Watchers;
import com.atlassian.jira.restjavaclient.json.IssueJsonParser;
import com.atlassian.jira.restjavaclient.json.JsonParseUtil;
import com.atlassian.jira.restjavaclient.json.JsonParser;
import com.atlassian.jira.restjavaclient.json.TransitionJsonParser;
import com.atlassian.jira.restjavaclient.json.WatchersJsonParserBuilder;
import com.atlassian.jira.restjavaclient.json.gen.CommentJsonGenerator;
import com.google.common.base.Joiner;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.apache.ApacheHttpClient;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.annotation.Nullable;
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
public class JerseyIssueRestClient implements IssueRestClient {

	private final ApacheHttpClient client;
	private final URI baseUri;

	private final IssueJsonParser issueParser = new IssueJsonParser();
	private final JsonParser<Watchers> watchersParser = WatchersJsonParserBuilder.createWatchersParser();
	private final TransitionJsonParser transitionJsonParser = new TransitionJsonParser();
	private final CommentJsonGenerator commentJsonGenerator = new CommentJsonGenerator();

	public JerseyIssueRestClient(URI baseUri, ApacheHttpClient client) {
		this.baseUri = baseUri;
		this.client = client;
	}

	@Override
	public Watchers getWatchers(Issue issue, ProgressMonitor progressMonitor) {
		final WebResource watchersResource = client.resource(issue.getWatchers().getSelf());

		final JSONObject s = watchersResource.get(JSONObject.class);
		try {
			return watchersParser.parse(s);
		} catch (JSONException e) {
			throw new RestClientException(e);
		}
	}

	@Override
	public Issue getIssue(final String issueKey, ProgressMonitor progressMonitor) {
		final UriBuilder uriBuilder = UriBuilder.fromUri(baseUri);
		uriBuilder.path("issue").path(issueKey);
//		final String expandoString = getExpandoString(args);
//		if (expandoString != null) {
//			uriBuilder.queryParam("expand", expandoString);
//		}

		final WebResource issueResource = client.resource(uriBuilder.build());

		final JSONObject s = issueResource.get(JSONObject.class);

		try {
//            System.out.println(s.toString(4));
			return issueParser.parse(s);
		} catch (JSONException e) {
			throw new RestClientException(e);
		}
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


	private <T> T invoke(Callable<T> callable) {
		try {
			return callable.call();
		} catch (UniformInterfaceException e) {
			final String body = e.getResponse().getEntity(String.class);
			try {
				JSONObject jsonObject = new JSONObject(body);
				final JSONArray errorMessages = jsonObject.getJSONArray("errorMessages");
				throw new RestClientException(JsonParseUtil.toStringCollection(errorMessages), e);
			} catch (JSONException e1) {
				throw new RestClientException(e);
			}
		} catch (RestClientException e) {
			throw e;
		} catch (Exception e) {
			throw new RestClientException(e);
		}
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
	public void watch(Issue issue, ProgressMonitor progressMonitor) {
	}

	@Override
	public void unwatch(Issue issue, ProgressMonitor progressMonitor) {
	}

	@Nullable
	private String getExpandoString(IssueArgs args) {
		Collection<String> expandos = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		if (args.withAttachments()) {
			expandos.add("attachments");
		}
		if (args.withComments()) {
			expandos.add("comments");
		}
		if (args.withHtml()) {
			expandos.add("html");
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

}
