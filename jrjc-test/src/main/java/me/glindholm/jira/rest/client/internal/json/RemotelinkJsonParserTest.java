package me.glindholm.jira.rest.client.internal.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;

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

}
