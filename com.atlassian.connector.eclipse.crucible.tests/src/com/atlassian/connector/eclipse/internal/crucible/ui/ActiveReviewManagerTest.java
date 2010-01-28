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
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.State;
import com.atlassian.theplugin.commons.crucible.api.model.User;

import org.eclipse.mylyn.internal.tasks.core.LocalTask;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

public class ActiveReviewManagerTest extends TestCase {

	private ActiveReviewManager activeReviewManager;

	@Override
	protected void setUp() throws Exception {
		activeReviewManager = new ActiveReviewManager(false);
		TasksUi.getTaskActivityManager().addActivationListener(activeReviewManager);
		CrucibleUiPlugin.getDefault().disableActiveReviewManager();
	}

	@Override
	protected void tearDown() throws Exception {
		TasksUi.getTaskActivityManager().removeActivationListener(activeReviewManager);
		CrucibleUiPlugin.getDefault().enableActiveReviewManager();
		activeReviewManager = null;
	}

	public void testReviewActivated() {
		String taskKey = "test-2";
		String taskId = CrucibleUtil.getTaskIdFromPermId(taskKey);

		String repositoryUrl = "https://testServer.com";

		TaskRepository taskRepository = new TaskRepository(CrucibleCorePlugin.CONNECTOR_KIND, repositoryUrl);
		TasksUi.getRepositoryManager().addRepository(taskRepository);

		ITask createdTask = TasksUi.getRepositoryModel().createTask(taskRepository, taskId);

		Review review = createReview(repositoryUrl, taskKey);
		CrucibleCorePlugin.getDefault().getReviewCache().updateCachedReview(repositoryUrl, taskId, review);

		assertFalse(activeReviewManager.isReviewActive());
		assertNull(activeReviewManager.getActiveReview());
		assertNull(activeReviewManager.getActiveTask());

		TasksUi.getTaskActivityManager().activateTask(createdTask);

		assertTrue(activeReviewManager.isReviewActive());
		assertEquals(review, activeReviewManager.getActiveReview());
		assertEquals(createdTask, activeReviewManager.getActiveTask());

	}

	public void testLocalTaskActivated() {
		assertFalse(activeReviewManager.isReviewActive());
		assertNull(activeReviewManager.getActiveReview());
		assertNull(activeReviewManager.getActiveTask());

		LocalTask task = TasksUiInternal.createNewLocalTask(null);
		TasksUi.getTaskActivityManager().activateTask(task);

		assertFalse(activeReviewManager.isReviewActive());
		assertNull(activeReviewManager.getActiveReview());
		assertNull(activeReviewManager.getActiveTask());
	}

	public void testReviewUpdatedWhileActive() {
		String taskKey = "test-2";
		String taskId = CrucibleUtil.getTaskIdFromPermId(taskKey);

		String repositoryUrl = "https://testServer.com";

		TaskRepository taskRepository = new TaskRepository(CrucibleCorePlugin.CONNECTOR_KIND, repositoryUrl);
		TasksUi.getRepositoryManager().addRepository(taskRepository);

		ITask createdTask = TasksUi.getRepositoryModel().createTask(taskRepository, taskId);

		Review review = createReview(repositoryUrl, taskKey);
		CrucibleCorePlugin.getDefault().getReviewCache().updateCachedReview(repositoryUrl, taskId, review);

		assertFalse(activeReviewManager.isReviewActive());
		assertNull(activeReviewManager.getActiveReview());
		assertNull(activeReviewManager.getActiveTask());

		TasksUi.getTaskActivityManager().activateTask(createdTask);

		assertTrue(activeReviewManager.isReviewActive());
		assertEquals(review, activeReviewManager.getActiveReview());
		assertEquals(createdTask, activeReviewManager.getActiveTask());

		Review updatedReview = createReview(repositoryUrl, taskKey);
		updatedReview.setDescription("something new");
		CrucibleCorePlugin.getDefault().getReviewCache().updateCachedReview(repositoryUrl, taskId, updatedReview);

		assertTrue(activeReviewManager.isReviewActive());
		assertEquals(updatedReview, activeReviewManager.getActiveReview());
		assertEquals(createdTask, activeReviewManager.getActiveTask());

	}

