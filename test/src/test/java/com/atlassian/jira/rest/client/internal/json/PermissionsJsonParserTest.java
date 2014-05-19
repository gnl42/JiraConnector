package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.api.domain.Permission;
import com.atlassian.jira.rest.client.api.domain.Permissions;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class PermissionsJsonParserTest {

    @Test
    public void testParse() throws Exception {
        final PermissionsJsonParser parser = new PermissionsJsonParser();
        final Permissions permissions = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/mypermission/valid.json"));

        assertThat(permissions.havePermission("WORKLOG_EDIT_OWN"), is(true));
        assertThat(permissions.havePermission("WORKLOG_DELETE_OWN"), is(false));
        Permission worklogDeleteOwn = permissions.getPermission("WORKLOG_DELETE_OWN");
        assertThat(worklogDeleteOwn, notNullValue());
        assertThat(worklogDeleteOwn.getId(), is(42));
        assertThat(worklogDeleteOwn.getKey(), is("WORKLOG_DELETE_OWN"));
        assertThat(worklogDeleteOwn.getName(), is("Delete Own Worklogs"));
        assertThat(worklogDeleteOwn.getDescription(), is("Ability to delete own worklogs made on issues."));
        assertThat(worklogDeleteOwn.havePermission(), is(false));
    }
}