/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.internal.jira.core.JiraAttributeFactory;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.JiraCustomQuery;
import org.eclipse.mylyn.internal.jira.core.JiraRepositoryConnector;
import org.eclipse.mylyn.internal.jira.core.JiraRepositoryQuery;
import org.eclipse.mylyn.internal.jira.core.JiraTask;
import org.eclipse.mylyn.internal.jira.ui.wizards.EditJiraQueryWizard;
import org.eclipse.mylyn.internal.jira.ui.wizards.JiraQueryPage;
import org.eclipse.mylyn.internal.jira.ui.wizards.JiraRepositorySettingsPage;
import org.eclipse.mylyn.internal.jira.ui.wizards.NewJiraQueryWizard;
import org.eclipse.mylyn.internal.jira.ui.wizards.NewJiraTaskWizard;
import org.eclipse.mylyn.internal.tasks.core.deprecated.RepositoryTaskData;
import org.eclipse.mylyn.internal.tasks.core.deprecated.TaskSelection;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryQuery;
import org.eclipse.mylyn.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.core.AbstractTaskContainer;
import org.eclipse.mylyn.tasks.core.ITaskComment;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentModel;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.TaskHyperlink;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.mylyn.tasks.ui.wizards.TaskAttachmentPage;

/**
 * @author Mik Kersten
 * @author Eugene Kuleshov
 * @author Steffen Pingel
 */
public class JiraConnectorUi extends AbstractRepositoryConnectorUi {

	@Override
	public String getTaskKindLabel(AbstractTask repositoryTask) {
		return "Issue";
	}

	@Override
	public List<AbstractTaskContainer> getLegendItems() {
		List<AbstractTaskContainer> legendItems = new ArrayList<AbstractTaskContainer>();

		JiraTask bug = new JiraTask("", "bug", "Bug");
		bug.setTaskKind(JiraTask.Kind.BUG.toString());
		legendItems.add(bug);

		JiraTask feature = new JiraTask("", "feature", "Feature");
		feature.setTaskKind(JiraTask.Kind.FEATURE.toString());
		legendItems.add(feature);

		JiraTask improvement = new JiraTask("", "improvement", "Improvement");
		improvement.setTaskKind(JiraTask.Kind.IMPROVEMENT.toString());
		legendItems.add(improvement);

		JiraTask task = new JiraTask("", "task", "Task");
		task.setTaskKind(JiraTask.Kind.TASK.toString());
		legendItems.add(task);

		JiraTask subTask = new JiraTask("", "task", "Sub-task");
		subTask.setTaskKind(JiraTask.Kind.SUB_TASK.toString());
		legendItems.add(subTask);

		return legendItems;
	}

	@Override
	public ImageDescriptor getTaskKindOverlay(AbstractTask repositoryTask) {
		if (repositoryTask instanceof JiraTask) {
			JiraTask task = (JiraTask) repositoryTask;
			if (JiraTask.Kind.BUG.toString().equals(task.getTaskKind())) {
				return JiraImages.OVERLAY_BUG;
			} else if (JiraTask.Kind.FEATURE.toString().equals(task.getTaskKind())) {
				return JiraImages.OVERLAY_FEATURE;
			} else if (JiraTask.Kind.IMPROVEMENT.toString().equals(task.getTaskKind())) {
				return JiraImages.OVERLAY_IMPROVEMENT;
			} else if (JiraTask.Kind.TASK.toString().equals(task.getTaskKind())) {
				return JiraImages.OVERLAY_TASK;
			} else if (JiraTask.Kind.SUB_TASK.toString().equals(task.getTaskKind())) {
				return JiraImages.OVERLAY_SUB_TASK;
			}
		}
		return super.getTaskKindOverlay(repositoryTask);
	}

	@Override
	public WizardPage getSearchPage(TaskRepository repository, IStructuredSelection selection) {
		return new JiraQueryPage(repository);
	}

	@Override
	public AbstractRepositorySettingsPage getSettingsPage() {
		return new JiraRepositorySettingsPage(this);
	}

