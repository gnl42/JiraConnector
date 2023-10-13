/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Eugene Kuleshov - improvements
 *     Pawel Niewiadomski - fixes for bug 290490
 *******************************************************************************/

package me.glindholm.connector.eclipse.internal.jira.core;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITask.PriorityLevel;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.eclipse.mylyn.tasks.core.data.TaskMapper;
import org.eclipse.mylyn.tasks.core.data.TaskRelation;
import org.eclipse.mylyn.tasks.core.sync.ISynchronizationSession;
import org.eclipse.osgi.util.NLS;

import me.glindholm.connector.eclipse.internal.jira.core.model.JiraFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssue;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraPriority;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraProject;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.DateRangeFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.FilterDefinition;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.IssueCollector;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.JiraFields;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.Order;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.RelativeDateRangeFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.RelativeDateRangeFilter.RangeType;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraClient;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraException;
import me.glindholm.connector.eclipse.internal.jira.core.util.JiraUtil;

/**
 * @author Mik Kersten
 * @author Steffen Pingel
 * @author Eugene Kuleshov
 * @author Jacek Jaroczynski
 * @since 3.0
 */
public class JiraRepositoryConnector extends AbstractRepositoryConnector {

    private static final String ID_STATUS_RESOLVED = "5"; //$NON-NLS-1$

    private static final String ID_STATUS_CLOSED = "6"; //$NON-NLS-1$

    private static final String ERROR_REPOSITORY_CONFIGURATION = Messages.JiraRepositoryConnector_The_repository_returned_an_unknown_project;

    private static final int MAX_MARK_STALE_QUERY_HITS = 500;

    private static final boolean TRACE_ENABLED = Boolean.parseBoolean(Platform.getDebugOption("me.glindholm.connector.eclipse.jira.core/debug/connector")); //$NON-NLS-1$

    /** Repository address + Issue Prefix + Issue key = the issue's web address */
    public final static String ISSUE_URL_PREFIX = "/browse/"; //$NON-NLS-1$

    /** Repository address + Filter Prefix + Issue key = the filter's web address */
    public final static String FILTER_URL_PREFIX = "/secure/IssueNavigator.jspa?mode=hide"; //$NON-NLS-1$

    public final static String FILTER_URL_PREFIX_NEW = "/issues/?jql="; //$NON-NLS-1$

    private final JiraTaskDataHandler taskDataHandler;

    private final JiraTaskAttachmentHandler attachmentHandler;

    public static final String UNASSIGNED_USER = "-1"; //$NON-NLS-1$

    public static final int RETURN_ALL_HITS = -1;

