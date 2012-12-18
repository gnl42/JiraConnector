package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.api.domain.BasicUser;
import org.junit.Assert;
import org.junit.Test;

import static com.atlassian.jira.rest.client.TestUtil.toUri;
import static com.atlassian.jira.rest.client.internal.json.ResourceUtil.getJsonObjectFromResource;

public class BasicUserJsonParserTest {

	private final BasicUserJsonParser parser = new BasicUserJsonParser();

	@Test
	public void testParseWhenAnonymousUser() throws Exception {
		final BasicUser user = parser.parse(getJsonObjectFromResource("/json/user/valid-basic-anonymous.json"));

		Assert.assertNull(user);
	}

	@Test
	public void testParseWhenDeletedUserBugJRA30263() throws Exception {
		final BasicUser user = parser.parse(getJsonObjectFromResource("/json/user/valid-basic-deleted-JRA-30263.json"));

		Assert.assertEquals("mark", user.getName());
		Assert.assertTrue(user.isSelfUriIncomplete());
	}


	@Test
	public void testParseWhenValid() throws Exception {
		final BasicUser user = parser.parse(getJsonObjectFromResource("/json/user/valid.json"));

		Assert.assertNotNull(user);
		Assert.assertEquals("admin", user.getName());
		Assert.assertEquals("Administrator", user.getDisplayName());
		Assert.assertEquals(toUri("http://localhost:8090/jira/rest/api/latest/user?username=admin"), user.getSelf());
		Assert.assertFalse(user.isSelfUriIncomplete());
		System.out.println(user);
	}
}
