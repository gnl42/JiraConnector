package com.atlassian.jira.rest.client.plugin.scope;

import com.atlassian.plugin.remotable.api.jira.JiraPermissions;
import com.atlassian.plugin.remotable.spi.permission.scope.RestApiScopeHelper;

import static java.util.Arrays.asList;

public final class BrowseProjectsScope extends JiraScope
{
    public BrowseProjectsScope()
    {
        super(
                JiraPermissions.BROWSE_PROJECTS,
                asList(
                        "getAttachmentsFromIssue",
                        "getComment",
                        "getComments",
                        "getIssue",
                        "getIssueById",
                        "getIssuesFromJqlSearch",
                        "getIssuesFromTextSearchWithLimit",
                        "getIssuesFromTextSearchWithProject",
                        "getProjectAvatar",
                        "getProjectAvatars",
                        "getProjectById",
                        "getProjectByKey",
                        "getProjectsNoSchemes",
                        "getResolutionDateById",
                        "getResolutionDateByKey",
                        "getResolutions",
                        "getSecurityLevel"
                ),
                asList(
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/project", asList("get")),
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/issue", asList("get")),
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/filter", asList("get")),
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/search", asList("get", "post"))
                )
        );
    }
}
