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

package org.eclipse.mylar.internal.jira;

import org.eclipse.mylar.internal.core.util.MylarStatusHandler;
import org.eclipse.mylar.internal.tasklist.AbstractQueryHit;
import org.eclipse.mylar.internal.tasklist.AbstractRepositoryClient;
import org.eclipse.mylar.internal.tasklist.AbstractRepositoryQuery;
import org.eclipse.mylar.internal.tasklist.DelegatingTaskExternalizer;
import org.eclipse.mylar.internal.tasklist.ITask;
import org.eclipse.mylar.internal.tasklist.ITaskContainer;
import org.eclipse.mylar.internal.tasklist.ITaskListExternalizer;
import org.eclipse.mylar.internal.tasklist.MylarTaskListPlugin;
import org.eclipse.mylar.internal.tasklist.TaskExternalizationException;
import org.eclipse.mylar.internal.tasklist.TaskList;
import org.tigris.jira.core.model.Issue;
import org.tigris.jira.core.model.NamedFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Mik Kersten
 * @author Wesley Coelho (filter prototyping)
 */
public class JiraTaskExternalizer extends DelegatingTaskExternalizer {

	private static final String KEY_ISSUE_SUMMARY = "IssueSummary";

	private static final String KEY_ISSUE_KEY = "IssueKey";

	private static final String KEY_ISSUE_ID = "IssueId";

	private static final String KEY_JIRA = "Jira";

	private static final String KEY_JIRA_CATEGORY = "JiraQuery" + KEY_CATEGORY;

	private static final String KEY_JIRA_QUERY_HIT = KEY_JIRA + KEY_QUERY_HIT;

	private static final String KEY_JIRA_QUERY = KEY_JIRA + KEY_QUERY;

	private static final String KEY_TASK = "JiraIssue";

	private static final String KEY_FILTER_NAME = "FilterName";

	private static final String KEY_FILTER_ID = "FilterID";

	private static final String KEY_FILTER_DESCRIPTION = "FilterDesc";

	public boolean canReadQuery(Node node) {
		return node.getNodeName().equals(KEY_JIRA_QUERY);
	}

	public boolean canCreateElementFor(ITaskContainer cat) {
		return false;
	}

	public boolean canCreateElementFor(AbstractRepositoryQuery category) {
		return category instanceof JiraFilter;
	}

	public boolean canCreateElementFor(ITask task) {
		return task instanceof JiraTask;
	}

