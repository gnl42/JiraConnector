/*******************************************************************************
 * Copyright (c) 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.jira.tests.util;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.atlassian.connector.eclipse.internal.jira.core.model.IssueField;
import com.atlassian.connector.eclipse.internal.jira.core.model.IssueType;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraAction;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraIssue;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraStatus;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraWorkLog;
import com.atlassian.connector.eclipse.internal.jira.core.model.NamedFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.Priority;
import com.atlassian.connector.eclipse.internal.jira.core.model.Project;
import com.atlassian.connector.eclipse.internal.jira.core.model.Resolution;
import com.atlassian.connector.eclipse.internal.jira.core.model.ServerInfo;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraClientCache;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraException;
import com.atlassian.connector.eclipse.internal.jira.core.service.rest.JiraRestClientAdapter;

public class MockJiraRestClientAdapter extends JiraRestClientAdapter {

	public MockJiraRestClientAdapter(String url, JiraClientCache cache) {
		super(url, cache);
	}

	@Override
	public void addComment(String issueKey, String comment) throws JiraException {
		// ignore
	}

	@Override
	public void addAttachment(String issueKey, byte[] content, String filename) throws JiraException {
		// ignore
	}

	@Override
	public InputStream getAttachment(URI attachmentUri) {
		return null;
	}

	@Override
	public Project[] getProjects() {
		return new Project[0];
	}

	@Override
	public NamedFilter[] getFavouriteFilters() {
		return new NamedFilter[0];
	}

	@Override
	public Resolution[] getResolutions() {
		return new Resolution[0];
	}

	@Override
	public Priority[] getPriorities() {
		return new Priority[0];
	}

	@Override
	public JiraIssue getIssueByKeyOrId(String issueKeyOrId, IProgressMonitor monitor) throws JiraException {
		return new JiraIssue();
	}

	@Override
	public JiraStatus[] getStatuses() {
		return new JiraStatus[0];
	}

	@Override
	public IssueType[] getIssueTypes() {
		return new IssueType[0];
	}

	@Override
	public IssueType[] getIssueTypes(String projectKey) {
		return new IssueType[0];
	}

	@Override
	public List<JiraIssue> getIssues(String jql, int maxResult, IProgressMonitor monitor) throws JiraException {
		return new ArrayList<JiraIssue>();
	}

	@Override
	public void getProjectDetails(Project project) {
		// ignore
	}

	@Override
	public void addWorklog(String issueKey, JiraWorkLog jiraWorklog) throws JiraException {
		// ignore
	}

	@Override
	public ServerInfo getServerInfo() throws JiraException {
		return new ServerInfo();
	}

	@Override
	public Iterable<JiraAction> getTransitions(String issueKey) throws JiraException {
		return new Iterable<JiraAction>() {
			public Iterator<JiraAction> iterator() {
				return null;
			}
		};
	}

	@Override
	public void transitionIssue(JiraIssue issue, String transitionKey, String comment,
			Iterable<IssueField> transitionFields) throws JiraException {
		// ignore
	}

	@Override
	public void assignIssue(String issueKey, String user, String comment) throws JiraException {
		// ignore
	}

	@Override
	public String createIssue(JiraIssue issue) throws JiraException {
		return "KEY-1";
	}

	@Override
	public void updateIssue(JiraIssue changedIssue) throws JiraException {
		// ignore
	}

}
