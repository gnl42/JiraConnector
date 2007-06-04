package org.eclipse.mylar.jira.tests;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylar.context.tests.support.MylarTestUtils;
import org.eclipse.mylar.context.tests.support.MylarTestUtils.Credentials;
import org.eclipse.mylar.context.tests.support.MylarTestUtils.PrivilegeLevel;
import org.eclipse.mylar.internal.jira.core.model.Issue;
import org.eclipse.mylar.internal.jira.core.service.JiraClient;
import org.eclipse.mylar.internal.jira.ui.JiraClientFacade;
import org.eclipse.mylar.internal.jira.ui.JiraUiPlugin;
import org.eclipse.mylar.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylar.tasks.core.ITaskDataHandler;
import org.eclipse.mylar.tasks.core.RepositoryTaskData;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;

public class JiraTaskDataHandlerTest extends TestCase {

	private TaskRepository repository;

	private ITaskDataHandler dataHandler;

	private JiraClient server;

	protected void init(String url) throws Exception {
		String kind = JiraUiPlugin.REPOSITORY_KIND;

		Credentials credentials = MylarTestUtils.readCredentials(PrivilegeLevel.USER);

		repository = new TaskRepository(kind, url);
		repository.setAuthenticationCredentials(credentials.username, credentials.password);

		AbstractRepositoryConnector connector = TasksUiPlugin.getRepositoryManager().getRepositoryConnector(kind);
		assertEquals(connector.getRepositoryType(), kind);

		TasksUiPlugin.getSynchronizationManager().setForceSyncExec(true);

		dataHandler = connector.getTaskDataHandler();

		server = JiraClientFacade.getDefault().getJiraClient(repository);
	}

	public void testGetTaskData() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		String commentText = "line1\nline2\n\nline4\n\n\n";
		Issue issue = JiraTestUtils.createIssue(server, "testUpdateTask");
		server.addCommentToIssue(issue, commentText);

		RepositoryTaskData taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertEquals(1, taskData.getComments().size());
		assertEquals(commentText, taskData.getComments().get(0).getText());
	}

}
