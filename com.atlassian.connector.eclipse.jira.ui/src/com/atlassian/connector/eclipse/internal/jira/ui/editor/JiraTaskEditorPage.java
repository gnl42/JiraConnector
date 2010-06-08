/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Atlassian - UI improvements, adding new features
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.ui.editor;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.mylyn.internal.tasks.core.data.ITaskDataManagerListener;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataManagerEvent;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorAttributePart;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorDescriptionPart;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.sync.SubmitJobEvent;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.mylyn.tasks.ui.editors.AttributeEditorFactory;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import com.atlassian.connector.eclipse.internal.jira.core.IJiraConstants;
import com.atlassian.connector.eclipse.internal.jira.core.JiraCorePlugin;
import com.atlassian.connector.eclipse.internal.jira.core.WorkLogConverter;
import com.atlassian.connector.eclipse.internal.jira.ui.actions.JiraUiUtil;
import com.atlassian.connector.eclipse.internal.jira.ui.actions.StartWorkEditorToolbarAction;

/**
 * @author Steffen Pingel
 * @author Wojciech Seliga
 */
@SuppressWarnings("restriction")
public class JiraTaskEditorPage extends AbstractTaskEditorPage {

	private StartWorkEditorToolbarAction startWorkAction;

	public JiraTaskEditorPage(TaskEditor editor) {
		super(editor, JiraCorePlugin.CONNECTOR_KIND);
		setNeedsPrivateSection(false);
		setNeedsSubmitButton(true);
	}

	@Override
	protected Set<TaskEditorPartDescriptor> createPartDescriptors() {
		Set<TaskEditorPartDescriptor> parts = super.createPartDescriptors();

		if (removePart(parts, ID_PART_ATTACHMENTS)) {
			parts.add(new TaskEditorPartDescriptor(ID_PART_ATTACHMENTS) {
				@Override
				public AbstractTaskEditorPart createPart() {
					final JiraTaskEditorAttachmentsPart jiraTaskEditorAttachmentsPart = new JiraTaskEditorAttachmentsPart();
					jiraTaskEditorAttachmentsPart.setUseDescriptionColumn(false);
					return jiraTaskEditorAttachmentsPart;
				}
			}.setPath(PATH_ATTACHMENTS));

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

		// remove planning part - it's now in a separate tab
		removePart(parts, ID_PART_PLANNING);

		removePart(parts, ID_PART_ATTRIBUTES);

		parts.add(new TaskEditorPartDescriptor(ID_PART_ATTRIBUTES) {
			@Override
			public AbstractTaskEditorPart createPart() {
				return new TaskEditorAttributePart() {
					@Override
					protected boolean shouldExpandOnCreate() {
						return true;
					}
				};
			}
		}.setPath(PATH_ATTRIBUTES));

		// move Description just below Attributes and expand it always
		removePart(parts, ID_PART_DESCRIPTION);
		parts.add(new TaskEditorPartDescriptor(ID_PART_DESCRIPTION) {
			@Override
			public AbstractTaskEditorPart createPart() {
				TaskEditorDescriptionPart part = new TaskEditorDescriptionPart();
				part.setExpandVertically(true);
				part.setSectionStyle(ExpandableComposite.TITLE_BAR | ExpandableComposite.EXPANDED);
				return part;
			}
		}.setPath(PATH_ATTRIBUTES));

		// and worklog at the very end
		if (getModel().getTaskData().getRoot().getAttribute(IJiraConstants.ATTRIBUTE_WORKLOG_NOT_SUPPORTED) == null) {
			parts.add(new TaskEditorPartDescriptor("com.atlassian.connnector.eclipse.jira.worklog") { //$NON-NLS-1$
				@Override
				public AbstractTaskEditorPart createPart() {
					return new WorkLogPart();
				}
			}.setPath(PATH_COMMENTS));
		}

		return parts;
	}

	private boolean removePart(Set<TaskEditorPartDescriptor> parts, String partId) {
		Iterator<TaskEditorPartDescriptor> iter;
		iter = parts.iterator();
		while (iter.hasNext()) {
			TaskEditorPartDescriptor part = iter.next();
			if (part.getId().equals(partId)) {
				parts.remove(part);
				return true;
			}
		}
		return false;
	}

	@Override
	protected AttributeEditorFactory createAttributeEditorFactory() {
		return new JiraAttributeEditorFactory(getModel(), getTaskRepository(), getEditorSite());
	}

	@Override
	public void fillToolBar(IToolBarManager toolBarManager) {
		super.fillToolBar(toolBarManager);

		if (getModel() != null && getModel().getTaskData() != null && !getModel().getTaskData().isNew()) {
			startWorkAction = new StartWorkEditorToolbarAction(this);
//			startWorkAction.selectionChanged(new StructuredSelection(getTaskEditor()));
			toolBarManager.appendToGroup("repository", startWorkAction); //$NON-NLS-1$
		}
	}

	@Override
	public void dispose() {
		TasksUiPlugin.getTaskDataManager().removeListener(updateStartWorkActionListener);
		super.dispose();
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);

		TasksUiPlugin.getTaskDataManager().addListener(updateStartWorkActionListener);
	}

	private final ITaskDataManagerListener updateStartWorkActionListener = new ITaskDataManagerListener() {

		public void taskDataUpdated(TaskDataManagerEvent event) {
			update(event);
		}

		public void editsDiscarded(TaskDataManagerEvent event) {
			update(event);
		}

		private void update(final TaskDataManagerEvent event) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (event.getTask() != null && getModel() != null) {
						if (event.getTask().equals(getModel().getTask())) {
							if (startWorkAction != null) {
								// event.getTaskData() sometimes returns null
								startWorkAction.update(getModel().getTaskData(), event.getTask());
							}
						}
					}
				}
			});
		}
	};

	private boolean isWorkLogSubmit = false;

	@Override
	public void doSubmit() {

		TaskAttribute attribute = getModel().getTaskData().getRoot().getMappedAttribute(
				WorkLogConverter.ATTRIBUTE_WORKLOG_NEW);
		if (attribute != null) {
			TaskAttribute submitFlagAttribute = attribute.getAttribute(WorkLogConverter.ATTRIBUTE_WORKLOG_NEW_SUBMIT_FLAG);
			//if flag is set and true, submit worklog will happen
			if (submitFlagAttribute != null && submitFlagAttribute.getValue().equals(String.valueOf(true))) {
				isWorkLogSubmit = true;
			}
		}

		super.doSubmit();
	}

	@Override
	protected void handleTaskSubmitted(SubmitJobEvent event) {

		if (isWorkLogSubmit) {
			isWorkLogSubmit = false;

			IStatus status = event.getJob().getStatus();
			if (status == null || status.getSeverity() == IStatus.OK) {
				// remember submitted time
				JiraUiUtil.setLoggedActivityTime(getModel().getTask());
			}
		}

		// ignore
		super.handleTaskSubmitted(event);
	}

}
