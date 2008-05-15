/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.ui;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.JiraTask;
import org.eclipse.mylyn.internal.jira.core.util.JiraUtil;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.core.deprecated.AbstractTaskListFactory;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.w3c.dom.Element;

/**
 * @author Mik Kersten
 * @author Wesley Coelho (filter prototyping)
 */
@SuppressWarnings( { "deprecation", "restriction" })
@Deprecated
public class JiraTaskListFactory extends AbstractTaskListFactory {

	private static final String KEY_JIRA = "Jira";

	private static final String KEY_CUSTOM = "JiraCustom";

	private static final String KEY_JIRA_QUERY = KEY_JIRA + AbstractTaskListFactory.KEY_QUERY;

	private static final String KEY_JIRA_CUSTOM = KEY_JIRA + KEY_CUSTOM + AbstractTaskListFactory.KEY_QUERY;

	private static final String KEY_JIRA_ISSUE = "JiraIssue";

	private static final String KEY_FILTER_NAME = "FilterName";

	private static final String KEY_FILTER_ID = "FilterID";

	private static final String KEY_FILTER_CUSTOM_URL = "FilterCustomUrl";

	private static final String KEY_KEY = "Key";

	@Override
	public String getTaskElementName() {
		return KEY_JIRA_ISSUE;
	}

	@Override
	public Set<String> getQueryElementNames() {
		Set<String> names = new HashSet<String>();
		names.add(KEY_JIRA_QUERY);
		names.add(KEY_JIRA_CUSTOM);
		return names;
	}

	@Override
	public boolean canCreate(IRepositoryQuery category) {
		return JiraCorePlugin.CONNECTOR_KIND.equals(category.getConnectorKind());
	}

	@Override
	public boolean canCreate(ITask task) {
		return task instanceof JiraTask;
	}

	@Override
	public RepositoryQuery createQuery(String repositoryUrl, String queryString, String label, Element element) {
		TaskRepository taskRepository = TasksUi.getRepositoryManager().getRepository(JiraCorePlugin.CONNECTOR_KIND,
				repositoryUrl);
		if (taskRepository != null) {
			IRepositoryQuery query = TasksUi.getTasksModel().createQuery(taskRepository);
			query.setSummary(label);
			query.setUrl(queryString);
			query.setAttribute(KEY_FILTER_CUSTOM_URL, element.getAttribute(KEY_FILTER_CUSTOM_URL));
			query.setAttribute(KEY_FILTER_ID, element.getAttribute(KEY_FILTER_ID));
			query.setAttribute(KEY_FILTER_NAME, element.getAttribute(KEY_FILTER_NAME));
			return (RepositoryQuery) query;
		}
		return null;
	}

	@Override
	public void setAdditionalAttributes(IRepositoryQuery query, Element node) {
		node.setAttribute(KEY_FILTER_ID, query.getAttribute(KEY_FILTER_ID));
		node.setAttribute(KEY_FILTER_NAME, query.getAttribute(KEY_FILTER_NAME));
		node.setAttribute(KEY_FILTER_CUSTOM_URL, query.getAttribute(KEY_FILTER_CUSTOM_URL));
	}

	@Override
	public void setAdditionalAttributes(ITask task, Element element) {
		element.setAttribute(KEY_KEY, ((JiraTask) task).getTaskKey());
	}

	@Override
	public AbstractTask createTask(String repositoryUrl, String taskId, String summary, Element element) {
		JiraTask task = new JiraTask(repositoryUrl, taskId, summary);

		if (element.hasAttribute(KEY_KEY)) {
			String key = element.getAttribute(KEY_KEY);
			task.setTaskKey(key);
		}
		return task;
	}

	@Override
	public String getQueryElementName(IRepositoryQuery query) {
		if (JiraCorePlugin.CONNECTOR_KIND.equals(query.getConnectorKind())) {
			if (JiraUtil.isFilterDefinition(query)) {
				return KEY_JIRA_CUSTOM;
			} else {
				return KEY_JIRA_QUERY;
			}
		}
		return "";
	}
}
