package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.TestUtil;
import com.atlassian.jira.rest.client.domain.IssuelinksType;
import com.google.common.collect.Iterables;
import org.junit.Test;

import static org.junit.Assert.*;

public class IssueLinkTypesJsonParserTest {
	@Test
	public void testParse() throws Exception {
		IssueLinkTypesJsonParser parser = new IssueLinkTypesJsonParser();
		final Iterable<IssuelinksType> issueLinks = parser.parse(ResourceUtil
				.getJsonObjectFromResource("/json/issueLinks/issue-links-5.0.json"));
		assertEquals(8, Iterables.size(issueLinks));
		assertEquals(new IssuelinksType(TestUtil.toUri("https://jdog.atlassian.com/rest/api/2/issueLinkType/10160"),
				"10160", "Bonfire Testing", "discovered while testing", "testing discovered"), Iterables.get(issueLinks, 0));

		assertEquals(new IssuelinksType(TestUtil.toUri("https://jdog.atlassian.com/rest/api/2/issueLinkType/10020"),
				"10020", "Relates", "is related to", "relates to"), Iterables.getLast(issueLinks));
	}
}
