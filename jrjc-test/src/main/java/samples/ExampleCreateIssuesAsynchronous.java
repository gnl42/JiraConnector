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


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.atlassian.util.concurrent.Promise;
import me.glindholm.jira.rest.client.api.IssueRestClient;
import me.glindholm.jira.rest.client.api.JiraRestClient;
import me.glindholm.jira.rest.client.api.domain.BasicIssue;
import me.glindholm.jira.rest.client.api.domain.input.IssueInput;
import me.glindholm.jira.rest.client.api.domain.input.IssueInputBuilder;
import me.glindholm.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

/**
 * This example shows how to create many issues using asynchronous API.
 *
 * @since v2.0
 */
public class ExampleCreateIssuesAsynchronous {

    private static URI jiraServerUri = URI.create("http://localhost:2990/jira");


    public static void main(String[] args) throws IOException, URISyntaxException {
        final AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        final JiraRestClient restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, "admin", "admin");

        try (restClient) {
            final List<Promise<BasicIssue>> promises = new ArrayList<>();
            final IssueRestClient issueClient = restClient.getIssueClient();

            System.out.println("Sending issue creation requests...");
            for (int i = 0; i < 100; i++) {
                final String summary = "NewIssue#" + i;
                final IssueInput newIssue = new IssueInputBuilder("TST", 1L, summary).build();
                System.out.println("\tCreating: " + summary);
                promises.add(issueClient.createIssue(newIssue));
            }

            System.out.println("Collecting responses...");
            final List<BasicIssue> createdIssues = promises.stream().map(promise -> promise.claim()).collect(Collectors.toList());
            System.out.println("Created issues:\n" + createdIssues.stream().map(issue -> String.valueOf(issue.getId())).collect(Collectors.joining("\n")));
        }
    }
}
