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
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.OpenReviewEditorToCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.annotations.CrucibleAnnotationModel;
import com.atlassian.connector.eclipse.internal.crucible.ui.annotations.CrucibleCommentAnnotation;
import com.atlassian.connector.eclipse.ui.team.CrucibleFile;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewBean;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;

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
		String currentUser = getCurrentUser(review);
		return CrucibleUtil.isUserCompleted(currentUser, review);
	}

	public static String getCurrentUser(Review review) {
		String currentUser = CrucibleCorePlugin.getRepositoryConnector().getClientManager().getClient(
				CrucibleUiUtil.getCrucibleTaskRepository(review)).getUserName();
		return currentUser;
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
		return isUserReviewer(CrucibleUiUtil.getCurrentUser(review), review);
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

}