    public JiraRepositoryConnector() {
        taskDataHandler = new JiraTaskDataHandler(JiraClientFactory.getDefault());
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
    public boolean canCreateTaskFromKey(final TaskRepository repository) {
        return true;
    }

    @Override
    public IStatus performQuery(final TaskRepository repository, final IRepositoryQuery repositoryQuery,
            final TaskDataCollector resultCollector, final ISynchronizationSession session, IProgressMonitor monitor) {
        monitor = Policy.monitorFor(monitor);
        try {
            if (repository.isOffline()) {
                return RepositoryStatus.createStatus(repository, IStatus.INFO, JiraCorePlugin.ID_PLUGIN,
                        NLS.bind(Messages.JiraRepositoryConnector_Disabled, repository.getRepositoryLabel()));
            }
            monitor.beginTask(Messages.JiraRepositoryConnector_Query_Repository, IProgressMonitor.UNKNOWN);
            final JiraClient client = JiraClientFactory.getDefault().getJiraClient(repository);
            JiraFilter filter;
            try {
                if (!client.getCache().hasDetails()) {
                    client.getCache().refreshDetails(monitor);
                }
                filter = JiraUtil.getQuery(repository, client, repositoryQuery, true, monitor);
                if (filter == null) {
                    return RepositoryStatus.createStatus(repository, IStatus.ERROR, JiraCorePlugin.ID_PLUGIN,
                            Messages.JiraRepositoryConnector_The_JIRA_query_is_invalid);
                }
            } catch (InvalidJiraQueryException | JiraException e) {
                return JiraCorePlugin.toStatus(repository, e);
            }
            try {
                final QueryHitCollector collector = new QueryHitCollector(repository, client, resultCollector, session,
                        monitor);
                client.search(filter, collector, monitor);
                return collector.getStatus();
            } catch (final JiraException e) {
                final IStatus status = JiraCorePlugin.toStatus(repository, e);
                trace(status);
                return status;
            }
        } finally {
            monitor.done();
        }
    }

    @Override
    public void preSynchronization(final ISynchronizationSession session, final IProgressMonitor monitor) throws CoreException {
        //		monitor = Policy.monitorFor(monitor);
        //		try {
        //			monitor.beginTask(Messages.JiraRepositoryConnector_Getting_changed_tasks, IProgressMonitor.UNKNOWN);
        //
        //			session.setNeedsPerformQueries(true);
        //
        //			if (!session.isFullSynchronization()) {
        //				return;
        //			}
        //
        //			Date now = new Date();
        //			TaskRepository repository = session.getTaskRepository();
        //			FilterDefinition changedFilter = getSynchronizationFilter(session, now);
        //			if (changedFilter == null) {
        //				// could not determine last time, rerun queries
        //				repository.setSynchronizationTimeStamp(JiraUtil.dateToString(now));
        //				return;
        //			}
        //
        //			List<JiraIssue> issues = new ArrayList<JiraIssue>();
        //			JiraClient client = JiraClientFactory.getDefault().getJiraClient(repository);
        //			int maxResults = JiraUtil.getMaxSearchResults(repository);
        //			if (maxResults <= 0) {
        //				maxResults = MAX_MARK_STALE_QUERY_HITS;
        //			} else {
        //				maxResults = Math.min(MAX_MARK_STALE_QUERY_HITS, maxResults);
        //			}
        //			// unlimited maxHits can create crazy amounts of traffic
        //			JiraIssueCollector issueCollector = new JiraIssueCollector(new NullProgressMonitor(), issues, maxResults);
        //			try {
        //				client.search(changedFilter, issueCollector, monitor);
        //
        //				if (issues.isEmpty()) {
        //					// repository is unchanged
        //					repository.setSynchronizationTimeStamp(JiraUtil.dateToString(now));
        //					session.setNeedsPerformQueries(false);
        //					return;
        //				}
        //
        //				HashMap<String, ITask> taskById = new HashMap<String, ITask>();
        //				for (ITask task : session.getTasks()) {
        //					taskById.put(task.getTaskId(), task);
        //				}
        //				for (JiraIssue issue : issues) {
        //					ITask task = taskById.get(issue.getId());
        //					if (task != null) {
        //						if (issue.getProject() == null) {
        //							throw new CoreException(new Status(IStatus.ERROR, JiraCorePlugin.ID_PLUGIN, 0,
        //									ERROR_REPOSITORY_CONFIGURATION, null));
        //						}
        //
        //						// for JIRA sufficient information to create task data is returned by the query so no need to mark tasks as stale
        //						monitor.subTask(issue.getKey() + " " + issue.getSummary()); //$NON-NLS-1$
        //						// only load old task data from if necessary
        //						if (hasChanged(task, issue)) {
        //							TaskData oldTaskData = null;
        //							if (session.getTaskDataManager() != null) {
        //								try {
        //									oldTaskData = session.getTaskDataManager().getTaskData(repository, issue.getId());
        //								} catch (CoreException e) {
        //									// ignore
        //								}
        //							}
        //							TaskData taskData = taskDataHandler.createTaskData(repository, client, issue, oldTaskData,
        //									monitor);
        //							session.putTaskData(task, taskData);
        //						}
        //					}
        //				}
        //
        //				repository.setSynchronizationTimeStamp(JiraUtil.dateToString(now));
        //
        //				Date lastUpdate = issues.get(0).getUpdated();
        //				Date repositoryUpdateTimeStamp = JiraUtil.getLastUpdate(repository);
        //				if (repositoryUpdateTimeStamp != null && repositoryUpdateTimeStamp.equals(lastUpdate)) {
        //					// didn't see any new changes
        //					session.setNeedsPerformQueries(false);
        //				} else {
        //					// updates may have caused tasks to match/not match a query therefore we need to rerun all queries
        //					if (lastUpdate != null) {
        //						JiraUtil.setLastUpdate(repository, lastUpdate);
        //					}
        //				}
        //			} catch (JiraException e) {
        //				e.printStackTrace();
        //				IStatus status = JiraCorePlugin.toStatus(repository, e);
        //				trace(status);
        //				throw new CoreException(status);
        //			}
        //		} finally {
        //			monitor.done();
        //		}
    }

    @Override
    public void postSynchronization(final ISynchronizationSession event, final IProgressMonitor monitor) throws CoreException {
        // ignore
    }

    /* Public for testing. */
    public FilterDefinition getSynchronizationFilter(final ISynchronizationSession session, final Instant now) {
        final Set<ITask> tasks = session.getTasks();
        // there are no JIRA tasks in the task list, skip contacting the repository
        if (tasks.isEmpty()) {
            return null;
        }

        final TaskRepository repository = session.getTaskRepository();
        Instant lastSyncDate = JiraUtil.stringToDate(repository.getSynchronizationTimeStamp());

        // repository was never synchronized, update all tasks
        if (lastSyncDate == null) {
            for (final ITask task : tasks) {
                session.markStale(task);
            }
            return null;
        }

        // use local time to determine time difference to last sync
        final long nowTime = now.getEpochSecond();
        final long lastSyncTime = lastSyncDate.getEpochSecond();

        // check if time stamp is skewed
        if (lastSyncTime >= nowTime) {
            trace(new Status(IStatus.WARNING, JiraCorePlugin.ID_PLUGIN, 0,
                    "Synchronization time stamp clock skew detected for " + repository.getRepositoryUrl() + ": " //$NON-NLS-1$ //$NON-NLS-2$
                    + lastSyncTime + " >= " + now, null)); //$NON-NLS-1$

            // use the time stamp on the task that was modified last
            lastSyncDate = null;
            for (final ITask task : tasks) {
                final Instant date = task.getModificationDate().toInstant();
                if (lastSyncDate == null || date != null && date.isAfter(lastSyncDate)) {
                    lastSyncDate = date;
                }
            }

            if (lastSyncDate == null) {
                // could not determine last synchronization point
                return null;
            }

            // get all tasks that were changed after the last known task modification
            final FilterDefinition changedFilter = new FilterDefinition();
            changedFilter.setUpdatedDateFilter(new DateRangeFilter(lastSyncDate, null, null, null));
            // make sure it's sorted so the most recent changes are returned in case the query maximum is hit
            changedFilter.setOrdering(new Order[] { new Order(JiraFields.UPDATED, false) });
            return changedFilter;
        }

        final FilterDefinition changedFilter = new FilterDefinition();
        // need to use RelativeDateRangeFilter since the granularity of DateRangeFilter is days
        // whereas this allows us to use minutes
        final long minutes = (now.getEpochSecond() - lastSyncDate.getEpochSecond()) / (60 * 1000) + 1;
        changedFilter.setUpdatedDateFilter(new RelativeDateRangeFilter(RangeType.MINUTE, -minutes));
        // make sure it's sorted so the most recent changes are returned in case the query maximum is hit
        changedFilter.setOrdering(new Order[] { new Order(JiraFields.UPDATED, false) });
        return changedFilter;
    }

    @Override
    public boolean canCreateNewTask(final TaskRepository repository) {
        return true;
    }

    @Override
    public String getRepositoryUrlFromTaskUrl(final String url) {
        if (url == null) {
            return null;
        }
        final int index = url.indexOf(ISSUE_URL_PREFIX);
        return index != -1 ? url.substring(0, index) : null;
    }

    @Override
    public String getTaskIdFromTaskUrl(final String url) {
        if (url == null) {
            return null;
        }
        final int index = url.indexOf(ISSUE_URL_PREFIX);
        if (index != -1) {
            String taskId = url.substring(index + ISSUE_URL_PREFIX.length());

            // strip query string
            final int index2 = taskId.indexOf("?"); //$NON-NLS-1$
            if (index2 != -1) {
                taskId = taskId.substring(0, index2);
            }

            if (taskId.contains("-")) { //$NON-NLS-1$
                return taskId;
            }
        }
        return null;
    }

    @Override
    public String getTaskUrl(final String repositoryUrl, final String taskId) {
        return repositoryUrl + ISSUE_URL_PREFIX + taskId;
    }

    @Override
    public String[] getTaskIdsFromComment(final TaskRepository repository, final String comment) {
        final JiraClient client = JiraClientFactory.getDefault().getJiraClient(repository);
        final JiraProject[] projects = client.getCache().getProjects();
        if (projects != null && projects.length > 0) {
            // (?:(MNGECLIPSE-\d+?)|(SPR-\d+?))\D
            final StringBuilder sb = new StringBuilder("("); //$NON-NLS-1$
            String sep = ""; //$NON-NLS-1$
            for (final JiraProject project : projects) {
                sb.append(sep).append("(?:" + project.getKey() + "\\-\\d+?)"); //$NON-NLS-1$ //$NON-NLS-2$
                sep = "|"; //$NON-NLS-1$
            }
            sb.append(")(?:\\D|\\z)"); //$NON-NLS-1$

            final Pattern p = Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
            final Matcher m = p.matcher(comment);
            if (m.find()) {
                final HashSet<String> ids = new HashSet<>();
                do {
                    ids.add(m.group(1));
                } while (m.find());
                return ids.toArray(new String[ids.size()]);
            }
        }

        return super.getTaskIdsFromComment(repository, comment);
    }

    public static String getTaskUrlFromKey(final String repositoryUrl, final String key) {
        return repositoryUrl + JiraRepositoryConnector.ISSUE_URL_PREFIX + key;
    }

    public static boolean isCompleted(final TaskData taskData) {
        final TaskAttribute attribute = taskData.getRoot().getAttribute(JiraAttribute.RESOLUTION.id());
        final boolean isResolved = attribute != null && attribute.getValue().length() > 0;
        if (isResolved) {
            return true;
        }
        // for backward compatibility we are also checking the status
        final TaskAttribute status = taskData.getRoot().getAttribute(JiraAttribute.STATUS.id());
        return status != null
                && (ID_STATUS_RESOLVED.equals(status.getValue()) || ID_STATUS_CLOSED.equals(status.getValue()));
    }

    public static boolean isClosed(final JiraIssue issue) {
        // TODO find a more robust way to determine if a status is closed
        return issue.getStatus() != null && ID_STATUS_CLOSED.equals(issue.getStatus().getId());
    }

    static PriorityLevel getPriorityLevel(final String priorityId) {
        if (JiraPriority.BLOCKER_ID.equals(priorityId)) {
            return PriorityLevel.P1;
        } else if (JiraPriority.CRITICAL_ID.equals(priorityId)) {
            return PriorityLevel.P2;
        } else if (JiraPriority.MAJOR_ID.equals(priorityId)) {
            return PriorityLevel.P3;
        } else if (JiraPriority.MINOR_ID.equals(priorityId)) {
            return PriorityLevel.P4;
        } else if (JiraPriority.TRIVIAL_ID.equals(priorityId)) {
            return PriorityLevel.P5;
        }
        return PriorityLevel.getDefault();
    }

    @Override
    public String toString() {
        return getLabel();
    }

    @Override
    public void updateRepositoryConfiguration(final TaskRepository repository, final IProgressMonitor monitor) throws CoreException {
        try {
            final JiraClient client = JiraClientFactory.getDefault().getJiraClient(repository);
            client.getCache().refreshDetails(monitor);
        } catch (final JiraException e) {
            final IStatus status = JiraCorePlugin.toStatus(repository, e);
            trace(status);
            throw new CoreException(status);
        }
    }

    @Override
    public void updateRepositoryConfiguration(final TaskRepository taskRepository, final ITask task, final IProgressMonitor monitor)
            throws CoreException {
        final String projectId = task == null ? null : task.getAttribute(JiraAttribute.PROJECT.id());
        if (projectId != null && !"".equals(projectId)) { //$NON-NLS-1$
            try {
                final JiraClient client = JiraClientFactory.getDefault().getJiraClient(taskRepository);
                client.getCache().refreshProjectDetails(projectId, monitor);
            } catch (final JiraException e) {
                final IStatus status = JiraCorePlugin.toStatus(taskRepository, e);
                trace(status);
                throw new CoreException(status);
            }
        } else {
            updateRepositoryConfiguration(taskRepository, monitor);
        }
    }

    @Override
    public String getTaskIdPrefix() {
        return "issue"; //$NON-NLS-1$
    }

    public static String getAssigneeFromAttribute(final String assignee) {
        return "".equals(assignee) ? UNASSIGNED_USER : assignee; //$NON-NLS-1$
    }

    private void trace(final IStatus status) {
        if (TRACE_ENABLED) {
            StatusHandler.log(status);
        }
    }

    @Override
    public boolean isRepositoryConfigurationStale(final TaskRepository repository, final IProgressMonitor monitor)
            throws CoreException {
        return JiraUtil.getAutoRefreshConfiguration(repository)
                && super.isRepositoryConfigurationStale(repository, monitor);
    }

    @Override
    public JiraTaskDataHandler getTaskDataHandler() {
        return taskDataHandler;
    }

    @Override
    public boolean hasTaskChanged(final TaskRepository taskRepository, final ITask task, final TaskData taskData) {
        final TaskMapper scheme = getTaskMapping(taskData);
        final Instant repositoryDate = scheme.getModificationDate().toInstant();
        final Instant localDate = task.getModificationDate() == null ? null : task.getModificationDate().toInstant();
        if (repositoryDate != null && repositoryDate.equals(localDate)) {
            return false;
        }
        return true;
    }

    private boolean hasChanged(final ITask task, final JiraIssue issue) {
        final Instant repositoryDate = issue.getUpdated();
        final Instant localDate = task.getModificationDate().toInstant();
        if (repositoryDate != null && repositoryDate.equals(localDate)) {
            return false;
        }
        return true;
    }

    @Override
    public TaskData getTaskData(final TaskRepository taskRepository, final String taskId, final IProgressMonitor monitor)
            throws CoreException {
        return taskDataHandler.getTaskData(taskRepository, taskId, monitor);
    }

    @Override
    public void updateTaskFromTaskData(final TaskRepository repository, final ITask task, final TaskData taskData) {
        final Date modificationDate = task.getModificationDate();
        final Instant originalModificationDate = modificationDate == null ? null : modificationDate.toInstant();

        final TaskMapper scheme = getTaskMapping(taskData);
        scheme.applyTo(task);
        task.setCompletionDate(scheme.getCompletionDate());

        // flag subtasks to disable creation of sub-subtasks
        TaskAttribute attribute = taskData.getRoot().getAttribute(JiraAttribute.TYPE.id());
        if (attribute != null) {
            final boolean isSubTask = Boolean.parseBoolean(attribute.getMetaData()
                    .getValue(JiraConstants.META_SUB_TASK_TYPE));
            task.setAttribute(JiraConstants.META_SUB_TASK_TYPE, Boolean.toString(isSubTask));
        } else {
            task.setAttribute(JiraConstants.META_SUB_TASK_TYPE, Boolean.toString(false));
        }

        // add project id for Refresh Attributes (#290490)
        attribute = taskData.getRoot().getAttribute(JiraAttribute.PROJECT.id());
        if (attribute != null) {
            task.setAttribute(JiraAttribute.PROJECT.id(), attribute.getValue());
        }

        // Don't set modification date on the task if the task data is partial: otherwise hasTaskChanged will fail
        // to detect changes and the synchronization will fail to store the task data when synchronized -- unless it's
        // run by the user.  This could have an adverse performance impact due to multiple task synchronizations.
        if (taskData.isPartial()) {
            task.setModificationDate(originalModificationDate == null ? null : Date.from(originalModificationDate));
        }

        // store rank for sorting
        attribute = taskData.getRoot().getAttribute(JiraAttribute.RANK.id());
        if (attribute != null) {
            task.setAttribute(TaskAttribute.RANK, attribute.getValue());
        }
    }

    @Override
    public JiraTaskAttachmentHandler getTaskAttachmentHandler() {
        return attachmentHandler;
    }

    @Override
    public JiraTaskMapper getTaskMapping(final TaskData taskData) {
        return new JiraTaskMapper(taskData);
    }

    @Override
    public Collection<TaskRelation> getTaskRelations(final TaskData taskData) {
        final List<TaskRelation> relations = new ArrayList<>();
        TaskAttribute attribute = taskData.getRoot().getAttribute(JiraAttribute.SUBTASK_IDS.id());
        if (attribute != null) {
            for (final String taskId : attribute.getValues()) {
                relations.add(TaskRelation.subtask(taskId));
            }
        }
        if (JiraUtil.getLinkedTasksAsSubtasks(taskData.getAttributeMapper().getTaskRepository())) {
            attribute = taskData.getRoot().getAttribute(JiraAttribute.LINKED_IDS.id());
            if (attribute != null) {
                for (final String taskId : attribute.getValues()) {
                    relations.add(TaskRelation.subtask(taskId));
                }
            }
        }
        return relations;
    }

    @Override
    public boolean hasRepositoryDueDate(final TaskRepository taskRepository, final ITask task, final TaskData taskData) {
        return taskData.getRoot().getMappedAttribute(JiraAttribute.DUE_DATE.id()) != null;
    }

    private class QueryHitCollector implements IssueCollector {

        private final IProgressMonitor monitor;

        private final JiraClient client;

        private final TaskRepository repository;

        private final TaskDataCollector collector;

        private final int maxHits;

        private List<IStatus> statuses;

        public QueryHitCollector(final TaskRepository repository, final JiraClient client, final TaskDataCollector collector,
                final ISynchronizationSession session, final IProgressMonitor monitor) {
            this.repository = repository;
            this.client = client;
            this.collector = collector;
            this.monitor = monitor;
            maxHits = JiraUtil.getMaxSearchResults(repository);
        }

        @Override
        public void collectIssue(final JiraIssue issue) {
            if (issue.getProject() == null) {
                addStatus(new Status(IStatus.ERROR, JiraCorePlugin.ID_PLUGIN, 0, ERROR_REPOSITORY_CONFIGURATION, null));
                return;
            }

            TaskData taskData;
            try {
                taskData = taskDataHandler.createTaskData(repository, client, issue, null, true, monitor);
                collector.accept(taskData);
            } catch (final JiraException e) {
                addStatus(JiraCorePlugin.toStatus(repository, e));
            }
        }

        private void addStatus(final IStatus status) {
            if (statuses == null) {
                statuses = new ArrayList<>();
            }
            statuses.add(status);
        }

        @Override
        public void done() {
            // ignore

        }

        @Override
        public int getMaxHits() {
            return maxHits;
        }

        public IStatus getStatus() {
            // TODO return all statuses in a MultiStatus
            return statuses != null ? statuses.get(0) : Status.OK_STATUS;
        }

        @Override
        public boolean isCancelled() {
            return monitor.isCanceled();
        }

        @Override
        public void start() {
            // ignore
        }

    }

}
