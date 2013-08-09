/*
 * Copyright (C) 2013 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package samples;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import static com.google.common.collect.Iterables.transform;

/**
 * This example shows how to create many issues using asynchronous API.
 *
 * @since v2.0
 */
public class ExampleCreateIssuesAsynchronous {

	private static URI jiraServerUri = URI.create("http://localhost:2990/jira");

	public static void main(String[] args) throws IOException {
		final AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
		final JiraRestClient restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, "admin", "admin");

		try {
			final List<Promise<BasicIssue>> promises = Lists.newArrayList();
			final IssueRestClient issueClient = restClient.getIssueClient();

			System.out.println("Sending issue creation requests...");
			for (int i = 0; i < 100; i++) {
				final String summary = "NewIssue#" + i;
				final IssueInput newIssue = new IssueInputBuilder("TST", 1L, summary).build();
				System.out.println("\tCreating: " + summary);
				promises.add(issueClient.createIssue(newIssue));
			}

			System.out.println("Collecting responses...");
			final Iterable<BasicIssue> createdIssues = transform(promises, new Function<Promise<BasicIssue>, BasicIssue>() {
				@Override
				public BasicIssue apply(Promise<BasicIssue> promise) {
					return promise.claim();
				}
			});

			System.out.println("Created issues:\n" + Joiner.on("\n").join(createdIssues));
		} finally {
			restClient.close();
		}
	}
}
