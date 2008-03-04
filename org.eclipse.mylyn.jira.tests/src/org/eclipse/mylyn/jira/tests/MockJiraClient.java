/*******************************************************************************
 * Copyright (c) 2005, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.mylyn.jira.tests;

import java.io.File;
import java.io.OutputStream;

import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.internal.jira.core.model.Attachment;
import org.eclipse.mylyn.internal.jira.core.model.Component;
import org.eclipse.mylyn.internal.jira.core.model.CustomField;
import org.eclipse.mylyn.internal.jira.core.model.Issue;
import org.eclipse.mylyn.internal.jira.core.model.IssueType;
import org.eclipse.mylyn.internal.jira.core.model.NamedFilter;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.Query;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.model.ServerInfo;
import org.eclipse.mylyn.internal.jira.core.model.Status;
import org.eclipse.mylyn.internal.jira.core.model.Version;
import org.eclipse.mylyn.internal.jira.core.model.filter.IssueCollector;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraClientCache;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.tasks.core.RepositoryOperation;
import org.eclipse.mylyn.tasks.core.RepositoryTaskAttribute;
import org.eclipse.mylyn.web.core.WebLocation;

public class MockJiraClient extends JiraClient {

	public static Component createComponent(String id, String name) {
		Component component = new Component();
		component.setId(id);
		component.setName(name);
		return component;
	}

	public static IssueType createIssueType(String id, String name) {
		IssueType issueType = new IssueType();
		issueType.setId(id);
		issueType.setName(name);
		return issueType;

	}

	public static Priority createPriority(String id, String name) {
		Priority priority = new Priority();
		priority.setId(id);
		priority.setName(name);
		return priority;
	}

	public static Project createProject() {
		Project project = new Project();
		project.setId(JiraTestUtils.PROJECT1);
		project.setKey("PRONE");
		project.setName("Prone");

		Component[] components = new Component[] { createComponent("1", "component1"),
				createComponent("2", "component2"), createComponent("3", "component3"),
				createComponent("4", "component4"), };
		project.setComponents(components);

		Version[] versions = new Version[] { createVersion("1", "1.0"), createVersion("2", "2.0"),
				createVersion("3", "3.0"), createVersion("4", "4.0"), };
		project.setVersions(versions);

		return project;
	}

	public static Version createVersion(String id, String name) {
		Version version = new Version();
		version.setId(id);
		version.setName(name);
		return version;
	}

	private JiraClientCache cache;

	public MockJiraClient(String baseUrl) {
		super(new WebLocation(baseUrl), false);
		this.cache = super.getCache();
	}

	@Override
	public JiraClientCache getCache() {
		return this.cache;
	}

	public void setCache(JiraClientCache cache) {
		this.cache = cache;
	}

	@Override
	public void addCommentToIssue(Issue issue, String comment) throws JiraException {
	}

	@Override
	public void advanceIssueWorkflow(Issue issue, String actionKey, String comment) throws JiraException {
	}

	@Override
	public void assignIssueTo(Issue issue, int assigneeType, String user, String comment) throws JiraException {
	}

	@Override
	public void attachFile(Issue issue, String comment, PartSource partSource, String contentType) throws JiraException {
	}

	@Override
	public void attachFile(Issue issue, String comment, String filename, byte[] contents, String contentType)
			throws JiraException {
	}

	@Override
	public void attachFile(Issue issue, String comment, String filename, File file, String contentType)
			throws JiraException {
	}

	@Override
	public Issue createIssue(Issue issue) throws JiraException {
		return null;
	}

	@Override
	public Issue createSubTask(Issue issue) throws JiraException {
		return null;
	}

	@Override
	public void deleteIssue(Issue issue) throws JiraException {
	}

	@Override
	public String[] getActionFields(String issueKey, String actionId) throws JiraException {
		return null;
	}

	@Override
	public RepositoryOperation[] getAvailableOperations(String issueKey) throws JiraException {
		return null;
	}

	@Override
	public Component[] getComponents(String key) throws JiraException {
		return null;
	}

	@Override
	public CustomField[] getCustomAttributes() throws JiraException {
		return null;
	}

	@Override
	public RepositoryTaskAttribute[] getEditableAttributes(String issueKey) throws JiraException {
		return null;
	}

	public Issue getIssueById(String issue) throws JiraException {
		return null;
	}

	@Override
	public Issue getIssueByKey(String issueKey) throws JiraException {
		return null;
	}

	@Override
	public IssueType[] getIssueTypes() throws JiraException {
		return null;
	}

	@Override
	public String getKeyFromId(String issueId) throws JiraException {
		return null;
	}

	@Override
	public NamedFilter[] getNamedFilters() throws JiraException {
		return null;
	}

	@Override
	public Priority[] getPriorities() throws JiraException {
		return null;
	}

	@Override
	public Project[] getProjects() throws JiraException {
		return null;
	}

	@Override
	public Resolution[] getResolutions() throws JiraException {
		return null;
	}

	@Override
	public ServerInfo getServerInfo(IProgressMonitor monitor) throws JiraException {
		return null;
	}

	@Override
	public Status[] getStatuses() throws JiraException {
		return null;
	}

	@Override
	public IssueType[] getSubTaskIssueTypes() throws JiraException {
		return null;
	}

	@Override
	public Version[] getVersions(String key) throws JiraException {
		return null;
	}

	@Override
	public void login() throws JiraException {
	}

	@Override
	public void logout() {
	}

	@Override
	public byte[] retrieveFile(Issue issue, Attachment attachment) throws JiraException {
		return null;
	}

	@Override
	public void retrieveFile(Issue issue, Attachment attachment, OutputStream out) throws JiraException {
	}

	@Override
	public void search(Query query, IssueCollector collector) throws JiraException {
	}

	@Override
	public void unvoteIssue(Issue issue) throws JiraException {
	}

	@Override
	public void unwatchIssue(Issue issue) throws JiraException {
	}

	@Override
	public void updateIssue(Issue issue, String comment) throws JiraException {
	}

	@Override
	public void voteIssue(Issue issue) throws JiraException {
	}

	@Override
	public void watchIssue(Issue issue) throws JiraException {
	}

}
