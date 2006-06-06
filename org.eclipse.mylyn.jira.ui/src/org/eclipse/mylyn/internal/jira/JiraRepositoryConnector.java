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

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylar.internal.core.util.MylarStatusHandler;
import org.eclipse.mylar.internal.jira.ui.wizards.AddExistingJiraTaskWizard;
import org.eclipse.mylar.internal.jira.ui.wizards.EditJiraQueryWizard;
import org.eclipse.mylar.internal.jira.ui.wizards.JiraRepositorySettingsPage;
import org.eclipse.mylar.internal.jira.ui.wizards.NewJiraQueryWizard;
import org.eclipse.mylar.internal.tasklist.AbstractAttributeFactory;
import org.eclipse.mylar.internal.tasklist.RepositoryTaskData;
import org.eclipse.mylar.internal.tasklist.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.mylar.provisional.tasklist.AbstractQueryHit;
import org.eclipse.mylar.provisional.tasklist.AbstractRepositoryConnector;
import org.eclipse.mylar.provisional.tasklist.AbstractRepositoryQuery;
import org.eclipse.mylar.provisional.tasklist.AbstractRepositoryTask;
import org.eclipse.mylar.provisional.tasklist.IRemoteContextDelegate;
import org.eclipse.mylar.provisional.tasklist.ITask;
import org.eclipse.mylar.provisional.tasklist.MylarTaskListPlugin;
import org.eclipse.mylar.provisional.tasklist.TaskRepository;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.tigris.jira.core.model.Issue;
import org.tigris.jira.core.model.filter.IssueCollector;
import org.tigris.jira.core.service.JiraServer;

/**
 * @author Mik Kersten
 */
public class JiraRepositoryConnector extends AbstractRepositoryConnector {

	private static final String DELIM_URL = "/browse/";

	private static final String VERSION_SUPPORT = "3.3.1 and higher";

	private List<String> supportedVersions;

	/** Name initially given to new tasks. Public for testing */
	public static final String NEW_TASK_DESC = "New Task";

	private final class JiraIssueCollector implements IssueCollector {

		private final IProgressMonitor monitor;

		private final List<Issue> issues;

		private boolean done = false;

		JiraIssueCollector(IProgressMonitor monitor, List<Issue> issues) {
			this.monitor = monitor;
			this.issues = issues;
		}

		public void done() {
			done = true;
		}

		public boolean isCancelled() {
			return monitor.isCanceled();
		}

		public void collectIssue(Issue issue) {
			issues.add(issue);
		}

		public void start() {

		}

		public boolean isDone() {
			return done;
		}
	}

	public JiraRepositoryConnector() {
		super(new AbstractAttributeFactory() {

			private static final long serialVersionUID = -4685044081450189855L;

			@Override
			public boolean getIsHidden(String key) {
				return false;
			}

			@Override
			public String getName(String key) {
				return key;
			}

			@Override
			public String mapCommonAttributeKey(String key) {
				return key;
			}

			@Override
			public boolean isReadOnly(String key) {				
				return false;
			}
		});
	}

	public String getLabel() {
		return MylarJiraPlugin.JIRA_CLIENT_LABEL;
	}

	public String getRepositoryType() {
		return MylarJiraPlugin.REPOSITORY_KIND;
	}

	public String toString() {
		return getLabel();
	}

	@Override
	public boolean canCreateTaskFromKey() {
		return true;
	}

	public ITask createTaskFromExistingKey(TaskRepository repository, String key) {
		JiraServer server = JiraServerFacade.getDefault().getJiraServer(repository);
		if (server != null) {
			Issue issue = server.getIssue(key);
			if (issue != null) {
				String handleIdentifier = AbstractRepositoryTask.getHandle(repository.getUrl(), issue.getId());
				JiraTask task = createTask(issue, handleIdentifier);
				updateTaskDetails(repository.getUrl(), task, issue);
				if (task != null) {
					MylarTaskListPlugin.getTaskListManager().getTaskList().addTask(task);
					return task;
				}
			}
		}
		return null;
	}

	public AbstractRepositorySettingsPage getSettingsPage() {
		return new JiraRepositorySettingsPage();
	}

	public IWizard getNewQueryWizard(TaskRepository repository) {
		return new NewJiraQueryWizard(repository);
	}

	public IWizard getAddExistingTaskWizard(TaskRepository repository) {
		return new AddExistingJiraTaskWizard(repository);
	}

	@Override
	public void openEditQueryDialog(AbstractRepositoryQuery query) {
		// JiraRepositoryQuery filter = (JiraRepositoryQuery) query;
		// String title = "Filter: " + filter.getDescription();
		// TaskUiUtil.openUrl(title, title, filter.getQueryUrl());

		// if (query instanceof JiraRepositoryQuery) {
		// JiraRepositoryQuery filter = (JiraRepositoryQuery) query;
		// String title = "Filter: " + filter.getDescription();
		// TaskUiUtil.openUrl(title, title, filter.getQueryUrl());
		// } else if(query instanceof JiraCustomQuery) {
		// // new JiraQueryWizardPage();
		// }

		try {
			TaskRepository repository = MylarTaskListPlugin.getRepositoryManager().getRepository(
					query.getRepositoryKind(), query.getRepositoryUrl());
			if (repository == null)
				return;

			IWizard wizard = this.getEditQueryWizard(repository, query);

			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			if (wizard != null && shell != null && !shell.isDisposed()) {
				WizardDialog dialog = new WizardDialog(shell, wizard);
				dialog.create();
				dialog.setTitle("Edit Jira Query");
				dialog.setBlockOnOpen(true);
				if (dialog.open() == Window.CANCEL) {
					dialog.close();
					return;
				}
			}
		} catch (Exception e) {
			MylarStatusHandler.fail(e, e.getMessage(), true);
		}

	}

