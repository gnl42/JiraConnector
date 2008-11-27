/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Eugene Kuleshov - improvements
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.ui;

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
import org.eclipse.mylyn.internal.jira.core.JiraClientFactory;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.JiraRepositoryConnector;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.util.JiraUtil;
import org.eclipse.mylyn.internal.jira.ui.wizards.JiraFilterDefinitionPage;
import org.eclipse.mylyn.internal.jira.ui.wizards.JiraNamedFilterPage;
import org.eclipse.mylyn.internal.jira.ui.wizards.JiraRepositorySettingsPage;
import org.eclipse.mylyn.internal.jira.ui.wizards.NewJiraTaskWizard;
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

/**
 * @author Mik Kersten
 * @author Eugene Kuleshov
 * @author Steffen Pingel
 */
public class JiraConnectorUi extends AbstractRepositoryConnectorUi {

	private static Pattern TASK_PATTERN = Pattern.compile("([A-Z]+)-\\d+");

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

	@SuppressWarnings("restriction")
	public JiraConnectorUi() {
		org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().addSearchHandler(new JiraSearchHandler());
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
		legendItems.add(LegendElement.createTask("Subtask", JiraImages.OVERLAY_SUB_TASK));
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
	public IHyperlink[] findHyperlinks(TaskRepository repository, String text, int index, int textOffset) {
		JiraClient client = JiraClientFactory.getDefault().getJiraClient(repository);
		if (client.getCache().hasDetails()) {
			List<IHyperlink> links = null;
			Matcher m = TASK_PATTERN.matcher(text);
			while (m.find()) {
				String projectKey = m.group(1);
				if (client.getCache().getProjectByKey(projectKey) != null) {
					if (links == null) {
						links = new ArrayList<IHyperlink>();
					}
					Region region = new Region(textOffset + m.start(), m.end() - m.start());
					links.add(new TaskHyperlink(region, repository, m.group()));
				}
			}
			return links == null ? null : links.toArray(new IHyperlink[0]);
		}
		return null;
	}

	@Override
	public String getTaskHistoryUrl(TaskRepository taskRepository, ITask task) {
		return taskRepository.getRepositoryUrl() + JiraRepositoryConnector.ISSUE_URL_PREFIX + task.getTaskKey()
				+ "?page=history";
	}

	public static String getTaskWorkLogUrl(TaskRepository taskRepository, ITask task) {
		return taskRepository.getRepositoryUrl() + JiraRepositoryConnector.ISSUE_URL_PREFIX + task.getTaskKey()
				+ "?page=com.atlassian.jira.plugin.system.issuetabpanels%3Aworklog-tabpanel";
	}

	@Override
	public String getReplyText(TaskRepository taskRepository, ITask task, ITaskComment taskComment, boolean includeTask) {
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
