package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.TestUtil;
import com.atlassian.jira.rest.client.domain.BasicUser;
import org.junit.Test;

import static org.junit.Assert.*;

public class BasicUserJsonParserTest {

	@Test
	public void testParseWhenAnonymousUser() throws Exception {
		final BasicUserJsonParser parser = new BasicUserJsonParser();
		final BasicUser user = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/user/valid-basic-anonymous.json"));

		assertNull(user);
	}

	@Test
	public void testParseWhenDeletedUserBugJRA30263() throws Exception {
		final BasicUserJsonParser parser = new BasicUserJsonParser();
		final BasicUser user = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/user/valid-basic-deleted-JRA-30263.json"));

		assertEquals("mark", user.getName());
		assertEquals(TestUtil.toUri("incomplete://user/mark"), user.getSelf());
	}

}
