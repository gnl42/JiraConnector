/*******************************************************************************
 * Copyright (c) 2004 - 2006 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylar.internal.jira;

import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylar.tasks.core.AbstractAttributeFactory;
import org.eclipse.mylar.tasks.core.AbstractRepositoryTask;
import org.eclipse.mylar.tasks.core.IOfflineTaskHandler;
import org.eclipse.mylar.tasks.core.ITask;
import org.eclipse.mylar.tasks.core.RepositoryTaskAttribute;
import org.eclipse.mylar.tasks.core.RepositoryTaskData;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;
import org.tigris.jira.core.model.Issue;
import org.tigris.jira.core.service.JiraServer;

/**
 * @author Mik Kersten
 */
public class JiraOfflineTaskHandler implements IOfflineTaskHandler {

	private AbstractAttributeFactory attributeFactory = new JiraAttributeFactory();

	private JiraRepositoryConnector connector;

	public JiraOfflineTaskHandler(JiraRepositoryConnector connector) {
		this.connector = connector;
	}

	@SuppressWarnings("deprecation")
	public RepositoryTaskData downloadTaskData(TaskRepository repository, String taskId, Proxy proxySettings)
			throws CoreException {
		JiraServer server = JiraServerFacade.getDefault().getJiraServer(repository);
		String handle = AbstractRepositoryTask.getHandle(repository.getUrl(), taskId);

		ITask task = TasksUiPlugin.getTaskListManager().getTaskList().getTask(handle);
		if (task instanceof JiraTask) {
			JiraTask jiraTask = (JiraTask) task;
			Issue jiraIssue = server.getIssue(jiraTask.getKey());
			
			RepositoryTaskData data = new RepositoryTaskData(attributeFactory, MylarJiraPlugin.REPOSITORY_KIND,
					repository.getUrl(), taskId);
			connector.updateAttributes(repository, new NullProgressMonitor());
			
			data.setAttributeValue(RepositoryTaskAttribute.DATE_CREATION, jiraIssue.getCreated().toGMTString());
			data.setAttributeValue(RepositoryTaskAttribute.SUMMARY, convertHtml(jiraIssue.getSummary()));
			data.setAttributeValue(RepositoryTaskAttribute.DESCRIPTION, convertHtml(jiraIssue.getDescription()));
			data.setAttributeValue(RepositoryTaskAttribute.STATUS, jiraIssue.getStatus().getName());			
			data.setAttributeValue(RepositoryTaskAttribute.PRIORITY, jiraIssue.getPriority().getName());			
			
			return data;
		} else {
			return null;
		}
	}

	public Date getDateForAttributeType(String attributeKey, String dateString) {
		return null;
	}
	
	private String convertHtml(String text) { 
		// TODO: hack, format properly
		return text.replace("<br/>", "").replace("&nbsp;", "").replace("\n\n", "\n");
	}

	public AbstractAttributeFactory getAttributeFactory() {
		return attributeFactory;
	}

	public Set<AbstractRepositoryTask> getChangedSinceLastSync(TaskRepository repository,
			Set<AbstractRepositoryTask> tasks, Proxy proxySettings) throws CoreException, UnsupportedEncodingException {
		return Collections.emptySet();
		// JiraServer server =
		// JiraServerFacade.getDefault().getJiraServer(repository);
		// if (server == null) {
		// return Collections.emptySet();
		// } else {
		// List<AbstractRepositoryTask> changedTasks = new
		// ArrayList<AbstractRepositoryTask>();
		// for (AbstractRepositoryTask task : tasks) {
		// if (task instanceof JiraTask) {
		// Date lastCommentDate = null;
		// JiraTask jiraTask = (JiraTask) task;
		// Issue issue = server.getIssue(jiraTask.getKey());
		// if (issue != null) {
		// Comment[] comments = issue.getComments();
		// if (comments != null && comments.length > 0) {
		// lastCommentDate = comments[comments.length - 1].getCreated();
		// }
		// }
		// if (lastCommentDate != null && task.getLastSynchronized() != null) {
		// if (lastCommentDate.after(task.getLastSynchronized())) {
		// changedTasks.add(task);
		// }
		// }
		// }
		// }
		// }
		// return Collections.emptySet();
	}
}
