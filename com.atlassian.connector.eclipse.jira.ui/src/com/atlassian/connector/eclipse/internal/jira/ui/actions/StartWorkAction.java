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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.sync.SynchronizeTasksJob;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.IFormPage;

import com.atlassian.connector.eclipse.internal.jira.core.JiraTaskDataHandler;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraIssue;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraClient;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraException;
import com.atlassian.connector.eclipse.internal.jira.ui.IJiraTask;
import com.atlassian.connector.eclipse.internal.jira.ui.JiraUiPlugin;
import com.atlassian.connector.eclipse.internal.jira.ui.editor.JiraTaskEditorPage;

@SuppressWarnings("restriction")
public class StartWorkAction extends AbstractStartWorkAction {

	private ITask task;

	private TaskData taskData;

	public StartWorkAction() {
	}

	@Override
	public void run(IAction action) {

//		Assert.isNotNull(tasks);
//
//		if (tasks.isEmpty()) {
//			return;
//		}
//
//		if (tasks.size() > 1) {
//			// this action should be enabled only for single selection in plugin.xml
//			StatusHandler.log(new Status(IStatus.ERROR, JiraUiPlugin.ID_PLUGIN,
//					Messages.StartWorkAction_enabled_for_single_selection));
//		}
//
//		Assert.isNotNull(tasks.get(0));
//		task = tasks.get(0).getTask();
//
//		try {
//			taskData = TasksUiPlugin.getTaskDataManager().getTaskData(task);
//		} catch (CoreException e) {
//			handleError(Messages.JiraConnectorUiActions_Cannot_get_task_data + task.getTaskKey(), e);
//			return;
//		}

		if (getTargetPart() instanceof TaskListView) {
			doActionOutsideEditor();
		} else if (getTargetPart() instanceof TaskEditor) {
			TaskEditor taskEditor = (TaskEditor) getTargetPart();

			IEditorInput editorInput = taskEditor.getEditorInput();

			if (editorInput instanceof TaskEditorInput) {
				TaskEditorInput taskEditorInput = (TaskEditorInput) editorInput;

				if (taskEditorInput.getTask().equals(task)) {

					IFormPage formPage = taskEditor.getActivePageInstance();
					if (formPage instanceof JiraTaskEditorPage) {
						JiraTaskEditorPage jiraFormPage = (JiraTaskEditorPage) formPage;

						doActionInsideEditor(jiraFormPage, taskData, task);

						// do editor submit
						// TODO jj change call (move stuff here from subclass)
//						new StartWorkEditorToolbarAction(jiraFormPage).run();
//						TasksUiInternal.synchronizeTasks(connector, new HashSet<ITask>(tasksToSync), true, null);
						return;
					}
				}
			}
		}
	}

