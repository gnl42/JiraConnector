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

import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.core.data.TaskOperation;
import org.eclipse.mylyn.tasks.core.sync.SubmitJobEvent;
import org.eclipse.mylyn.tasks.core.sync.SubmitJobListener;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.ui.PlatformUI;

import com.atlassian.connector.eclipse.internal.jira.core.JiraAttribute;
import com.atlassian.connector.eclipse.internal.jira.core.JiraTaskDataHandler;
import com.atlassian.connector.eclipse.internal.jira.ui.JiraImages;
import com.atlassian.connector.eclipse.internal.jira.ui.JiraUiPlugin;
import com.atlassian.connector.eclipse.internal.jira.ui.editor.JiraTaskEditorPage;

public class StartWorkEditorToolbarAction extends StartWorkAction {

	private static final String ID = "com.atlassian.connector.eclipse.internal.jira.ui.actions.StartWorkAction"; //$NON-NLS-1$

	private final JiraTaskEditorPage editorPage;

	public StartWorkEditorToolbarAction(JiraTaskEditorPage editorPage) {
		super();
		this.editorPage = editorPage;
		setImageDescriptor(JiraImages.START_PROGRESS);
		setId(ID);

		update();
	}

	@Override
	public void run() {
		doActionInsideEditor(editorPage, editorPage.getModel().getTaskData(), editorPage.getModel().getTask());
//		if (isTaskInProgress(editorPage.getModel().getTaskData(), editorPage.getModel().getTask())) {
//			stopProgress();
//		} else if (isTaskInStop(editorPage.getModel().getTaskData(), editorPage.getModel().getTask())) {
//			startProgress();
//		} else {
//			StatusHandler.log(new Status(IStatus.ERROR, JiraUiPlugin.ID_PLUGIN, Messages.StartWorkAction_cannot_perform));
//		}
	}

	private void startProgress() {

		TaskRepository repository = TasksUi.getRepositoryManager().getRepository(
				editorPage.getTask().getConnectorKind(), editorPage.getTask().getRepositoryUrl());

		TaskAttribute rootAttribute = editorPage.getModel().getTaskData().getRoot();

		Assert.isNotNull(repository);
		Assert.isNotNull(rootAttribute);

		// change assignee
		TaskAttribute assigneeAttribute = rootAttribute.getAttribute(JiraAttribute.USER_ASSIGNED.id());
		if (repository.getUserName() != null && !repository.getUserName().equals(assigneeAttribute.getValue())) {
			assigneeAttribute.setValue(repository.getUserName());
			editorPage.getModel().attributeChanged(assigneeAttribute);
			editorPage.refreshFormContent();
			editorPage.doJiraSubmit(new SubmitJiraIssueListener());
		} else {
			new SubmitJiraIssueListener().startProgress();
		}

	}

	protected void update() {
		if (isTaskInProgress(editorPage.getModel().getTaskData(), editorPage.getModel().getTask())) {
			setChecked(true);
			setToolTipText(Messages.StartWorkAction_stop);
		} else if (isTaskInStop(editorPage.getModel().getTaskData(), editorPage.getModel().getTask())) {
			setChecked(false);
			setToolTipText(Messages.StartWorkAction_start);
		} else {
			setChecked(false);
			setEnabled(false);
			setToolTipText(Messages.StartWorkAction_disabled);
		}
	}

	private void stopProgress() {

		final TaskDataModel taskModel = editorPage.getModel();

		TaskAttribute selectedOperationAttribute = taskModel.getTaskData().getRoot().getMappedAttribute(
				TaskAttribute.OPERATION);

		List<TaskOperation> operations = taskModel.getTaskData().getAttributeMapper().getTaskOperations(
				selectedOperationAttribute);

		for (TaskOperation operation : operations) {

			if (JiraTaskDataHandler.STOP_PROGRESS_OPERATION.equals(operation.getOperationId())) {
				taskModel.getTaskData().getAttributeMapper().setTaskOperation(selectedOperationAttribute, operation);
				taskModel.attributeChanged(selectedOperationAttribute);
				editorPage.doJiraSubmit(new SubmitJobListener() {

					@Override
					public void taskSynchronized(SubmitJobEvent event, IProgressMonitor monitor) throws CoreException {
					}

					@Override
					public void taskSubmitted(SubmitJobEvent event, IProgressMonitor monitor) throws CoreException {
					}

					@Override
					public void done(final SubmitJobEvent event) {
						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
							public void run() {
								editorPage.refreshFormContent();
								TasksUi.getTaskActivityManager().deactivateTask(editorPage.getTask());
								update();
							}
						});
					}
				});
				break;
			}
		}
	}

	private class SubmitJiraIssueListener extends SubmitJobListener {

		@Override
		public void done(SubmitJobEvent event) {
			final IStatus status = event.getJob().getStatus();

			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (status == null || IStatus.OK == status.getSeverity()) {
						editorPage.refreshFormContent();
						startProgress();
					}
				}
			});
		}

		protected void startProgress() {
			TaskDataModel taskModel = editorPage.getModel();

			TaskAttribute selectedOperationAttribute = taskModel.getTaskData().getRoot().getMappedAttribute(
					TaskAttribute.OPERATION);

			List<TaskOperation> operations = taskModel.getTaskData().getAttributeMapper().getTaskOperations(
					selectedOperationAttribute);

			for (TaskOperation operation : operations) {

				if (JiraTaskDataHandler.START_PROGRESS_OPERATION.equals(operation.getOperationId())) {
					taskModel.getTaskData()
							.getAttributeMapper()
							.setTaskOperation(selectedOperationAttribute, operation);
					taskModel.attributeChanged(selectedOperationAttribute);
					editorPage.doJiraSubmit(new SubmitJobListener() {

						@Override
						public void taskSynchronized(SubmitJobEvent event, IProgressMonitor monitor)
								throws CoreException {
						}

						@Override
						public void taskSubmitted(SubmitJobEvent event, IProgressMonitor monitor) throws CoreException {
						}

						@Override
						public void done(final SubmitJobEvent event) {
							PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
								public void run() {
									editorPage.refreshFormContent();
//									TasksUiInternal.activateTaskThroughCommand(event.getJob().getTask());
									TasksUi.getTaskActivityManager().activateTask(event.getJob().getTask());
									update();
								}
							});
						}
					});

					return;
				}
			}

			StatusHandler.log(new Status(IStatus.WARNING, JiraUiPlugin.ID_PLUGIN,
					Messages.StartWorkAction_Start_Progress_Not_Available));
		}

		@Override
		public void taskSubmitted(SubmitJobEvent event, IProgressMonitor monitor) throws CoreException {
		}

		@Override
		public void taskSynchronized(SubmitJobEvent event, IProgressMonitor monitor) throws CoreException {
		}

	}
}