	public AbstractRepositoryQuery readQuery(Node node, TaskList tlist) throws TaskExternalizationException {
		boolean hasCaughtException = false;
		Element element = (Element) node;

		NamedFilter namedFilter = new NamedFilter();
		namedFilter.setId(element.getAttribute(KEY_FILTER_ID));
		namedFilter.setName(element.getAttribute(KEY_FILTER_NAME));
//		namedFilter.setDescription(element.getAttribute(KEY_FILTER_DESCRIPTION));

		AbstractRepositoryQuery query = new JiraFilter(namedFilter, false);
		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node child = list.item(i);
			try {
				readQueryHit(child, tlist, query);
			} catch (TaskExternalizationException e) {
				hasCaughtException = true;
			}
		}
		if (hasCaughtException) {
			throw new TaskExternalizationException("Failed to load all hits");
		} else {
			return query;
		}
	}

	public Element createQueryElement(AbstractRepositoryQuery query, Document doc, Element parent) {
		String queryTagName = getQueryTagNameForElement(query);
		Element node = doc.createElement(queryTagName);

		NamedFilter filter = ((JiraFilter) query).getNamedFilter();
		node.setAttribute(KEY_NAME, query.getDescription());
		node.setAttribute(KEY_QUERY_MAX_HITS, query.getMaxHits() + "");
		node.setAttribute(KEY_QUERY_STRING, query.getQueryUrl());
		node.setAttribute(KEY_REPOSITORY_URL, query.getRepositoryUrl());

		node.setAttribute(KEY_FILTER_ID, filter.getId());
		node.setAttribute(KEY_FILTER_NAME, filter.getName());
		node.setAttribute(KEY_FILTER_DESCRIPTION, filter.getDescription());

		for (AbstractQueryHit hit : query.getHits()) {
			try {
				Element element = null;
				for (ITaskListExternalizer externalizer : super.getDelegateExternalizers()) {
					if (externalizer.canCreateElementFor(hit))
						element = externalizer.createQueryHitElement(hit, doc, node);
				}
				if (element == null)
					createQueryHitElement(hit, doc, node);
			} catch (Exception e) {
				MylarStatusHandler.log(e, e.getMessage());
			}
		}
		parent.appendChild(node);
		return node;
	}

	@Override
	public ITask readTask(Node node, TaskList taskList, ITaskContainer category, ITask parent)
			throws TaskExternalizationException {

		Element element = (Element) node;
		String handle;
		String label;
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
		readTaskInfo(task, taskList, element, parent);

		taskList.addTaskToArchive(task);
		return task;
	}

	public boolean canReadQueryHit(Node node) {
		return node.getNodeName().equals(getQueryHitTagName());
	}

	public Element createQueryHitElement(AbstractQueryHit queryHit, Document doc, Element parent) {
		Element node = doc.createElement(getQueryHitTagName());

		JiraFilterHit hit = (JiraFilterHit) queryHit;
		Issue issue = hit.getIssue();

		node.setAttribute(KEY_HANDLE, queryHit.getHandleIdentifier());
		node.setAttribute(KEY_PRIORITY, queryHit.getPriority());

		node.setAttribute(KEY_ISSUE_ID, issue.getId());
		node.setAttribute(KEY_ISSUE_KEY, issue.getKey());
		node.setAttribute(KEY_ISSUE_SUMMARY, issue.getSummary());

//		if (queryHit.isCompleted()) {
//			node.setAttribute(COMPLETE, TRUE);
//		} else {
//			node.setAttribute(COMPLETE, FALSE);
//		}
		parent.appendChild(node);
		return null;
	}

	public void readQueryHit(Node node, TaskList tlist, AbstractRepositoryQuery query)
			throws TaskExternalizationException {
		Element element = (Element) node;
		Issue issue = new Issue();

		String handle;
		if (element.hasAttribute(KEY_HANDLE)) {
			handle = element.getAttribute(KEY_HANDLE);

		} else {
			throw new TaskExternalizationException("Handle not stored for bug report");
		}
		if (element.hasAttribute(KEY_ISSUE_SUMMARY)) {
			issue.setSummary(element.getAttribute(KEY_ISSUE_SUMMARY));
		} else {
			throw new TaskExternalizationException("Summary not stored for bug report");
		}
		if (element.hasAttribute(KEY_ISSUE_KEY)) {
			issue.setKey(element.getAttribute(KEY_ISSUE_KEY));
		} else {
			throw new TaskExternalizationException("Key not stored for bug report");
		}
		if (element.hasAttribute(KEY_ISSUE_ID)) {
			issue.setId(element.getAttribute(KEY_ISSUE_ID));
		} else {
			throw new TaskExternalizationException("Id not stored for bug report");
		}

		JiraFilterHit hit = new JiraFilterHit(issue);
		hit.setHandleIdentifier(handle);
		query.addHit(hit);
	}

	public String getQueryTagNameForElement(AbstractRepositoryQuery query) {
		if (query instanceof JiraFilter) {
			return KEY_JIRA_QUERY;
		}
		return "";
	}

	@Override
	public String getCategoryTagName() {
		return KEY_JIRA_CATEGORY;
	}

	@Override
	public String getTaskTagName() {
		return KEY_TASK;
	}

	@Override
	public String getQueryHitTagName() {
		return KEY_JIRA_QUERY_HIT;
	}

	public AbstractRepositoryClient getRepositoryClient() {
		return MylarTaskListPlugin.getRepositoryManager().getRepositoryClient(MylarJiraPlugin.JIRA_REPOSITORY_KIND);
	}
}