	public void testDifferentReviewUpdatedWhileActive() {
		String taskKey = "test-2";
		String taskKey2 = "test-3";
		String taskId = CrucibleUtil.getTaskIdFromPermId(taskKey);

		String repositoryUrl = "https://testServer.com";

		TaskRepository taskRepository = new TaskRepository(CrucibleCorePlugin.CONNECTOR_KIND, repositoryUrl);
		TasksUi.getRepositoryManager().addRepository(taskRepository);

		ITask createdTask = TasksUi.getRepositoryModel().createTask(taskRepository, taskId);

		Review review = createReview(repositoryUrl, taskKey);
		CrucibleCorePlugin.getDefault().getReviewCache().updateCachedReview(repositoryUrl, taskId, review);

		assertFalse(activeReviewManager.isReviewActive());
		assertNull(activeReviewManager.getActiveReview());
		assertNull(activeReviewManager.getActiveTask());

		TasksUi.getTaskActivityManager().activateTask(createdTask);

		assertTrue(activeReviewManager.isReviewActive());
		assertEquals(review, activeReviewManager.getActiveReview());
		assertEquals(createdTask, activeReviewManager.getActiveTask());

		Review updatedReview = createReview(repositoryUrl, taskKey2);
		CrucibleCorePlugin.getDefault().getReviewCache().updateCachedReview(repositoryUrl, taskId, updatedReview);

		assertTrue(activeReviewManager.isReviewActive());
		assertEquals(review, activeReviewManager.getActiveReview());
		assertEquals(createdTask, activeReviewManager.getActiveTask());

	}

	public void testReviewUpdatedWhileNotActive() {
		String taskKey = "test-2";
		String taskKey2 = "test-3";
		String taskId = CrucibleUtil.getTaskIdFromPermId(taskKey);

		String repositoryUrl = "https://testServer.com";

		TaskRepository taskRepository = new TaskRepository(CrucibleCorePlugin.CONNECTOR_KIND, repositoryUrl);
		TasksUi.getRepositoryManager().addRepository(taskRepository);

		Review review = createReview(repositoryUrl, taskKey);
		CrucibleCorePlugin.getDefault().getReviewCache().updateCachedReview(repositoryUrl, taskId, review);

		assertFalse(activeReviewManager.isReviewActive());
		assertNull(activeReviewManager.getActiveReview());
		assertNull(activeReviewManager.getActiveTask());

		Review updatedReview = createReview(repositoryUrl, taskKey2);
		CrucibleCorePlugin.getDefault().getReviewCache().updateCachedReview(repositoryUrl, taskId, updatedReview);

		assertFalse(activeReviewManager.isReviewActive());
		assertNull(activeReviewManager.getActiveReview());
		assertNull(activeReviewManager.getActiveTask());
	}

	public void testReviewAddedWhileActive() {
		String taskKey = "test-2";
		String taskId = CrucibleUtil.getTaskIdFromPermId(taskKey);

		String repositoryUrl = "https://testServer.com";

		TaskRepository taskRepository = new TaskRepository(CrucibleCorePlugin.CONNECTOR_KIND, repositoryUrl);
		TasksUi.getRepositoryManager().addRepository(taskRepository);

		ITask createdTask = TasksUi.getRepositoryModel().createTask(taskRepository, taskId);

		Review review = createReview(repositoryUrl, taskKey);
		CrucibleCorePlugin.getDefault().getReviewCache().updateCachedReview(repositoryUrl, taskId, review);

		assertFalse(activeReviewManager.isReviewActive());
		assertNull(activeReviewManager.getActiveReview());
		assertNull(activeReviewManager.getActiveTask());

		TasksUi.getTaskActivityManager().activateTask(createdTask);

		assertTrue(activeReviewManager.isReviewActive());
		assertEquals(review, activeReviewManager.getActiveReview());
		assertEquals(createdTask, activeReviewManager.getActiveTask());

		Review updatedReview = createReview(repositoryUrl, taskKey);
		updatedReview.setDescription("something new");
		activeReviewManager.reviewAdded(repositoryUrl, taskId, updatedReview);

		assertTrue(activeReviewManager.isReviewActive());
		assertEquals(updatedReview, activeReviewManager.getActiveReview());
		assertEquals(createdTask, activeReviewManager.getActiveTask());

	}

