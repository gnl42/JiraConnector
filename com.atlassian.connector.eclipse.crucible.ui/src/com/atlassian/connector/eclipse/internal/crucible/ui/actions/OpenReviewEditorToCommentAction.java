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

package com.atlassian.connector.eclipse.internal.crucible.ui.actions;

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleConstants;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.IReviewAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.IReviewActionListener;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.CrucibleReviewEditorPage;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.IFormPage;

/**
 * Action that will open the Review editor and highlight the review comment that was selected
 * 
 * @author Shawn Minto
 */
public class OpenReviewEditorToCommentAction extends Action implements IReviewAction {

	private final VersionedComment comment;

	private final Review review;

	private final CrucibleFileInfo crucibleFile;

	private IReviewActionListener actionListener;

	private final boolean openAndActivate;

	public OpenReviewEditorToCommentAction(Review review, VersionedComment comment, CrucibleFileInfo crucibleFile) {
		this(review, comment, crucibleFile, true);
	}

	public OpenReviewEditorToCommentAction(Review review, VersionedComment comment, CrucibleFileInfo crucibleFile,
			boolean openAndActivate) {
		this.review = review;
		this.comment = comment;
		this.crucibleFile = crucibleFile;
		this.openAndActivate = openAndActivate;
	}

	@Override
	public String getText() {
		return "Open Review";
	}

	@Override
	public String getToolTipText() {
		return "Open associated review to this comment";
	}

	@Override
	public void run() {
		if (actionListener != null) {
			actionListener.actionAboutToRun(this);
		}
		if (review != null) {
			TaskRepository taskRepository = CrucibleUiUtil.getCrucibleTaskRepository(review);
			if (taskRepository != null) {
				ITask task = CrucibleUiUtil.getCrucibleTask(review);
				if (task != null) {
					TaskEditorInput editorInput = new TaskEditorInput(taskRepository, task);
					for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
						for (IWorkbenchPage page : window.getPages()) {
							IEditorPart part = page.findEditor(editorInput);
							if (part != null) {
								selectAndRevealCommentInEditorPage(page, part, true);
								return;
							}
						}
					}
					if (openAndActivate) {
						try {
							IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
							IEditorPart part = page.openEditor(editorInput, TaskEditor.ID_EDITOR);
							if (part != null) {
								selectAndRevealCommentInEditorPage(page, part, true);
								return;
							}
						} catch (PartInitException e) {
							StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
									"Unable to open crucible editor", e));
						}
					}
				}
			}
		}
		if (actionListener != null) {
			actionListener.actionRan(this);
		}
	}

	private void selectAndRevealCommentInEditorPage(IWorkbenchPage page, IEditorPart part, boolean reveal) {
		TaskEditor taskEditor = (TaskEditor) part;
		taskEditor.setActivePage(CrucibleConstants.CRUCIBLE_EDITOR_PAGE_ID);
		IFormPage activePage = taskEditor.getActivePageInstance();
		if (activePage instanceof CrucibleReviewEditorPage) {
			if (openAndActivate) {
				page.bringToTop(part);
			}

			((CrucibleReviewEditorPage) activePage).selectAndReveal(crucibleFile, comment, reveal);
		}
		if (actionListener != null) {
			actionListener.actionRan(this);
		}
	}

	public void setActionListener(IReviewActionListener listner) {
		this.actionListener = listner;
	}
}
