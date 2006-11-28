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

package org.eclipse.mylar.internal.jira.core;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylar.internal.jira.core.JiraTask.PriorityLevel;
import org.eclipse.mylar.internal.jira.core.ui.JiraUiPlugin;
import org.eclipse.mylar.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylar.tasks.core.AbstractRepositoryQuery;
import org.eclipse.mylar.tasks.core.AbstractRepositoryTask;
import org.eclipse.mylar.tasks.core.IAttachmentHandler;
import org.eclipse.mylar.tasks.core.ITask;
import org.eclipse.mylar.tasks.core.ITaskDataHandler;
import org.eclipse.mylar.tasks.core.QueryHitCollector;
import org.eclipse.mylar.tasks.core.RepositoryTaskAttribute;
import org.eclipse.mylar.tasks.core.RepositoryTaskData;
import org.eclipse.mylar.tasks.core.Task;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;
import org.tigris.jira.core.model.Component;
import org.tigris.jira.core.model.Issue;
import org.tigris.jira.core.model.IssueType;
import org.tigris.jira.core.model.Priority;
import org.tigris.jira.core.model.Version;
import org.tigris.jira.core.service.JiraServer;

/**
 * @author Mik Kersten
 */
public class JiraRepositoryConnector extends AbstractRepositoryConnector {

	private static final String DELIM_URL = "/browse/";

	private static final String VERSION_SUPPORT = "3.3.1 and higher";

	private JiraOfflineTaskHandler offlineHandler;
	
	/** Name initially given to new tasks. Public for testing */
	public static final String NEW_TASK_DESC = "New Task";

	public JiraRepositoryConnector() {
		offlineHandler = new JiraOfflineTaskHandler(this);
	}
	
	@Override
	public String getLabel() {
		return JiraUiPlugin.JIRA_CLIENT_LABEL;
	}

	@Override
	public String getRepositoryType() {
		return JiraUiPlugin.REPOSITORY_KIND;
	}

	@Override
	public IAttachmentHandler getAttachmentHandler() {
		// not implemented
		return null;
	}

	@Override
	public ITaskDataHandler getTaskDataHandler() {
		return offlineHandler;
	}

	@Override
	public boolean canCreateTaskFromKey(TaskRepository repository) {
		return true;
	}


	@Override
	public AbstractRepositoryTask createTaskFromExistingKey(TaskRepository repository, String key)
			throws CoreException {
		JiraServer server = JiraServerFacade.getDefault().getJiraServer(repository);
		if (server != null) {
			Issue issue = server.getIssue(key);
			if (issue != null) {
				String handleIdentifier = AbstractRepositoryTask.getHandle(repository.getUrl(), issue.getId());
				JiraTask task = createTask(issue, handleIdentifier);
				updateTaskDetails(repository.getUrl(), task, issue, true);
				if (task != null) {
					TasksUiPlugin.getTaskListManager().getTaskList().addTask(task);
					return task;
				}
			}
		}
		return null;
	}

