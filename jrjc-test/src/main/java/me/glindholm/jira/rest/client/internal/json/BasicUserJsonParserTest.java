package me.glindholm.jira.rest.client.internal.json;

import static me.glindholm.jira.rest.client.TestUtil.toUri;
import static me.glindholm.jira.rest.client.internal.json.ResourceUtil.getJsonObjectFromResource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import me.glindholm.jira.rest.client.api.domain.BasicUser;

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
        assertNull(user.getAccountId());
        assertEquals(toUri("http://localhost:8090/jira/rest/api/latest/user?username=admin"), user.getSelf());
        assertFalse(user.isSelfUriIncomplete());
    }

    @Test
    public void testParseWhenValidWithAccountId() throws Exception {
        final BasicUser user = parser.parse(getJsonObjectFromResource("/json/user/valid-with-accountId.json"));

        assertNotNull(user);
        assertEquals("admin", user.getName());
        assertEquals("Administrator", user.getDisplayName());
        assertEquals("uuid-accountId-admin", user.getAccountId());
        assertEquals(toUri("http://localhost:8090/jira/rest/api/latest/user?username=admin"), user.getSelf());
        assertFalse(user.isSelfUriIncomplete());
    }

    @Test
    public void testParseWhenValidWithAccountIdWithoutName() throws Exception {
        final BasicUser user = parser.parse(getJsonObjectFromResource("/json/user/valid-with-accountId-without-name.json"));

        assertNotNull(user);
        assertNull(user.getName());
        assertEquals("Administrator", user.getDisplayName());
        assertEquals("uuid-accountId-admin", user.getAccountId());
        assertEquals(toUri("http://localhost:8090/jira/rest/api/latest/user?username=admin"), user.getSelf());
        assertFalse(user.isSelfUriIncomplete());
    }
}