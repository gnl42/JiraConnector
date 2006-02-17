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
 * Reads and writes Jira task, query, and registry information to/from XML form.
 * This code was adapted from the BugzillaTaskExternalizer
 * 
 * @author Wesley Coelho (initial integration patch)
 * @author Mik Kersten
 */
public class JiraTaskExternalizer extends DelegatingTaskExternalizer {

	private static final String ISSUE_SUMMARY = "IssueSummary";

	private static final String ISSUE_KEY = "IssueKey";

	private static final String ISSUE_ID = "IssueId";

	private static final String JIRA = "Jira";

	private static final String TAG_JIRA_CATEGORY = "JiraQuery" + KEY_CATEGORY;

	private static final String TAG_JIRA_QUERY_HIT = JIRA + KEY_QUERY_HIT;

	private static final String TAG_JIRA_QUERY = JIRA + KEY_QUERY;

	private static final String TAG_TASK = "JiraIssue";

	private static final String JIRA_TASK_REGISTRY = "JiraTaskRegistry" + KEY_CATEGORY;

	private static final String FILTER_NAME = "FilterName";

	private static final String FILTER_ID = "FilterID";

	private static final String FILTER_DESCRIPTION = "FilterDesc";

	@Override
	public boolean canReadCategory(Node node) {
		return node.getNodeName().equals(getCategoryTagName());// || node.getNodeName().equals(JIRA_TASK_REGISTRY);
	}

	@Override
	public void readCategory(Node node, TaskList taskList) throws TaskExternalizationException {
		Element element = (Element) node;
		if (element.getNodeName().equals(JIRA_TASK_REGISTRY)) {
			readRegistry(node, taskList);
		}
	}

	public boolean canReadQuery(Node node) {
		return node.getNodeName().equals(TAG_JIRA_QUERY);
	}

	// TODO: Better understand archiving
	public void readRegistry(Node node, TaskList taskList) throws TaskExternalizationException {
		boolean hasCaughtException = false;
		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			try {
				Node child = list.item(i);
				ITask task = readTask(child, taskList, null, null);
				if (task instanceof JiraTask) {
					taskList.addTaskToArchive(task);
				}
			} catch (TaskExternalizationException e) {
				hasCaughtException = true;
			}
		}

		if (hasCaughtException)
			throw new TaskExternalizationException("Failed to restore all tasks");
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

	public void readQuery(Node node, TaskList tlist) throws TaskExternalizationException {
		boolean hasCaughtException = false;
		Element element = (Element) node;

		NamedFilter namedFilter = new NamedFilter();
		namedFilter.setId(element.getAttribute(FILTER_ID));
		namedFilter.setName(element.getAttribute(FILTER_NAME));
		namedFilter.setDescription(element.getAttribute(FILTER_DESCRIPTION));

		AbstractRepositoryQuery cat = new JiraFilter(namedFilter, false);

		tlist.internalAddQuery(cat);

		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node child = list.item(i);
			try {
				readQueryHit(child, tlist, cat);
			} catch (TaskExternalizationException e) {
				hasCaughtException = true;
			}
		}
		if (hasCaughtException)
			throw new TaskExternalizationException("Failed to load all tasks");
	}

	public Element createQueryElement(AbstractRepositoryQuery query, Document doc, Element parent) {
		String queryTagName = getQueryTagNameForElement(query);
		Element node = doc.createElement(queryTagName);

		NamedFilter filter = ((JiraFilter) query).getNamedFilter();
		node.setAttribute(KEY_NAME, query.getDescription());
		node.setAttribute(KEY_QUERY_MAX_HITS, query.getMaxHits() + "");
		node.setAttribute(KEY_QUERY_STRING, query.getQueryUrl());
		node.setAttribute(KEY_REPOSITORY_URL, query.getRepositoryUrl());

		node.setAttribute(FILTER_ID, filter.getId());
		node.setAttribute(FILTER_NAME, filter.getName());
		node.setAttribute(FILTER_DESCRIPTION, filter.getDescription());

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
	public ITask readTask(Node node, TaskList tlist, ITaskContainer category, ITask parent)
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
		readTaskInfo(task, tlist, element, category, parent);

		AbstractRepositoryClient client = MylarTaskListPlugin.getRepositoryManager().getRepositoryClient(
				MylarJiraPlugin.JIRA_REPOSITORY_KIND);
		if (client != null) {
			MylarTaskListPlugin.getTaskListManager().getTaskList().addTaskToArchive(task);
//			client.addTaskToArchive(task);
		} else {
			MylarStatusHandler.log("No Jira Client for Jira Task", this);
		}

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

		node.setAttribute(ISSUE_ID, issue.getId());
		node.setAttribute(ISSUE_KEY, issue.getKey());
		node.setAttribute(ISSUE_SUMMARY, issue.getSummary());

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
		if (element.hasAttribute(ISSUE_SUMMARY)) {
			issue.setSummary(element.getAttribute(ISSUE_SUMMARY));
		} else {
			throw new TaskExternalizationException("Summary not stored for bug report");
		}
		if (element.hasAttribute(ISSUE_KEY)) {
			issue.setKey(element.getAttribute(ISSUE_KEY));
		} else {
			throw new TaskExternalizationException("Key not stored for bug report");
		}
		if (element.hasAttribute(ISSUE_ID)) {
			issue.setId(element.getAttribute(ISSUE_ID));
		} else {
			throw new TaskExternalizationException("Id not stored for bug report");
		}

		JiraFilterHit hit = new JiraFilterHit(issue);
		hit.setHandleIdentifier(handle);
		query.addHit(hit);
	}

	public String getQueryTagNameForElement(AbstractRepositoryQuery query) {
		if (query instanceof JiraFilter) {
			return TAG_JIRA_QUERY;
		}
		return "";
	}

	@Override
	public String getCategoryTagName() {
		return TAG_JIRA_CATEGORY;
	}

	@Override
	public String getTaskTagName() {
		return TAG_TASK;
	}

	@Override
	public String getQueryHitTagName() {
		return TAG_JIRA_QUERY_HIT;
	}

	public AbstractRepositoryClient getRepositoryClient() {
		return MylarTaskListPlugin.getRepositoryManager().getRepositoryClient(MylarJiraPlugin.JIRA_REPOSITORY_KIND);
	}
}
