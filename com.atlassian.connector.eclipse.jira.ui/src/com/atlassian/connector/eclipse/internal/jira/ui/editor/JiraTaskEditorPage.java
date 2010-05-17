/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.ui.editor;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer;
import org.eclipse.mylyn.internal.tasks.ui.editors.CheckboxMultiSelectAttributeEditor;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionPart;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskMigrator;
import org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.core.data.TaskOperation;
import org.eclipse.mylyn.tasks.core.sync.SubmitJob;
import org.eclipse.mylyn.tasks.core.sync.SubmitJobEvent;
import org.eclipse.mylyn.tasks.core.sync.SubmitJobListener;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.mylyn.tasks.ui.editors.AttributeEditorFactory;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan;
import org.eclipse.ui.PlatformUI;

import com.atlassian.connector.eclipse.internal.jira.core.IJiraConstants;
import com.atlassian.connector.eclipse.internal.jira.core.JiraAttribute;
import com.atlassian.connector.eclipse.internal.jira.core.JiraCorePlugin;
import com.atlassian.connector.eclipse.internal.jira.core.JiraFieldType;
import com.atlassian.connector.eclipse.internal.jira.core.JiraTaskDataHandler;
import com.atlassian.connector.eclipse.internal.jira.core.util.JiraUtil;
import com.atlassian.connector.eclipse.internal.jira.ui.actions.StartWorkAction;

/**
 * @author Steffen Pingel
 */
public class JiraTaskEditorPage extends AbstractTaskEditorPage {

	public JiraTaskEditorPage(TaskEditor editor) {
		super(editor, JiraCorePlugin.CONNECTOR_KIND);
		setNeedsPrivateSection(true);
		setNeedsSubmitButton(true);
	}

	@Override
	protected Set<TaskEditorPartDescriptor> createPartDescriptors() {
		Set<TaskEditorPartDescriptor> parts = super.createPartDescriptors();
		if (getModel().getTaskData().getRoot().getAttribute(IJiraConstants.ATTRIBUTE_WORKLOG_NOT_SUPPORTED) == null) {
			parts.add(new TaskEditorPartDescriptor("org.eclipse.mylyn.jira.worklog") { //$NON-NLS-1$
				@Override
				public AbstractTaskEditorPart createPart() {
					return new WorkLogPart();
				}
			}.setPath(ID_PART_ATTRIBUTES + "/" + PATH_PLANNING)); //$NON-NLS-1$
		}

		// replace summary part
		Iterator<TaskEditorPartDescriptor> iter = parts.iterator();
		while (iter.hasNext()) {
			TaskEditorPartDescriptor part = iter.next();
			if (part.getId().equals(ID_PART_SUMMARY)) {
				parts.remove(part);

				// add JIRA specific summary part (with votes number)
				parts.add(new TaskEditorPartDescriptor(ID_PART_SUMMARY) {
					@Override
					public AbstractTaskEditorPart createPart() {
						return new JiraTaskEditorSummaryPart();
					}
				}.setPath(part.getPath()));

				break;
			}
		}

		// remove comments part
		iter = parts.iterator();
		while (iter.hasNext()) {
			TaskEditorPartDescriptor part = iter.next();
			if (part.getId().equals(ID_PART_COMMENTS)) {
				parts.remove(part);

				// add JIRA specific comments part (with visibility restriction info for each comment)
				parts.add(new TaskEditorPartDescriptor(ID_PART_COMMENTS) {
					@Override
					public AbstractTaskEditorPart createPart() {
						return new JiraCommentPartCopy();
//						return new JiraCommentPart();
					}
				}.setPath(part.getPath()));

				break;
			}
		}

		// remove standard new comment part 
		iter = parts.iterator();
		while (iter.hasNext()) {
			TaskEditorPartDescriptor part = iter.next();
			if (part.getId().equals(ID_PART_NEW_COMMENT)) {
				parts.remove(part);

				// add JIRA specific comment part (with visibility restriction combo)
				parts.add(new TaskEditorPartDescriptor(ID_PART_NEW_COMMENT) {
					@Override
					public AbstractTaskEditorPart createPart() {
						return new JiraNewCommentPart(getModel());
					}
				}.setPath(part.getPath()));

				break;
			}
		}

		return parts;
	}

