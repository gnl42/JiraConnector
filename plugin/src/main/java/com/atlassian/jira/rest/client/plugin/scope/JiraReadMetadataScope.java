package com.atlassian.jira.rest.client.plugin.scope;

import com.atlassian.plugin.remotable.api.jira.JiraPermissions;
import com.atlassian.plugin.remotable.spi.permission.scope.RestApiScopeHelper;
import com.google.common.collect.ImmutableList;

import static java.util.Arrays.asList;

public final class JiraReadMetadataScope extends JiraScope
{
    public JiraReadMetadataScope()
    {
        super(JiraPermissions.READ_USER_SESSION_DATA,
                ImmutableList.<String>of(),
                asList(
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/issuetype", asList("get")),
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/issueLinkType", asList("get")),
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/priority", asList("get")),
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/resolution", asList("get")),
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/serverInfo", asList("get")),
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/field", asList("get"))
                )
        );
    }
}
