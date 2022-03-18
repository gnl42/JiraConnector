package com.atlassian.theplugin.commons.bamboo;

/**
 * User: kalamon
 * Date: Aug 18, 2009
 * Time: 3:32:40 PM
 */
public interface BuildIssue {
    String getIssueKey();
    String getIssueUrl();
    String getServerUrl();
}
