/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.internal.jira.core.model.JiraFilter;
import org.eclipse.mylyn.internal.jira.core.model.JiraIssue;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.filter.DateRangeFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.jira.core.model.filter.Order;
import org.eclipse.mylyn.internal.jira.core.model.filter.RelativeDateRangeFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.RelativeDateRangeFilter.RangeType;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.jira.core.util.JiraUtil;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.ITask.PriorityLevel;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.eclipse.mylyn.tasks.core.data.TaskMapper;
import org.eclipse.mylyn.tasks.core.sync.ISynchronizationContext;

/**
 * @author Mik Kersten
 * @author Steffen Pingel
 * @author Eugene Kuleshov
 * @since 3.0
 */
public class JiraRepositoryConnector extends AbstractRepositoryConnector {

	private static final String ERROR_REPOSITORY_CONFIGURATION = "The repository returned an unknown project. Please update the repository attributes.";

	private static final int MAX_MARK_STALE_QUERY_HITS = 500;

	private static final boolean TRACE_ENABLED = Boolean.valueOf(Platform.getDebugOption("org.eclipse.mylyn.internal.jira.ui/connector"));

	/** Repository address + Issue Prefix + Issue key = the issue's web address */
	public final static String ISSUE_URL_PREFIX = "/browse/";

	/** Repository address + Filter Prefix + Issue key = the filter's web address */
	public final static String FILTER_URL_PREFIX = "/secure/IssueNavigator.jspa?mode=hide";

	private final JiraTaskDataHandler taskDataHandler2;

	private final JiraTaskAttachmentHandler attachmentHandler;

	public static final String UNASSIGNED_USER = "-1";

	/** Name initially given to new tasks. Public for testing */
	public static final String NEW_TASK_DESC = "New Task";

	public static final int RETURN_ALL_HITS = -1;

	public JiraRepositoryConnector() {
		taskDataHandler2 = new JiraTaskDataHandler(JiraClientFactory.getDefault());
		attachmentHandler = new JiraTaskAttachmentHandler();
	}

	@Override
	public String getLabel() {
		return JiraCorePlugin.LABEL;
	}

	@Override
	public String getConnectorKind() {
		return JiraCorePlugin.CONNECTOR_KIND;
	}

	@Override
	public boolean canCreateTaskFromKey(TaskRepository repository) {
		return true;
	}

	@Override
	public IStatus performQuery(TaskRepository repository, IRepositoryQuery repositoryQuery,
			TaskDataCollector resultCollector, ISynchronizationContext context, IProgressMonitor monitor) {
		monitor = Policy.monitorFor(monitor);
		try {
			monitor.beginTask("Query Repository", IProgressMonitor.UNKNOWN);
			JiraClient client = JiraClientFactory.getDefault().getJiraClient(repository);
			try {
				if (!client.getCache().hasDetails()) {
					client.getCache().refreshDetails(monitor);
				}
			} catch (JiraException e) {
				return JiraCorePlugin.toStatus(repository, e);
			}
			JiraFilter filter = JiraUtil.getQuery(repository, client, repositoryQuery, true);
			if (filter == null) {
				return RepositoryStatus.createStatus(repository, IStatus.ERROR, JiraCorePlugin.ID_PLUGIN,
						"The JIRA query is invalid");
			}
			try {
				List<JiraIssue> issues = new ArrayList<JiraIssue>();
				client.search(filter, new JiraIssueCollector(monitor, issues, TaskDataCollector.MAX_HITS), monitor);
				for (JiraIssue issue : issues) {
					if (monitor.isCanceled()) {
						return Status.CANCEL_STATUS;
					}
					if (issue.getProject() == null) {
						return new Status(IStatus.ERROR, JiraCorePlugin.ID_PLUGIN, 0, ERROR_REPOSITORY_CONFIGURATION,
								null);
					}
					monitor.subTask("Retrieving issue " + issue.getKey() + " " + issue.getSummary());
					TaskData oldTaskData = null;
					// TODO consider marking result as partial instead of loading task data from disk
					if (context != null) {
						try {
							oldTaskData = context.getTaskDataManager().getTaskData(repository, issue.getId());
						} catch (CoreException e) {
							// ignore
						}
					}
					TaskData taskData = taskDataHandler2.createTaskData(repository, client, issue, oldTaskData, monitor);
					resultCollector.accept(taskData);
				}
				return Status.OK_STATUS;
			} catch (JiraException e) {
				IStatus status = JiraCorePlugin.toStatus(repository, e);
				trace(status);
				return status;
			}
		} finally {
			monitor.done();
		}
	}

