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

import org.eclipse.mylyn.internal.jira.core.model.NamedFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.monitor.core.util.StatusManager;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryQuery;
import org.eclipse.mylyn.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.core.AbstractTaskContainer;
import org.eclipse.mylyn.tasks.core.DelegatingTaskExternalizer;
import org.eclipse.mylyn.tasks.core.TaskExternalizationException;
import org.eclipse.mylyn.tasks.core.TaskList;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUiPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Mik Kersten
 * @author Wesley Coelho (filter prototyping)
 */
public class JiraTaskExternalizer extends DelegatingTaskExternalizer {

	private static final String KEY_JIRA = "Jira";

	private static final String KEY_CUSTOM = "JiraCustom";

	private static final String KEY_JIRA_CATEGORY = "JiraQuery" + KEY_CATEGORY;

//	private static final String KEY_JIRA_QUERY_HIT = KEY_JIRA + KEY_QUERY_HIT;

	private static final String KEY_JIRA_QUERY = KEY_JIRA + KEY_QUERY;

	private static final String KEY_JIRA_CUSTOM = KEY_JIRA + KEY_CUSTOM + KEY_QUERY;

	private static final String KEY_JIRA_ISSUE = "JiraIssue";

	private static final String KEY_FILTER_NAME = "FilterName";

	private static final String KEY_FILTER_ID = "FilterID";

	private static final String KEY_FILTER_CUSTOM = "FilterCustom";

	private static final String KEY_FILTER_CUSTOM_URL = "FilterCustomUrl";

	private static final String KEY_KEY = "Key";

	@Override
	public boolean canReadQuery(Node node) {
		return node.getNodeName().equals(KEY_JIRA_QUERY) || node.getNodeName().equals(KEY_JIRA_CUSTOM);
	}

// @Override
// public boolean canCreateElementFor(AbstractTask queryHit) {
// return queryHit instanceof JiraQueryHit;
// }

// @Override
// public Element createQueryHitElement(AbstractTask queryHit,
// Document doc, Element parent) {
// Element node = super.createQueryHitElement(queryHit, doc, parent);
// node.setAttribute(KEY_KEY, ((JiraTask) queryHit).getKey());
// return node;
// }

	@Override
	public boolean canCreateElementFor(AbstractRepositoryQuery category) {
		return category instanceof JiraRepositoryQuery || category instanceof JiraCustomQuery;
	}

	@Override
	public boolean canCreateElementFor(AbstractTask task) {
		return task instanceof JiraTask;
	}

	@Override
	public AbstractRepositoryQuery readQuery(Node node, TaskList taskList) throws TaskExternalizationException {
		Element element = (Element) node;
		String repositoryUrl = element.getAttribute(KEY_REPOSITORY_URL);
		String custom = element.getAttribute(KEY_FILTER_CUSTOM);
		String customUrl = element.getAttribute(KEY_FILTER_CUSTOM_URL);
		AbstractRepositoryQuery query;
		if (custom != null && custom.length() > 0) {
			// TODO remove this at some point
			FilterDefinition filter = decodeFilter(custom);
			if (filter == null) {
				throw new TaskExternalizationException("Failed to restore custom query "
						+ element.getAttribute(KEY_FILTER_ID));
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
	public Element createQueryElement(AbstractRepositoryQuery query, Document doc, Element parent) {
		String queryTagName = getQueryTagNameForElement(query);
		Element node = doc.createElement(queryTagName);

		node.setAttribute(KEY_NAME, query.getSummary());
// node.setAttribute(KEY_QUERY_MAX_HITS, query.getMaxHits() + "");
		node.setAttribute(KEY_QUERY_STRING, query.getUrl());
		node.setAttribute(KEY_REPOSITORY_URL, query.getRepositoryUrl());

		if (query instanceof JiraRepositoryQuery) {
			NamedFilter filter = ((JiraRepositoryQuery) query).getNamedFilter();
			node.setAttribute(KEY_FILTER_ID, filter.getId());
			node.setAttribute(KEY_FILTER_NAME, filter.getName());
			// node.setAttribute(KEY_FILTER_DESCRIPTION,
			// filter.getDescription());
		} else if (query instanceof JiraCustomQuery) {
			JiraCustomQuery customQuery = (JiraCustomQuery) query;
			// FilterDefinition filter = customQuery.getFilterDefinition();
			node.setAttribute(KEY_FILTER_ID, customQuery.getSummary());
			node.setAttribute(KEY_FILTER_NAME, customQuery.getSummary());
			// node.setAttribute(KEY_FILTER_DESCRIPTION,
			// filter.getDescription());
			node.setAttribute(KEY_FILTER_CUSTOM_URL, customQuery.getUrl());
		}

		for (AbstractTask hit : query.getHits()) {
			try {
				createQueryHitElement(hit, doc, node);
			} catch (Exception e) {
				StatusManager.log(e, e.getMessage());
			}
		}

		parent.appendChild(node);
		return node;
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
	public String getTaskTagName() {
		return KEY_JIRA_ISSUE;
	}

	@Override
	public Element createTaskElement(AbstractTask task, Document doc, Element parent) {
		Element node = super.createTaskElement(task, doc, parent);
		node.setAttribute(KEY_KEY, ((JiraTask) task).getTaskKey());
		return node;
	}

	@Override
	public AbstractTask createTask(String repositoryUrl, String taskId, String summary, Element element, TaskList taskList,
			AbstractTaskContainer category, AbstractTask parent) throws TaskExternalizationException {
		JiraTask task = new JiraTask(repositoryUrl, taskId, summary);

		if (element.hasAttribute(KEY_KEY)) {
			String key = element.getAttribute(KEY_KEY);
			task.setTaskKey(key);
		} else {
			// ignore if key not found
		}
		return task;
	}

//	@Override
//	public boolean canReadQueryHit(Node node) {
//		return node.getNodeName().equals(getQueryHitTagName());
//	}

//	@Override
//	public AbstractTask createQueryHit(String repositoryUrl, String taskId, String summary, Element element,
//			TaskList taskList, AbstractRepositoryQuery query) throws TaskExternalizationException {
//		String key = "";
//		if (element.hasAttribute(KEY_KEY)) {
//			key = element.getAttribute(KEY_KEY);
//		}
//
//		return new JiraQueryHit(taskList, summary, repositoryUrl, taskId, key);
//	}

	@Override
	public String getQueryTagNameForElement(AbstractRepositoryQuery query) {
		if (query instanceof JiraRepositoryQuery) {
			return KEY_JIRA_QUERY;
		} else if (query instanceof JiraCustomQuery) {
			return KEY_JIRA_CUSTOM;
		}
		return "";
	}

	@Override
	public String getCategoryTagName() {
		return KEY_JIRA_CATEGORY;
	}

//	@Override
//	public String getQueryHitTagName() {
//		return KEY_JIRA_QUERY_HIT;
//	}
}