	@Override
	public IWizard getQueryWizard(TaskRepository repository, AbstractRepositoryQuery query) {
		if (query instanceof JiraRepositoryQuery || query instanceof JiraCustomQuery) {
			return new EditJiraQueryWizard(repository, query);
		}
		return new NewJiraQueryWizard(repository);
	}

	@Override
	public IWizard getNewTaskWizard(TaskRepository taskRepository, TaskSelection taskSelection) {
		return new NewJiraTaskWizard(taskRepository, taskSelection);
	}

	@Override
	public String getConnectorKind() {
		return JiraCorePlugin.CONNECTOR_KIND;
	}

	@Override
	public boolean hasSearchPage() {
		return true;
	}

	@Override
	public IHyperlink[] findHyperlinks(TaskRepository repository, String text, int lineOffset, int regionOffset) {
		AbstractRepositoryConnector connector = TasksUi.getRepositoryManager().getRepositoryConnector(
				repository.getConnectorKind());
		if (text.length() == 0) {
			return null;
		}

		int startPos = lineOffset;
		if (startPos < 0) {
			startPos = 0;
		} else if (startPos >= text.length()) {
			startPos = text.length() - 1;
		}
		while (startPos > 0) {
			char c = text.charAt(startPos);
			if (Character.isWhitespace(c) || ",.;[](){}".indexOf(c) > -1) {
				break;
			}
			startPos--;
		}
		int endPos = lineOffset;
		if (endPos < 0) {
			endPos = 0;
		} else if (endPos >= text.length()) {
			endPos = text.length() - 1;
		}
		while (endPos < text.length()) {
			char c = text.charAt(endPos);
			if (Character.isWhitespace(c) || ",.;[](){}".indexOf(c) > -1) {
				break;
			}
			endPos++;
		}

		String[] taskIds = connector.getTaskIdsFromComment(repository, text.substring(startPos, endPos));
		if (taskIds == null || taskIds.length == 0) {
			return null;
		}

		IHyperlink[] links = new IHyperlink[taskIds.length];
		for (int i = 0; i < taskIds.length; i++) {
			String taskId = taskIds[i];
			int startRegion = text.indexOf(taskId, startPos);
			links[i] = new TaskHyperlink(new Region(regionOffset + startRegion, taskId.length()), repository, taskId);
		}
		return links;
	}

	@SuppressWarnings("restriction")
	@Override
	public boolean supportsDueDates(AbstractTask task) {
		if (task instanceof JiraTask) {
			// XXX This is only used in the planning editor, and if its input was set correctly as a RepositoryTaskEditorInput
			// we wouldn't have to get the task data this way from here
			RepositoryTaskData taskData = TasksUiPlugin.getTaskDataStorageManager().getNewTaskData(
					task.getRepositoryUrl(), task.getTaskId());
			if (taskData != null && taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_DUE_DATE) != null) {
				return true;
			}
		}
		return super.supportsDueDates(task);
	}

	@Override
	public String getTaskHistoryUrl(TaskRepository taskRepository, AbstractTask task) {
		return taskRepository.getRepositoryUrl() + JiraRepositoryConnector.ISSUE_URL_PREFIX + task.getTaskKey()
				+ "?page=history";
	}

	@Override
	public String getReply(TaskRepository taskRepository, AbstractTask task, ITaskComment taskComment,
			boolean includeTask) {
		if (taskComment == null) {
			return "In reply to " + task.getTaskKey() + ":";
		} else if (includeTask) {
			return "In reply to " + task.getTaskKey() + " comment #" + taskComment.getNumber() + ":";
		} else {
			return "In reply to comment #" + taskComment.getNumber() + ":";
		}
	}

	@Override
	public IWizardPage getAttachmentPage(TaskAttachmentModel model) {
		TaskAttachmentPage page = new TaskAttachmentPage(model);
		page.setNeedsDescription(false);
		return page;
	}

}
