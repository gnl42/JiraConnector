package me.glindholm.jira.rest.client.internal.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Test;

import me.glindholm.jira.rest.client.api.domain.BasicUser;
import me.glindholm.jira.rest.client.api.domain.Remotelink;
import me.glindholm.jira.rest.client.api.domain.RemotelinkApplication;
import me.glindholm.jira.rest.client.api.domain.RemotelinkIcon;
import me.glindholm.jira.rest.client.api.domain.RemotelinkObject;
import me.glindholm.jira.rest.client.api.domain.RemotelinkStatus;

public class RemotelinkJsonParserTest {

    @Test
    public void testParse() throws Exception {
        final RemotelinksJsonParser parser = new RemotelinksJsonParser();
        final List<Remotelink> remotelinks = parser.parse(ResourceUtil.getJsonArrayFromResource("/json/issueRemotelink/remotelink.json"));

        assertNotNull(remotelinks);
        assertEquals(4, remotelinks.size());

        final Remotelink remotelink = remotelinks.get(0);
        assertEquals("Bamboo Branches", remotelink.getRelationship());

        final RemotelinkApplication application = remotelink.getApplication();
        assertNotNull(application);

        final RemotelinkObject object = remotelink.getObject();
        assertNotNull(object);
        final RemotelinkIcon icon = object.getIcon();
        assertNull(icon.getLink());

        final RemotelinkStatus status = object.getStatus();
        final RemotelinkIcon statusIcon = status.getIcon();
        assertNull(statusIcon.getLink());
    }

    @Test
    public void TestSetAssignables() {
        final List<BasicUser> assignables = new ArrayList<>();
        assignables.add(new BasicUser(null, "a", "Alpha"));
        assignables.add(new BasicUser(null, "b", "Beta"));
        assignables.add(new BasicUser(null, "a", "Alpha2"));
        final Map<String, BasicUser> mapped = assignables.stream().collect(Collectors.toConcurrentMap(BasicUser::getId, Function.identity(), (x1, x2) -> x1));
        assertEquals(2, mapped.size());
    }

}