	@Override
	public void preSynchronization(ISynchronizationContext context, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			monitor.beginTask("Getting changed tasks", IProgressMonitor.UNKNOWN);

			context.setNeedsPerformQueries(true);

			if (!context.isFullSynchronization()) {
				return;
			}

			Date now = new Date();
			TaskRepository repository = context.getTaskRepository();
			FilterDefinition changedFilter = getSynchronizationFilter(repository, context.getTasks(), now);
			if (changedFilter == null) {
				// could not determine last time, rerun queries
				repository.setSynchronizationTimeStamp(JiraUtil.dateToString(now));
				return;
			}

			List<JiraIssue> issues = new ArrayList<JiraIssue>();
			JiraClient client = JiraClientFactory.getDefault().getJiraClient(repository);
			// unlimited maxHits can create crazy amounts of traffic
			JiraIssueCollector issueCollector = new JiraIssueCollector(new NullProgressMonitor(), issues,
					MAX_MARK_STALE_QUERY_HITS);
			try {
				client.search(changedFilter, issueCollector, monitor);

				if (issues.isEmpty()) {
					// repository is unchanged
					repository.setSynchronizationTimeStamp(JiraUtil.dateToString(now));
					context.setNeedsPerformQueries(false);
					return;
				}

				HashMap<String, ITask> taskById = new HashMap<String, ITask>();
				for (ITask task : context.getTasks()) {
					taskById.put(task.getTaskId(), task);
				}
				for (JiraIssue issue : issues) {
					ITask task = taskById.get(issue.getId());
					if (task != null) {
						if (issue.getProject() == null) {
							throw new CoreException(new Status(IStatus.ERROR, JiraCorePlugin.ID_PLUGIN, 0,
									ERROR_REPOSITORY_CONFIGURATION, null));
						}

						// for JIRA sufficient information to create task data is returned by the query so no need to mark tasks as stale
						monitor.subTask(issue.getKey() + " " + issue.getSummary());
						// only load old task data from if necessary 
						if (hasChanged(task, issue)) {
							TaskData oldTaskData = null;
							try {
								oldTaskData = context.getTaskDataManager().getTaskData(repository, issue.getId());
							} catch (CoreException e) {
								// ignore
							}
							TaskData taskData = taskDataHandler2.createTaskData(repository, client, issue, oldTaskData,
									monitor);
							context.getTaskDataManager().putUpdatedTaskData(task, taskData, false);
						}
					}
				}

				repository.setSynchronizationTimeStamp(JiraUtil.dateToString(now));

				Date lastUpdate = issues.get(0).getUpdated();
				Date repositoryUpdateTimeStamp = JiraUtil.getLastUpdate(repository);
				if (repositoryUpdateTimeStamp != null && repositoryUpdateTimeStamp.equals(lastUpdate)) {
					// didn't see any new changes
					context.setNeedsPerformQueries(false);
				} else {
					// updates may have caused tasks to match/not match a query therefore we need to rerun all queries  			
					if (lastUpdate != null) {
						JiraUtil.setLastUpdate(repository, lastUpdate);
					}
				}
			} catch (JiraException e) {
				IStatus status = JiraCorePlugin.toStatus(repository, e);
				trace(status);
				throw new CoreException(status);
			}
		} finally {
			monitor.done();
		}
	}

	@Override
	public void postSynchronization(ISynchronizationContext event, IProgressMonitor monitor) throws CoreException {
		// ignore
	}

	/* Public for testing. */
	public FilterDefinition getSynchronizationFilter(TaskRepository repository, Set<ITask> tasks, Date now) {
		// there are no JIRA tasks in the task list, skip contacting the repository
		if (tasks.isEmpty()) {
			return null;
		}

		Date lastSyncDate = JiraUtil.stringToDate(repository.getSynchronizationTimeStamp());

		// repository was never synchronized, update all tasks
		if (lastSyncDate == null) {
			for (ITask task : tasks) {
				task.setStale(true);
			}
			return null;
		}

		// use local time to determine time difference to last sync  
		long nowTime = now.getTime();
		long lastSyncTime = lastSyncDate.getTime();

		// check if time stamp is skewed
		if (lastSyncTime >= nowTime) {
			trace(new Status(IStatus.WARNING, JiraCorePlugin.ID_PLUGIN, 0,
					"Synchronization time stamp clock skew detected for " + repository.getRepositoryUrl() + ": "
							+ lastSyncTime + " >= " + now, null));

			// use the timestamp on the task that was modified last
			lastSyncDate = null;
			for (ITask task : tasks) {
				Date date = task.getModificationDate();
				if (lastSyncDate == null || (date != null && date.after(lastSyncDate))) {
					lastSyncDate = date;
				}
			}

			if (lastSyncDate == null) {
				// could not determine last synchronization point
				return null;
			}

			// get all tasks that were changed after the last known task modification
			FilterDefinition changedFilter = new FilterDefinition();
			changedFilter.setUpdatedDateFilter(new DateRangeFilter(lastSyncDate, null));
			// make sure it's sorted so the most recent changes are returned in case the query maximum is hit
			changedFilter.setOrdering(new Order[] { new Order(Order.Field.UPDATED, false) });
			return changedFilter;
		}

		FilterDefinition changedFilter = new FilterDefinition();
		// need to use RelativeDateRangeFilter since the granularity of DateRangeFilter is days
		// whereas this allows us to use minutes 
		long minutes = (now.getTime() - lastSyncDate.getTime()) / (60 * 1000) + 1;
		changedFilter.setUpdatedDateFilter(new RelativeDateRangeFilter(RangeType.MINUTE, -minutes));
		// make sure it's sorted so the most recent changes are returned in case the query maximum is hit
		changedFilter.setOrdering(new Order[] { new Order(Order.Field.UPDATED, false) });
		return changedFilter;
	}

	@Override
	public boolean canCreateNewTask(TaskRepository repository) {
		return true;
	}

	@Override
	public String getRepositoryUrlFromTaskUrl(String url) {
		if (url == null) {
			return null;
		}
		int index = url.indexOf(ISSUE_URL_PREFIX);
		return index == -1 ? null : url.substring(0, index);
	}

	@Override
	public String getTaskIdFromTaskUrl(String url) {
		if (url == null) {
			return null;
		}
		int index = url.indexOf(ISSUE_URL_PREFIX);
		return index == -1 ? null : url.substring(index + ISSUE_URL_PREFIX.length());
	}

	@Override
	public String getTaskUrl(String repositoryUrl, String taskId) {
		return repositoryUrl + ISSUE_URL_PREFIX + taskId;
	}

	@Override
	public String[] getTaskIdsFromComment(TaskRepository repository, String comment) {
		JiraClient client = JiraClientFactory.getDefault().getJiraClient(repository);
		Project[] projects = client.getCache().getProjects();
		if (projects != null && projects.length > 0) {
			// (?:(MNGECLIPSE-\d+?)|(SPR-\d+?))\D
			StringBuilder sb = new StringBuilder("(");
			String sep = "";
			for (Project project : projects) {
				sb.append(sep).append("(?:" + project.getKey() + "\\-\\d+?)");
				sep = "|";
			}
			sb.append(")(?:\\D|\\z)");

			Pattern p = Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
			Matcher m = p.matcher(comment);
			if (m.find()) {
				HashSet<String> ids = new HashSet<String>();
				do {
					ids.add(m.group(1));
				} while (m.find());
				return ids.toArray(new String[ids.size()]);
			}
		}

		return super.getTaskIdsFromComment(repository, comment);
	}

	public static String getTaskUrlFromKey(String repositoryUrl, String key) {
		return repositoryUrl + JiraRepositoryConnector.ISSUE_URL_PREFIX + key;
	}

	public static boolean isCompleted(TaskData taskData) {
		TaskAttribute attribute = taskData.getRoot().getAttribute(JiraAttribute.RESOLUTION.id());
		return attribute != null && attribute.getValue().length() > 0;
	}

	public static boolean isCompleted(JiraIssue issue) {
		return issue.getResolution() != null;
	}

	public static boolean isClosed(JiraIssue issue) {
		// TODO find a more robust way to determine if a status is closed
		return issue.getStatus() != null && "6".equals(issue.getStatus().getId());
	}

	private static PriorityLevel getPriorityLevel(String priorityId) {
		if (Priority.BLOCKER_ID.equals(priorityId)) {
			return PriorityLevel.P1;
		} else if (Priority.CRITICAL_ID.equals(priorityId)) {
			return PriorityLevel.P2;
		} else if (Priority.MAJOR_ID.equals(priorityId)) {
			return PriorityLevel.P3;
		} else if (Priority.MINOR_ID.equals(priorityId)) {
			return PriorityLevel.P4;
		} else if (Priority.TRIVIAL_ID.equals(priorityId)) {
			return PriorityLevel.P5;
		}
		return PriorityLevel.getDefault();
	}

	@Override
	public String toString() {
		return getLabel();
	}

	@Override
	public void updateRepositoryConfiguration(TaskRepository repository, IProgressMonitor monitor) throws CoreException {
		try {
			JiraClient client = JiraClientFactory.getDefault().getJiraClient(repository);
			client.getCache().refreshDetails(monitor);
		} catch (JiraException e) {
			IStatus status = JiraCorePlugin.toStatus(repository, e);
			trace(status);
			throw new CoreException(status);
		}
	}

	@Override
	public String getTaskIdPrefix() {
		return "issue";
	}

	public static String getAssigneeFromAttribute(String assignee) {
		return "".equals(assignee) ? UNASSIGNED_USER : assignee;
	}

	private void trace(IStatus status) {
		if (TRACE_ENABLED) {
			JiraCorePlugin.getDefault().getLog().log(status);
		}
	}

	@Override
	public boolean isRepositoryConfigurationStale(TaskRepository repository, IProgressMonitor monitor)
			throws CoreException {
		return JiraUtil.getAutoRefreshConfiguration(repository);
	}

	@Override
	public AbstractTaskDataHandler getTaskDataHandler() {
		return taskDataHandler2;
	}

	@Override
	public boolean hasChanged(TaskRepository taskRepository, ITask task, TaskData taskData) {
		TaskMapper scheme = new TaskMapper(taskData);
		Date repositoryDate = scheme.getModificationDate();
		Date localDate = task.getModificationDate();
		if (repositoryDate != null && repositoryDate.equals(localDate)) {
			return false;
		}
		return true;
	}

	private boolean hasChanged(ITask task, JiraIssue issue) {
		Date repositoryDate = issue.getUpdated();
		Date localDate = task.getModificationDate();
		if (repositoryDate != null && repositoryDate.equals(localDate)) {
			return false;
		}
		return true;
	}

	@Override
	public TaskData getTaskData(TaskRepository taskRepository, String taskId, IProgressMonitor monitor)
			throws CoreException {
		return taskDataHandler2.getTaskData(taskRepository, taskId, monitor);
	}

	@Override
	public void updateTaskFromTaskData(TaskRepository repository, ITask task, TaskData taskData) {
		TaskMapper scheme = getTaskMapper(taskData);
		scheme.applyTo(task);
	}

	@Override
	public JiraTaskAttachmentHandler getTaskAttachmentHandler() {
		return attachmentHandler;
	}

	public TaskMapper getTaskMapper(TaskData taskData) {
		return new TaskMapper(taskData) {
			@Override
			public PriorityLevel getPriority() {
				TaskAttribute attribute = getTaskData().getRoot().getAttribute(JiraAttribute.PRIORITY.id());
				if (attribute != null) {
					return getPriorityLevel(attribute.getValue());
				}
				return PriorityLevel.getDefault();
			}

			@Override
			public void setPriority(PriorityLevel priority) {
				// TODO implement
			}

			@Override
			public Date getCompletionDate() {
				if (isCompleted(getTaskData())) {
					return getModificationDate();
				} else {
					return null;
				}
			}

			@Override
			public void setCompletionDate(Date dateCompleted) {
				// ignore
			}
		};
	}

	@Override
	public ITaskMapping getTaskMapping(TaskData taskData) {
		return getTaskMapper(taskData);
	}

}