	private IWizard getEditQueryWizard(TaskRepository repository, AbstractRepositoryQuery query) {
		if (query instanceof JiraRepositoryQuery || query instanceof JiraCustomQuery) {
			return new EditJiraQueryWizard(repository, query);
		}
		return null;
	}

	@Override
	public List<AbstractQueryHit> performQuery(AbstractRepositoryQuery repositoryQuery, final IProgressMonitor monitor,
			MultiStatus queryStatus) {
		List<AbstractQueryHit> hits = new ArrayList<AbstractQueryHit>();
		final List<Issue> issues = new ArrayList<Issue>();

		TaskRepository repository = MylarTaskListPlugin.getRepositoryManager().getRepository(
				MylarJiraPlugin.REPOSITORY_KIND, repositoryQuery.getRepositoryUrl());

		JiraIssueCollector collector = new JiraIssueCollector(monitor, issues);

		try {
			JiraServer jiraServer = JiraServerFacade.getDefault().getJiraServer(repository);
			if (repositoryQuery instanceof JiraRepositoryQuery) {
				jiraServer.search(((JiraRepositoryQuery) repositoryQuery).getNamedFilter(), collector);
			} else if (repositoryQuery instanceof JiraCustomQuery) {
				jiraServer.search(((JiraCustomQuery) repositoryQuery).getFilterDefinition(), collector);
			}
		} catch (Throwable t) {
			queryStatus.add(new Status(IStatus.OK, MylarTaskListPlugin.PLUGIN_ID, IStatus.OK,
					"Could not log in to server: " + repositoryQuery.getRepositoryUrl()
							+ "\n\nCheck network connection.", t));
			return hits;
		}
		// TODO: work-around no other way of determining failure
		if (!collector.isDone()) {
			queryStatus.add(new Status(IStatus.OK, MylarTaskListPlugin.PLUGIN_ID, IStatus.OK,
					"Could not log in to server: " + repositoryQuery.getRepositoryUrl()
							+ "\n\nCheck network connection.", new UnknownHostException()));
			return hits;
		} else {
		for (Issue issue : issues) {
			int issueId = new Integer(issue.getId());
			String handleIdentifier = AbstractRepositoryTask.getHandle(repository.getUrl(), issueId);
			ITask task = MylarTaskListPlugin.getTaskListManager().getTaskList().getTask(handleIdentifier);
			if (!(task instanceof JiraTask)) {
				task = createTask(issue, handleIdentifier);
			}
			updateTaskDetails(repository.getUrl(), (JiraTask) task, issue);

			JiraQueryHit hit = new JiraQueryHit((JiraTask) task, repositoryQuery.getRepositoryUrl(), issueId);
			hits.add(hit);
		}
		queryStatus.add(Status.OK_STATUS);
		return hits;
		}
	}

	@Override
	public boolean canCreateNewTask() {
		return false;
	}

	@Override
	public IWizard getNewTaskWizard(TaskRepository taskRepository) {
		return null;
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
	protected void updateOfflineState(AbstractRepositoryTask repositoryTask, boolean forceSync) {
		TaskRepository repository = MylarTaskListPlugin.getRepositoryManager().getRepository(
				repositoryTask.getRepositoryKind(), repositoryTask.getRepositoryUrl());
		if (repository != null && repositoryTask instanceof JiraTask) {
			JiraTask jiraTask = (JiraTask) repositoryTask;
			JiraServer server = JiraServerFacade.getDefault().getJiraServer(repository);
			if (server != null) {
				Issue issue = server.getIssue(jiraTask.getKey());
				if (issue != null) {
					updateTaskDetails(repository.getUrl(), jiraTask, issue);
				}
			}
		}
	}

	@Override
	public boolean attachContext(TaskRepository repository, AbstractRepositoryTask task, String longComment)
			throws IOException {
		return false;
	}

	@Override
	public Set<IRemoteContextDelegate> getAvailableContexts(TaskRepository repository, AbstractRepositoryTask task) {
		return Collections.emptySet();
	}

	@Override
	public boolean retrieveContext(TaskRepository repository, AbstractRepositoryTask task,
			IRemoteContextDelegate remoteContextDelegate) throws IOException {
		return false;
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

	public static void updateTaskDetails(String repositoryUrl, JiraTask task, Issue issue) {
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
			String translatedPriority = JiraTask.PriorityLevel.fromPriority(issue.getPriority()).toString();
			task.setPriority(translatedPriority);
			task.setKind(issue.getType().getName());
		}
		MylarTaskListPlugin.getTaskListManager().getTaskList().notifyLocalInfoChanged(task);
	}

	public static JiraTask createTask(Issue issue, String handleIdentifier) {
		JiraTask task;
		String description = issue.getKey() + ": " + issue.getSummary();
		ITask existingTask = MylarTaskListPlugin.getTaskListManager().getTaskList().getTask(handleIdentifier);
		if (existingTask instanceof JiraTask) {
			task = (JiraTask) existingTask;
		} else {
			task = new JiraTask(handleIdentifier, description, true);
			task.setKey(issue.getKey());
			MylarTaskListPlugin.getTaskListManager().getTaskList().addTask(task);
		}
		return task;
	}

	@Override
	public Set<AbstractRepositoryTask> getChangedSinceLastSync(TaskRepository repository,
			Set<AbstractRepositoryTask> tasks) throws GeneralSecurityException, IOException {
		return Collections.emptySet();
	}

	@Override
	protected RepositoryTaskData downloadTaskData(AbstractRepositoryTask bugzillaTask) {
		MylarStatusHandler.log("Unexpected call to JiraRepositoryConnector.downloadTaskData()", this);
		return null;
	}
}
