package com.atlassian.jira.rest.client.plugin.scope;

import com.atlassian.plugin.remotable.api.jira.JiraPermissions;
import com.atlassian.plugin.remotable.spi.permission.scope.RestApiScopeHelper;
import com.google.common.collect.ImmutableList;

import static java.util.Arrays.asList;

public final class JiraReadUserSessionData extends JiraScope
{
    public JiraReadUserSessionData()
    {
        super(JiraPermissions.READ_USER_SESSION_DATA,
                ImmutableList.<String>of(),
                asList(
                        new RestApiScopeHelper.RestScope("auth", asList("latest", "2", "2.0.alpha1"), "/session", asList("get"))
                )
        );
    }
}
