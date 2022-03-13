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

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.jira.beans.*;
import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.remoteapi.ProductServerFacade;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;

public interface JIRAServerFacade2 extends ProductServerFacade {

//    List<JIRAIssue> getIssues(ConnectionCfg httpConnectionCfg, String queryString, String sort,
//                              String sortOrder, int start, int size) throws JIRAException;

//    List<JIRAIssue> getIssues(ConnectionCfg httpConnectionCfg, String queryString, String sort,
//                              String sortOrder, int start, int size) throws JIRAException;

	List<JIRAIssue> getIssues(ConnectionCfg httpConnectionCfg, JiraFilter filter,
			String sort,
			String sortOrder,
			int start,
			int size) throws JIRAException;

//	List<JIRAIssue> getSavedFilterIssues(ConnectionCfg httpConnectionCfg,
//			List<JIRAQueryFragment> query,
//			String sort,
//			String sortOrder,
//			int start,
//			int size) throws JIRAException;

    List<JIRAIssue> getSavedFilterIssues(ConnectionCfg httpConnectionCfg,
                                         JIRASavedFilter filter,
                                         String sort,
                                         String sortOrder,
                                         int start,
                                         int size) throws JIRAException;

    List<JIRAProject> getProjects(ConnectionCfg httpConnectionCfg) throws JIRAException;

    List<JIRAProject> getProjectsForIssueCreation(ConnectionCfg httpConnectionCfg) throws JIRAException;

	List<JIRAConstant> getStatuses(ConnectionCfg httpConnectionCfg) throws JIRAException;

	List<JIRAConstant> getIssueTypes(ConnectionCfg httpConnectionCfg) throws JIRAException;

	List<JIRAConstant> getIssueTypesForProject(ConnectionCfg httpConnectionCfg, long projectId, String projectKey) throws JIRAException;

	List<JIRAConstant> getSubtaskIssueTypes(ConnectionCfg httpConnectionCfg) throws JIRAException;

	List<JIRAConstant> getSubtaskIssueTypesForProject(ConnectionCfg httpConnectionCfg, long projectId, String projectKey) throws JIRAException;

	List<JIRAQueryFragment> getSavedFilters(ConnectionCfg httpConnectionCfg) throws JIRAException;

	List<JIRAComponentBean> getComponents(ConnectionCfg httpConnectionCfg, String projectKey) throws JIRAException;

	List<JIRAVersionBean> getVersions(ConnectionCfg httpConnectionCfg, String projectKey) throws JIRAException;

	List<JIRAPriorityBean> getPriorities(ConnectionCfg httpConnectionCfg) throws JIRAException;

	List<JIRAResolutionBean> getResolutions(ConnectionCfg httpConnectionCfg) throws JIRAException;

	List<JIRAAction> getAvailableActions(ConnectionCfg httpConnectionCfg, JIRAIssue issue) throws JIRAException;

	List<JIRAActionField> getFieldsForAction(ConnectionCfg httpConnectionCfg, JIRAIssue issue, JIRAAction action)
            throws JIRAException;

	void progressWorkflowAction(ConnectionCfg httpConnectionCfg, JIRAIssue issue, JIRAAction action)
            throws JIRAException;

	void progressWorkflowAction(ConnectionCfg httpConnectionCfg, JIRAIssue issue,
			JIRAAction action, List<JIRAActionField> fields) throws JIRAException;

	void addComment(ConnectionCfg httpConnectionCfg, String issueKey, String comment) throws JIRAException;

	void addAttachment(ConnectionCfg httpConnectionCfg, String issueKey, String name, byte[] content) throws JIRAException;

	JIRAIssue createIssue(ConnectionCfg httpConnectionCfg, JIRAIssue issue) throws JIRAException;

    JIRAIssue createSubtask(JiraServerData jiraServerData, JIRAIssue parent, JIRAIssue issue) throws JIRAException;

    JIRAIssue getIssue(ConnectionCfg httpConnectionCfg, String key) throws JIRAException;

	JIRAIssue getIssueDetails(ConnectionCfg httpConnectionCfg, JIRAIssue issue) throws JIRAException;

	void logWork(ConnectionCfg httpConnectionCfg, JIRAIssue issue, String timeSpent, Calendar startDate,
			String comment, boolean updateEstimate, String newEstimate)
			throws JIRAException;

	void setField(ConnectionCfg httpConnectionCfg, JIRAIssue issue, String fieldId, String value) throws JIRAException;

	void setField(ConnectionCfg httpConnectionCfg, JIRAIssue issue, String fieldId, String[] values) throws JIRAException;

	void setFields(ConnectionCfg httpConnectionCfg, JIRAIssue issue, List<JIRAActionField> fields) throws JIRAException;

	JIRAUserBean getUser(ConnectionCfg httpConnectionCfg, String loginName)
            throws JIRAException, JiraUserNotFoundException;

	List<JIRAComment> getComments(ConnectionCfg httpConnectionCfg, JIRAIssue issue) throws JIRAException;

    Collection<JIRAAttachment> getIssueAttachements(ConnectionCfg httpConnectionCfg, JIRAIssue issue)
            throws JIRAException;

    List<JIRASecurityLevelBean> getSecurityLevels(ConnectionCfg connectionCfg, String projectKey) throws JIRAException;

    void reset();

    boolean usesRest(JiraServerData jiraServerData);

    List<JIRAIssue> getIssues(JiraServerData server, String query, String sort, String sortOrder, int start, int size) throws JIRAException;
}
