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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringWriter;

import org.eclipse.mylar.internal.core.util.MylarStatusHandler;
import org.eclipse.mylar.internal.tasklist.TaskExternalizationException;
import org.eclipse.mylar.provisional.tasklist.AbstractQueryHit;
import org.eclipse.mylar.provisional.tasklist.AbstractRepositoryQuery;
import org.eclipse.mylar.provisional.tasklist.AbstractRepositoryTask;
import org.eclipse.mylar.provisional.tasklist.AbstractTaskContainer;
import org.eclipse.mylar.provisional.tasklist.DelegatingTaskExternalizer;
import org.eclipse.mylar.provisional.tasklist.ITask;
import org.eclipse.mylar.provisional.tasklist.ITaskListExternalizer;
import org.eclipse.mylar.provisional.tasklist.MylarTaskListPlugin;
import org.eclipse.mylar.provisional.tasklist.TaskList;
import org.tigris.jira.core.model.NamedFilter;
import org.tigris.jira.core.model.filter.FilterDefinition;
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

	private static final String KEY_FILTER_DESCRIPTION = "FilterDesc";

	private static final String KEY_FILTER_CUSTOM = "FilterCustom";

	private static final String KEY_KEY = "Key";
	
//	private static final String KEY_ISSUE_SUMMARY = "IssueSummary";

	public boolean canReadQuery(Node node) {
		return node.getNodeName().equals(KEY_JIRA_QUERY) || node.getNodeName().equals(KEY_JIRA_CUSTOM);
	}

	public boolean canCreateElementFor(AbstractRepositoryQuery category) {
		return category instanceof JiraRepositoryQuery || category instanceof JiraCustomQuery;
	}

	public boolean canCreateElementFor(ITask task) {
		return task instanceof JiraTask;
	}

	public AbstractRepositoryQuery readQuery(Node node, TaskList taskList) throws TaskExternalizationException {
		boolean hasCaughtException = false;
		Element element = (Element) node;

		AbstractRepositoryQuery query;
		String custom = element.getAttribute(KEY_FILTER_CUSTOM);
		if (custom != null && custom.length() > 0) {
			FilterDefinition filter = decodeFilter(custom);
			if (filter == null) {
				throw new TaskExternalizationException("Failed to restore custom query "
						+ element.getAttribute(KEY_FILTER_ID));
			}
			filter.setName(element.getAttribute(KEY_FILTER_ID));
			filter.setDescription(element.getAttribute(KEY_FILTER_DESCRIPTION));

			query = new JiraCustomQuery(element.getAttribute(KEY_REPOSITORY_URL), filter, MylarTaskListPlugin
					.getTaskListManager().getTaskList());
		} else {
			NamedFilter namedFilter = new NamedFilter();
			namedFilter.setId(element.getAttribute(KEY_FILTER_ID));
			namedFilter.setName(element.getAttribute(KEY_FILTER_NAME));
			namedFilter.setDescription(element.getAttribute(KEY_FILTER_DESCRIPTION));

			query = new JiraRepositoryQuery(element.getAttribute(KEY_REPOSITORY_URL), namedFilter, MylarTaskListPlugin
					.getTaskListManager().getTaskList());
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

	public Element createQueryElement(AbstractRepositoryQuery query, Document doc, Element parent) {
		String queryTagName = getQueryTagNameForElement(query);
		Element node = doc.createElement(queryTagName);

		node.setAttribute(KEY_NAME, query.getDescription());
		node.setAttribute(KEY_QUERY_MAX_HITS, query.getMaxHits() + "");
		node.setAttribute(KEY_QUERY_STRING, query.getQueryUrl());
		node.setAttribute(KEY_REPOSITORY_URL, query.getRepositoryUrl());

		if (query instanceof JiraRepositoryQuery) {
			NamedFilter filter = ((JiraRepositoryQuery) query).getNamedFilter();
			node.setAttribute(KEY_FILTER_ID, filter.getId());
			node.setAttribute(KEY_FILTER_NAME, filter.getName());
			node.setAttribute(KEY_FILTER_DESCRIPTION, filter.getDescription());
		} else {
			FilterDefinition filter = ((JiraCustomQuery) query).getFilterDefinition();
			node.setAttribute(KEY_FILTER_ID, filter.getName());
			node.setAttribute(KEY_FILTER_NAME, filter.getName());
			node.setAttribute(KEY_FILTER_DESCRIPTION, filter.getDescription());

			// XXX implement actual export
			node.setAttribute(KEY_FILTER_CUSTOM, encodeFilter(filter));
		}

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

	private String encodeFilter(FilterDefinition filter) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(bos);
			oos.writeObject(filter);
			oos.flush();
		} catch (IOException ex) {
			return null;
		}

		StringWriter sw = new StringWriter();
		byte[] bytes = bos.toByteArray();
		for (int i = 0; i < bytes.length; i++) {
			byte b = bytes[i];
			sw.append((char) ('A' + (b >> 4))).append((char) ('A' + (b & 0xf)));
		}
		return sw.toString();
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

	public boolean canReadQueryHit(Node node) {
		return node.getNodeName().equals(getQueryHitTagName());
	}

	public Element createQueryHitElement(AbstractQueryHit queryHit, Document doc, Element parent) {
		Element node = doc.createElement(getQueryHitTagName());

//		JiraQueryHit hit = (JiraQueryHit) queryHit;
//		Issue issue = hit.getIssue();

		node.setAttribute(KEY_HANDLE, queryHit.getHandleIdentifier());
		node.setAttribute(KEY_PRIORITY, queryHit.getPriority());

//		node.setAttribute(KEY_ISSUE_SUMMARY, issue.getSummary());
		parent.appendChild(node);
		return null;
	}

	public void readQueryHit(Node node, TaskList taskList, AbstractRepositoryQuery query)
			throws TaskExternalizationException {
		Element element = (Element) node;
//		Issue issue = new Issue();

		String handle;
		if (element.hasAttribute(KEY_HANDLE)) {
			handle = element.getAttribute(KEY_HANDLE);
		} else {
			throw new TaskExternalizationException("Handle not stored for bug report");
		}
//		if (element.hasAttribute(KEY_ISSUE_SUMMARY)) {
//			issue.setSummary(element.getAttribute(KEY_ISSUE_SUMMARY));
//		} else {
//			throw new TaskExternalizationException("Summary not stored for bug report");
//		}

		ITask correspondingTask = taskList.getTask(handle);
		if (correspondingTask instanceof JiraTask) {
			int issueId = new Integer(AbstractRepositoryTask.getTaskIdAsInt(handle));
			JiraQueryHit hit = new JiraQueryHit((JiraTask)correspondingTask, query.getRepositoryUrl(), issueId);
			hit.setHandleIdentifier(handle);
			query.addHit(hit);
		}
	}

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
