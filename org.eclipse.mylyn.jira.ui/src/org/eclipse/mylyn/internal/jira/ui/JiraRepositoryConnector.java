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
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylar.core.MylarStatusHandler;
import org.eclipse.mylar.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylar.internal.jira.core.model.Component;
import org.eclipse.mylar.internal.jira.core.model.Issue;
import org.eclipse.mylar.internal.jira.core.model.IssueType;
import org.eclipse.mylar.internal.jira.core.model.Priority;
import org.eclipse.mylar.internal.jira.core.model.Project;
import org.eclipse.mylar.internal.jira.core.model.Query;
import org.eclipse.mylar.internal.jira.core.model.Version;
import org.eclipse.mylar.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylar.internal.jira.core.model.filter.Order;
import org.eclipse.mylar.internal.jira.core.model.filter.RelativeDateRangeFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.RelativeDateRangeFilter.RangeType;
import org.eclipse.mylar.internal.jira.core.service.JiraException;
import org.eclipse.mylar.internal.jira.core.service.JiraClient;
import org.eclipse.mylar.tasks.core.AbstractAttributeFactory;
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
import org.eclipse.mylar.tasks.core.Task.PriorityLevel;
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
			IProgressMonitor monitor, QueryHitCollector resultCollector) {
		final List<Issue> issues = new ArrayList<Issue>();
		JiraIssueCollector collector = new JiraIssueCollector(monitor, issues, QueryHitCollector.MAX_HITS);

		// TODO: Get rid of JiraIssueCollector and pass IQueryHitCollector

		try {
			JiraClient client = JiraClientFacade.getDefault().getJiraClient(repository);
			Query filter;
			if (repositoryQuery instanceof JiraRepositoryQuery) {
				filter = ((JiraRepositoryQuery) repositoryQuery).getNamedFilter();
			} else if (repositoryQuery instanceof JiraCustomQuery) {
				if (!client.hasDetails()) {
					client.refreshDetails(monitor);
				}
				try {
					filter = ((JiraCustomQuery) repositoryQuery).getFilterDefinition(client, true);
				} catch (InvalidJiraQueryException e) {
					return new Status(IStatus.ERROR, TasksUiPlugin.PLUGIN_ID, 0,
							"The query parameters do not match the repository configuration, please check the query properties.", null);
				}
			} else {
				return new Status(IStatus.ERROR, TasksUiPlugin.PLUGIN_ID, 0,
						"Invalid query type: " + repositoryQuery.getClass(), null);				
			}
			client.search(filter, collector);
		} catch (JiraException e) {
			return JiraCorePlugin.toStatus(repository, e);
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
			hit.setCompleted(isCompleted(issue.getStatus()));
			hit.setPriority(getMylarPriority(issue.getPriority()).toString());
			resultCollector.accept(hit);
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
			lastSyncDate = new SimpleDateFormat(JiraAttributeFactory.JIRA_DATE_FORMAT, Locale.US).parse(dateString);
		} catch (ParseException e) {
			return tasks;
		}

		final List<Issue> issues = new ArrayList<Issue>();
		// if the maximum is unlimited this will can create crazy amounts of
		// traffic
		JiraIssueCollector collector = new JiraIssueCollector(new NullProgressMonitor(), issues, 500);
		JiraClient client = JiraClientFacade.getDefault().getJiraClient(repository);

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
			// jiraServer.getServerInfo();
			// Will get ALL issues that have changed since lastSyncDate
			client.search(changedFilter, collector);
		} catch (JiraException e) {
			throw new CoreException(JiraCorePlugin.toStatus(repository, e));
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

	@Override
	public boolean canCreateNewTask(TaskRepository repository) {
		return true;
	}

	@Override
	public void updateTaskFromRepository(TaskRepository repository, AbstractRepositoryTask repositoryTask) {
		// final TaskRepository repository =
		// TasksUiPlugin.getRepositoryManager().getRepository(
		// repositoryTask.getRepositoryKind(),
		// repositoryTask.getRepositoryUrl());
		// if (repository != null && repositoryTask instanceof JiraTask) {
		// final JiraTask jiraTask = (JiraTask) repositoryTask;
		// final JiraServer client =
		// JiraServerFacade.getDefault().getJiraServer(repository);
		// if (client != null) {
		// final Issue issue = client.getIssue(jiraTask.getKey());
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
		JiraClient client = JiraClientFacade.getDefault().getJiraClient(repository);

		// (?:(MNGECLIPSE-\d+?)|(SPR-\d+?))\D
		StringBuffer sb = new StringBuffer("(");
		String sep = "";
		for (Project project : client.getProjects()) {
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

	private static boolean isCompleted(org.eclipse.mylar.internal.jira.core.model.Status status) {
		return status != null && (status.isClosed() || status.isResolved());
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

	protected AbstractRepositoryTask makeTask(String repositoryUrl, String id, String summary) {
		return new JiraTask(repositoryUrl, id, summary, true);
	}

	public void updateTaskFromTaskData(TaskRepository repository, AbstractRepositoryTask repositoryTask, RepositoryTaskData taskData, boolean retrieveSubTasks) {
		if (repositoryTask instanceof JiraTask) {
			JiraTask jiraTask = (JiraTask) repositoryTask;			
			jiraTask.setSummary(taskData.getAttributeValue(RepositoryTaskAttribute.SUMMARY));
			jiraTask.setOwner(taskData.getAttributeValue(RepositoryTaskAttribute.USER_OWNER));			
			jiraTask.setTaskKey(taskData.getAttributeValue(JiraAttributeFactory.ATTRIBUTE_ISSUE_KEY));
			jiraTask.setTaskUrl(getTaskUrl(repository.getUrl(), repositoryTask.getTaskKey()));

			JiraClient client = JiraClientFacade.getDefault().getJiraClient(repository);
			jiraTask.setPriority(getMylarPriority(client, taskData.getAttributeValue(RepositoryTaskAttribute.PRIORITY)).toString());
			for (org.eclipse.mylar.internal.jira.core.model.Status status : client.getStatuses()) {
				if (status.getName().equals(taskData.getAttributeValue(RepositoryTaskAttribute.STATUS))) {
					if (isCompleted(status)) {
						AbstractAttributeFactory factory = getTaskDataHandler().getAttributeFactory(repository.getUrl(), repository.getKind(), Task.DEFAULT_TASK_KIND);
						String dateString = taskData.getAttributeValue(RepositoryTaskAttribute.DATE_MODIFIED);
						jiraTask.setCompletionDate(factory.getDateForAttributeType(RepositoryTaskAttribute.DATE_MODIFIED, dateString));
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

	public static Issue buildJiraIssue(RepositoryTaskData taskData, JiraClient client) {
		Issue issue = new Issue();
		issue.setSummary(taskData.getAttributeValue(RepositoryTaskAttribute.SUMMARY));
		issue.setDescription(taskData.getAttributeValue(RepositoryTaskAttribute.DESCRIPTION));
		for (org.eclipse.mylar.internal.jira.core.model.Project project : client.getProjects()) {
			if (project.getName().equals(taskData.getAttributeValue(RepositoryTaskAttribute.PRODUCT))) {
				issue.setProject(project);
				break;
			}
		}
		// issue.setEstimate(Long.parseLong(taskData.getAttributeValue(JiraAttributeFactory.ATTRIBUTE_ESTIMATE)));

		for (IssueType type : client.getIssueTypes()) {
			if (type.getName().equals(taskData.getAttributeValue(JiraAttributeFactory.ATTRIBUTE_TYPE))) {
				issue.setType(type);
				break;
			}
		}
		for (org.eclipse.mylar.internal.jira.core.model.Status status : client.getStatuses()) {
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
				MylarStatusHandler.fail(null, "Error setting component for JIRA issue. Component id is null: "
						+ compStr, false);
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
				MylarStatusHandler.fail(null,
						"Error setting fix version for JIRA issue. Version id is null: " + fixStr, false);
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
				MylarStatusHandler.fail(null, "Error setting affects version for JIRA issue. Version id is null: "
						+ fixStr, false);
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
		for (Priority priority : client.getPriorities()) {
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