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

package it;

import com.atlassian.jira.restjavaclient.TestUtil;
import com.atlassian.jira.restjavaclient.domain.BasicIssueType;
import com.atlassian.jira.restjavaclient.domain.BasicPriority;
import com.atlassian.jira.restjavaclient.domain.BasicResolution;
import com.atlassian.jira.restjavaclient.domain.BasicStatus;
import com.atlassian.jira.restjavaclient.domain.Issue;
import com.atlassian.jira.restjavaclient.domain.IssueType;
import com.atlassian.jira.restjavaclient.domain.Priority;
import com.atlassian.jira.restjavaclient.domain.Resolution;
import com.atlassian.jira.restjavaclient.domain.ServerInfo;
import com.atlassian.jira.restjavaclient.domain.Status;
import com.atlassian.jira.restjavaclient.domain.Transition;
import com.atlassian.jira.restjavaclient.domain.TransitionInput;
import org.joda.time.DateTime;

import javax.ws.rs.core.Response;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class JerseyMetadataRestClientTest extends AbstractRestoringJiraStateJerseyRestClientTest {
	public void testGetServerInfo() throws Exception {
		final ServerInfo serverInfo = client.getMetadataClient().getServerInfo(pm);
		assertEquals("Your Company JIRA", serverInfo.getServerTitle());
		assertTrue(serverInfo.getBuildDate().isBeforeNow());
		assertTrue(serverInfo.getServerTime().isAfter(new DateTime().minusMinutes(5)));
		assertTrue(serverInfo.getServerTime().isBefore(new DateTime().plusMinutes(5)));
	}

	public void testGetIssueTypeNonExisting() throws Exception {
		final BasicIssueType basicIssueType = client.getIssueClient().getIssue("TST-1", pm).getIssueType();
		TestUtil.assertErrorCode(Response.Status.NOT_FOUND, "The issue type with id '" +
				TestUtil.getLastPathSegment(basicIssueType.getSelf()) + "fake" +
				"' does not exist", new Runnable() {
			@Override
			public void run() {
				client.getMetadataClient().getIssueType(TestUtil.toUri(basicIssueType.getSelf() + "fake"), pm);
			}
		});
	}

	public void testGetIssueType() {
		final BasicIssueType basicIssueType = client.getIssueClient().getIssue("TST-1", pm).getIssueType();
		final IssueType issueType = client.getMetadataClient().getIssueType(basicIssueType.getSelf(), pm);
		assertEquals("Bug", issueType.getName());
		assertEquals("A problem which impairs or prevents the functions of the product.", issueType.getDescription());
		assertTrue(issueType.getIconUri().toString().endsWith("bug.gif"));

	}

	public void testGetStatus() {
		final BasicStatus basicStatus = client.getIssueClient().getIssue("TST-1", pm).getStatus();
		final Status status = client.getMetadataClient().getStatus(basicStatus.getSelf(), pm);
		assertEquals("The issue is open and ready for the assignee to start work on it.", status.getDescription());
		assertTrue(status.getIconUrl().toString().endsWith("status_open.gif"));
		assertEquals("Open", status.getName());
	}

	public void testGetStatusNonExisting() throws Exception {
		final BasicStatus basicStatus = client.getIssueClient().getIssue("TST-1", pm).getStatus();
		TestUtil.assertErrorCode(Response.Status.NOT_FOUND, "The status with id '" +
				TestUtil.getLastPathSegment(basicStatus.getSelf()) + "fake" +
				"' does not exist", new Runnable() {
			@Override
			public void run() {
				client.getMetadataClient().getStatus(TestUtil.toUri(basicStatus.getSelf() + "fake"), pm);
			}
		});
	}

	public void testGetPriority() {
		final BasicPriority basicPriority = client.getIssueClient().getIssue("TST-2", pm).getPriority();
		final Priority priority = client.getMetadataClient().getPriority(basicPriority.getSelf(), pm);
		assertEquals(basicPriority.getSelf(), priority.getSelf());
		assertEquals("Major", priority.getName());
		assertEquals("#009900", priority.getStatusColor());
		assertEquals("Major loss of function.", priority.getDescription());
		assertTrue(priority.getIconUri().toString().startsWith(jiraUri.toString()));
		assertTrue(priority.getIconUri().toString().endsWith("/images/icons/priority_major.gif"));

	}


	public void testGetResolution() {
		final Issue issue = client.getIssueClient().getIssue("TST-2", pm);
		assertNull(issue.getResolution());
		final Iterable<Transition> transitions = client.getIssueClient().getTransitions(issue, pm);
		final Transition resolveTransition = getTransitionByName(transitions, "Resolve Issue");

		client.getIssueClient().transition(issue, new TransitionInput(resolveTransition.getId()), pm);

		final Issue resolvedIssue = client.getIssueClient().getIssue("TST-2", pm);
		final BasicResolution basicResolution = resolvedIssue.getResolution();
		assertNotNull(basicResolution);

		final Resolution resolution = client.getMetadataClient().getResolution(basicResolution.getSelf(), pm);
		assertEquals(basicResolution.getName(), resolution.getName());
		assertEquals(basicResolution.getSelf(), resolution.getSelf());
		assertEquals("A fix for this issue is checked into the tree and tested.", resolution.getDescription());
	}
}
