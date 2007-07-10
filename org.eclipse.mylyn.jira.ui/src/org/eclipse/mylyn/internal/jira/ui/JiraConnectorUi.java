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
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.internal.jira.ui.wizards.EditJiraQueryWizard;
import org.eclipse.mylyn.internal.jira.ui.wizards.JiraQueryPage;
import org.eclipse.mylyn.internal.jira.ui.wizards.JiraRepositorySettingsPage;
import org.eclipse.mylyn.internal.jira.ui.wizards.NewJiraQueryWizard;
import org.eclipse.mylyn.internal.jira.ui.wizards.NewJiraTaskWizard;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryQuery;
import org.eclipse.mylyn.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.core.AbstractTaskContainer;
import org.eclipse.mylyn.tasks.core.RepositoryTaskData;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;

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
	public String getTaskKindLabel(RepositoryTaskData taskData) {
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
	public IWizard getNewTaskWizard(TaskRepository taskRepository) {
		return new NewJiraTaskWizard(taskRepository);
	}

	@Override
	public String getConnectorKind() {
		return JiraUiPlugin.REPOSITORY_KIND;
	}

	@Override
	public boolean hasSearchPage() {
		return true;
	}

	@Override
	public IHyperlink[] findHyperlinks(TaskRepository repository, String text, int lineOffset, int regionOffset) {
		AbstractRepositoryConnector connector = TasksUiPlugin.getRepositoryManager().getRepositoryConnector(
				repository.getConnectorKind());

		int startPos = lineOffset;
		while (startPos > 0) {
			char c = text.charAt(startPos);
			if (Character.isWhitespace(c) || ",.;[](){}".indexOf(c) > -1)
				break;
			startPos--;
		}
		int endPos = lineOffset;
		while (endPos < text.length()) {
			char c = text.charAt(endPos);
			if (Character.isWhitespace(c) || ",.;[](){}".indexOf(c) > -1)
				break;
			endPos++;
		}

		String[] taskIds = connector.getTaskIdsFromComment(repository, text.substring(startPos, endPos));
		if (taskIds == null || taskIds.length == 0)
			return null;

		IHyperlink[] links = new IHyperlink[taskIds.length];
		for (int i = 0; i < taskIds.length; i++) {
			String taskId = taskIds[i];
			int startRegion = text.indexOf(taskId, startPos);
			links[i] = new JiraHyperLink(new Region(regionOffset + startRegion, taskId.length()), repository, taskId,
					connector.getTaskUrl(repository.getUrl(), taskId));
		}
		return links;
	}

}
