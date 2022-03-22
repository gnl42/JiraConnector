/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.glindholm.connector.commons.jira.rss;

import me.glindholm.connector.commons.api.ConnectionCfg;
import me.glindholm.connector.commons.jira.JIRAIssue;
import me.glindholm.connector.commons.jira.JIRASessionPartTwo;
import me.glindholm.connector.commons.jira.beans.JIRASavedFilter;
import me.glindholm.connector.commons.jira.beans.JiraFilter;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiException;
import me.glindholm.theplugin.commons.remoteapi.jira.JiraCaptchaRequiredException;

import java.util.Date;
import java.util.List;

/**
 * @autrhor pmaruszak
 * @date May 11, 2010
 * For future use. Here should be renewed session if expires
 */
public class JiraRssAutoRenewClient implements JIRASessionPartTwo {
    private final JIRARssClient rssClient;

    private Date lastUsed;
    
    public JiraRssAutoRenewClient(JIRARssClient rssClient) {
        this.rssClient = rssClient;
    }

    public void login() throws JIRAException, JiraCaptchaRequiredException {
        rssClient.login();
    }

    public void testConnection() throws RemoteApiException {
        rssClient.testConnection();
    }

    public boolean isLoggedIn(ConnectionCfg server) {
        return rssClient != null && rssClient.isLoggedIn(server);
    }

    public List<JIRAIssue> getIssues(JiraFilter filter, String sortBy, String sortOrder, int start, int max) throws JIRAException {
        return rssClient.getIssues(filter, sortBy, sortOrder, start, max);
    }

    public List<JIRAIssue> getIssues(String jql, String sort, String sortOrder, int start, int size) throws JIRAException {
        return rssClient.getIssues(jql, sort, sortOrder, start, size);
    }

    //    public List<JIRAIssue> getIssues(String queryString, String sortBy, String sortOrder, int start, int max)
//            throws JIRAException {
//        return rssClient.getIssues(queryString, sortBy, sortOrder, start, max);
//    }

//    public List<JIRAIssue> getIssues(List<JIRAQueryFragment> fragments, String sortBy, String sortOrder,
//                                     int start, int max) throws JIRAException {
//        return rssClient.getIssues(fragments, sortBy, sortOrder, start, max);
//    }

//    public List<JIRAIssue> getAssignedIssues(String assignee) throws JIRAException {
//        return rssClient.getAssignedIssues(assignee);
//    }

//    public List<JIRAIssue> getSavedFilterIssues(JIRAQueryFragment fragment, String sortBy, String sortOrder, int start,
//                                                int max) throws JIRAException {
//        return rssClient.getSavedFilterIssues(fragment, sortBy, sortOrder, start, max);
//    }


    public List<JIRAIssue> getSavedFilterIssues(JIRASavedFilter filter, String sortBy, String sortOrder, int start, int max) throws JIRAException {
        return rssClient.getSavedFilterIssues(filter, sortBy, sortOrder, start, max);
    }

    public JIRAIssue getIssue(String issueKey) throws JIRAException {
        return rssClient.getIssue(issueKey);
    }

    public Date getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(Date lastUsed) {
        this.lastUsed = lastUsed;
    }
}
