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

package me.glindholm.connector.eclipse.internal.jira.ui.actions;

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
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
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

import me.glindholm.connector.eclipse.internal.jira.core.JiraClientFactory;
import me.glindholm.connector.eclipse.internal.jira.core.JiraTaskDataHandler;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssue;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraClient;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraRemoteMessageException;
import me.glindholm.connector.eclipse.internal.jira.ui.IJiraTask;
import me.glindholm.connector.eclipse.internal.jira.ui.JiraUiPlugin;

/**
 * @author Jacek Jaroczynski
 */
@SuppressWarnings("restriction")
public abstract class AbstractJiraAction extends BaseSelectionListenerAction implements IObjectActionDelegate {

    private IStructuredSelection selection;

    private IWorkbenchPart targetPart;

    public AbstractJiraAction(final String text) {
        super(text);
    }

    @Override
    public void run(final IAction action) {
        if (selection != null && !selection.isEmpty()) {
            final List<IJiraTask> tasks = new ArrayList<>();
            final Iterator<?> iter = selection.iterator();
            while (iter.hasNext()) {
                final Object sel = iter.next();
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

    @Override
    public void selectionChanged(final IAction action, final ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            this.selection = (IStructuredSelection) selection;
        } else {
            this.selection = StructuredSelection.EMPTY;
        }
    }

    protected static JiraIssue getIssue(final ITask task) throws CoreException {
        final TaskData taskData = TasksUi.getTaskDataManager().getTaskData(task);
        return JiraTaskDataHandler.buildJiraIssue(taskData);
    }

    @Override
    public void setActivePart(final IAction action, final IWorkbenchPart targetPart) {
        this.targetPart = targetPart;
    }

    public IWorkbenchPart getTargetPart() {
        return targetPart;
    }

    protected static JiraClient getClient(final ITask task) {
        final TaskRepository repo = TasksUi.getRepositoryManager().getRepository(task.getConnectorKind(), task.getRepositoryUrl());

        return JiraClientFactory.getDefault().getJiraClient(repo);
    }

    protected static AbstractRepositoryConnector getConnector(final ITask task) {
        return TasksUi.getRepositoryConnector(task.getConnectorKind());
    }

    protected static void handleError(final String message, final Throwable e) {
        PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
            MessageDialog.openError(WorkbenchUtil.getShell(), JiraUiPlugin.PRODUCT_NAME, message);
            StatusHandler.log(new Status(IStatus.ERROR, JiraUiPlugin.ID_PLUGIN, message, e));
        });
    }

    protected static void handleInformation(final String message) {
        PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
            MessageDialog.openInformation(WorkbenchUtil.getShell(), JiraUiPlugin.PRODUCT_NAME, message);
            StatusHandler.log(new Status(IStatus.INFO, JiraUiPlugin.ID_PLUGIN, message));
        });
    }

    protected static void handleErrorWithDetails(final String message, final Throwable e) {

        PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {

            Throwable t = e;
            final StringBuilder m = new StringBuilder().append(message);

            final String searchDetails_1 = "The likely cause is that somebody has changed the issue recently"; //$NON-NLS-1$
            final String searchDetails_2 = "No workflow action with id"; //$NON-NLS-1$

            if (e.getMessage().contains(searchDetails_1)) {
                m.append(" \n").append(Messages.JiraAction_Issue_Refresh_Try_Again); //$NON-NLS-1$
            } else if (e.getMessage().contains(searchDetails_2)) {
                m.append(" \n").append(Messages.JiraAction_Issue_Refresh); //$NON-NLS-1$
            } else if (e instanceof final JiraRemoteMessageException jiraException) {
                if (jiraException.getHtmlMessage().contains(searchDetails_1)) {
                    m.append(". \n").append(Messages.JiraAction_Issue_Changed).append(" \n" //$NON-NLS-1$ //$NON-NLS-2$
                    ).append(Messages.JiraAction_Issue_Refresh_Try_Again);
                } else if (jiraException.getHtmlMessage().contains(searchDetails_2)) {
                    m.append(" \n").append(Messages.JiraAction_Issue_Refresh); //$NON-NLS-1$
                }
            }

            final int _300 = 300;

            if (e.getMessage().length() > 300) {
                t = new Exception(e.getMessage().substring(0, _300) + "...", e); //$NON-NLS-1$
            }

            final Status status = new Status(IStatus.ERROR, JiraUiPlugin.ID_PLUGIN, m.toString(), t);
            StatusManager.getManager().handle(status, StatusManager.SHOW | StatusManager.LOG | StatusManager.BLOCK);
        });
    }
}