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

package com.atlassian.jira.restjavaclient.json;

import com.atlassian.jira.restjavaclient.ExpandableProperty;
import com.atlassian.jira.restjavaclient.IsIterableOf;
import com.atlassian.jira.restjavaclient.IssueArgsBuilder;
import com.atlassian.jira.restjavaclient.TestUtil;
import com.atlassian.jira.restjavaclient.domain.Attachment;
import com.atlassian.jira.restjavaclient.domain.Issue;
import com.atlassian.jira.restjavaclient.domain.IssueLink;
import com.atlassian.jira.restjavaclient.domain.IssueLinkType;
import com.atlassian.jira.restjavaclient.domain.IssueType;
import com.atlassian.jira.restjavaclient.domain.Project;
import com.atlassian.jira.restjavaclient.domain.Watchers;
import com.atlassian.jira.restjavaclient.domain.Worklog;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Iterator;

import static com.atlassian.jira.restjavaclient.TestUtil.toDateTime;
import static com.atlassian.jira.restjavaclient.TestUtil.toUri;
import static org.junit.Assert.*;

/**
 * @since v0.1
 */
public class IssueJsonParserTest {
	@Test
	public void x() {
		fail("abc");

	}
	@Test
	public void testParseIssue() throws Exception {
		fail("fds");

		final JSONObject issueJson = ResourceUtil.getJsonObjectFromResource("/json/issue/valid-all-expanded.json");
		final IssueJsonParser parser = new IssueJsonParser();
		final Issue issue = parser.parseIssue(new IssueArgsBuilder("TST-2").build(), issueJson);
		assertEquals("TST-2", issue.getKey());
		assertEquals(new IssueType(toUri("http://localhost:8090/jira/rest/api/latest/issueType/1"), "Bug", false),
				issue.getIssueType());
		assertEquals(new Project(toUri("http://localhost:8090/jira/rest/api/latest/project/TST"), "TST"), issue.getProject());
		
		// issue links
		Assert.assertThat(issue.getIssueLinks(), IsIterableOf.hasOnlyElements(
				new IssueLink("TST-1", toUri("http://localhost:8090/jira/rest/api/latest/issue/TST-1"),
						new IssueLinkType("Duplicate", "duplicates", IssueLinkType.Direction.OUTBOUND)),
				new IssueLink("TST-1", toUri("http://localhost:8090/jira/rest/api/latest/issue/TST-1"), 
						new IssueLinkType("Duplicate", "is duplicated by", IssueLinkType.Direction.INBOUND))
				));


		// watchers
		final Watchers watchers = issue.getWatchers();
		assertFalse(watchers.isWatching());
		assertEquals(toUri("http://localhost:8090/jira/rest/api/latest/issue/TST-2/watchers"), watchers.getSelf());
		assertThat(watchers.getList().getItems(), IsIterableOf.hasOnlyElements(TestConstants.USER1));

		// attachments
		final ExpandableProperty<Attachment> attachments = issue.getAttachments();
		assertEquals(3, attachments.getSize());
		final Attachment attachment = attachments.getItems().iterator().next();
		assertEquals("jira_logo.gif", attachment.getFilename());
		assertEquals(TestConstants.USER_ADMIN, attachment.getAuthor());
		assertEquals(2517, attachment.getSize());
		assertEquals(toUri("http://localhost:8090/jira/secure/thumbnail/10036/10036_jira_logo.gif"), attachment.getThumbnailUri());
		final Iterator<Attachment> attachmentIt = attachments.getItems().iterator();
		attachmentIt.next();
		attachmentIt.next();
		final Attachment lastAttachment = attachmentIt.next();
		assertEquals("transparent-png.png2", lastAttachment.getFilename());

		// worklogs
		final ExpandableProperty<Worklog> worklogs = issue.getWorklogs();
		assertEquals(3, worklogs.getSize());
		final Worklog worklog = Iterables.get(worklogs.getItems(), 2);
		assertEquals(new Worklog(toUri("http://localhost:8090/jira/rest/api/latest/worklog/10012"),
				toUri("http://localhost:8090/jira/rest/api/latest/issue/TST-2"), TestConstants.USER1,
				TestConstants.USER1, "a worklog viewable just by jira-users",
				toDateTime("2010-08-17T16:53:15.848+0200"), toDateTime("2010-08-17T16:53:15.848+0200"),
				toDateTime("2010-08-11T16:52:00.000+0200"), 3), null);
	}
}
