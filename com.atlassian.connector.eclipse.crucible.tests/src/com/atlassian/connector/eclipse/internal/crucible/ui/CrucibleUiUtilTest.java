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
import com.atlassian.theplugin.commons.crucible.api.model.PermIdBean;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewBean;

import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;

import junit.framework.TestCase;

public class CrucibleUiUtilTest extends TestCase {

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
}