	@Override
	public IStatus performQuery(AbstractRepositoryQuery repositoryQuery, TaskRepository repository,
			IProgressMonitor monitor, QueryHitCollector resultCollector) {
		final List<Issue> issues = new ArrayList<Issue>();
		JiraIssueCollector collector = new JiraIssueCollector(monitor, issues);

		// TODO: Get rid of JiraIssueCollector and pass IQueryHitCollector

		try {
			JiraServer jiraServer = JiraServerFacade.getDefault().getJiraServer(repository);
			// TODO: remove, added to re-open connection, bug 164543
			jiraServer.getServerInfo();
			
			if (repositoryQuery instanceof JiraRepositoryQuery) {
				jiraServer.search(((JiraRepositoryQuery) repositoryQuery).getNamedFilter(), collector);
			} else if (repositoryQuery instanceof JiraCustomQuery) {
				jiraServer.search(((JiraCustomQuery) repositoryQuery).getFilterDefinition(), collector);
			}
		} catch (Throwable t) {
			return new Status(IStatus.OK, TasksUiPlugin.PLUGIN_ID, IStatus.OK, "Could not log in to server: "
					+ repositoryQuery.getRepositoryUrl() + "\n\nCheck network connection.", t);
		}
		// TODO: work-around no other way of determining failure
		if (!collector.isDone()) {
			return new Status(IStatus.OK, TasksUiPlugin.PLUGIN_ID, IStatus.OK, "Could not log in to server: "
					+ repositoryQuery.getRepositoryUrl() + "\n\nCheck network connection.", new UnknownHostException());
		}
		for (Issue issue : issues) {
			String issueId = issue.getId();
			String handleIdentifier = AbstractRepositoryTask.getHandle(repository.getUrl(), issueId);
			ITask task = TasksUiPlugin.getTaskListManager().getTaskList().getTask(handleIdentifier);
//			if (!(task instanceof JiraTask)) {
//				task = createTask(issue, handleIdentifier);
//			}
			if (task instanceof JiraTask) {
				updateTaskDetails(repository.getUrl(), (JiraTask) task, issue, false);
			}
//			JiraQueryHit hit = new JiraQueryHit((JiraTask) task, repositoryQuery.getRepositoryUrl(), issueId);
			// TODO: set completion status
			JiraQueryHit hit = new JiraQueryHit(taskList, issue.getSummary(), repositoryQuery.getRepositoryUrl(), issueId, issue.getKey(), false);
			// XXX: HACK, need to map jira priority to tasklist priorities
			hit.setPriority(Task.PriorityLevel.P3.toString());
			try {
				resultCollector.accept(hit);
			} catch (CoreException e) {
				return new Status(IStatus.OK, TasksUiPlugin.PLUGIN_ID, IStatus.ERROR,
						"Error while retrieving results from: " + repositoryQuery.getRepositoryUrl(), e);
			}
		}
		return Status.OK_STATUS;
	}

	@Override
	public boolean canCreateNewTask(TaskRepository repository) {
		return true;
	}

	@Override
	public List<String> getSupportedVersions() {
		if (supportedVersions == null) {
			supportedVersions = new ArrayList<String>();
			supportedVersions.add(VERSION_SUPPORT);
		}
		return supportedVersions;
	}

	@Override
	public void updateTask(TaskRepository repository, AbstractRepositoryTask repositoryTask) {
//		final TaskRepository repository = TasksUiPlugin.getRepositoryManager().getRepository(
//				repositoryTask.getRepositoryKind(), repositoryTask.getRepositoryUrl());
//		if (repository != null && repositoryTask instanceof JiraTask) {
//			final JiraTask jiraTask = (JiraTask) repositoryTask;
//			final JiraServer server = JiraServerFacade.getDefault().getJiraServer(repository);
//			if (server != null) {
//				final Issue issue = server.getIssue(jiraTask.getKey());
//				if (issue != null) {
//					// TODO: may not need to update details here
//					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
//						public void run() {
//							updateTaskDetails(repository.getUrl(), jiraTask, issue, true);
//						}
//					});
//				}
//			}
//		}
	}

	@Override
	public String getRepositoryUrlFromTaskUrl(String url) {
		if (url == null) {
			return null;
		}
		int index = url.indexOf(DELIM_URL);
		if (index != -1) {
			return url.substring(0, index);
		}
		return null;
	}

	public static void updateTaskDetails(String repositoryUrl, JiraTask task, Issue issue, boolean notifyOfChange) {
		if (issue.getKey() != null) {
			String url = repositoryUrl + JiraUiPlugin.ISSUE_URL_PREFIX + issue.getKey();
			task.setUrl(url);
			if (issue.getDescription() != null) {
				task.setDescription(issue.getSummary());
				task.setKey(issue.getKey());
			}
		}
		if (issue.getStatus() != null && (issue.getStatus().isClosed() || issue.getStatus().isResolved())) {
			task.setCompleted(true);
			task.setCompletionDate(issue.getUpdated());
		} else {
			task.setCompleted(false);
			task.setCompletionDate(null);
		}

		if (issue.getPriority() != null) {
			task.setKind(issue.getType().getName());

			PriorityLevel priorityLevel = JiraTask.PriorityLevel.fromPriority(issue.getPriority());
			if (priorityLevel != null) {
				task.setPriority(priorityLevel.toString());
			} 
//			else {
//				MylarStatusHandler.log("unrecognized priority: " + issue.getPriority().getDescription(), null);
//			}
		}
		if (notifyOfChange) {
			TasksUiPlugin.getTaskListManager().getTaskList().notifyLocalInfoChanged(task);
		}
	}

