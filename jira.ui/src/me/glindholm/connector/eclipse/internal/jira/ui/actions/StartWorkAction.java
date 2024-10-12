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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.sync.SynchronizeTasksJob;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.IFormPage;

import me.glindholm.connector.eclipse.internal.jira.core.JiraTaskDataHandler;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssue;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraWorkLog;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraClient;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraException;
import me.glindholm.connector.eclipse.internal.jira.ui.IJiraTask;
import me.glindholm.connector.eclipse.internal.jira.ui.JiraUiPlugin;
import me.glindholm.connector.eclipse.internal.jira.ui.JiraUiUtil;
import me.glindholm.connector.eclipse.internal.jira.ui.editor.JiraTaskEditorPage;

/**
 * @author Jacek Jaroczynski
 */
@SuppressWarnings("restriction")
public class StartWorkAction extends AbstractStartWorkAction {

    private ITask task;

    private TaskData taskData;

    private JiraWorkLog workLog;

    public StartWorkAction() {
    }

    @Override
    public void run(final IAction action) {
        update(action);

        if (getTargetPart() instanceof TaskEditor) {
            final TaskEditor taskEditor = (TaskEditor) getTargetPart();

            final IEditorInput editorInput = taskEditor.getEditorInput();

            if ((editorInput instanceof final TaskEditorInput taskEditorInput) && taskEditorInput.getTask().equals(task)) {

                final IFormPage formPage = taskEditor.getActivePageInstance();
                if (formPage instanceof final JiraTaskEditorPage jiraFormPage) {
                    startWork(jiraFormPage);

                    return;
                }
            }
        }

        startWork(null);
    }

    protected void run(final JiraTaskEditorPage editorPage, final TaskData taskData, final ITask task) {
        this.taskData = taskData;
        this.task = task;

        startWork(editorPage);
    }

    private void startWork(final JiraTaskEditorPage page) {
        Job job = null;

        if (isTaskInStop(taskData, task)) {
            job = getStartWorkJob(taskData, task);
        } else if (isTaskInProgress(taskData, task)) {
            if (!showLogWorkDialog(taskData, task)) {
                return;
            }
            job = getStopWorkJob(taskData, task, workLog);
        } else {
            StatusHandler.log(new Status(IStatus.ERROR, JiraUiPlugin.ID_PLUGIN, Messages.StartWorkAction_cannot_perform));
            return;
        }

        if (page == null) {
            doActionOutsideEditor(job);
        } else {
            doActionInsideEditor(job, page);
        }
    }

