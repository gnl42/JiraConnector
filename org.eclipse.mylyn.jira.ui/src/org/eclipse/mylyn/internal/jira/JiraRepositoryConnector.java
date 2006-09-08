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

import java.net.Proxy;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylar.context.core.MylarStatusHandler;
import org.eclipse.mylar.internal.jira.JiraTask.PriorityLevel;
import org.eclipse.mylar.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylar.tasks.core.AbstractRepositoryQuery;
import org.eclipse.mylar.tasks.core.AbstractRepositoryTask;
import org.eclipse.mylar.tasks.core.IAttachmentHandler;
import org.eclipse.mylar.tasks.core.IOfflineTaskHandler;
import org.eclipse.mylar.tasks.core.IQueryHitCollector;
import org.eclipse.mylar.tasks.core.ITask;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;
import org.tigris.jira.core.model.Issue;
import org.tigris.jira.core.service.JiraServer;

/**
 * @author Mik Kersten
 */
public class JiraRepositoryConnector extends AbstractRepositoryConnector {

	private static final String DELIM_URL = "/browse/";

	private static final String VERSION_SUPPORT = "3.3.1 and higher";

	/** Name initially given to new tasks. Public for testing */
	public static final String NEW_TASK_DESC = "New Task";

	public String getLabel() {
		return MylarJiraPlugin.JIRA_CLIENT_LABEL;
	}

	public String getRepositoryType() {
		return MylarJiraPlugin.REPOSITORY_KIND;
	}

	@Override
	public IAttachmentHandler getAttachmentHandler() {
		// not implemented
		return null;
	}

	@Override
	public IOfflineTaskHandler getOfflineTaskHandler() {
		// not implemented
		return null;
	}

	@Override
	public boolean canCreateTaskFromKey(TaskRepository repository) {
		return true;
	}

