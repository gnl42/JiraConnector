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

package com.atlassian.connector.eclipse.internal.jira.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

import com.atlassian.connector.eclipse.internal.jira.core.JiraClientFactory;
import com.atlassian.connector.eclipse.internal.jira.core.JiraCorePlugin;
import com.atlassian.connector.eclipse.internal.jira.core.JiraTaskDataHandler;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraIssue;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraClient;
import com.atlassian.connector.eclipse.internal.jira.ui.JiraConstants;
import com.atlassian.connector.eclipse.internal.jira.ui.JiraUiPlugin;

/**
 * @author Jacek Jaroczynski
 */
@SuppressWarnings("restriction")
public abstract class AbstractJiraAction extends BaseSelectionListenerAction implements IViewActionDelegate,
		IEditorActionDelegate {

	public AbstractJiraAction(String text) {
		super(text);
	}

	public void run(IAction action) {
		if (this.getStructuredSelection() != null && !this.getStructuredSelection().isEmpty()) {
			List<AbstractTask> tasks = new ArrayList<AbstractTask>();
			Iterator<?> iter = this.getStructuredSelection().iterator();
			while (iter.hasNext()) {
				Object sel = iter.next();
				if (sel instanceof ITask) {
					tasks.add((AbstractTask) sel);
				}
			}

			if (!tasks.isEmpty()) {
				doAction(tasks);
			}
		}
	}

	protected abstract void doAction(List<AbstractTask> tasks);

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		if (!selection.isEmpty()) {
			for (Object element : selection.toList()) {
				if (!(element instanceof ITask)) {
					return false;
				} else {
					ITask task = (ITask) element;
					if (!task.getConnectorKind().equals(JiraCorePlugin.CONNECTOR_KIND)) {
						return false;
					}
				}
			}
		} else {
			return false;
		}

		return true;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			selectionChanged((IStructuredSelection) selection);
		} else {
			selectionChanged(StructuredSelection.EMPTY);
		}
		action.setEnabled(isEnabled());

		updateVisibility(selection);
	}

	private void updateVisibility(ISelection selection) {

		Iterator<?> iter = this.getStructuredSelection().iterator();
		while (iter.hasNext()) {
			Object sel = iter.next();
			if (sel instanceof ITask) {
				ITask task = (ITask) sel;
				if (task.getConnectorKind().equals(JiraCorePlugin.CONNECTOR_KIND)) {
					System.setProperty(JiraConstants.ISSUE_SELECTED_SYSTEM_PROPERTY, "true"); //$NON-NLS-1$
					return;
				}

			}
		}
		System.setProperty(JiraConstants.ISSUE_SELECTED_SYSTEM_PROPERTY, "false"); //$NON-NLS-1$
	}

	public void init(IViewPart view) {
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
	}

	protected JiraIssue getIssue(AbstractTask task) throws CoreException {
		TaskData taskData = TasksUi.getTaskDataManager().getTaskData(task);
		return JiraTaskDataHandler.buildJiraIssue(taskData);
	}

	protected JiraClient getClient(ITask task) {
		TaskRepository repo = TasksUi.getRepositoryManager().getRepository(task.getConnectorKind(),
				task.getRepositoryUrl());

		return JiraClientFactory.getDefault().getJiraClient(repo);
	}

	protected static void handleError(final String message, final Throwable e) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(WorkbenchUtil.getShell(), JiraUiPlugin.PRODUCT_NAME, message);
				StatusHandler.log(new Status(IStatus.ERROR, JiraUiPlugin.ID_PLUGIN, message, e));
			}
		});
	}

	protected static void handleInformation(final String message) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openInformation(WorkbenchUtil.getShell(), JiraUiPlugin.PRODUCT_NAME, message);
				StatusHandler.log(new Status(IStatus.INFO, JiraUiPlugin.ID_PLUGIN, message));
			}
		});
	}

}