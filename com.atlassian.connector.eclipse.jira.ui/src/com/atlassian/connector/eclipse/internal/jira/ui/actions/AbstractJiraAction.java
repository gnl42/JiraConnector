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
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.ui.statushandlers.StatusManager;

import com.atlassian.connector.eclipse.internal.jira.core.JiraClientFactory;
import com.atlassian.connector.eclipse.internal.jira.core.JiraTaskDataHandler;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraIssue;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraClient;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraRemoteMessageException;
import com.atlassian.connector.eclipse.internal.jira.ui.IJiraTask;
import com.atlassian.connector.eclipse.internal.jira.ui.JiraUiPlugin;

/**
 * @author Jacek Jaroczynski
 */
@SuppressWarnings("restriction")
public abstract class AbstractJiraAction extends BaseSelectionListenerAction implements IObjectActionDelegate {

	private IStructuredSelection selection;

	private IWorkbenchPart targetPart;

	public AbstractJiraAction(String text) {
		super(text);
	}

	public void run(IAction action) {
		if (this.selection != null && !this.selection.isEmpty()) {
			List<IJiraTask> tasks = new ArrayList<IJiraTask>();
			Iterator<?> iter = this.selection.iterator();
			while (iter.hasNext()) {
				Object sel = iter.next();
				if (sel instanceof IJiraTask) {
					tasks.add((IJiraTask) sel);
				}
			}

			if (!tasks.isEmpty()) {
				doAction(tasks);
			}
		}
	}

	protected abstract void doAction(List<IJiraTask> tasks);

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			this.selection = (IStructuredSelection) selection;
		} else {
			this.selection = StructuredSelection.EMPTY;
		}
	}

	protected static JiraIssue getIssue(ITask task) throws CoreException {
		TaskData taskData = TasksUi.getTaskDataManager().getTaskData(task);
		return JiraTaskDataHandler.buildJiraIssue(taskData);
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

	public IWorkbenchPart getTargetPart() {
		return targetPart;
	}

	protected static JiraClient getClient(ITask task) {
		TaskRepository repo = TasksUi.getRepositoryManager().getRepository(task.getConnectorKind(),
				task.getRepositoryUrl());

		return JiraClientFactory.getDefault().getJiraClient(repo);
	}

	protected static AbstractRepositoryConnector getConnector(ITask task) {
		return TasksUi.getRepositoryConnector(task.getConnectorKind());
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

	protected static void handleErrorWithDetails(final String message, final Throwable e) {

		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {

				Throwable t = e;
				String m = message;

				final String searchDetails_1 = "The likely cause is that somebody has changed the issue recently"; //$NON-NLS-1$
				final String searchDetails_2 = "No workflow action with id"; //$NON-NLS-1$

				if (e.getMessage().contains(searchDetails_1)) {
					m += " \n" + Messages.JiraAction_Issue_Refresh_Try_Again; //$NON-NLS-1$
				} else if (e.getMessage().contains(searchDetails_2)) {
					m += " \n" + Messages.JiraAction_Issue_Refresh; //$NON-NLS-1$
				} else if (e instanceof JiraRemoteMessageException) {
					JiraRemoteMessageException jiraException = (JiraRemoteMessageException) e;
					if (jiraException.getHtmlMessage().contains(searchDetails_1)) {
						m += ". \n" + Messages.JiraAction_Issue_Changed + " \n" //$NON-NLS-1$ //$NON-NLS-2$ 
								+ Messages.JiraAction_Issue_Refresh_Try_Again;
					} else if (jiraException.getHtmlMessage().contains(searchDetails_2)) {
						m += " \n" + Messages.JiraAction_Issue_Refresh; //$NON-NLS-1$
					}
				}

				final int _300 = 300;

				if (e.getMessage().length() > 300) {
					t = new Exception(e.getMessage().substring(0, _300) + "...", e); //$NON-NLS-1$
				}

				Status status = new Status(IStatus.ERROR, JiraUiPlugin.ID_PLUGIN, m, t);
				StatusManager.getManager().handle(status, StatusManager.SHOW | StatusManager.LOG | StatusManager.BLOCK);
			}
		});
	}
}