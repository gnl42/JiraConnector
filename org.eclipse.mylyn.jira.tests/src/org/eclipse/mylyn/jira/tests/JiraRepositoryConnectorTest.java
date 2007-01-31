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

import java.net.MalformedURLException;

import junit.framework.TestCase;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylar.internal.jira.core.JiraServerFacade;
import org.eclipse.mylar.internal.jira.core.ui.JiraUiPlugin;
import org.eclipse.mylar.internal.tasks.ui.wizards.EditRepositoryWizard;
import org.eclipse.mylar.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.core.TaskRepositoryManager;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.mylar.jira.core.internal.service.JiraServer;

/**
 * @author Steffen Pingel
 */
public class JiraRepositoryConnectorTest extends TestCase {

	private final static String USER = "mylartest";

	private final static String PASSWORD = "mylartest";

	private final static String SERVER_URL = "http://developer.atlassian.com/jira";

	private TaskRepository repository;

	private TaskRepositoryManager manager;

//	private JiraRepositoryConnector connector;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		manager = TasksUiPlugin.getRepositoryManager();
		manager.clearRepositories(TasksUiPlugin.getDefault().getRepositoriesFilePath());
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	protected void init() {
		String kind = JiraUiPlugin.REPOSITORY_KIND;

		repository = new TaskRepository(kind, SERVER_URL);
		repository.setAuthenticationCredentials(USER, PASSWORD);

		manager.addRepository(repository, TasksUiPlugin.getDefault().getRepositoriesFilePath());

		AbstractRepositoryConnector abstractConnector = manager.getRepositoryConnector(kind);
		assertEquals(abstractConnector.getRepositoryType(), kind);

//		connector = (JiraRepositoryConnector) abstractConnector;
		TasksUiPlugin.getSynchronizationManager().setForceSyncExec(true);
	}

	public void testChangeTaskRepositorySettings() throws MalformedURLException {
		init();
		JiraServer server = JiraServerFacade.getDefault().getJiraServer(repository);
		assertEquals(USER, server.getCurrentUserName());

		EditRepositoryWizard wizard = new EditRepositoryWizard(repository);
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		WizardDialog dialog = new WizardDialog(shell, wizard);
		dialog.create();

		wizard.getSettingsPage().setUserId("newuser");
		assertTrue(wizard.performFinish());

		server = JiraServerFacade.getDefault().getJiraServer(repository);
		assertEquals("newuser", server.getCurrentUserName());
	}

}