	private static void synchronizeTask(final ITask task, IProgressMonitor monitor) {

//		ITaskList taskList = TasksUiInternal.getTaskList();
//
//		AbstractTask abstractTask = ((AbstractTask) aTask);
//		abstractTask.setSynchronizing(true);
//		((TaskList) taskList).notifySynchronizationStateChanged(asSet(aTask));

		SynchronizeTasksJob job = (SynchronizeTasksJob) TasksUiPlugin.getTaskJobFactory().createSynchronizeTasksJob(
				getConnector(task), asSet(task));

		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				if (task instanceof AbstractTask && ((AbstractTask) task).getStatus() != null) {
					TasksUiInternal.asyncDisplayStatus(
							org.eclipse.mylyn.internal.tasks.ui.util.Messages.TasksUiInternal_Task_Synchronization_Failed,
							((AbstractTask) task).getStatus());
				}
			}
		});

		job.run(monitor);

	}

	private static <T> Set<T> asSet(T... values) {
		return new HashSet<T>(Arrays.asList(values));
	}

	protected static void doActionInsideEditor(final JiraTaskEditorPage jiraFormPage, TaskData taskData, ITask task) {
		Job job = null;

		if (isTaskInStop(taskData, task)) {
			job = getStartProgressJob(taskData, task);
		} else if (isTaskInProgress(taskData, task)) {
			job = getStopProgressJob(taskData, task);
		} else {
			StatusHandler.log(new Status(IStatus.ERROR, JiraUiPlugin.ID_PLUGIN, Messages.StartWorkAction_cannot_perform));
			return;
		}

		jiraFormPage.showEditorBusy(true);
		job.setUser(false);
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					public void run() {
						jiraFormPage.refreshFormContent();
//						jiraFormPage.showEditorBusy(false);
					}
				});
			}
		});

		job.schedule();
	}

	private void doActionOutsideEditor() {
		if (isTaskInStop(taskData, task)) {
			Job job = getStartProgressJob(taskData, task);
			job.setUser(true);
			job.schedule();
		} else if (isTaskInProgress(taskData, task)) {
			Job job = getStopProgressJob(taskData, task);
			job.setUser(true);
			job.schedule();
		} else {
			StatusHandler.log(new Status(IStatus.ERROR, JiraUiPlugin.ID_PLUGIN, Messages.StartWorkAction_cannot_perform));
		}
	}

	private static Job getStartProgressJob(final TaskData taskData, final ITask task) {

		Job startProgressJob = new Job(Messages.StartWorkAction_Start_Progress) {

			@Override
			protected IStatus run(final IProgressMonitor monitor) {

				monitor.beginTask(Messages.StartWorkAction_Start_Progress, IProgressMonitor.UNKNOWN);

				final JiraClient client = getClient(task);

				boolean shouldSynchronize = false;

				try {
					final JiraIssue issue = getIssue(task);

					if (!isAssignedToMe(taskData, task)) {
						client.assignIssueTo(issue, JiraClient.ASSIGNEE_USER, getCurrentUser(task), null, monitor);
						shouldSynchronize = true;
					}
					client.advanceIssueWorkflow(issue, JiraTaskDataHandler.START_PROGRESS_OPERATION, null, monitor);
					shouldSynchronize = true;

					// activate task
					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
						public void run() {
							TasksUi.getTaskActivityManager().activateTask(task);
						}
					});
				} catch (CoreException e) {
					handleError(Messages.JiraConnectorUiActions_Cannot_get_task_data + task.getTaskKey(), e);
				} catch (JiraException e) {
					handleError(Messages.StartWorkAction_Start_Progress_Failed, e);
				} finally {
					if (shouldSynchronize) {
						synchronizeTask(task, monitor);
					}
				}

				return Status.OK_STATUS;
			}
		};

		return startProgressJob;
	}

	private static Job getStopProgressJob(final TaskData taskData, final ITask task) {
		Job stopProgressJob = new Job(Messages.StartWorkAction_Stop_Progress) {

			@Override
			protected IStatus run(final IProgressMonitor monitor) {

				monitor.beginTask(Messages.StartWorkAction_Stop_Progress, IProgressMonitor.UNKNOWN);

				final JiraClient client = getClient(task);

				try {
					final JiraIssue issue = getIssue(task);

					client.advanceIssueWorkflow(issue, JiraTaskDataHandler.STOP_PROGRESS_OPERATION, null, monitor);

					synchronizeTask(task, monitor);

					// deactivate task
					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
						public void run() {
							TasksUi.getTaskActivityManager().deactivateTask(task);
						}
					});

				} catch (CoreException e) {
					handleError(Messages.JiraConnectorUiActions_Cannot_get_task_data + task.getTaskKey(), e);
				} catch (JiraException e) {
					handleError(Messages.StartWorkAction_Stop_Progress_Failed, e);
				}

				return Status.OK_STATUS;
			}
		};

		return stopProgressJob;
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);

		task = null;

		if (selection instanceof IStructuredSelection) {

			IStructuredSelection ss = (IStructuredSelection) selection;

			if (!ss.isEmpty()) {

				Iterator<?> iter = ss.iterator();
				while (iter.hasNext()) {
					Object sel = iter.next();
					if (sel instanceof IJiraTask) {
						task = ((IJiraTask) sel).getTask();
						try {
							taskData = TasksUiPlugin.getTaskDataManager().getTaskData(task);
						} catch (CoreException e) {
							handleError(Messages.JiraConnectorUiActions_Cannot_get_task_data + task.getTaskKey(), e);
						}

						break;
					}
				}
			}
		}

		update(action);
	}

	private void update(IAction action) {
		if (isTaskInProgress(taskData, task)) {
			action.setText(Messages.StartWorkAction_Stop_Work);
		} else if (isTaskInStop(taskData, task)) {
			action.setText(Messages.StartWorkAction_Start_Work);
		} else {
			action.setEnabled(false);
			action.setText(Messages.StartWorkAction_Start_Work);
		}
	}

	private static String getCurrentUser(ITask task) {
		TaskRepository repository = TasksUi.getRepositoryManager().getRepository(task.getConnectorKind(),
				task.getRepositoryUrl());

		return repository.getUserName();
	}

}
