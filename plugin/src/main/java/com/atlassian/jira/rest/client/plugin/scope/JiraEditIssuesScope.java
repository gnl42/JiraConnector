package com.atlassian.jira.rest.client.plugin.scope;

import com.atlassian.plugin.remotable.api.jira.JiraPermissions;
import com.atlassian.plugin.remotable.spi.permission.scope.RestApiScopeHelper;

import static java.util.Arrays.asList;

public final class JiraEditIssuesScope extends JiraScope
{
    public JiraEditIssuesScope()
    {
        super(
                JiraPermissions.EDIT_ISSUES,
                asList(
                        "updateIssue",
                        "getComponents",
                        "getFieldsForEdit",
                        "getIssueTypesForProject",
                        "getPriorities",
                        "getSecurityLevels",
                        "getStatuses",
                        "getSubTaskIssueTypesForProject",
                        "getVersions"
                ),
                asList(
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/issue", asList("put", "delete"))
                )
        );
    }
}
