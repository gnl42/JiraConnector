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
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleRepositoryConnector;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClientData;
import com.atlassian.connector.eclipse.internal.crucible.core.client.model.CrucibleCachedProject;
import com.atlassian.connector.eclipse.internal.crucible.core.client.model.CrucibleCachedRepository;
import com.atlassian.connector.eclipse.internal.crucible.core.client.model.CrucibleCachedUser;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.OpenReviewEditorToCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.annotations.CrucibleAnnotationModel;
import com.atlassian.connector.eclipse.internal.crucible.ui.annotations.CrucibleCommentAnnotation;
import com.atlassian.connector.eclipse.ui.team.CrucibleFile;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewBean;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for the UI
 * 
 * @author Shawn Minto
 */
public final class CrucibleUiUtil {

	private CrucibleUiUtil() {
		// ignore
	}

	public static TaskRepository getCrucibleTaskRepository(String repositoryUrl) {
		if (repositoryUrl != null) {
			return TasksUi.getRepositoryManager().getRepository(CrucibleCorePlugin.CONNECTOR_KIND, repositoryUrl);
		}
		return null;
	}

	public static ITask getCrucibleTask(TaskRepository taskRepository, String taskId) {
		if (taskRepository != null && taskId != null) {
			return TasksUi.getRepositoryModel().getTask(taskRepository, taskId);
		}
		return null;
	}

	public static TaskRepository getCrucibleTaskRepository(Review review) {
		if (review != null && review instanceof ReviewBean) {
			String repositoryUrl = ((ReviewBean) review).getServerUrl();
			if (repositoryUrl != null) {
				return getCrucibleTaskRepository(repositoryUrl);
			}
		}
		return null;
	}

	public static CrucibleClient getClient(Review review) {
		CrucibleRepositoryConnector connector = CrucibleCorePlugin.getRepositoryConnector();
		return connector.getClientManager().getClient(getCrucibleTaskRepository(review));
	}

	public static ITask getCrucibleTask(Review review) {
		if (review != null) {
			TaskRepository taskRepository = getCrucibleTaskRepository(review);
			String taskId = CrucibleUtil.getTaskIdFromPermId(review.getPermId().getId());
			if (taskRepository != null && taskId != null) {
				return getCrucibleTask(taskRepository, taskId);
			}
		}

		return null;
	}

	public static void highlightAnnotationInRichEditor(int offset, CrucibleAnnotationModel annotationModel) {
		if (annotationModel != null) {
			CrucibleCommentAnnotation annotation = annotationModel.getFirstAnnotationForOffset(offset);
			if (annotation != null) {
				Review review = annotation.getReview();
				VersionedComment comment = annotation.getVersionedComment();
				CrucibleFileInfo crucibleFile = annotation.getCrucibleFileInfo();
				new OpenReviewEditorToCommentAction(review, comment, crucibleFile, false).run();
			} else {
				new OpenReviewEditorToCommentAction(CrucibleUiPlugin.getDefault()
						.getActiveReviewManager()
						.getActiveReview(), null, null, false).run();
			}
		}
	}

	public static boolean hasCurrentUserCompletedReview(Review review) {
		String currentUser = getCurrentUserName(review);
		return CrucibleUtil.isUserCompleted(currentUser, review);
	}

	public static String getCurrentUserName(Review review) {
		return getCurrentUserName(CrucibleUiUtil.getCrucibleTaskRepository(review));
	}

	public static CrucibleCachedUser getCurrentCachedUser(TaskRepository repository) {
		return getCachedUser(getCurrentUserName(repository), repository);
	}

	public static CrucibleCachedUser getCurrentCachedUser(Review review) {
		TaskRepository repository = CrucibleUiUtil.getCrucibleTaskRepository(review);
		return getCachedUser(getCurrentUserName(repository), repository);
	}

	public static String getCurrentUserName(TaskRepository repository) {
		/*
		String currentUser = CrucibleCorePlugin.getRepositoryConnector()
				.getClientManager()
				.getClient(repository)
				.getUserName();
		*/
		return repository.getUserName();
	}

	public static CrucibleCachedUser getCachedUser(String userName, TaskRepository repository) {
		for (CrucibleCachedUser user : getCachedUsers(repository)) {
			if (userName.equals(user.getUserName())) {
				return user;
			}
		}
		return null;
	}

	public static boolean isUserReviewer(String userName, Review review) {
		try {
			for (Reviewer reviewer : review.getReviewers()) {
				if (reviewer.getUserName().equals(userName)) {
					return true;
				}
			}
		} catch (ValueNotYetInitialized e) {
			// ignore
		}
		return false;
	}