	public void testDifferentReviewAddedWhileActive() {
		String taskKey = "test-2";
		String taskKey2 = "test-3";
		String taskId = CrucibleUtil.getTaskIdFromPermId(taskKey);
		String taskId2 = CrucibleUtil.getTaskIdFromPermId(taskKey2);

		String repositoryUrl = "https://testServer.com";

		TaskRepository taskRepository = new TaskRepository(CrucibleCorePlugin.CONNECTOR_KIND, repositoryUrl);
		TasksUi.getRepositoryManager().addRepository(taskRepository);

		ITask createdTask = TasksUi.getRepositoryModel().createTask(taskRepository, taskId);

		Review review = createReview(repositoryUrl, taskKey);
		CrucibleCorePlugin.getDefault().getReviewCache().updateCachedReview(repositoryUrl, taskId, review);

		assertFalse(activeReviewManager.isReviewActive());
		assertNull(activeReviewManager.getActiveReview());
		assertNull(activeReviewManager.getActiveTask());

		TasksUi.getTaskActivityManager().activateTask(createdTask);

		assertTrue(activeReviewManager.isReviewActive());
		assertEquals(review, activeReviewManager.getActiveReview());
		assertEquals(createdTask, activeReviewManager.getActiveTask());

		Review updatedReview = createReview(repositoryUrl, taskKey2);
		activeReviewManager.reviewAdded(repositoryUrl, taskId2, updatedReview);

		assertTrue(activeReviewManager.isReviewActive());
		assertEquals(review, activeReviewManager.getActiveReview());
		assertEquals(createdTask, activeReviewManager.getActiveTask());

		updatedReview = createReview(repositoryUrl, taskKey);
		activeReviewManager.reviewAdded(repositoryUrl, taskId, updatedReview);

		assertTrue(activeReviewManager.isReviewActive());
		assertEquals(updatedReview, activeReviewManager.getActiveReview());
		assertEquals(createdTask, activeReviewManager.getActiveTask());

	}

	public void testReviewAddedWhileNotActive() {
		String taskKey = "test-2";
		String taskKey2 = "test-3";
		String taskId = CrucibleUtil.getTaskIdFromPermId(taskKey);

		String repositoryUrl = "https://testServer.com";

		TaskRepository taskRepository = new TaskRepository(CrucibleCorePlugin.CONNECTOR_KIND, repositoryUrl);
		TasksUi.getRepositoryManager().addRepository(taskRepository);

		Review review = createReview(repositoryUrl, taskKey);
		CrucibleCorePlugin.getDefault().getReviewCache().updateCachedReview(repositoryUrl, taskId, review);

		assertFalse(activeReviewManager.isReviewActive());
		assertNull(activeReviewManager.getActiveReview());
		assertNull(activeReviewManager.getActiveTask());

		Review updatedReview = createReview(repositoryUrl, taskKey2);
		activeReviewManager.reviewAdded(repositoryUrl, taskId, updatedReview);

		assertFalse(activeReviewManager.isReviewActive());
		assertNull(activeReviewManager.getActiveReview());
		assertNull(activeReviewManager.getActiveTask());
	}

	private Review createReview(String repositoryUrl, String taskKey) {
		Review review = new Review(repositoryUrl, "prj", new User("aut"), new User("mod"));
		Set<CrucibleAction> actions = new LinkedHashSet<CrucibleAction>();
		actions.add(CrucibleAction.ABANDON);
		actions.add(CrucibleAction.APPROVE);
		review.setActions(actions);
		review.setAllowReviewerToJoin(true);
		review.setCloseDate(new Date(1L));
		review.setCreateDate(new Date(1L));
		review.setCreator(new User("cre"));
//		review.setProjectKey("pro");
		review.setDescription("des");
		Set<CrucibleFileInfo> files = new LinkedHashSet<CrucibleFileInfo>();
		review.setFiles(files);
		List<Comment> genC = new ArrayList<Comment>();
		review.setGeneralComments(genC);
		review.setMetricsVersion(5);
		review.setName("nam");
		review.setPermId(new PermId(taskKey));
		review.setRepoName("rep");
		Set<Reviewer> reviewers = new LinkedHashSet<Reviewer>();
		Reviewer reviewer = new Reviewer("user", false);
		reviewers.add(reviewer);
		review.setReviewers(reviewers);
		review.setState(State.CLOSED);
		return review;
	}

}
