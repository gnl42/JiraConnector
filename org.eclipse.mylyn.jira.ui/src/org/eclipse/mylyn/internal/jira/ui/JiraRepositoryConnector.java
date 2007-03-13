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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylar.core.MylarStatusHandler;
import org.eclipse.mylar.internal.jira.core.model.Component;
import org.eclipse.mylar.internal.jira.core.model.Issue;
import org.eclipse.mylar.internal.jira.core.model.IssueType;
import org.eclipse.mylar.internal.jira.core.model.Priority;
import org.eclipse.mylar.internal.jira.core.model.Project;
import org.eclipse.mylar.internal.jira.core.model.Version;
import org.eclipse.mylar.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylar.internal.jira.core.model.filter.Order;
import org.eclipse.mylar.internal.jira.core.model.filter.RelativeDateRangeFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.RelativeDateRangeFilter.RangeType;
import org.eclipse.mylar.internal.jira.core.service.AuthenticationException;
import org.eclipse.mylar.internal.jira.core.service.InsufficientPermissionException;
import org.eclipse.mylar.internal.jira.core.service.JiraServer;
import org.eclipse.mylar.internal.jira.core.service.ServiceUnavailableException;
import org.eclipse.mylar.internal.jira.ui.JiraTask.PriorityLevel;
import org.eclipse.mylar.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylar.tasks.core.AbstractRepositoryQuery;
import org.eclipse.mylar.tasks.core.AbstractRepositoryTask;
import org.eclipse.mylar.tasks.core.IAttachmentHandler;
import org.eclipse.mylar.tasks.core.ITask;
import org.eclipse.mylar.tasks.core.ITaskDataHandler;
import org.eclipse.mylar.tasks.core.QueryHitCollector;
import org.eclipse.mylar.tasks.core.RepositoryOperation;
import org.eclipse.mylar.tasks.core.RepositoryTaskAttribute;
import org.eclipse.mylar.tasks.core.RepositoryTaskData;
import org.eclipse.mylar.tasks.core.Task;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;

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

	/** Name initially given to new tasks. Public for testing */
	public static final String NEW_TASK_DESC = "New Task";

	public static final String COMPRESSION_KEY = "compression";
	
	public JiraRepositoryConnector() {
		offlineHandler = new JiraTaskDataHandler(this);
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
	public AbstractRepositoryTask createTaskFromExistingKey(TaskRepository repository, String key) throws CoreException {

		ITask existingTask = taskList.getRepositoryTask(getTaskWebUrl(repository.getUrl(), key));
		if (existingTask instanceof JiraTask) {
			return (JiraTask) existingTask;
		}

		// existingTask = taskList.getTask(repository.getUrl(), key);
		// if (existingTask instanceof JiraTask) {
		// return (JiraTask) existingTask;
		// }

		RepositoryTaskData taskData = offlineHandler.getTaskData(repository, key);
		return createTask(repository.getUrl(), taskData);
	}

	@Override
	public IStatus performQuery(AbstractRepositoryQuery repositoryQuery, TaskRepository repository,
			IProgressMonitor monitor, QueryHitCollector resultCollector) {
		final List<Issue> issues = new ArrayList<Issue>();
		JiraIssueCollector collector = new JiraIssueCollector(monitor, issues, repositoryQuery.getMaxHits());

		// TODO: Get rid of JiraIssueCollector and pass IQueryHitCollector

		try {
			JiraServer jiraServer = JiraServerFacade.getDefault().getJiraServer(repository);

			if (repositoryQuery instanceof JiraRepositoryQuery) {
				jiraServer.search(((JiraRepositoryQuery) repositoryQuery).getNamedFilter(), collector);
			} else if (repositoryQuery instanceof JiraCustomQuery) {
				jiraServer.search(((JiraCustomQuery) repositoryQuery).getFilterDefinition(jiraServer), collector);
			}
		} catch (AuthenticationException ex) {
			return new Status(IStatus.ERROR, TasksUiPlugin.PLUGIN_ID, IStatus.ERROR,
					"Unable to login to the repository. Check credentials", ex);
		} catch (Throwable t) {
			// TODO need to refactor this to use better checked exceptions and
			// only log severe cases
			String msg = t.getMessage();
			if (msg == null) {
				msg = t.toString();
			}
			Status status = new Status(IStatus.ERROR, TasksUiPlugin.PLUGIN_ID, IStatus.ERROR,
					"Unable to retrieve query results from " + repositoryQuery.getRepositoryUrl() + "\n" + msg, t);
			MylarStatusHandler.log(status);
			return status;
		}
		// TODO: work-around no other way of determining failure
		Exception ex = collector.getException();
		if (ex != null) {
			String msg = ex.getMessage();
			if (msg == null) {
				msg = ex.toString();
			}
			return new Status(IStatus.ERROR, TasksUiPlugin.PLUGIN_ID, IStatus.ERROR,
					"Unable to retrieve query results from " + repositoryQuery.getRepositoryUrl() + "\n" + msg, ex);
		}
		for (Issue issue : issues) {
			String taskId = issue.getId();
			// String handleIdentifier =
			// AbstractRepositoryTask.getHandle(repository.getUrl(), taskId);
			ITask task = TasksUiPlugin.getTaskListManager().getTaskList().getTask(repository.getUrl(), taskId);
			// if (!(task instanceof JiraTask)) {
			// task = createTask(issue, handleIdentifier);
			// }
			if (task instanceof JiraTask) {
				updateTaskDetails(repository.getUrl(), (JiraTask) task, issue, false);
			}
			JiraQueryHit hit = new JiraQueryHit(taskList, issue.getSummary(), repositoryQuery.getRepositoryUrl(),
					taskId, issue.getKey());
			hit.setCompleted(isCompleted(issue));
			// XXX: HACK, need to map jira priority to tasklist priorities
			hit.setPriority(Task.PriorityLevel.P3.toString());
			try {
				resultCollector.accept(hit);
			} catch (CoreException e) {
				return new Status(IStatus.ERROR, TasksUiPlugin.PLUGIN_ID, IStatus.ERROR,
						"Error while retrieving results from: " + repositoryQuery.getRepositoryUrl(), e);
			}
		}
		return Status.OK_STATUS;
	}

	@SuppressWarnings("deprecation")
	public Set<AbstractRepositoryTask> getChangedSinceLastSync(TaskRepository repository,
			Set<AbstractRepositoryTask> tasks) throws CoreException {

		Set<AbstractRepositoryTask> changedTasks = new HashSet<AbstractRepositoryTask>();

		String dateString = repository.getSyncTimeStamp();
		if (dateString == null) {
			dateString = "";
		}

		Date lastSyncDate;
		try {
			lastSyncDate = new SimpleDateFormat(JiraAttributeFactory.JIRA_DATE_FORMAT).parse(dateString);
		} catch (ParseException e) {
			return tasks;
		}

		final List<Issue> issues = new ArrayList<Issue>();
		// if the maximum is unlimited this will can create crazy amounts of
		// traffic
		JiraIssueCollector collector = new JiraIssueCollector(new NullProgressMonitor(), issues, 500);
		JiraServer jiraServer = JiraServerFacade.getDefault().getJiraServer(repository);
		if (jiraServer == null) {
			return tasks;
		}

		long mil = lastSyncDate.getTime();
		long now = Calendar.getInstance().getTimeInMillis();
		if (now - mil <= 0) {
			// return empty set
			return changedTasks;
		}
		long minutes = -1 * ((now - mil) / (1000 * 60));
		if (minutes == 0)
			return changedTasks;

		FilterDefinition changedFilter = new FilterDefinition("Changed Tasks");
		changedFilter.setUpdatedDateFilter(new RelativeDateRangeFilter(RangeType.MINUTE, minutes));
		changedFilter.setOrdering(new Order[] { new Order(Order.Field.UPDATED, false) });

		// TODO: Need some way to further scope this query

		try {
			// XXX: disabled work around
			// TODO: remove, added to re-open connection, bug 164543
			//jiraServer.getServerInfo();
			// Will get ALL issues that have changed since lastSyncDate
			jiraServer.search(changedFilter, collector);
		} catch (AuthenticationException ex) {
			throw createCoreException(ex, "Authentication Error: " + jiraServer.getBaseURL());
		} catch (InsufficientPermissionException ex) {
			throw createCoreException(ex, "Insufficient Permissions: " + jiraServer.getBaseURL());
		} catch (ServiceUnavailableException ex) {
			throw createCoreException(ex, "Service Unavailable: " + jiraServer.getBaseURL());
		}

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

		repository.setSyncTimeStamp(lastSyncDate.toGMTString());
		
		return changedTasks;
	}
	
	@Override
	public String getLastSyncTimestamp(TaskRepository repository, Set<AbstractRepositoryTask> changedTasks) {
		// XXX to late for JIRA to calcualate the timestamp: bug 176934
		return repository.getSyncTimeStamp();
	}

	private CoreException createCoreException(Exception ex, String msg) {
		return new CoreException(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.Status.ERROR,
				TasksUiPlugin.PLUGIN_ID, IStatus.OK, msg, ex));
	}

	@Override
	public boolean canCreateNewTask(TaskRepository repository) {
		return true;
	}

	@Override
	public void updateTask(TaskRepository repository, AbstractRepositoryTask repositoryTask) {
		// final TaskRepository repository =
		// TasksUiPlugin.getRepositoryManager().getRepository(
		// repositoryTask.getRepositoryKind(),
		// repositoryTask.getRepositoryUrl());
		// if (repository != null && repositoryTask instanceof JiraTask) {
		// final JiraTask jiraTask = (JiraTask) repositoryTask;
		// final JiraServer server =
		// JiraServerFacade.getDefault().getJiraServer(repository);
		// if (server != null) {
		// final Issue issue = server.getIssue(jiraTask.getKey());
		// if (issue != null) {
		// // TODO: may not need to update details here
		// PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
		// public void run() {
		// updateTaskDetails(repository.getUrl(), jiraTask, issue, true);
		// }
		// });
		// }
		// }
		// }
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
		JiraServer server = JiraServerFacade.getDefault().getJiraServer(repository);
		if (server != null) {
			// (?:(MNGECLIPSE-\d+?)|(SPR-\d+?))\D
			StringBuffer sb = new StringBuffer("(");
			String sep = "";
			for (Project project : server.getProjects()) {
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
				task.setDescription(issue.getSummary());
			}
		}
		if (isCompleted(issue)) {
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
			// else {
			// MylarStatusHandler.log("unrecognized priority: " +
			// issue.getPriority().getDescription(), null);
			// }
		}
		if (notifyOfChange) {
			TasksUiPlugin.getTaskListManager().getTaskList().notifyLocalInfoChanged(task);
		}
	}

	public static String getTaskUrl(String repositoryUrl, String key) {
		return repositoryUrl + JiraRepositoryConnector.ISSUE_URL_PREFIX + key;
	}

	private static boolean isCompleted(Issue issue) {
		return issue.getStatus() != null && (issue.getStatus().isClosed() || issue.getStatus().isResolved());
	}

	public static JiraTask createTask(String repositoryUrl, String taskId, String key, String description) {
		JiraTask task;
		// String handle = AbstractRepositoryTask.getHandle(repositoryUrl,
		// taskId);
		ITask existingTask = TasksUiPlugin.getTaskListManager().getTaskList().getTask(repositoryUrl, taskId);
		if (existingTask instanceof JiraTask) {
			task = (JiraTask) existingTask;
		} else {
			task = new JiraTask(repositoryUrl, taskId, description, true);
			task.setTaskKey(key);// issue.getKey());
			task.setTaskUrl(getTaskUrl(repositoryUrl, key));
			TasksUiPlugin.getTaskListManager().getTaskList().addTask(task);
		}
		return task;
	}

	public static JiraTask createTask(Issue issue, String repositoryUrl, String taskId) {
		JiraTask task;
		String summary = issue.getSummary();
		// String handle = AbstractRepositoryTask.getHandle(repositoryUrl,
		// taskId);
		ITask existingTask = TasksUiPlugin.getTaskListManager().getTaskList().getTask(repositoryUrl, taskId);
		if (existingTask instanceof JiraTask) {
			task = (JiraTask) existingTask;
		} else {
			task = new JiraTask(repositoryUrl, taskId, summary, true);
			task.setTaskKey(issue.getKey());
			task.setTaskUrl(getTaskUrl(repositoryUrl, issue.getKey()));
			TasksUiPlugin.getTaskListManager().getTaskList().addTask(task);
		}
		return task;
	}

	private JiraTask createTask(String repositoryUrl, RepositoryTaskData taskData) {
		JiraTask task = new JiraTask(repositoryUrl, taskData.getId(), taskData.getSummary(), true);
		task.setTaskKey(taskData.getAttributeValue(JiraAttributeFactory.ATTRIBUTE_ISSUE_KEY));
		task.setTaskUrl(getTaskUrl(repositoryUrl, task.getTaskKey()));
		task.setTaskData(taskData);
		TasksUiPlugin.getTaskListManager().getTaskList().addTask(task);
		return task;
	}

	@Override
	public String toString() {
		return getLabel();
	}

	@Override
	public void updateAttributes(TaskRepository repository, IProgressMonitor monitor) throws CoreException {
		JiraServerFacade.getDefault().refreshServerSettings(repository, monitor);
	}

	@Override
	public String getTaskIdPrefix() {
		return "issue";
	}

	public static Issue buildJiraIssue(RepositoryTaskData taskData, JiraServer server) {
		Issue issue = new Issue();
		issue.setSummary(taskData.getAttributeValue(RepositoryTaskAttribute.SUMMARY));
		issue.setDescription(taskData.getAttributeValue(RepositoryTaskAttribute.DESCRIPTION));
		for (org.eclipse.mylar.internal.jira.core.model.Project project : server.getProjects()) {
			if (project.getName().equals(taskData.getAttributeValue(RepositoryTaskAttribute.PRODUCT))) {
				issue.setProject(project);
				break;
			}
		}
		// issue.setEstimate(Long.parseLong(taskData.getAttributeValue(JiraAttributeFactory.ATTRIBUTE_ESTIMATE)));

		for (IssueType type : server.getIssueTypes()) {
			if (type.getName().equals(taskData.getAttributeValue(JiraAttributeFactory.ATTRIBUTE_TYPE))) {
				issue.setType(type);
				break;
			}
		}
		for (org.eclipse.mylar.internal.jira.core.model.Status status : server.getStatuses()) {
			if (status.getName().equals(taskData.getAttributeValue(RepositoryTaskAttribute.STATUS))) {
				issue.setStatus(status);
				break;
			}
		}
		ArrayList<Component> components = new ArrayList<Component>();
		RepositoryTaskAttribute attrib = taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_COMPONENTS);
		for (String compStr : taskData.getAttributeValues(JiraAttributeFactory.ATTRIBUTE_COMPONENTS)) {
			if (attrib.getOptionParameter(compStr) != null) {
				Component comp = new Component();
				comp.setId(attrib.getOptionParameter(compStr));
				comp.setName(compStr);
				components.add(comp);
			} else {
				MylarStatusHandler.fail(null, "Error setting component for JIRA issue. Component id is null: " + compStr, false);
			}
		}
		issue.setComponents(components.toArray(new Component[components.size()]));

		ArrayList<Version> fixversions = new ArrayList<Version>();
		attrib = taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_FIXVERSIONS);
		for (String fixStr : taskData.getAttributeValues(JiraAttributeFactory.ATTRIBUTE_FIXVERSIONS)) {
			if (attrib.getOptionParameter(fixStr) != null) {
				Version version = new Version();
				version.setId(attrib.getOptionParameter(fixStr));
				version.setName(fixStr);
				fixversions.add(version);
			} else {
				MylarStatusHandler.fail(null, "Error setting fix version for JIRA issue. Version id is null: " + fixStr, false);
			}
		}
		issue.setFixVersions(fixversions.toArray(new Version[fixversions.size()]));

		ArrayList<Version> affectsversions = new ArrayList<Version>();
		attrib = taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_AFFECTSVERSIONS);
		for (String fixStr : taskData.getAttributeValues(JiraAttributeFactory.ATTRIBUTE_AFFECTSVERSIONS)) {
			if (attrib.getOptionParameter(fixStr) != null) {
				Version version = new Version();
				version.setId(attrib.getOptionParameter(fixStr));
				version.setName(fixStr);
				affectsversions.add(version);
			} else {
				MylarStatusHandler.fail(null, "Error setting affects version for JIRA issue. Version id is null: " + fixStr, false);
			}
		}
		issue.setReportedVersions(affectsversions.toArray(new Version[affectsversions.size()]));
		issue.setReporter(taskData.getAttributeValue(RepositoryTaskAttribute.USER_REPORTER));
		String assignee;
		RepositoryOperation operation = taskData.getSelectedOperation();
		if (operation != null && "reassign".equals(operation.getKnobName())) {
			assignee = operation.getInputValue();
		} else {
			assignee = taskData.getAttributeValue(RepositoryTaskAttribute.USER_ASSIGNED);
		}
		issue.setAssignee(getAssigneeFromAttribute(assignee));
		issue.setEnvironment(taskData.getAttributeValue(JiraAttributeFactory.ATTRIBUTE_ENVIRONMENT));
		issue.setId(taskData.getId());
		issue.setKey(taskData.getAttributeValue(JiraAttributeFactory.ATTRIBUTE_ISSUE_KEY));
		for (Priority priority : server.getPriorities()) {
			if (priority.getName().equals(taskData.getAttributeValue(RepositoryTaskAttribute.PRIORITY))) {
				issue.setPriority(priority);
				break;
			}
		}
		return issue;
	}

	public static String getAssigneeFromAttribute(String assignee) {
		return "".equals(assignee) ? JiraTask.UNASSIGNED_USER : assignee;
	}

}