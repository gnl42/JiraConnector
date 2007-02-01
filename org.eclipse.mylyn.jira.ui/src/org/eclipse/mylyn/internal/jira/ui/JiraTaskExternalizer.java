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

package org.eclipse.mylar.internal.jira.ui;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import org.eclipse.mylar.context.core.MylarStatusHandler;
import org.eclipse.mylar.internal.jira.core.model.NamedFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylar.internal.jira.core.service.JiraServer;
import org.eclipse.mylar.tasks.core.AbstractQueryHit;
import org.eclipse.mylar.tasks.core.AbstractRepositoryQuery;
import org.eclipse.mylar.tasks.core.AbstractRepositoryTask;
import org.eclipse.mylar.tasks.core.AbstractTaskContainer;
import org.eclipse.mylar.tasks.core.DelegatingTaskExternalizer;
import org.eclipse.mylar.tasks.core.ITask;
import org.eclipse.mylar.tasks.core.ITaskListExternalizer;
import org.eclipse.mylar.tasks.core.TaskExternalizationException;
import org.eclipse.mylar.tasks.core.TaskList;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Mik Kersten
 * @author Wesley Coelho (filter prototyping)
 */
public class JiraTaskExternalizer extends DelegatingTaskExternalizer {

	private static final String KEY_JIRA = "Jira";

	private static final String KEY_CUSTOM = "JiraCustom";

	private static final String KEY_JIRA_CATEGORY = "JiraQuery" + KEY_CATEGORY;

	private static final String KEY_JIRA_QUERY_HIT = KEY_JIRA + KEY_QUERY_HIT;

	private static final String KEY_JIRA_QUERY = KEY_JIRA + KEY_QUERY;

	private static final String KEY_JIRA_CUSTOM = KEY_JIRA + KEY_CUSTOM + KEY_QUERY;

	private static final String KEY_JIRA_ISSUE = "JiraIssue";

	private static final String KEY_FILTER_NAME = "FilterName";

	private static final String KEY_FILTER_ID = "FilterID";

	// private static final String KEY_FILTER_DESCRIPTION = "FilterDesc";

	private static final String KEY_FILTER_CUSTOM = "FilterCustom";

	private static final String KEY_FILTER_CUSTOM_URL = "FilterCustomUrl";

	private static final String KEY_KEY = "Key";

	@Override
	public boolean canReadQuery(Node node) {
		return node.getNodeName().equals(KEY_JIRA_QUERY) || node.getNodeName().equals(KEY_JIRA_CUSTOM);
	}

	@Override
	public boolean canCreateElementFor(AbstractQueryHit queryHit) {
		return queryHit instanceof JiraQueryHit;
	}

	@Override
	public Element createQueryHitElement(AbstractQueryHit queryHit, Document doc, Element parent) {
		Element node = super.createQueryHitElement(queryHit, doc, parent);
		node.setAttribute(KEY_KEY, ((JiraQueryHit) queryHit).getKey());
		return node;
	}

	@Override
	public boolean canCreateElementFor(AbstractRepositoryQuery category) {
		return category instanceof JiraRepositoryQuery || category instanceof JiraCustomQuery;
	}

	@Override
	public boolean canCreateElementFor(ITask task) {
		return task instanceof JiraTask;
	}

