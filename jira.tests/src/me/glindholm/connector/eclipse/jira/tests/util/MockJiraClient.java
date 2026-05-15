/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package me.glindholm.connector.eclipse.jira.tests.util;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.net.WebLocation;

import me.glindholm.connector.eclipse.internal.jira.core.model.JiraAction;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraAttachment;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraComponent;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssue;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssueField;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssueType;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraNamedFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraPriority;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraProject;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraProjectRole;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraResolution;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraSecurityLevel;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraServerInfo;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraServerVersion;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraStatus;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraVersion;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.FilterDefinition;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.IssueCollector;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraClient;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraClientCache;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraException;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraLocalConfiguration;

public class MockJiraClient extends JiraClient {

	public static JiraComponent createComponent(final String id, final String name) {
		final var component = new JiraComponent();
		component.setId(id);
		component.setName(name);
		return component;
	}

	public static JiraIssueType createIssueType(final String id, final String name) {
		return new JiraIssueType(id, name, null, null);
	}

	public static JiraPriority createPriority(final String id, final String name) {
		return new JiraPriority(id, name, null, null, null);
	}

	public static JiraProject createProject() {
		final var project = new JiraProject();
		project.setId(JiraTestUtil.PROJECT1);
		project.setKey("PRONE");
		project.setName("Prone");

		final JiraComponent[] components = { createComponent("1", "component1"),
				createComponent("2", "component2"), createComponent("3", "component3"),
				createComponent("4", "component4"), };
		project.setComponents(components);

		final JiraVersion[] versions = { createVersion("1", "1.0"), createVersion("2", "2.0"),
				createVersion("3", "3.0"), createVersion("4", "4.0"), };
		project.setVersions(versions);

		return project;
	}

	public static JiraVersion createVersion(final String id, final String name) {
		return new JiraVersion(id, name);
	}

	private JiraClientCache cache;

	static class MockWebLocation extends WebLocation {
		MockWebLocation(final String baseUrl) {
			super(baseUrl);
			setCredentials(AuthenticationType.REPOSITORY, "username", "password");
		}
	}

	public MockJiraClient(final String baseUrl) {
		super(new MockWebLocation(baseUrl), new JiraLocalConfiguration(), new MockJiraRestClientAdapter(baseUrl, null));
		cache = super.getCache();
	}

	//	@Override
	//	public void addCommentToIssue(String issueKey, Comment comment, IProgressMonitor monitor) throws JiraException {
	//		// ignore
	//	}

	@Override
	public void addCommentToIssue(final String issueKey, final String comment, final IProgressMonitor monitor) throws JiraException {
		// ignore
	}

	@Override
	public void advanceIssueWorkflow(final JiraIssue issue, final String actionKey, final String comment, final IProgressMonitor monitor)
			throws JiraException {
		// ignore
	}

	@Override
	public void assignIssueTo(final JiraIssue issue, final String user, final String comment, final IProgressMonitor monitor)
			throws JiraException {
		// ignore
	}

	@Override
	public void addAttachment(final JiraIssue issue, final String comment, final String filename, final byte[] content, final IProgressMonitor monitor)
			throws JiraException {
		// ignore
	}

	@Override
	public JiraIssue createIssue(final JiraIssue issue, final IProgressMonitor monitor) throws JiraException {
		// ignore
		return null;
	}

	@Override
	public void deleteIssue(final JiraIssue issue, final IProgressMonitor monitor) throws JiraException {
		// ignore
	}

	//	@Override
	//	public void executeNamedFilter(NamedFilter filter, IssueCollector collector, IProgressMonitor monitor)
	//			throws JiraException {
	//		// ignore
	//	}

	@Override
	public void findIssues(final FilterDefinition filterDefinition, final IssueCollector collector, final IProgressMonitor monitor)
			throws JiraException {
		// ignore
	}

	@Override
	public List<JiraIssueField> getActionFields(final String issueKey, final String actionId, final IProgressMonitor monitor)
			throws JiraException {
		return null;
	}

	@Override
	public List<JiraAction> getAvailableActions(final String issueKey, final IProgressMonitor monitor) throws JiraException {
		return null;
	}

	@Override
	public JiraClientCache getCache() {
		return cache;
	}

