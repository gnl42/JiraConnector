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
import org.eclipse.mylyn.commons.net.WebLocation;
import org.eclipse.mylyn.internal.jira.core.model.Attachment;
import org.eclipse.mylyn.internal.jira.core.model.Component;
import org.eclipse.mylyn.internal.jira.core.model.CustomField;
import org.eclipse.mylyn.internal.jira.core.model.IssueType;
import org.eclipse.mylyn.internal.jira.core.model.JiraAction;
import org.eclipse.mylyn.internal.jira.core.model.JiraField;
import org.eclipse.mylyn.internal.jira.core.model.JiraIssue;
import org.eclipse.mylyn.internal.jira.core.model.JiraStatus;
import org.eclipse.mylyn.internal.jira.core.model.NamedFilter;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.JiraFilter;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.model.ServerInfo;
import org.eclipse.mylyn.internal.jira.core.model.Version;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.jira.core.model.filter.IssueCollector;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraClientCache;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;

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
	public void addCommentToIssue(JiraIssue issue, String comment, IProgressMonitor monitor) throws JiraException {
		// ignore
	}

	@Override
	public void advanceIssueWorkflow(JiraIssue issue, String actionKey, String comment, IProgressMonitor monitor)
			throws JiraException {
		// ignore
	}

	@Override
	public void assignIssueTo(JiraIssue issue, int assigneeType, String user, String comment, IProgressMonitor monitor)
			throws JiraException {
		// ignore
	}

	@Override
	public void addAttachment(JiraIssue issue, String comment, PartSource partSource, String contentType,
			IProgressMonitor monitor) throws JiraException {
		// ignore
	}

	@Override
	public void addAttachment(JiraIssue issue, String comment, String filename, byte[] contents, String contentType,
			IProgressMonitor monitor) throws JiraException {
		// ignore
	}

	@Override
	public void addAttachment(JiraIssue issue, String comment, String filename, File file, String contentType,
			IProgressMonitor monitor) throws JiraException {
		// ignore
	}

	@Override
	public JiraIssue createIssue(JiraIssue issue, IProgressMonitor monitor) throws JiraException {
		// ignore
		return null;
	}

	@Override
	public JiraIssue createSubTask(JiraIssue issue, IProgressMonitor monitor) throws JiraException {
		// ignore
		return null;
	}

	@Override
	public void deleteIssue(JiraIssue issue, IProgressMonitor monitor) throws JiraException {
		// ignore
	}

	@Override
	public void executeNamedFilter(NamedFilter filter, IssueCollector collector, IProgressMonitor monitor)
			throws JiraException {
		// ignore
	}

	@Override
	public void findIssues(FilterDefinition filterDefinition, IssueCollector collector, IProgressMonitor monitor)
			throws JiraException {
		// ignore
	}

	@Override
	public String[] getActionFields(String issueKey, String actionId, IProgressMonitor monitor) throws JiraException {
		return null;
	}

	@Override
	public JiraAction[] getAvailableActions(String issueKey, IProgressMonitor monitor) throws JiraException {
		return null;
	}

	@Override
	public JiraClientCache getCache() {
		return this.cache;
	}

	@Override
	public Component[] getComponents(String key, IProgressMonitor monitor) throws JiraException {
		return null;
	}

	@Override
	public CustomField[] getCustomAttributes(IProgressMonitor monitor) throws JiraException {
		return null;
	}

	@Override
	public JiraField[] getEditableAttributes(String issueKey, IProgressMonitor monitor) throws JiraException {
		return null;
	}

	public JiraIssue getIssueById(String issue) throws JiraException {
		return null;
	}

	@Override
	public JiraIssue getIssueByKey(String issueKey, IProgressMonitor monitor) throws JiraException {
		// ignore
		return null;
	}

	@Override
	public IssueType[] getIssueTypes(IProgressMonitor monitor) throws JiraException {
		return null;
	}

	@Override
	public String getKeyFromId(String issueId, IProgressMonitor monitor) throws JiraException {
		return null;
	}

	@Override
	public NamedFilter[] getNamedFilters(IProgressMonitor monitor) throws JiraException {
		return null;
	}

	@Override
	public Priority[] getPriorities(IProgressMonitor monitor) throws JiraException {
		return null;
	}

	@Override
	public Project[] getProjects(IProgressMonitor monitor) throws JiraException {
		return null;
	}

	@Override
	public Resolution[] getResolutions(IProgressMonitor monitor) throws JiraException {
		return null;
	}

	@Override
	public ServerInfo getServerInfo(IProgressMonitor monitor) throws JiraException {
		return null;
	}

	@Override
	public JiraStatus[] getStatuses(IProgressMonitor monitor) throws JiraException {
		return null;
	}

	@Override
	public IssueType[] getSubTaskIssueTypes(IProgressMonitor monitor) throws JiraException {
		return null;
	}

	@Override
	public Version[] getVersions(String key, IProgressMonitor monitor) throws JiraException {
		return null;
	}

	@Override
	public void login(IProgressMonitor monitor) throws JiraException {
	}

	@Override
	public void logout(IProgressMonitor monitor) {
	}

	@Override
	public void quickSearch(String searchString, IssueCollector collector, IProgressMonitor monitor)
			throws JiraException {
		// ignore
	}

	@Override
	public byte[] getAttachment(JiraIssue issue, Attachment attachment, IProgressMonitor monitor) throws JiraException {
		// ignore
		return null;
	}

	@Override
	public void getAttachment(JiraIssue issue, Attachment attachment, OutputStream out, IProgressMonitor monitor)
			throws JiraException {
		// ignore
	}

	@Override
	public void search(JiraFilter query, IssueCollector collector, IProgressMonitor monitor) throws JiraException {
		// ignore
	}

	public void setCache(JiraClientCache cache) {
		this.cache = cache;
	}

	@Override
	public void unvoteIssue(JiraIssue issue, IProgressMonitor monitor) throws JiraException {
		// ignore
	}

	@Override
	public void unwatchIssue(JiraIssue issue, IProgressMonitor monitor) throws JiraException {
		// ignore
	}

	@Override
	public void updateIssue(JiraIssue issue, String comment, IProgressMonitor monitor) throws JiraException {
		// ignore
	}

	@Override
	public void voteIssue(JiraIssue issue, IProgressMonitor monitor) throws JiraException {
		// ignore
	}

	@Override
	public void watchIssue(JiraIssue issue, IProgressMonitor monitor) throws JiraException {
		// ignore
	}

}
