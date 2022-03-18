package com.atlassian.connector.commons.jira;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.jira.beans.JIRASavedFilter;
import com.atlassian.connector.commons.jira.beans.JiraFilter;
import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.jira.JiraCaptchaRequiredException;

import java.util.List;

/**
 * User: kalamon
 * Date: 14.11.12
 * Time: 16:33
 */

// this originates from RSS/XML session
public interface JIRASessionPartTwo {
//    List<JIRAIssue> getIssues(String queryString, String sortBy, String sortOrder, int start, int max) throws JIRAException;
//    List<JIRAIssue> getIssues(List<JIRAQueryFragment> fragments, String sortBy, String sortOrder, int start, int max) throws JIRAException;
    List<JIRAIssue> getIssues(JiraFilter filter, String sortBy, String sortOrder, int start, int max) throws JIRAException;
//    List<JIRAIssue> getAssignedIssues(String assignee) throws JIRAException;
//    List<JIRAIssue> getSavedFilterIssues(JIRAQueryFragment fragment, String sortBy, String sortOrder, int start, int max) throws JIRAException;
    List<JIRAIssue> getSavedFilterIssues(JIRASavedFilter filter, String sortBy, String sortOrder, int start, int max) throws JIRAException;

    List<JIRAIssue> getIssues(String jql, String sort, String sortOrder, int start, int size) throws JIRAException;

    JIRAIssue getIssue(String issueKey) throws JIRAException;
    void login() throws JIRAException, JiraCaptchaRequiredException;
    boolean isLoggedIn(ConnectionCfg server);
    void testConnection() throws RemoteApiException;
}
