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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.core.MylarStatusHandler;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.model.Issue;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.Query;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.jira.core.model.filter.Order;
import org.eclipse.mylyn.internal.jira.core.model.filter.RelativeDateRangeFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.RelativeDateRangeFilter.RangeType;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector;
import org.eclipse.mylyn.tasks.core.AbstractAttributeFactory;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryQuery;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryTask;
import org.eclipse.mylyn.tasks.core.IAttachmentHandler;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskDataHandler;
import org.eclipse.mylyn.tasks.core.QueryHitCollector;
import org.eclipse.mylyn.tasks.core.RepositoryTaskAttribute;
import org.eclipse.mylyn.tasks.core.RepositoryTaskData;
import org.eclipse.mylyn.tasks.core.Task;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryTask.PriorityLevel;
import org.eclipse.mylyn.tasks.ui.TasksUiPlugin;

/**
 * @author Mik Kersten
 * @author Steffen Pingel
 */
public class JiraRepositoryConnector extends AbstractRepositoryConnector {

	/** Repository address + Issue Prefix + Issue key = the issue's web address */
	public final static String ISSUE_URL_PREFIX = "/browse/";

	/** Repository address + Filter Prefix + Issue key = the filter's web address */
	public final static String FILTER_URL_PREFIX = "/secure/IssueNavigator.jspa?mode=hide";

	private JiraTaskDataHandler offlineHandler;

	private JiraAttachmentHandler attachmentHandler;

	/** Name initially given to new tasks. Public for testing */
	public static final String NEW_TASK_DESC = "New Task";

	public static final String COMPRESSION_KEY = "compression";

	public static final int RETURN_ALL_HITS = -1;

