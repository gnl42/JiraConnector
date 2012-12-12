package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.domain.BasicUser;
import org.junit.Test;

import static com.atlassian.jira.rest.client.TestUtil.toUri;
import static com.atlassian.jira.rest.client.internal.json.ResourceUtil.getJsonObjectFromResource;
import static org.junit.Assert.*;

public class BasicUserJsonParserTest {

	private final BasicUserJsonParser parser = new BasicUserJsonParser();

	@Test
	public void testParseWhenAnonymousUser() throws Exception {
		final BasicUser user = parser.parse(getJsonObjectFromResource("/json/user/valid-basic-anonymous.json"));

		assertNull(user);
	}

	@Test
	public void testParseWhenDeletedUserBugJRA30263() throws Exception {
		final BasicUser user = parser.parse(getJsonObjectFromResource("/json/user/valid-basic-deleted-JRA-30263.json"));

		assertEquals("mark", user.getName());
		assertTrue(user.isSelfUriIncomplete());
	}


	@Test
	public void testParseWhenValid() throws Exception {
		final BasicUser user = parser.parse(getJsonObjectFromResource("/json/user/valid.json"));

		assertNotNull(user);
		assertEquals("admin", user.getName());
		assertEquals("Administrator", user.getDisplayName());
		assertEquals(toUri("http://localhost:8090/jira/rest/api/latest/user?username=admin"), user.getSelf());
		assertFalse(user.isSelfUriIncomplete());
		System.out.println(user);
	}
}
