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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.jira.core.JiraAttribute;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.JiraRepositoryConnector;
import org.eclipse.mylyn.internal.jira.core.util.JiraUtil;
import org.eclipse.mylyn.internal.jira.ui.wizards.JiraFilterDefinitionPage;
import org.eclipse.mylyn.internal.jira.ui.wizards.JiraNamedFilterPage;
import org.eclipse.mylyn.internal.jira.ui.wizards.JiraRepositorySettingsPage;
import org.eclipse.mylyn.internal.jira.ui.wizards.NewJiraTaskWizard;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskComment;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentModel;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.LegendElement;
import org.eclipse.mylyn.tasks.ui.TaskHyperlink;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskSearchPage;
import org.eclipse.mylyn.tasks.ui.wizards.RepositoryQueryWizard;
import org.eclipse.mylyn.tasks.ui.wizards.TaskAttachmentPage;

/**
 * @author Mik Kersten
 * @author Eugene Kuleshov
 * @author Steffen Pingel
 */
@SuppressWarnings("restriction")
public class JiraConnectorUi extends AbstractRepositoryConnectorUi {

	public enum JiraTaskKind {
		BUG, FEATURE, TASK, IMPROVEMENT, CUSTOM_ISSUE, SUB_TASK;

		@Override
		public String toString() {
			switch (this) {
			case BUG:
				return "Bug";
			case FEATURE:
				return "New Feature";
			case TASK:
				return "Task";
			case IMPROVEMENT:
				return "Improvement";
			case CUSTOM_ISSUE:
				return "Custom Issue";
			case SUB_TASK:
				return "Sub-task";
			default:
				return "";
			}
		}
	}

	public JiraConnectorUi() {
		TasksUiPlugin.getDefault().addSearchHandler(new JiraSearchHandler());
	}

	@Override
	public String getTaskKindLabel(ITask repositoryTask) {
		return "Issue";
	}

	@Override
	public List<LegendElement> getLegendElements() {
		List<LegendElement> legendItems = new ArrayList<LegendElement>();
		legendItems.add(LegendElement.createTask("Bug", JiraImages.OVERLAY_BUG));
		legendItems.add(LegendElement.createTask("Feature", JiraImages.OVERLAY_FEATURE));
		legendItems.add(LegendElement.createTask("Improvement", JiraImages.OVERLAY_IMPROVEMENT));
		legendItems.add(LegendElement.createTask("Task", JiraImages.OVERLAY_TASK));
		legendItems.add(LegendElement.createTask("Sub-task", JiraImages.OVERLAY_SUB_TASK));
		return legendItems;
	}

	@Override
	public ImageDescriptor getTaskKindOverlay(ITask task) {
		if (JiraCorePlugin.CONNECTOR_KIND.equals(task.getConnectorKind())) {
			if (JiraTaskKind.BUG.toString().equals(task.getTaskKind())) {
				return JiraImages.OVERLAY_BUG;
			} else if (JiraTaskKind.FEATURE.toString().equals(task.getTaskKind())) {
				return JiraImages.OVERLAY_FEATURE;
			} else if (JiraTaskKind.IMPROVEMENT.toString().equals(task.getTaskKind())) {
				return JiraImages.OVERLAY_IMPROVEMENT;
			} else if (JiraTaskKind.TASK.toString().equals(task.getTaskKind())) {
				return JiraImages.OVERLAY_TASK;
			} else if (JiraTaskKind.SUB_TASK.toString().equals(task.getTaskKind())) {
				return JiraImages.OVERLAY_SUB_TASK;
			}
		}
		return null;
	}

	@Override
	public ITaskSearchPage getSearchPage(TaskRepository repository, IStructuredSelection selection) {
		return new JiraFilterDefinitionPage(repository);
	}

	@Override
	public ITaskRepositoryPage getSettingsPage(TaskRepository taskRepository) {
		return new JiraRepositorySettingsPage(taskRepository);
	}

	@Override
	public IWizard getQueryWizard(TaskRepository repository, IRepositoryQuery query) {
		RepositoryQueryWizard wizard = new RepositoryQueryWizard(repository);
		if (query != null) {
			if (JiraUtil.isFilterDefinition(query)) {
				wizard.addPage(new JiraFilterDefinitionPage(repository, query));
			} else {
				wizard.addPage(new JiraNamedFilterPage(repository, query));
			}
		} else {
			wizard.addPage(new JiraNamedFilterPage(repository));
		}
		return wizard;
	}

	@Override
	public IWizard getNewTaskWizard(TaskRepository taskRepository, ITaskMapping taskSelection) {
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

	@Override
	public boolean supportsDueDates(ITask task) {
		if (JiraCorePlugin.CONNECTOR_KIND.equals(task.getConnectorKind())) {
			// XXX This is only used in the planning editor, and if its input was set correctly as a RepositoryTaskEditorInput
			// we wouldn't have to get the task data this way from here
			TaskData taskData;
			try {
				taskData = TasksUi.getTaskDataManager().getTaskData(task, task.getConnectorKind());
				return taskData.getMappedAttribute(JiraAttribute.DUE_DATE.getId()) != null;
			} catch (CoreException e) {
				StatusHandler.fail(new Status(IStatus.WARNING, JiraUiPlugin.ID_PLUGIN, "Failed to load task data", e));
			}
		}
		return false;
	}

	@Override
	public String getTaskHistoryUrl(TaskRepository taskRepository, ITask task) {
		return taskRepository.getRepositoryUrl() + JiraRepositoryConnector.ISSUE_URL_PREFIX + task.getTaskKey()
				+ "?page=history";
	}

	@Override
	public String getReply(TaskRepository taskRepository, ITask task, ITaskComment taskComment, boolean includeTask) {
		if (taskComment == null) {
			return "In reply to " + task.getTaskKey() + ":";
		} else if (includeTask) {
			return "In reply to " + task.getTaskKey() + " comment #" + taskComment.getNumber() + ":";
		} else {
			return "In reply to comment #" + taskComment.getNumber() + ":";
		}
	}

	@Override
	public IWizardPage getTaskAttachmentPage(TaskAttachmentModel model) {
		TaskAttachmentPage page = new TaskAttachmentPage(model);
		page.setNeedsDescription(false);
		return page;
	}

}