	public JiraRepositoryConnector() {
		offlineHandler = new JiraTaskDataHandler(this);
		attachmentHandler = new JiraAttachmentHandler();
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
		return attachmentHandler;
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
	public IStatus performQuery(AbstractRepositoryQuery repositoryQuery, TaskRepository repository,
			IProgressMonitor monitor, QueryHitCollector resultCollector, boolean forced) {
		JiraClient client = JiraClientFacade.getDefault().getJiraClient(repository);

		Query filter;
		if (repositoryQuery instanceof JiraRepositoryQuery) {
			filter = ((JiraRepositoryQuery) repositoryQuery).getNamedFilter();
		} else if (repositoryQuery instanceof JiraCustomQuery) {
			try {
				if (!client.hasDetails()) {
					client.refreshDetails(monitor);
				}
				filter = ((JiraCustomQuery) repositoryQuery).getFilterDefinition(client, true);
			} catch (JiraException e) {
				return JiraCorePlugin.toStatus(repository, e);
			} catch (InvalidJiraQueryException e) {
				return new Status(IStatus.ERROR, TasksUiPlugin.PLUGIN_ID, 0,
						"The query parameters do not match the repository configuration, please check the query properties; "
								+ e.getMessage(), null);
			}
		} else {
			return new Status(IStatus.ERROR, TasksUiPlugin.PLUGIN_ID, 0, //
					"Invalid query type: " + repositoryQuery.getClass(), null);
		}

		List<Issue> issues = new ArrayList<Issue>();
		try {
			client.search(filter, new JiraIssueCollector(monitor, issues, QueryHitCollector.MAX_HITS));
		} catch (JiraException e) {
			return JiraCorePlugin.toStatus(repository, e);
		}

		try {
			int n = 0;
			for (Issue issue : issues) {
				if(monitor.isCanceled()) return Status.CANCEL_STATUS; 

				monitor.subTask(n++ + "/" + issues.size() +" " + issue.getKey() + " " + issue.getSummary());
				resultCollector.accept(offlineHandler.createTaskData(repository, client, issue));
			}
			return Status.OK_STATUS;
		} catch (JiraException e) {
			return JiraCorePlugin.toStatus(repository, e);
		} catch (CoreException e) {
			return e.getStatus();
		}
	}

	public Set<AbstractRepositoryTask> getChangedSinceLastSync(TaskRepository repository,
			Set<AbstractRepositoryTask> tasks, IProgressMonitor monitor) throws CoreException {

		String dateString = repository.getSyncTimeStamp();
		Date lastSyncDate = convertDate(dateString);
		if (lastSyncDate == null) {
			for (AbstractRepositoryTask task : tasks) {
				Date date = convertDate(task.getLastSyncDateStamp());
				if (lastSyncDate == null || (date != null && date.before(lastSyncDate))) {
					lastSyncDate = date;
				}
			}
		}
		if (lastSyncDate == null) {
			return tasks;
		}

		long mil = lastSyncDate.getTime();
		long now = Calendar.getInstance().getTimeInMillis();
		if (now - mil <= 0) {
			return Collections.emptySet();
		}
		long minutes = -1 * ((now - mil) / (1000 * 60));
		if (minutes == 0) {
			return Collections.emptySet();
		}

		FilterDefinition changedFilter = new FilterDefinition("Changed Tasks");
		changedFilter.setUpdatedDateFilter(new RelativeDateRangeFilter(RangeType.MINUTE, minutes));
		changedFilter.setOrdering(new Order[] { new Order(Order.Field.UPDATED, false) });

		final List<Issue> issues = new ArrayList<Issue>();
		// unlimited maxHits can create crazy amounts of traffic
		JiraIssueCollector collector = new JiraIssueCollector(new NullProgressMonitor(), issues, 500);
		JiraClient client = JiraClientFacade.getDefault().getJiraClient(repository);

		// TODO: Need some way to further scope this query

		try {
			// XXX: disabled work around
			// TODO: remove, added to re-open connection, bug 164543
			// jiraServer.getServerInfo();
			// Will get ALL issues that have changed since lastSyncDate
			client.search(changedFilter, collector);
		} catch (JiraException e) {
			throw new CoreException(JiraCorePlugin.toStatus(repository, e));
		}

		Set<AbstractRepositoryTask> changedTasks = new HashSet<AbstractRepositoryTask>();
		for (Issue issue : issues) {
			// String handle =
			// AbstractRepositoryTask.getHandle(repository.getUrl(),
			// issue.getId());
			ITask task = TasksUiPlugin.getTaskListManager().getTaskList().getTask(repository.getUrl(), issue.getId());
			if (task instanceof AbstractRepositoryTask) {
				changedTasks.add((AbstractRepositoryTask) task);
			}

			if (issue.getUpdated() != null && issue.getUpdated().after(lastSyncDate)) {
				lastSyncDate = issue.getUpdated();
			}
		}

		repository.setSyncTimeStamp( // 
				new SimpleDateFormat(JiraAttributeFactory.JIRA_DATE_FORMAT, Locale.US).format(lastSyncDate));

		return changedTasks;
	}

	private Date convertDate(String dateString) {
		if (dateString == null || dateString.length() == 0) {
			return null;
		}
		try {
			return new SimpleDateFormat(JiraAttributeFactory.JIRA_DATE_FORMAT, Locale.US).parse(dateString);
		} catch (ParseException e) {
			MylarStatusHandler.log(e, "Error while parsing date string " + dateString);
		}
		return null;
	}

	@Override
	public String getLastSyncTimestamp(TaskRepository repository, Set<AbstractRepositoryTask> changedTasks) {
		// XXX to late for JIRA to calcualate the timestamp: bug 176934
		return repository.getSyncTimeStamp();
	}

	@Override
	public boolean canCreateNewTask(TaskRepository repository) {
		return true;
	}

	@Override
	public void updateTaskFromRepository(TaskRepository repository, AbstractRepositoryTask repositoryTask,
			IProgressMonitor monitor) {
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
	public String getTaskWebUrl(String repositoryUrl, String taskId) {
		return repositoryUrl + ISSUE_URL_PREFIX + taskId;
	}

	@Override
	public String[] getTaskIdsFromComment(TaskRepository repository, String comment) {
		JiraClient client = JiraClientFacade.getDefault().getJiraClient(repository);
		Project[] projects = client.getProjects();
		if (projects != null && projects.length > 0) {
			// (?:(MNGECLIPSE-\d+?)|(SPR-\d+?))\D
			StringBuffer sb = new StringBuffer("(");
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

	public static void updateTaskDetails(String repositoryUrl, JiraTask task, Issue issue, boolean notifyOfChange) {
		if (issue.getKey() != null) {
			task.setTaskKey(issue.getKey());
			task.setTaskUrl(getTaskUrl(repositoryUrl, issue.getKey()));
			if (issue.getDescription() != null) {
				task.setSummary(issue.getSummary());
			}
		}
		if (isCompleted(issue.getStatus())) {
			task.setCompleted(true);
			task.setCompletionDate(issue.getUpdated());
		} else {
			task.setCompleted(false);
			task.setCompletionDate(null);
		}
		if (issue.getType() != null) {
			task.setKind(issue.getType().getName());
		}
		task.setPriority(getMylarPriority(issue.getPriority()).toString());

		if (notifyOfChange) {
			TasksUiPlugin.getTaskListManager().getTaskList().notifyLocalInfoChanged(task);
		}
	}

	public static String getTaskUrl(String repositoryUrl, String key) {
		return repositoryUrl + JiraRepositoryConnector.ISSUE_URL_PREFIX + key;
	}

	private static boolean isCompleted(org.eclipse.mylyn.internal.jira.core.model.Status status) {
		return status != null && (status.isClosed() || status.isResolved());
	}

	public AbstractRepositoryTask createTask(String repositoryUrl, String id, String summary) {
		JiraTask jiraTask = new JiraTask(repositoryUrl, id, summary);
		jiraTask.setCreationDate(new Date());
		return jiraTask;
	}

	public void updateTaskFromTaskData(TaskRepository repository, AbstractRepositoryTask repositoryTask,
			RepositoryTaskData taskData) {
		if (repositoryTask instanceof JiraTask) {
			JiraTask jiraTask = (JiraTask) repositoryTask;

			// subtasks
//			repositoryTask.dropSubTasks();
//			for (String subId : getSubTaskIds(taskData)) {
//				ITask subTask = taskList.getTask(repository.getUrl(), subId);
//				if (subTask == null && retrieveSubTasks) {
//					if (!subId.trim().equals(taskData.getId()) && !subId.equals("")) {
//						try {
//							subTask = createTaskFromExistingId(repository, subId, false, new NullProgressMonitor());
//						} catch (CoreException e) {
//							// ignore
//						}
//					}
//				}
//				if (subTask != null) {
//					repositoryTask.addSubTask(subTask);
//				}
//			}

			jiraTask.setSummary(taskData.getAttributeValue(RepositoryTaskAttribute.SUMMARY));
			jiraTask.setOwner(taskData.getAttributeValue(RepositoryTaskAttribute.USER_OWNER));
			jiraTask.setTaskKey(taskData.getAttributeValue(RepositoryTaskAttribute.TASK_KEY));
			jiraTask.setTaskUrl(getTaskUrl(repository.getUrl(), repositoryTask.getTaskKey()));

			JiraClient client = JiraClientFacade.getDefault().getJiraClient(repository);
			jiraTask.setPriority(getMylarPriority(client, taskData.getAttributeValue(RepositoryTaskAttribute.PRIORITY))
					.toString());
			for (org.eclipse.mylyn.internal.jira.core.model.Status status : client.getStatuses()) {
				if (status.getName().equals(taskData.getAttributeValue(RepositoryTaskAttribute.STATUS))) {
					if (isCompleted(status)) {
						AbstractAttributeFactory factory = getTaskDataHandler().getAttributeFactory(
								repository.getUrl(), repository.getKind(), AbstractRepositoryTask.DEFAULT_TASK_KIND);
						String dateString = taskData.getAttributeValue(RepositoryTaskAttribute.DATE_MODIFIED);
						jiraTask.setCompletionDate(factory.getDateForAttributeType(
								RepositoryTaskAttribute.DATE_MODIFIED, dateString));
						jiraTask.setCompleted(true);
					} else {
						jiraTask.setCompletionDate(null);
						jiraTask.setCompleted(false);
					}
					break;
				}
			}
		} else {
			MylarStatusHandler.log("Unable to update data for task of type " + repositoryTask.getClass().getName(),
					this);
		}
	}

	private static PriorityLevel getMylarPriority(JiraClient client, String jiraPriority) {
		if (jiraPriority != null) {
			for (Priority priority : client.getPriorities()) {
				if (jiraPriority.equals(priority.getName())) {
					return getMylarPriority(priority);
				}
			}
		}
		return PriorityLevel.getDefault();
	}

	public static PriorityLevel getMylarPriority(Priority jiraPriority) {
		if (jiraPriority != null) {
			String priorityId = jiraPriority.getId();
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
		}
		return PriorityLevel.getDefault();
	}

	@Override
	public String toString() {
		return getLabel();
	}

	@Override
	public void updateAttributes(TaskRepository repository, IProgressMonitor monitor) throws CoreException {
		try {
			JiraClient client = JiraClientFacade.getDefault().getJiraClient(repository);
			client.refreshDetails(monitor);
		} catch (JiraException e) {
			throw new CoreException(JiraCorePlugin.toStatus(repository, e));
		}
	}

	@Override
	public String getTaskIdPrefix() {
		return "issue";
	}

	public static String getAssigneeFromAttribute(String assignee) {
		return "".equals(assignee) ? JiraTask.UNASSIGNED_USER : assignee;
	}

}