	public static JiraTask createTask(String handleIdentifier, String key, String description) {
		JiraTask task;
		ITask existingTask = TasksUiPlugin.getTaskListManager().getTaskList().getTask(handleIdentifier);
		if (existingTask instanceof JiraTask) {
			task = (JiraTask) existingTask;
		} else {
			task = new JiraTask(handleIdentifier, description, true);
			task.setKey(key);//issue.getKey());
			TasksUiPlugin.getTaskListManager().getTaskList().addTask(task);
		}
		return task;
	}
	
	public static JiraTask createTask(Issue issue, String handleIdentifier) {
		JiraTask task;
		String summary = issue.getSummary();
		ITask existingTask = TasksUiPlugin.getTaskListManager().getTaskList().getTask(handleIdentifier);
		if (existingTask instanceof JiraTask) {
			task = (JiraTask) existingTask;
		} else {
			task = new JiraTask(handleIdentifier, summary, true);
			task.setKey(issue.getKey());
			TasksUiPlugin.getTaskListManager().getTaskList().addTask(task);
		}
		return task;
	}

	@Override
	public String toString() {
		return getLabel();
	}

	@Override
	public void updateAttributes(TaskRepository repository, IProgressMonitor monitor)
			throws CoreException {
		JiraServerFacade.getDefault().refreshServerSettings(repository);
	}

	@Override
	public String getTaskIdPrefix() {
		return "issue";
	}

	public static Issue buildJiraIssue(RepositoryTaskData taskData, JiraServer server) {
		Issue issue = new Issue();
		issue.setSummary(taskData.getAttributeValue(RepositoryTaskAttribute.SUMMARY));
		issue.setDescription(taskData.getAttributeValue(RepositoryTaskAttribute.DESCRIPTION));
		
		//issue.setEstimate(Long.parseLong(taskData.getAttributeValue(JiraAttributeFactory.ATTRIBUTE_ESTIMATE)));
		
		for (IssueType type: server.getIssueTypes()) {
			if(type.getName().equals(taskData.getAttributeValue(JiraAttributeFactory.ATTRIBUTE_TYPE))) {
				issue.setType(type);
				break;
			}
		}
		for (org.tigris.jira.core.model.Status status: server.getStatuses()) {
			if(status.getName().equals(taskData.getAttributeValue(RepositoryTaskAttribute.STATUS))) {
				issue.setStatus(status);
				break;
			}
		}
		ArrayList<Component> components = new ArrayList<Component>();
		RepositoryTaskAttribute attrib = taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_COMPONENTS);
		for (String compStr: taskData.getAttributeValues(JiraAttributeFactory.ATTRIBUTE_COMPONENTS)) {		
			Component comp = new Component();
			comp.setId(attrib.getOptionParameter(compStr));
			comp.setName(compStr);			
			components.add(comp);
		}
		issue.setComponents(components.toArray(new Component[components.size()]));
		
		ArrayList<Version> fixversions = new ArrayList<Version>();
		attrib = taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_FIXVERSIONS);	
		for (String fixStr: taskData.getAttributeValues(JiraAttributeFactory.ATTRIBUTE_FIXVERSIONS)) {	
			Version version = new Version();
			version.setId(attrib.getOptionParameter(fixStr));
			version.setName(fixStr);			
			fixversions.add(version);
		}
		issue.setFixVersions(fixversions.toArray(new Version[fixversions.size()]));
		
		ArrayList<Version> affectsversions = new ArrayList<Version>();
		attrib = taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_AFFECTSVERSIONS);	
		for (String fixStr: taskData.getAttributeValues(JiraAttributeFactory.ATTRIBUTE_AFFECTSVERSIONS)) {	
			Version version = new Version();
			version.setId(attrib.getOptionParameter(fixStr));
			version.setName(fixStr);			
			affectsversions.add(version);
		}
		issue.setReportedVersions(affectsversions.toArray(new Version[affectsversions.size()]));
		
		issue.setAssignee(taskData.getAttributeValue(RepositoryTaskAttribute.USER_ASSIGNED));
		issue.setEnvironment(taskData.getAttributeValue(JiraAttributeFactory.ATTRIBUTE_ENVIRONMENT));
		issue.setId(taskData.getId());
		issue.setKey(taskData.getAttributeValue(JiraAttributeFactory.ATTRIBUTE_ISSUE_KEY));
		for (Priority priority: server.getPriorities()) {
			if(priority.getName().equals(taskData.getAttributeValue(RepositoryTaskAttribute.PRIORITY))) {
				issue.setPriority(priority);
				break;
			}
		}			
		return issue;
	}
}