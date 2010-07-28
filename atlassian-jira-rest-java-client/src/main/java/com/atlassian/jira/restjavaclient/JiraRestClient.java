package com.atlassian.jira.restjavaclient;

import com.atlassian.jira.restjavaclient.domain.Issue;
import com.atlassian.jira.restjavaclient.domain.User;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public interface JiraRestClient {
	void login();
	Issue getIssue(IssueArgs args);
	User getUser();
	
}