	@Override
	protected AttributeEditorFactory createAttributeEditorFactory() {
		AttributeEditorFactory factory = new AttributeEditorFactory(getModel(), getTaskRepository(), getEditorSite()) {
			@Override
			public AbstractAttributeEditor createEditor(String type, TaskAttribute taskAttribute) {
				if (JiraTaskDataHandler.isTimeSpanAttribute(taskAttribute)) {
					return new TimeSpanAttributeEditor(getModel(), taskAttribute);
				}
				if (JiraUtil.isCustomDateTimeAttribute(taskAttribute)) {
					String metaType = taskAttribute.getMetaData().getValue(IJiraConstants.META_TYPE);
					if (JiraFieldType.DATETIME.getKey().equals(metaType)) {
						return new DateTimeAttributeEditor(getModel(), taskAttribute, true);
					} else if (JiraFieldType.DATE.getKey().equals(metaType)) {
						return new DateTimeAttributeEditor(getModel(), taskAttribute, false);
					}
				}
				if (TaskAttribute.TYPE_MULTI_SELECT.equals(type)) {
					CheckboxMultiSelectAttributeEditor attributeEditor = new CheckboxMultiSelectAttributeEditor(
							getModel(), taskAttribute);
					attributeEditor.setLayoutHint(new LayoutHint(RowSpan.SINGLE, ColumnSpan.SINGLE));
					return attributeEditor;
				}
				if (IJiraConstants.TYPE_NUMBER.equals(type)) {
					return new NumberAttributeEditor(getModel(), taskAttribute);
				}
				return super.createEditor(type, taskAttribute);
			}
		};
		return factory;
	}

	@Override
	public void fillToolBar(IToolBarManager toolBarManager) {
		super.fillToolBar(toolBarManager);

		// TODO jj check isNew
		// TODO jj check isDirty

		if (!getModel().getTaskData().isNew()) {
			StartWorkAction startWorkAction = new StartWorkAction(this);
//			startWorkAction.selectionChanged(new StructuredSelection(getTaskEditor()));
			toolBarManager.appendToGroup("repository", startWorkAction); //$NON-NLS-1$
		}
	}

	@SuppressWarnings("restriction")
	public void doJiraSubmit(SubmitJobListener submitListener) {
//		if (!submitEnabled || !needsSubmit()) {
//			return;
//		}

		try {
			showEditorBusy(true);

			doSave(new NullProgressMonitor());

			SubmitJob submitJob = TasksUiInternal.getJobFactory().createSubmitTaskJob(getConnector(),
					getModel().getTaskRepository(), getTask(), getModel().getTaskData(),
					getModel().getChangedOldAttributes());
			submitJob.addSubmitJobListener(submitListener);
			submitJob.addSubmitJobListener(new SubmitTaskJobListener(getAttachContext()));
			submitJob.schedule();
		} catch (RuntimeException e) {
			showEditorBusy(false);
			throw e;
		}
	}

	public boolean isTaskInProgress() {
		return isAssignedToMe() && isInProgressState() && haveStopProgressOperation();
	}

	public boolean isTaskInStop() {
		if (isAssignedToMe() && isInOpenState() && haveStartProgressOperation()) {
			return true;
		} else if (!isAssignedToMe()) {
			return true;
		}

		return false;
	}

	private boolean haveStopProgressOperation() {
		return haveOperation(JiraTaskDataHandler.STOP_PROGRESS_OPERATION);
	}

	private boolean haveStartProgressOperation() {
		return haveOperation(JiraTaskDataHandler.START_PROGRESS_OPERATION);
	}

