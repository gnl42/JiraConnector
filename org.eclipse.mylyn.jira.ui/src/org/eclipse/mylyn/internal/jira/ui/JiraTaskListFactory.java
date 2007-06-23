/*******************************************************************************
 * Copyright (c) 2006 - 2006 Mylar eclipse.org project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mylar project committers - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.ui;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.mylyn.internal.jira.core.model.NamedFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.monitor.core.util.StatusManager;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryQuery;
import org.eclipse.mylyn.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.core.AbstractTaskListFactory;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUiPlugin;
import org.w3c.dom.Element;

/**
 * @author Mik Kersten
 * @author Wesley Coelho (filter prototyping)
 */
public class JiraTaskListFactory extends AbstractTaskListFactory {

	private static final String KEY_JIRA = "Jira";

	private static final String KEY_CUSTOM = "JiraCustom";

	private static final String KEY_JIRA_QUERY = KEY_JIRA + AbstractTaskListFactory.KEY_QUERY;

	private static final String KEY_JIRA_CUSTOM = KEY_JIRA + KEY_CUSTOM + AbstractTaskListFactory.KEY_QUERY;

	private static final String KEY_JIRA_ISSUE = "JiraIssue";

	private static final String KEY_FILTER_NAME = "FilterName";

	private static final String KEY_FILTER_ID = "FilterID";

	private static final String KEY_FILTER_CUSTOM = "FilterCustom";

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
	public boolean canCreate(AbstractRepositoryQuery category) {
		return category instanceof JiraRepositoryQuery || category instanceof JiraCustomQuery;
	}

	@Override
	public boolean canCreate(AbstractTask task) {
		return task instanceof JiraTask;
	}

	@Override
	public AbstractRepositoryQuery createQuery(String repositoryUrl, String queryString, String label, Element element) {
		String custom = element.getAttribute(KEY_FILTER_CUSTOM);
		String customUrl = element.getAttribute(KEY_FILTER_CUSTOM_URL);
		AbstractRepositoryQuery query;
		if (custom != null && custom.length() > 0) {
			// TODO remove this at some point
			FilterDefinition filter = decodeFilter(custom);
			if (filter == null) {
				StatusManager.log("Failed to restore custom query "
						+ element.getAttribute(KEY_FILTER_ID), this);
				return null;
			}
			filter.setName(element.getAttribute(KEY_FILTER_ID));
			// filter.setDescription(element.getAttribute(KEY_FILTER_DESCRIPTION));

			query = new JiraCustomQuery(repositoryUrl, filter, TasksUiPlugin.getRepositoryManager().getRepository(
					JiraUiPlugin.REPOSITORY_KIND, repositoryUrl).getCharacterEncoding());
		} else if (customUrl != null && customUrl.length() > 0) {
			TaskRepository repository = TasksUiPlugin.getRepositoryManager().getRepository(
					JiraUiPlugin.REPOSITORY_KIND, repositoryUrl);
			query = new JiraCustomQuery(element.getAttribute(KEY_FILTER_ID), customUrl, repositoryUrl, repository
					.getCharacterEncoding());

		} else {
			NamedFilter namedFilter = new NamedFilter();
			namedFilter.setId(element.getAttribute(KEY_FILTER_ID));
			namedFilter.setName(element.getAttribute(KEY_FILTER_NAME));
			query = new JiraRepositoryQuery(repositoryUrl, namedFilter);
		}

		return query;
	}

	@Override
	public void setAdditionalAttributes(AbstractRepositoryQuery query, Element node) {
//		String queryTagName = getQueryElementName(query);
		if (query instanceof JiraRepositoryQuery) {
			NamedFilter filter = ((JiraRepositoryQuery) query).getNamedFilter();
			node.setAttribute(KEY_FILTER_ID, filter.getId());
			node.setAttribute(KEY_FILTER_NAME, filter.getName());
		} else if (query instanceof JiraCustomQuery) {
			JiraCustomQuery customQuery = (JiraCustomQuery) query;
			node.setAttribute(KEY_FILTER_ID, customQuery.getSummary());
			node.setAttribute(KEY_FILTER_NAME, customQuery.getSummary());
			node.setAttribute(KEY_FILTER_CUSTOM_URL, customQuery.getUrl());
		}
	}

	private FilterDefinition decodeFilter(String filter) {
		byte[] buff = new byte[filter.length() / 2];
		char[] chars = filter.toCharArray();
		for (int i = 0, k = 0; i < chars.length; i += 2, k++) {
			buff[k] = (byte) ((((chars[i] - 'A') << 4) | (chars[i + 1] - 'A')) & 0xff);
		}

		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(new ByteArrayInputStream(buff));
			return (FilterDefinition) ois.readObject();
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			return null;
		}
	}

	@Override
	public void setAdditionalAttributes(AbstractTask task, Element element) {
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
	public String getQueryElementName(AbstractRepositoryQuery query) {
		if (query instanceof JiraRepositoryQuery) {
			return KEY_JIRA_QUERY;
		} else if (query instanceof JiraCustomQuery) {
			return KEY_JIRA_CUSTOM;
		}
		return "";
	}
}