	public static boolean isCurrentUserReviewer(Review review) {
		return isUserReviewer(CrucibleUiUtil.getCurrentUserName(review), review);
	}

	public static boolean isFilePartOfActiveReview(CrucibleFile crucibleFile) {
		Review activeReview = CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveReview();
		if (activeReview == null || crucibleFile == null || crucibleFile.getCrucibleFileInfo() == null
				|| crucibleFile.getCrucibleFileInfo().getFileDescriptor() == null) {
			return false;
		}
		try {
			for (CrucibleFileInfo fileInfo : activeReview.getFiles()) {
				if (fileInfo != null
						&& fileInfo.getFileDescriptor() != null
						&& fileInfo.getFileDescriptor().getUrl().equals(
								crucibleFile.getCrucibleFileInfo().getFileDescriptor().getUrl())
						&& fileInfo.getFileDescriptor().getRevision().equals(
								crucibleFile.getCrucibleFileInfo().getFileDescriptor().getRevision())) {
					return true;
				}
			}
		} catch (ValueNotYetInitialized e) {
			//ignore
		}
		return false;
	}

	public static void checkAndRequestReviewActivation(Review review) {
		Review activeReview = CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveReview();
		if (activeReview == null || !activeReview.getPermId().equals(review.getPermId())) {
			//review activation
			boolean activate = false;
			String pref = CrucibleUiPlugin.getActivateReviewPreference();
			if (pref.equals(MessageDialogWithToggle.ALWAYS)) {
				activate = true;
			} else if (pref.equals(MessageDialogWithToggle.NEVER)) {
				activate = false;
			} else {
				// Ask the user whether to switch
				final MessageDialogWithToggle m = MessageDialogWithToggle.openYesNoQuestion(null, "Activate Review",
						"Review comments will only be visible in editors if the corresponding review is active."
								+ "\n\nWould you like to activate this review?", "Remember my decision", false,
						CrucibleUiPlugin.getDefault().getPreferenceStore(),
						CrucibleUIConstants.PREFERENCE_ACTIVATE_REVIEW);

				activate = m.getReturnCode() == IDialogConstants.YES_ID || m.getReturnCode() == IDialogConstants.OK_ID;
			}
			if (activate) {
				ITask task = CrucibleUiUtil.getCrucibleTask(review);
				TasksUi.getTaskActivityManager().activateTask(task);
			}
		}
	}

	public static Set<CrucibleCachedUser> getCachedUsers(Review review) {
		return getCachedUsers(getCrucibleTaskRepository(review));
	}

	public static Set<CrucibleCachedUser> getCachedUsers(TaskRepository repository) {
		CrucibleClient client = CrucibleCorePlugin.getRepositoryConnector().getClientManager().getClient(repository);
		CrucibleClientData clientData = client.getClientData();
		Set<CrucibleCachedUser> users;
		if (clientData == null) {
			users = new HashSet<CrucibleCachedUser>();
		} else {
			users = clientData.getCachedUsers();
		}
		return users;
	}

	public static Set<CrucibleCachedRepository> getCachedRepositories(TaskRepository repository) {
		CrucibleClient client = CrucibleCorePlugin.getRepositoryConnector().getClientManager().getClient(repository);
		CrucibleClientData clientData = client.getClientData();
		Set<CrucibleCachedRepository> repositories;
		if (clientData == null) {
			repositories = new HashSet<CrucibleCachedRepository>();
		} else {
			repositories = clientData.getCachedRepositories();
		}
		return repositories;
	}

	public static Set<CrucibleCachedProject> getCachedProjects(TaskRepository repository) {
		CrucibleClient client = CrucibleCorePlugin.getRepositoryConnector().getClientManager().getClient(repository);
		CrucibleClientData clientData = client.getClientData();
		Set<CrucibleCachedProject> projects;
		if (clientData == null) {
			projects = new HashSet<CrucibleCachedProject>();
		} else {
			projects = clientData.getCachedProjects();
		}
		return projects;
	}

	public static boolean canModifyComment(Review review, Comment comment) {
		return CrucibleUtil.canAddCommentToReview(review)
				&& comment.getAuthor().getUserName().equals(CrucibleUiUtil.getCurrentUserName(review));
	}

	public static Set<String> getUserNamesFromUsers(Collection<? extends User> users) {
		final Set<String> userNames = new HashSet<String>();
		for (User user : users) {
			userNames.add(user.getUserName());
		}
		return userNames;
	}

	public static boolean hasCachedData(TaskRepository repository) {
		CrucibleClient client = CrucibleCorePlugin.getRepositoryConnector().getClientManager().getClient(repository);
		CrucibleClientData clientData = client.getClientData();
		return clientData.hasData();
	}

}