	//	@Override
	//	public JiraComponent[] getJiraComponents(String key, IProgressMonitor monitor) throws JiraException {
	//		return null;
	//	}

	//	@Override
	//	public CustomField[] getCustomAttributes(IProgressMonitor monitor) throws JiraException {
	//		return null;
	//	}

	//	@Override
	//	public IssueField[] getEditableAttributes(String issueKey, IProgressMonitor monitor) throws JiraException {
	//		return null;
	//	}

	public JiraIssue getIssueById(final String issue) throws JiraException {
		return null;
	}

	@Override
	public JiraIssue getIssueByKey(final String issueKey, final IProgressMonitor monitor) throws JiraException {
		// ignore
		return null;
	}

	@Override
	public JiraIssueType[] getIssueTypes(final IProgressMonitor monitor) throws JiraException {
		return new JiraIssueType[0];
	}

	//	@Override
	//	public String getKeyFromId(String issueId, IProgressMonitor monitor) throws JiraException {
	//		return null;
	//	}

	@Override
	public JiraNamedFilter[] getNamedFilters(final IProgressMonitor monitor) throws JiraException {
		return null;
	}

	@Override
	public JiraPriority[] getPriorities(final IProgressMonitor monitor) throws JiraException {
		return new JiraPriority[0];
	}

	@Override
	public JiraProject[] getProjects(final IProgressMonitor monitor) throws JiraException {
		return new JiraProject[0];
	}

	@Override
	public JiraServerInfo getServerInfo(final IProgressMonitor monitor) throws JiraException {
		final var si = new JiraServerInfo();
		si.setVersion(JiraServerVersion.JIRA_8_0);
		return si;
	}

	@Override
	public JiraSecurityLevel[] getAvailableSecurityLevels(final String projectKey, final IProgressMonitor monitor)
			throws JiraException {
		return new JiraSecurityLevel[0];
	}

	//	@Override
	//	public JiraIssueType[] getSubTaskIssueTypes(final String projectId, IProgressMonitor monitor) throws JiraException {
	//		return new JiraIssueType[0];
	//	}
	//
	//	@Override
	//	public JiraIssueType[] getIssueTypes(String projectId, IProgressMonitor monitor) throws JiraException {
	//		return new JiraIssueType[0];
	//	}
	//
	//	@Override
	//	public JiraIssueType[] getSubTaskIssueTypes(IProgressMonitor monitor) throws JiraException {
	//		return new JiraIssueType[0];
	//	}

	//	@Override
	//	public JiraVersion[] getVersions(String key, IProgressMonitor monitor) throws JiraException {
	//		return null;
	//	}

	//	@Override
	//	public void login(IProgressMonitor monitor) throws JiraException {
	//	}

	@Override
	public void logout(final IProgressMonitor monitor) {
	}

	//	@Override
	//	public void quickSearch(String searchString, IssueCollector collector, IProgressMonitor monitor)
	//			throws JiraException {
	//		// ignore
	//	}

	@Override
	public InputStream getAttachment(final JiraIssue issue, final JiraAttachment attachment, final IProgressMonitor monitor)
			throws JiraException {
		// ignore
		return null;
	}

	@Override
	public void search(final JiraFilter query, final IssueCollector collector, final IProgressMonitor monitor) throws JiraException {
		// ignore
	}

	public void setCache(final JiraClientCache cache) {
		this.cache = cache;
	}

	@Override
	public void updateIssue(final JiraIssue issue, final String comment, final Set<String> changeIds, final IProgressMonitor monitor) throws JiraException {
		// ignore
	}

	//	@Override
	//	public JiraWorkLog[] getWorklogs(String issueKey, IProgressMonitor monitor) throws JiraException {
	//		return new JiraWorkLog[0];
	//	}

	@Override
	public JiraProjectRole[] getProjectRoles(final IProgressMonitor monitor) throws JiraException {
		return new JiraProjectRole[0];
	}

	@Override
	public JiraResolution[] getResolutions(final IProgressMonitor monitor) throws JiraException {
		return new JiraResolution[0];
	}

	@Override
	public JiraStatus[] getStatuses(final IProgressMonitor monitor) throws JiraException {
		return new JiraStatus[0];
	}

}
