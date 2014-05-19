package it;

import com.atlassian.jira.nimblefunctests.annotation.RestoreOnce;
import com.atlassian.jira.rest.client.api.domain.Permission;
import com.atlassian.jira.rest.client.api.domain.Permissions;
import com.atlassian.jira.rest.client.internal.json.TestConstants;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;

@RestoreOnce(TestConstants.DEFAULT_JIRA_DUMP_FILE)
public class AsynchronousMyPermissionsRestClientTest extends AbstractAsynchronousRestClientTest {

    @Test
    public void testGetMyPermissions() throws Exception {
        // when
        final Permissions permissions = client.getMyPermissionsRestClient().getMyPermissions("TST-1").claim();

        // then
        Permission worklogDeleteOwn = permissions.getPermission("WORKLOG_DELETE_OWN");
        assertThat(worklogDeleteOwn, notNullValue());
        assertThat(worklogDeleteOwn.getId(), is(42));
        assertThat(worklogDeleteOwn.getKey(), is("WORKLOG_DELETE_OWN"));
        assertThat(worklogDeleteOwn.getName(), is("Delete Own Worklogs"));
        assertThat(worklogDeleteOwn.getDescription(), is("Ability to delete own worklogs made on issues."));
        assertThat(worklogDeleteOwn.havePermission(), is(true));
    }
}