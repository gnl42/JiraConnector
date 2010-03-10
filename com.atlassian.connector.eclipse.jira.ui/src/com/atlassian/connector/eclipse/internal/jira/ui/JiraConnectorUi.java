/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Eugene Kuleshov - improvements
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.ui;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskComment;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentModel;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.LegendElement;
import org.eclipse.mylyn.tasks.ui.TaskHyperlink;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskSearchPage;
import org.eclipse.mylyn.tasks.ui.wizards.RepositoryQueryWizard;
import org.eclipse.mylyn.tasks.ui.wizards.TaskAttachmentPage;

import com.atlassian.connector.eclipse.internal.jira.core.JiraClientFactory;
import com.atlassian.connector.eclipse.internal.jira.core.JiraCorePlugin;
import com.atlassian.connector.eclipse.internal.jira.core.JiraRepositoryConnector;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraClient;
import com.atlassian.connector.eclipse.internal.jira.core.util.JiraUtil;
import com.atlassian.connector.eclipse.internal.jira.ui.wizards.JiraFilterDefinitionPage;
import com.atlassian.connector.eclipse.internal.jira.ui.wizards.JiraNamedFilterPage;
import com.atlassian.connector.eclipse.internal.jira.ui.wizards.JiraRepositorySettingsPage;
import com.atlassian.connector.eclipse.internal.jira.ui.wizards.NewJiraTaskWizard;

/**
 * @author Mik Kersten
 * @author Eugene Kuleshov
 * @author Steffen Pingel
 */
public class JiraConnectorUi extends AbstractRepositoryConnectorUi {

	/**
	 * Public for testing only.
	 */
	public static Pattern TASK_PATTERN = Pattern.compile("(^|[\\s\\(\\)])(([A-Z]+)-\\d+)"); //$NON-NLS-1$

	public enum JiraTaskKind {
		BUG, FEATURE, STORY, TASK, IMPROVEMENT, CUSTOM_ISSUE, SUB_TASK;

		@Override
		public String toString() {
			switch (this) {
			case BUG:
				return "Bug"; //$NON-NLS-1$
			case STORY:
				return "Story"; //$NON-NLS-1$
			case FEATURE:
				return "New Feature"; //$NON-NLS-1$
			case TASK:
				return "Task"; //$NON-NLS-1$
			case IMPROVEMENT:
				return "Improvement"; //$NON-NLS-1$
			case CUSTOM_ISSUE:
				return "Custom Issue"; //$NON-NLS-1$
			case SUB_TASK:
				return "Sub-task"; //$NON-NLS-1$
			default:
				return ""; //$NON-NLS-1$
			}
		}
	}

	@SuppressWarnings("restriction")
	public JiraConnectorUi() {
		org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().addSearchHandler(new JiraSearchHandler());
	}

	@Override
	public String getTaskKindLabel(ITask repositoryTask) {
		return Messages.JiraConnectorUi_Issue;
	}

	@Override
	public List<LegendElement> getLegendElements() {
		List<LegendElement> legendItems = new ArrayList<LegendElement>();
		legendItems.add(LegendElement.createTask(Messages.JiraConnectorUi_Bug, JiraImages.OVERLAY_BUG));
		legendItems.add(LegendElement.createTask(Messages.JiraConnectorUi_Story, JiraImages.OVERLAY_STORY));
		legendItems.add(LegendElement.createTask(Messages.JiraConnectorUi_Feature, JiraImages.OVERLAY_FEATURE));
		legendItems.add(LegendElement.createTask(Messages.JiraConnectorUi_Improvement, JiraImages.OVERLAY_IMPROVEMENT));
		legendItems.add(LegendElement.createTask(Messages.JiraConnectorUi_Task, JiraImages.OVERLAY_TASK));
		legendItems.add(LegendElement.createTask(Messages.JiraConnectorUi_Subtask, JiraImages.OVERLAY_SUB_TASK));
		return legendItems;
	}

	@Override
	public ImageDescriptor getTaskKindOverlay(ITask task) {
		if (JiraCorePlugin.CONNECTOR_KIND.equals(task.getConnectorKind())) {
			if (JiraTaskKind.BUG.toString().equals(task.getTaskKind())) {
				return JiraImages.OVERLAY_BUG;
			} else if (JiraTaskKind.STORY.toString().equals(task.getTaskKind())) {
				return JiraImages.OVERLAY_STORY;
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
	public IHyperlink[] findHyperlinks(TaskRepository repository, String text, int index, int textOffset) {
		JiraClient client = JiraClientFactory.getDefault().getJiraClient(repository);
		if (client.getCache().hasDetails()) {
			List<IHyperlink> links = null;
			Matcher m = TASK_PATTERN.matcher(text);
			while (m.find()) {
				String projectKey = m.group(3);
				if (client.getCache().getProjectByKey(projectKey) != null) {
					if (links == null) {
						links = new ArrayList<IHyperlink>();
					}
					Region region = new Region(textOffset + m.start(2), m.end() - m.start(2));
					links.add(new TaskHyperlink(region, repository, m.group(2)));
				}
			}
			return links == null ? null : links.toArray(new IHyperlink[0]);
		}
		return null;
	}

	@Override
	public String getTaskHistoryUrl(TaskRepository taskRepository, ITask task) {
		return taskRepository.getRepositoryUrl() + JiraRepositoryConnector.ISSUE_URL_PREFIX + task.getTaskKey()
				+ "?page=history"; //$NON-NLS-1$
	}

	public static String getTaskWorkLogUrl(TaskRepository taskRepository, ITask task) {
		return taskRepository.getRepositoryUrl() + JiraRepositoryConnector.ISSUE_URL_PREFIX + task.getTaskKey()
				+ "?page=com.atlassian.jira.plugin.system.issuetabpanels%3Aworklog-tabpanel"; //$NON-NLS-1$
	}

	@Override
	public String getReplyText(TaskRepository taskRepository, ITask task, ITaskComment taskComment, boolean includeTask) {
		if (taskComment == null) {
			return MessageFormat.format(Messages.JiraConnectorUi_In_reply_to_X, task.getTaskKey()) + ":"; //$NON-NLS-1$
		} else if (includeTask) {
			return MessageFormat.format(Messages.JiraConnectorUi_In_reply_to_X_comment_X, task.getTaskKey(),
					taskComment.getNumber())
					+ ":"; //$NON-NLS-1$
		} else {
			return MessageFormat.format(Messages.JiraConnectorUi_In_reply_to_comment_X, taskComment.getNumber()) + ":"; //$NON-NLS-1$
		}
	}

	@Override
	public IWizardPage getTaskAttachmentPage(TaskAttachmentModel model) {
		TaskAttachmentPage page = new TaskAttachmentPage(model);
		page.setNeedsDescription(false);
		return page;
	}

}
