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

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

/**
 * Abstract action of review actions
 * 
 * @author Thomas Ehrnhoefer
 */
public abstract class AbstractReviewAction extends BaseSelectionListenerAction implements
		IWorkbenchWindowActionDelegate {

	protected IWorkbenchWindow workbenchWindow;

	protected Review review;

	public AbstractReviewAction(String text) {
		super(text);
	}

	public void dispose() {
		// ignore
	}

	public void init(IWorkbenchWindow window) {
		this.workbenchWindow = window;
	}

	@Override
	public void run() {
		review = getReview();
		run(this);
	}

	public void selectionChanged(IAction action, ISelection selection) {
		review = getReview();
		if (review != null) {
			action.setEnabled(true);
			setEnabled(true);
		} else {
			action.setEnabled(false);
			setEnabled(false);
		}
	}

	protected IEditorPart getActiveEditor() {
		IWorkbenchWindow window = workbenchWindow;
		if (window == null) {
			window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		}
		if (window != null && window.getActivePage() != null) {
			return window.getActivePage().getActiveEditor();
		}
		return null;
	}

	protected IEditorInput getEditorInputFromSelection(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = ((IStructuredSelection) selection);
			if (structuredSelection.getFirstElement() instanceof IEditorInput) {
				return (IEditorInput) structuredSelection.getFirstElement();
			}
		}
		return null;
	}

	protected String getTaskKey() {
		if (review == null) {
			return null;
		}
		return review.getPermId().getId();
	}

	protected String getTaskId() {
		if (review == null) {
			return null;
		}
		return CrucibleUtil.getTaskIdFromPermId(review.getPermId().getId());
	}

	protected abstract Review getReview();

	protected TaskRepository getTaskRepository() {
		if (review == null) {
			return null;
		}

		return CrucibleUiUtil.getCrucibleTaskRepository(review);
	}

}