	public ITask createTaskFromExistingKey(TaskRepository repository, String key, Proxy proxySettings) throws CoreException {
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
			Proxy proxySettings, IProgressMonitor monitor, IQueryHitCollector resultCollector) {
		//List<AbstractQueryHit> hits = new ArrayList<AbstractQueryHit>();
		final List<Issue> issues = new ArrayList<Issue>();

//		TaskRepository repository = TasksUiPlugin.getRepositoryManager().getRepository(MylarJiraPlugin.REPOSITORY_KIND,
//				repositoryQuery.getRepositoryUrl());

		JiraIssueCollector collector = new JiraIssueCollector(monitor, issues);

		// TODO: Get rid of JiraIssueCollector and pass IQueryHitCollector
		
		try {
			JiraServer jiraServer = JiraServerFacade.getDefault().getJiraServer(repository);
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
							+ repositoryQuery.getRepositoryUrl() + "\n\nCheck network connection.",
							new UnknownHostException());			
		}
		for (Issue issue : issues) {
			String issueId = issue.getId();
			String handleIdentifier = AbstractRepositoryTask.getHandle(repository.getUrl(), issueId);
			ITask task = TasksUiPlugin.getTaskListManager().getTaskList().getTask(handleIdentifier);
			if (!(task instanceof JiraTask)) {
				task = createTask(issue, handleIdentifier);
			}
			updateTaskDetails(repository.getUrl(), (JiraTask) task, issue, false);

			JiraQueryHit hit = new JiraQueryHit((JiraTask) task, repositoryQuery.getRepositoryUrl(), issueId);
			try {
				resultCollector.accept(hit);
			} catch (CoreException e) {
				return new Status(IStatus.OK, TasksUiPlugin.PLUGIN_ID, IStatus.ERROR, "Error while retrieving results from: "
						+ repositoryQuery.getRepositoryUrl(), e );
			}
		}
		return Status.OK_STATUS;		
	}

	// @Override
	// public List<AbstractQueryHit> performQuery(AbstractRepositoryQuery
	// repositoryQuery, final IProgressMonitor monitor,
	// MultiStatus queryStatus) {
	// List<AbstractQueryHit> hits = new ArrayList<AbstractQueryHit>();
	// final List<Issue> issues = new ArrayList<Issue>();
	//
	// TaskRepository repository =
	// TasksUiPlugin.getRepositoryManager().getRepository(
	// MylarJiraPlugin.REPOSITORY_KIND, repositoryQuery.getRepositoryUrl());
	//
	// JiraIssueCollector collector = new JiraIssueCollector(monitor, issues);
	//
	// try {
	// JiraServer jiraServer =
	// JiraServerFacade.getDefault().getJiraServer(repository);
	// if (repositoryQuery instanceof JiraRepositoryQuery) {
	// jiraServer.search(((JiraRepositoryQuery)
	// repositoryQuery).getNamedFilter(), collector);
	// } else if (repositoryQuery instanceof JiraCustomQuery) {
	// jiraServer.search(((JiraCustomQuery)
	// repositoryQuery).getFilterDefinition(), collector);
	// }
	// } catch (Throwable t) {
	// queryStatus.add(new Status(IStatus.OK, TasksUiPlugin.PLUGIN_ID,
	// IStatus.OK,
	// "Could not log in to server: " + repositoryQuery.getRepositoryUrl()
	// + "\n\nCheck network connection.", t));
	// return hits;
	// }
	// // TODO: work-around no other way of determining failure
	// if (!collector.isDone()) {
	// queryStatus.add(new Status(IStatus.OK, TasksUiPlugin.PLUGIN_ID,
	// IStatus.OK,
	// "Could not log in to server: " + repositoryQuery.getRepositoryUrl()
	// + "\n\nCheck network connection.", new UnknownHostException()));
	// return hits;
	// }
	// for (Issue issue : issues) {
	// String issueId = issue.getId();
	// String handleIdentifier =
	// AbstractRepositoryTask.getHandle(repository.getUrl(), issueId);
	// ITask task =
	// TasksUiPlugin.getTaskListManager().getTaskList().getTask(handleIdentifier);
	// if (!(task instanceof JiraTask)) {
	// task = createTask(issue, handleIdentifier);
	// }
	// updateTaskDetails(repository.getUrl(), (JiraTask) task, issue, false);
	//
	// JiraQueryHit hit = new JiraQueryHit((JiraTask) task,
	// repositoryQuery.getRepositoryUrl(), issueId);
	// hits.add(hit);
	// }
	// queryStatus.add(Status.OK_STATUS);
	// return hits;
	// }

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
	public void updateTaskState(AbstractRepositoryTask repositoryTask) {
		TaskRepository repository = TasksUiPlugin.getRepositoryManager().getRepository(
				repositoryTask.getRepositoryKind(), repositoryTask.getRepositoryUrl());
		if (repository != null && repositoryTask instanceof JiraTask) {
			JiraTask jiraTask = (JiraTask) repositoryTask;
			JiraServer server = JiraServerFacade.getDefault().getJiraServer(repository);
			if (server != null) {
				Issue issue = server.getIssue(jiraTask.getKey());
				if (issue != null) {
					updateTaskDetails(repository.getUrl(), jiraTask, issue, true);
				}
			}
		}
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
			String url = repositoryUrl + MylarJiraPlugin.ISSUE_URL_PREFIX + issue.getKey();
			task.setUrl(url);
			if (issue.getDescription() != null) {
				task.setDescription(issue.getKey() + ": " + issue.getSummary());
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
			} else {
				MylarStatusHandler.log("unrecognized priority: " + issue.getPriority().getDescription(), null);
			}
		}
		if (notifyOfChange) {
			TasksUiPlugin.getTaskListManager().getTaskList().notifyLocalInfoChanged(task);
		}
	}

	public static JiraTask createTask(Issue issue, String handleIdentifier) {
		JiraTask task;
		String description = issue.getKey() + ": " + issue.getSummary();
		ITask existingTask = TasksUiPlugin.getTaskListManager().getTaskList().getTask(handleIdentifier);
		if (existingTask instanceof JiraTask) {
			task = (JiraTask) existingTask;
		} else {
			task = new JiraTask(handleIdentifier, description, true);
			task.setKey(issue.getKey());
			TasksUiPlugin.getTaskListManager().getTaskList().addTask(task);
		}
		return task;
	}

	// @Override
	// public Set<AbstractRepositoryTask> getChangedSinceLastSync(TaskRepository
	// repository,
	// Set<AbstractRepositoryTask> tasks) throws GeneralSecurityException,
	// IOException {
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
	// }

	public String toString() {
		return getLabel();
	}

	@Override
	public void updateAttributes(TaskRepository repository, Proxy proxySettings, IProgressMonitor monitor) throws CoreException {
		JiraServerFacade.getDefault().refreshServerSettings(repository);
	}
	
}