	@Override
	public AbstractRepositoryQuery readQuery(Node node, TaskList taskList) throws TaskExternalizationException {
		boolean hasCaughtException = false;
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

			query = new JiraCustomQuery(repositoryUrl, filter, TasksUiPlugin.getTaskListManager().getTaskList(),
					TasksUiPlugin.getRepositoryManager().getRepository(JiraUiPlugin.REPOSITORY_KIND, repositoryUrl));
		} else if (customUrl != null && customUrl.length() > 0) {
			TaskRepository repository = TasksUiPlugin.getRepositoryManager().getRepository(
					JiraUiPlugin.REPOSITORY_KIND, repositoryUrl);
			JiraServer jiraServer = JiraServerFacade.getDefault().getJiraServer(repository);
			query = new JiraCustomQuery(element.getAttribute(KEY_FILTER_ID), customUrl, repositoryUrl, jiraServer,
					TasksUiPlugin.getTaskListManager().getTaskList(), repository);

		} else {
			NamedFilter namedFilter = new NamedFilter();
			namedFilter.setId(element.getAttribute(KEY_FILTER_ID));
			namedFilter.setName(element.getAttribute(KEY_FILTER_NAME));
			// namedFilter.setDescription(element.getAttribute(KEY_FILTER_DESCRIPTION));

			query = new JiraRepositoryQuery(repositoryUrl, namedFilter, TasksUiPlugin.getTaskListManager()
					.getTaskList());
		}

		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node child = list.item(i);
			try {
				readQueryHit(child, taskList, query);
			} catch (TaskExternalizationException e) {
				hasCaughtException = true;
			}
		}
		if (hasCaughtException) {
			throw new TaskExternalizationException("Failed to load all hits");
		}
		return query;
	}

	@Override
	public Element createQueryElement(AbstractRepositoryQuery query, Document doc, Element parent) {
		String queryTagName = getQueryTagNameForElement(query);
		Element node = doc.createElement(queryTagName);

		node.setAttribute(KEY_NAME, query.getSummary());
		node.setAttribute(KEY_QUERY_MAX_HITS, query.getMaxHits() + "");
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
//			FilterDefinition filter = customQuery.getFilterDefinition();
			node.setAttribute(KEY_FILTER_ID, customQuery.getSummary());
			node.setAttribute(KEY_FILTER_NAME, customQuery.getSummary());
			// node.setAttribute(KEY_FILTER_DESCRIPTION,
			// filter.getDescription());
			node.setAttribute(KEY_FILTER_CUSTOM_URL, customQuery.getUrl());
		}

		for (AbstractQueryHit hit : query.getHits()) {
			try {
				Element element = null;
				for (ITaskListExternalizer externalizer : super.getDelegateExternalizers()) {
					if (externalizer.canCreateElementFor(hit)) {
						element = externalizer.createQueryHitElement(hit, doc, node);
					}
				}
				if (element == null) {
					createQueryHitElement(hit, doc, node);
				}
			} catch (Exception e) {
				MylarStatusHandler.log(e, e.getMessage());
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
	public Element createTaskElement(ITask task, Document doc, Element parent) {
		Element node = super.createTaskElement(task, doc, parent);
		node.setAttribute(KEY_KEY, ((JiraTask) task).getKey());
		return node;
	}

	@Override
	public ITask readTask(Node node, TaskList taskList, AbstractTaskContainer category, ITask parent)
			throws TaskExternalizationException {

		Element element = (Element) node;
		String handle;
		String label;
		String key;
		if (element.hasAttribute(KEY_HANDLE)) {
			handle = element.getAttribute(KEY_HANDLE);
		} else {
			throw new TaskExternalizationException("Handle not stored for bug report");
		}
		if (element.hasAttribute(KEY_LABEL)) {
			label = element.getAttribute(KEY_LABEL);
		} else {
			throw new TaskExternalizationException("Description not stored for bug report");
		}

		JiraTask task = new JiraTask(handle, label, false);
		if (element.hasAttribute(KEY_KEY)) {
			key = element.getAttribute(KEY_KEY);
			task.setKey(key);
		} else {
			// ignore if key not found
		}
		readTaskInfo(task, taskList, element, parent, category);
		return task;
	}

	@Override
	public boolean canReadQueryHit(Node node) {
		return node.getNodeName().equals(getQueryHitTagName());
	}

	@Override
	public void readQueryHit(Node node, TaskList taskList, AbstractRepositoryQuery query)
			throws TaskExternalizationException {
		Element element = (Element) node;
		// Issue issue = new Issue();

		String handle;
		if (element.hasAttribute(KEY_HANDLE)) {
			handle = element.getAttribute(KEY_HANDLE);
		} else {
			throw new TaskExternalizationException("Handle not stored for bug report");
		}

		String key = "";
		if (element.hasAttribute(KEY_KEY)) {
			key = element.getAttribute(KEY_KEY);
		}

		String issueId = AbstractRepositoryTask.getTaskId(handle);

		// TODO: implement completion
		JiraQueryHit hit = new JiraQueryHit(taskList, "<description>", query.getRepositoryUrl(), issueId, key, false);
		readQueryHitInfo(hit, taskList, query, element);
	}

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

	@Override
	public String getQueryHitTagName() {
		return KEY_JIRA_QUERY_HIT;
	}
}
