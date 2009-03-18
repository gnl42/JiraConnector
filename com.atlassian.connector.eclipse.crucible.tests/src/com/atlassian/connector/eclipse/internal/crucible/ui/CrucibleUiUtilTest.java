/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.crucible.ui;

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.ui.team.CrucibleFile;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfoImpl;
import com.atlassian.theplugin.commons.crucible.api.model.PermIdBean;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewBean;

import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.tests.connector.MockTask;
import org.eclipse.mylyn.tasks.ui.TasksUi;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

/**
 * @author Shawn Minto
 */
public class CrucibleUiUtilTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
		resetTaskListAndRepositories();
	}

	public static void resetTaskListAndRepositories() throws Exception {
		TasksUiPlugin.getRepositoryManager().clearRepositories(TasksUiPlugin.getDefault().getRepositoriesFilePath());
		TasksUiPlugin.getDefault().getLocalTaskRepository();
		resetTaskList();
	}

	public static void resetTaskList() throws Exception {
		TasksUi.getTaskActivityManager().deactivateActiveTask();
		TasksUiPlugin.getTaskListExternalizationParticipant().resetTaskList();
		TaskListView view = TaskListView.getFromActivePerspective();
		if (view != null) {
			view.refresh();
		}
	}

	public void testGetCrucibleTaskRepositoryFromString() {

		String repositoryUrl = "https://testServer.com";
		String anotherRepositoryUrl = "http://testServer.com";

		assertNull(CrucibleUiUtil.getCrucibleTaskRepository((String) null));
		assertNull(CrucibleUiUtil.getCrucibleTaskRepository(repositoryUrl));
		assertNull(CrucibleUiUtil.getCrucibleTaskRepository(anotherRepositoryUrl));

		TaskRepository taskRepository = new TaskRepository(CrucibleCorePlugin.CONNECTOR_KIND, repositoryUrl);

		TasksUi.getRepositoryManager().addRepository(taskRepository);

		TaskRepository retrievedRepository = CrucibleUiUtil.getCrucibleTaskRepository(repositoryUrl);
		assertNotNull(retrievedRepository);
		assertEquals(taskRepository, retrievedRepository);

		assertNull(CrucibleUiUtil.getCrucibleTaskRepository(anotherRepositoryUrl));

		((TaskRepositoryManager) TasksUi.getRepositoryManager()).removeRepository(taskRepository,
				TasksUiPlugin.getDefault().getRepositoriesFilePath());
	}

	public void testGetCrucibleTaskFromId() {
		String taskKey = "test-2";
		String taskId = CrucibleUtil.getTaskIdFromPermId(taskKey);

		String taskKey2 = "test-3";
		String taskId2 = CrucibleUtil.getTaskIdFromPermId(taskKey2);

		assertNull(CrucibleUiUtil.getCrucibleTask(null, null));
		assertNull(CrucibleUiUtil.getCrucibleTask(null, taskId));

		String repositoryUrl = "https://testServer.com";

		assertNull(CrucibleUiUtil.getCrucibleTaskRepository(repositoryUrl));

		TaskRepository taskRepository = new TaskRepository(CrucibleCorePlugin.CONNECTOR_KIND, repositoryUrl);
		TasksUi.getRepositoryManager().addRepository(taskRepository);

		assertNull(CrucibleUiUtil.getCrucibleTask(taskRepository, taskId));

		ITask createdTask = TasksUi.getRepositoryModel().createTask(taskRepository, taskId);
		ITask task = CrucibleUiUtil.getCrucibleTask(taskRepository, taskId);
		assertNotNull(task);
		assertEquals(createdTask, task);

		assertNull(CrucibleUiUtil.getCrucibleTask(taskRepository, taskId2));

		TasksUiPlugin.getTaskList().deleteTask(createdTask);
		((TaskRepositoryManager) TasksUi.getRepositoryManager()).removeRepository(taskRepository,
				TasksUiPlugin.getDefault().getRepositoriesFilePath());

	}

	public void testGetCrucibleTaskRepositoryFromReview() {

		String repositoryUrl = "https://testServer.com";

		Review review = new ReviewBean(repositoryUrl);

		assertNull(CrucibleUiUtil.getCrucibleTaskRepository((Review) null));
		assertNull(CrucibleUiUtil.getCrucibleTaskRepository(review));

		TaskRepository taskRepository = new TaskRepository(CrucibleCorePlugin.CONNECTOR_KIND, repositoryUrl);

		TasksUi.getRepositoryManager().addRepository(taskRepository);

		TaskRepository reviewTaskRepository = CrucibleUiUtil.getCrucibleTaskRepository(review);
		assertNotNull(reviewTaskRepository);
		assertEquals(taskRepository, reviewTaskRepository);

		((TaskRepositoryManager) TasksUi.getRepositoryManager()).removeRepository(taskRepository,
				TasksUiPlugin.getDefault().getRepositoriesFilePath());
	}

	public void testGetCrucibleTaskFromReview() {

		String taskKey = "test-3";
		String taskId = CrucibleUtil.getTaskIdFromPermId(taskKey);

		String repositoryUrl = "https://testServer.com";

		Review review = new ReviewBean(repositoryUrl);
		review.setPermId(new PermIdBean(taskKey));

		assertNull(CrucibleUiUtil.getCrucibleTask(null));
		assertNull(CrucibleUiUtil.getCrucibleTask(review));

		assertNull(CrucibleUiUtil.getCrucibleTaskRepository(repositoryUrl));

		TaskRepository taskRepository = new TaskRepository(CrucibleCorePlugin.CONNECTOR_KIND, repositoryUrl);
		TasksUi.getRepositoryManager().addRepository(taskRepository);

		assertNull(CrucibleUiUtil.getCrucibleTask(review));

		ITask createdTask = TasksUi.getRepositoryModel().createTask(taskRepository, taskId);
		ITask task = CrucibleUiUtil.getCrucibleTask(review);
		assertNotNull(task);
		assertEquals(createdTask, task);

		TasksUiPlugin.getTaskList().deleteTask(createdTask);
		((TaskRepositoryManager) TasksUi.getRepositoryManager()).removeRepository(taskRepository,
				TasksUiPlugin.getDefault().getRepositoriesFilePath());

	}

	public void testIsFilePartOfActiveReview() {
		String repositoryUrl = "http://crucible.atlassian.com/cru/";

		Review reviewA = new ReviewBean(repositoryUrl);
		Review reviewB = new ReviewBean(repositoryUrl);

		ITask task = new MockTask("A");

		CrucibleFile file1 = new CrucibleFile(new CrucibleFileInfoImpl(new VersionedVirtualFile("abc", "123"), null,
				new PermIdBean("1")), false);
		CrucibleFile file2 = new CrucibleFile(new CrucibleFileInfoImpl(new VersionedVirtualFile("def", "456"), null,
				new PermIdBean("2")), false);
		CrucibleFile file3 = new CrucibleFile(new CrucibleFileInfoImpl(new VersionedVirtualFile("ghi", "789"), null,
				new PermIdBean("3")), false);
		CrucibleFile file4 = new CrucibleFile(new CrucibleFileInfoImpl(new VersionedVirtualFile(null, null), null,
				new PermIdBean("4")), false);
		CrucibleFile file5 = new CrucibleFile(new CrucibleFileInfoImpl(null, null, new PermIdBean("5")), false);
		CrucibleFile file6 = new CrucibleFile(null, false);

		Set<CrucibleFileInfo> setA = new HashSet<CrucibleFileInfo>();
		setA.add(file1.getCrucibleFileInfo());
		setA.add(file2.getCrucibleFileInfo());
		Set<CrucibleFileInfo> setB = new HashSet<CrucibleFileInfo>();
		setB.add(file2.getCrucibleFileInfo());
		setB.add(file3.getCrucibleFileInfo());
		reviewA.setFiles(setA);
		reviewB.setFiles(setB);

		CrucibleUiPlugin.getDefault().getActiveReviewManager().setActiveReview(reviewA, task);

		assertTrue(CrucibleUiUtil.isFilePartOfActiveReview(file1));
		assertTrue(CrucibleUiUtil.isFilePartOfActiveReview(file2));
		assertFalse(CrucibleUiUtil.isFilePartOfActiveReview(file3));

		CrucibleUiPlugin.getDefault().getActiveReviewManager().setActiveReview(reviewB, task);

		assertFalse(CrucibleUiUtil.isFilePartOfActiveReview(file1));
		assertTrue(CrucibleUiUtil.isFilePartOfActiveReview(file2));
		assertTrue(CrucibleUiUtil.isFilePartOfActiveReview(file3));

		assertFalse(CrucibleUiUtil.isFilePartOfActiveReview(file4));
		assertFalse(CrucibleUiUtil.isFilePartOfActiveReview(file5));
		assertFalse(CrucibleUiUtil.isFilePartOfActiveReview(file6));
	}
}