    private static void synchronizeTask(final ITask task, final IProgressMonitor monitor) {

        final SynchronizeTasksJob job = (SynchronizeTasksJob) TasksUiPlugin.getTaskJobFactory().createSynchronizeTasksJob(getConnector(task), asSet(task));

        job.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void done(final IJobChangeEvent event) {
                if ((task instanceof AbstractTask) && (((AbstractTask) task).getStatus() != null)) {
                    TasksUiInternal.asyncDisplayStatus(org.eclipse.mylyn.internal.tasks.ui.util.Messages.TasksUiInternal_Task_Synchronization_Failed,
                            ((AbstractTask) task).getStatus());
                }
            }
        });

        job.run(monitor);

    }

    private static <T> Set<T> asSet(final T... values) {
        return new HashSet<>(Arrays.asList(values));
    }

    private void doActionInsideEditor(final Job job, final JiraTaskEditorPage jiraFormPage) {
        if (jiraFormPage.isDirty()) {
            jiraFormPage.getEditor().doSave(new NullProgressMonitor());
        }
        jiraFormPage.showEditorBusy(true);
        job.setUser(false);
        job.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void done(final IJobChangeEvent event) {
                PlatformUI.getWorkbench().getDisplay().asyncExec(() -> jiraFormPage.refresh());
            }
        });

        job.schedule();
    }

    private void doActionOutsideEditor(final Job job) {
        job.setUser(true);
        job.schedule();
    }

    /**
     * @param taskData
     * @param iTask
     * @return false if the process of stopping work should be broken
     */
    private boolean showLogWorkDialog(final TaskData taskData, final ITask iTask) {
        if (MonitorUiPlugin.getDefault().isActivityTrackingEnabled()) {

            final long seconds = JiraUiUtil.getLoggedActivityTime(iTask);

            final LogJiraTimeDialog dialog = new LogJiraTimeDialog(
                    PlatformUI.getWorkbench().getDisplay().getActiveShell(), taskData, iTask, seconds);

            final int result = dialog.open();

            if (result == Window.OK) {
                workLog = dialog.getWorkLog();
            } else if (result == LogJiraTimeDialog.SKIP_LOGGING) {
                workLog = null;
            } else {
                return false;
            }
        }
        return true;
    }

    private static String showSelectActionDialog(final TaskData taskData, final ITask iTask, final boolean start) {
        final SelectWorkflowActionDialog dialog = new SelectWorkflowActionDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), taskData, iTask,
                start);

        final int result = dialog.open();

        if (result == Window.OK) {
            return dialog.getSelectedAction();
        }
        return null;
    }

    private static boolean smartAdvanceAction(final boolean start, final JiraClient client, final JiraIssue issue, final TaskData taskData, final ITask task,
            final IProgressMonitor monitor) throws JiraException {

        final boolean[] doAdvanceAction = new boolean[1];
        doAdvanceAction[0] = false;
        final String[] selectedAction = new String[1];
        selectedAction[0] = start ? JiraTaskDataHandler.START_PROGRESS_OPERATION : JiraTaskDataHandler.STOP_PROGRESS_OPERATION;
        if ((start && !haveStartProgressOperation(taskData)) || (!start && !haveStopProgressOperation(taskData))) {
            PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
                selectedAction[0] = showSelectActionDialog(taskData, task, start);
                doAdvanceAction[0] = selectedAction[0] != null;
            });
        } else {
            doAdvanceAction[0] = true;
        }
        if (doAdvanceAction[0]) {
            client.advanceIssueWorkflow(issue, selectedAction[0], null, monitor);
            return true;
        }
        return false;
    }

    private static Job getStartWorkJob(final TaskData taskData, final ITask task) {

        return new Job(Messages.StartWorkAction_Start_Work) {

            @Override
            protected IStatus run(final IProgressMonitor monitor) {

                monitor.beginTask(Messages.StartWorkAction_Starting_Work + task.getTaskKey(), IProgressMonitor.UNKNOWN);

                final JiraClient client = getClient(task);

                boolean shouldSynchronize = false;
                boolean shouldActivate = false;

                try {
                    final JiraIssue issue = getIssue(task);

                    if (!isAssignedToMe(taskData, task)) {
                        client.assignIssueTo(issue, getCurrentUser(task), null, monitor);
                        shouldSynchronize = true;
                    }

                    smartAdvanceAction(true, client, issue, taskData, task, monitor);

                    shouldSynchronize = true;
                    shouldActivate = true;

                } catch (final CoreException e) {
                    handleError(Messages.JiraConnectorUiActions_Cannot_get_task_data + task.getTaskKey(), e);
                } catch (final JiraException e) {
                    handleErrorWithDetails(Messages.StartWorkAction_Start_Work_Failed + task.getTaskKey(), e);
                } finally {
                    if (shouldSynchronize) {
                        synchronizeTask(task, monitor);
                    }
                    if (shouldActivate) {
                        // activate task
                        PlatformUI.getWorkbench().getDisplay().asyncExec(() -> TasksUi.getTaskActivityManager().activateTask(task));
                    }
                }

                return Status.OK_STATUS;
            }
        };
    }

    private static Job getStopWorkJob(final TaskData taskData, final ITask task, final JiraWorkLog jiraWorkLog) {
        return new Job(Messages.StartWorkAction_Stop_Work) {

            @Override
            protected IStatus run(final IProgressMonitor monitor) {

                monitor.beginTask(Messages.StartWorkAction_Stoping_Work + task.getTaskKey(), IProgressMonitor.UNKNOWN);

                final JiraClient client = getClient(task);

                try {
                    final JiraIssue issue = getIssue(task);

                    if (jiraWorkLog != null) {
                        client.addWorkLog(task.getTaskKey(), jiraWorkLog, monitor);

                        // reset activity time
                        JiraUiUtil.setLoggedActivityTime(task);
                    }

                    smartAdvanceAction(false, client, issue, taskData, task, monitor);

                    synchronizeTask(task, monitor);

                    // deactivate task
                    PlatformUI.getWorkbench().getDisplay().asyncExec(() -> TasksUi.getTaskActivityManager().deactivateTask(task));

                } catch (final CoreException e) {
                    handleError(Messages.JiraConnectorUiActions_Cannot_get_task_data + task.getTaskKey(), e);
                } catch (final JiraException e) {
                    handleErrorWithDetails(Messages.StartWorkAction_Stop_Work_Failed + task.getTaskKey(), e);
                }

                return Status.OK_STATUS;
            }
        };
    }

    @Override
    public void selectionChanged(final IAction action, final ISelection selection) {
        super.selectionChanged(action, selection);

        task = null;

        if ((selection instanceof final IStructuredSelection ss) && !ss.isEmpty()) {

            final Iterator<?> iter = ss.iterator();
            while (iter.hasNext()) {
                final Object sel = iter.next();
                if (sel instanceof IJiraTask) {
                    task = ((IJiraTask) sel).getTask();
                    try {
                        taskData = TasksUiPlugin.getTaskDataManager().getTaskData(task);
                    } catch (final CoreException e) {
                        handleError(Messages.JiraConnectorUiActions_Cannot_get_task_data + task.getTaskKey(), e);
                    }

                    break;
                }
            }
        }

        update(action);
    }

    private void update(final IAction action) {
        if (isTaskInProgress(taskData, task)) {
            action.setText(Messages.StartWorkAction_Stop_Work);
        } else if (isTaskInStop(taskData, task)) {
            action.setText(Messages.StartWorkAction_Start_Work);
        } else {
            action.setEnabled(false);
            action.setText(Messages.StartWorkAction_Start_Work);
        }
    }

    private static String getCurrentUser(final ITask task) {
        final TaskRepository repository = TasksUi.getRepositoryManager().getRepository(task.getConnectorKind(), task.getRepositoryUrl());

        return repository.getUserName();
    }

}
