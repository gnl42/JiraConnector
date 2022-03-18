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

package com.atlassian.connector.commons.jira;

import com.atlassian.connector.commons.jira.beans.JIRAAttachment;
import com.atlassian.connector.commons.jira.beans.JIRAComment;
import com.atlassian.connector.commons.jira.beans.JIRAComponentBean;
import com.atlassian.connector.commons.jira.beans.JIRAConstant;
import com.atlassian.connector.commons.jira.beans.JIRAPriorityBean;
import com.atlassian.connector.commons.jira.beans.JIRAProject;
import com.atlassian.connector.commons.jira.beans.JIRAQueryFragment;
import com.atlassian.connector.commons.jira.beans.JIRAResolutionBean;
import com.atlassian.connector.commons.jira.beans.JIRASecurityLevelBean;
import com.atlassian.connector.commons.jira.beans.JIRAUserBean;
import com.atlassian.connector.commons.jira.beans.JIRAVersionBean;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;

// this originates from SOAP session
public interface JIRASessionPartOne {
	void login(String userName, String password) throws RemoteApiException;

	void logout();

	void logWork(JIRAIssue issue, String timeSpent, Calendar startDate, String comment,
			boolean updateEstimate, String newEstimate)
			throws RemoteApiException;

	void addComment(String issueKey, String comment) throws RemoteApiException;

	void addAttachment(String issueKey, String name, byte[] content) throws RemoteApiException;

	JIRAIssue createIssue(JIRAIssue issue) throws RemoteApiException;

	JIRAIssue getIssueDetails(JIRAIssue issue) throws RemoteApiException;

	List<JIRAProject> getProjects() throws RemoteApiException;

	List<JIRAConstant> getIssueTypes() throws RemoteApiException;

	List<JIRAConstant> getIssueTypesForProject(long projectId, String projectKey) throws RemoteApiException;

	List<JIRAConstant> getSubtaskIssueTypes() throws RemoteApiException;

	List<JIRAConstant> getSubtaskIssueTypesForProject(long projectId, String projectKey) throws RemoteApiException;

	List<JIRAConstant> getStatuses() throws RemoteApiException;

	List<JIRAComponentBean> getComponents(String projectKey) throws RemoteApiException;

	List<JIRAVersionBean> getVersions(String projectKey) throws RemoteApiException;

	List<JIRAPriorityBean> getPriorities() throws RemoteApiException;

	List<JIRAResolutionBean> getResolutions() throws RemoteApiException;

	List<JIRAQueryFragment> getSavedFilters() throws RemoteApiException;

	List<JIRAAction> getAvailableActions(JIRAIssue issue) throws RemoteApiException;

	List<JIRAActionField> getFieldsForAction(JIRAIssue issue, JIRAAction action) throws RemoteApiException;

	void progressWorkflowAction(JIRAIssue issue, JIRAAction action, List<JIRAActionField> fields) throws RemoteApiException;

	void setField(JIRAIssue issue, String fieldId, String value) throws RemoteApiException;

	void setField(JIRAIssue issue, String fieldId, String[] values) throws RemoteApiException;

	void setFields(JIRAIssue issue, List<JIRAActionField> fields) throws RemoteApiException;

	JIRAUserBean getUser(String loginName) throws RemoteApiException, JiraUserNotFoundException;

	List<JIRAComment> getComments(JIRAIssue issue) throws RemoteApiException;

	boolean isLoggedIn();

	Collection<JIRAAttachment> getIssueAttachements(JIRAIssue issue) throws RemoteApiException;

    List<JIRASecurityLevelBean> getSecurityLevels(String projectKey) throws RemoteApiException;
}
