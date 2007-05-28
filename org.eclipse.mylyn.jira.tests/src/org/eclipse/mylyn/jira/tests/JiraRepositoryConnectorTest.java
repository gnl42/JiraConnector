/*******************************************************************************
 * Copyright (c) 2006 - 2006 Mylar eclipse.org project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mylar project committers - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.jira.tests;

import java.io.File;
import java.util.Collections;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylar.context.core.ContextCorePlugin;
import org.eclipse.mylar.context.tests.support.MylarTestUtils;
import org.eclipse.mylar.context.tests.support.MylarTestUtils.Credentials;
import org.eclipse.mylar.context.tests.support.MylarTestUtils.PrivilegeLevel;
import org.eclipse.mylar.internal.jira.core.model.CustomField;
import org.eclipse.mylar.internal.jira.core.model.Issue;
import org.eclipse.mylar.internal.jira.core.service.JiraClient;
import org.eclipse.mylar.internal.jira.ui.JiraClientFacade;
import org.eclipse.mylar.internal.jira.ui.JiraRepositoryConnector;
import org.eclipse.mylar.internal.jira.ui.JiraUiPlugin;
import org.eclipse.mylar.internal.tasks.ui.wizards.EditRepositoryWizard;
import org.eclipse.mylar.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylar.tasks.core.AbstractRepositoryTask;
import org.eclipse.mylar.tasks.core.RepositoryAttachment;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.core.TaskRepositoryManager;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * @author Steffen Pingel
 */
public class JiraRepositoryConnectorTest extends TestCase {

	private TaskRepository repository;

	private TaskRepositoryManager manager;

	private JiraRepositoryConnector connector;

	private JiraClient server;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		manager = TasksUiPlugin.getRepositoryManager();
		manager.clearRepositories(TasksUiPlugin.getDefault().getRepositoriesFilePath());
		
		JiraClientFacade.getDefault().clearClients();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	protected void init(String url) throws Exception {
		String kind = JiraUiPlugin.REPOSITORY_KIND;

		repository = new TaskRepository(kind, url);
		Credentials credentials = MylarTestUtils.readCredentials(PrivilegeLevel.USER);
		repository.setAuthenticationCredentials(credentials.username, credentials.password);

		manager.addRepository(repository, TasksUiPlugin.getDefault().getRepositoriesFilePath());

		AbstractRepositoryConnector abstractConnector = manager.getRepositoryConnector(kind);
		assertEquals(abstractConnector.getRepositoryType(), kind);

		connector = (JiraRepositoryConnector) abstractConnector;
		TasksUiPlugin.getSynchronizationManager().setForceSyncExec(true);

		server = JiraClientFacade.getDefault().getJiraClient(repository);
	}

	public void testChangeTaskRepositorySettings() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);
		assertEquals(repository.getUserName(), server.getUserName());

		EditRepositoryWizard wizard = new EditRepositoryWizard(repository);
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		WizardDialog dialog = new WizardDialog(shell, wizard);
		dialog.create();

		wizard.getSettingsPage().setUserId("newuser");
		assertTrue(wizard.performFinish());

		server = JiraClientFacade.getDefault().getJiraClient(repository);
		assertEquals("newuser", server.getUserName());
	}

	public void testUpdateTask() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);
		
		Issue issue = JiraTestUtils.createIssue(server, "testUpdateTask");
		AbstractRepositoryTask task = connector.createTaskFromExistingId(repository, issue.getKey(), new NullProgressMonitor());
		assertEquals("testUpdateTask", task.getSummary());
		assertEquals(false, task.isCompleted());
		assertNull(task.getDueDate());

		String issueKey = issue.getKey();
		
		String fieldId = "customfield_10011";
		String fieldName = "Free Text";

		issue = server.getIssueByKey(issueKey);
		issue.setCustomFields(new CustomField[] { new CustomField(fieldId, //
				"com.atlassian.jira.plugin.system.customfieldtypes:textfield", // 
				fieldName, Collections.singletonList("foo")) });
		server.updateIssue(issue, "add comment");

		issue = server.getIssueByKey(issueKey);
		CustomField customField = issue.getCustomFieldById(fieldId);
		assertNotNull("Expecting to see custom field " + fieldName, customField);
		assertEquals(fieldName, customField.getName());
		assertEquals(1, customField.getValues().size());
		assertEquals("foo", customField.getValues().get(0));
		
		issue = server.getIssueByKey(issueKey);
		server.resolveIssue(issue, JiraTestUtils.getFixedResolution(server), null, "comment", JiraClient.ASSIGNEE_DEFAULT, "");
		TasksUiPlugin.getSynchronizationManager().synchronize(connector, task, true, null);
		assertEquals("testUpdateTask", task.getSummary());
		assertEquals(true, task.isCompleted());
		assertNotNull(task.getCompletionDate());

		issue = server.getIssueByKey(issueKey);
		customField = issue.getCustomFieldById(fieldId);
		assertNotNull("Expecting to see custom field " + fieldName, customField);
		assertEquals(fieldName, customField.getName());
		assertEquals(1, customField.getValues().size());
		assertEquals("foo", customField.getValues().get(0));
		
		issue = server.getIssueByKey(issue.getKey());
		server.closeIssue(issue, JiraTestUtils.getFixedResolution(server), null, "comment", JiraClient.ASSIGNEE_DEFAULT, "");
		TasksUiPlugin.getSynchronizationManager().synchronize(connector, task, true, null);
		assertEquals("testUpdateTask", task.getSummary());
		assertEquals(true, task.isCompleted());
		assertNotNull(task.getCompletionDate());
	}

	public void testAttachContext() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		Issue issue = JiraTestUtils.createIssue(server, "testAttachContext");
		AbstractRepositoryTask task = connector.createTaskFromExistingId(repository, issue.getKey(), new NullProgressMonitor());
		assertEquals("testAttachContext", task.getSummary());

		File sourceContextFile = ContextCorePlugin.getContextManager().getFileForContext(task.getHandleIdentifier());
		JiraTestUtils.writeFile(sourceContextFile, "Mylar".getBytes());
		sourceContextFile.deleteOnExit();

		assertTrue(connector.attachContext(repository, task, "", new NullProgressMonitor()));
		
		TasksUiPlugin.getSynchronizationManager().synchronize(connector, task, true, null);

		Set<RepositoryAttachment> contextAttachments = connector.getContextAttachments(repository, task);
		assertEquals(1, contextAttachments.size());
		
		RepositoryAttachment attachment = contextAttachments.iterator().next();
		assertTrue(connector.retrieveContext(repository, task, attachment, System.getProperty("java.io.tmpdir"), new NullProgressMonitor()));
	}
	
}