	private boolean haveOperation(String operationId) {
		TaskDataModel taskModel = getModel();

		TaskAttribute selectedOperationAttribute = taskModel.getTaskData().getRoot().getMappedAttribute(
				TaskAttribute.OPERATION);

		List<TaskOperation> operations = taskModel.getTaskData().getAttributeMapper().getTaskOperations(
				selectedOperationAttribute);

		for (TaskOperation operation : operations) {
			if (operationId.equals(operation.getOperationId())) {
				return true;
			}
		}

		return false;
	}

	private boolean isInOpenState() {

		String statusId = getModel().getTaskData().getRoot().getAttribute(JiraAttribute.STATUS.id()).getValue();

		return statusId != null
				&& (statusId.equals(JiraTaskDataHandler.OPEN_STATUS) || statusId.equals(JiraTaskDataHandler.REOPEN_STATUS));
	}

	private boolean isInProgressState() {

		String statusId = getModel().getTaskData().getRoot().getAttribute(JiraAttribute.STATUS.id()).getValue();

		return statusId != null && statusId.equals(JiraTaskDataHandler.IN_PROGRESS_STATUS);
	}

	private boolean isAssignedToMe() {
		TaskRepository repository = TasksUi.getRepositoryManager().getRepository(getTask().getConnectorKind(),
				getTask().getRepositoryUrl());

		if (repository == null) {
			return false;
		}

		TaskAttribute rootAttribute = getModel().getTaskData().getRoot();

		if (rootAttribute == null) {
			return false;
		}

		TaskAttribute assigneeAttribute = rootAttribute.getAttribute(JiraAttribute.USER_ASSIGNED.id());

		return repository.getUserName() != null && repository.getUserName().equals(assigneeAttribute.getValue());
	}

	private class SubmitTaskJobListener extends SubmitJobListener {

		private final boolean attachContext;

		public SubmitTaskJobListener(boolean attachContext) {
			this.attachContext = attachContext;
		}

		@Override
		public void done(SubmitJobEvent event) {
			final SubmitJob job = event.getJob();
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

				@SuppressWarnings("restriction")
				private void addTask(ITask newTask) {
					AbstractTaskContainer parent = null;
					AbstractTaskEditorPart actionPart = getPart(ID_PART_ACTIONS);
					if (actionPart instanceof TaskEditorActionPart) {
						parent = ((TaskEditorActionPart) actionPart).getCategory();
					}
					TasksUiInternal.getTaskList().addTask(newTask, parent);
				}

				@SuppressWarnings("restriction")
				public void run() {
					try {
						if (job.getStatus() == null) {
							TasksUiInternal.synchronizeRepositoryInBackground(getTaskRepository());
							if (job.getTask().equals(getTask())) {
								refreshFormContent();
							} else {
								ITask oldTask = getTask();
								ITask newTask = job.getTask();
								addTask(newTask);

								TaskMigrator migrator = new TaskMigrator(oldTask);
								migrator.setDelete(true);
								migrator.setEditor(getTaskEditor());
								migrator.execute(newTask);
							}
						}
						handleTaskSubmitted(new SubmitJobEvent(job));
					} finally {
						showEditorBusy(false);
					}
				}
			});
		}

		@SuppressWarnings("restriction")
		@Override
		public void taskSubmitted(SubmitJobEvent event, IProgressMonitor monitor) throws CoreException {
			if (!getModel().getTaskData().isNew() && attachContext) {
				AttachmentUtil.postContext(getConnector(), getModel().getTaskRepository(), getTask(), "", null, monitor); //$NON-NLS-1$
			}
		}

		@Override
		public void taskSynchronized(SubmitJobEvent event, IProgressMonitor monitor) {
		}
	}

	@SuppressWarnings("restriction")
	private boolean getAttachContext() {
		AbstractTaskEditorPart actionPart = getPart(ID_PART_ACTIONS);
		if (actionPart instanceof TaskEditorActionPart) {
			return ((TaskEditorActionPart) actionPart).getAttachContext();
		}
		return false;
	}
}
