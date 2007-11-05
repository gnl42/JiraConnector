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
import java.net.Proxy;

import org.apache.commons.httpclient.methods.multipart.PartSource;
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
import org.eclipse.mylyn.internal.jira.core.service.AbstractJiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.tasks.core.RepositoryOperation;
import org.eclipse.mylyn.tasks.core.RepositoryTaskAttribute;
import org.eclipse.mylyn.web.core.WebLocation;

public class MockJiraClient extends AbstractJiraClient {

	public MockJiraClient(String baseUrl) {
		super(new WebLocation(baseUrl), false);
	}

	@Override
	public Component[] getComponentsRemote(String key) throws JiraException {
		return null;
	}

	@Override
	public IssueType[] getIssueTypesRemote() throws JiraException {
		return null;
	}

	@Override
	public Priority[] getPrioritiesRemote() throws JiraException {
		return null;
	}

	@Override
	public Project[] getProjectsRemote() throws JiraException {
		return null;
	}

	@Override
	public Project[] getProjectsRemoteNoSchemes() throws JiraException {
		return null;
	}

	@Override
	public Resolution[] getResolutionsRemote() throws JiraException {
		return null;
	}

	@Override
	public ServerInfo getServerInfoRemote() throws JiraException {
		return null;
	}

	@Override
	public Status[] getStatusesRemote() throws JiraException {
		return null;
	}

	@Override
	public IssueType[] getSubTaskIssueTypesRemote() throws JiraException {
		return null;
	}

	@Override
	public Version[] getVersionsRemote(String key) throws JiraException {
		return null;
	}

	public void addCommentToIssue(Issue issue, String comment) throws JiraException {
	}

	public void assignIssueTo(Issue issue, int assigneeType, String user, String comment) throws JiraException {
	}

	public void attachFile(Issue issue, String comment, String filename, byte[] contents, String contentType)
			throws JiraException {
	}

	public void attachFile(Issue issue, String comment, String filename, File file, String contentType)
			throws JiraException {
	}

	public Issue createIssue(Issue issue) throws JiraException {
		return null;
	}

	public Issue createSubTask(Issue issue) throws JiraException {
		return null;
	}

	public Issue getIssueById(String issue) throws JiraException {
		return null;
	}

	public Issue getIssueByKey(String issueKey) throws JiraException {
		return null;
	}

	public String getKeyFromId(String issueId) throws JiraException {
		return null;
	}

	public NamedFilter[] getNamedFilters() throws JiraException {
		return null;
	}

	public void login() throws JiraException {
	}

	public void logout() {
	}

	public byte[] retrieveFile(Issue issue, Attachment attachment) throws JiraException {
		return null;
	}

	public void retrieveFile(Issue issue, Attachment attachment, OutputStream out) throws JiraException {
	}

	public void search(Query query, IssueCollector collector) throws JiraException {
	}

	public void unvoteIssue(Issue issue) throws JiraException {
	}

	public void unwatchIssue(Issue issue) throws JiraException {
	}

	public void updateIssue(Issue issue, String comment) throws JiraException {
	}

	public void voteIssue(Issue issue) throws JiraException {
	}

	public void watchIssue(Issue issue) throws JiraException {
	}

	public RepositoryOperation[] getAvailableOperations(String issueKey) throws JiraException {
		return null;
	}

	public RepositoryTaskAttribute[] getEditableAttributes(String issueKey) throws JiraException {
		return null;
	}

	public String[] getActionFields(String issueKey, String actionId) throws JiraException {
		return null;
	}

	public CustomField[] getCustomAttributes() throws JiraException {
		return null;
	}

	public void advanceIssueWorkflow(Issue issue, String actionKey, String comment) throws JiraException {
	}

	public void attachFile(Issue issue, String comment, PartSource partSource, String contentType) throws JiraException {
	}

	// factory methods
	
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
	
	public static IssueType createIssueType(String id, String name) {
		IssueType issueType = new IssueType();
		issueType.setId(id);
		issueType.setName(name);
		return issueType;
		
	}
	
	public static Version createVersion(String id, String name) {
		Version version = new Version();
		version.setId(id);
		version.setName(name);
		return version;
	}

	public static Component createComponent(String id, String name) {
		Component component = new Component();
		component.setId(id);
		component.setName(name);
		return component;
	}
	
	public static Priority createPriority(String id, String name) {
		Priority priority = new Priority();
		priority.setId(id);
		priority.setName(name);
		return priority;
	}

	public String getHttpPassword() {
		// ignore
		return null;
	}

	public String getHttpUser() {
		// ignore
		return null;
	}

	public Proxy getProxy() {
		// ignore
		return null;
	}
	
}
