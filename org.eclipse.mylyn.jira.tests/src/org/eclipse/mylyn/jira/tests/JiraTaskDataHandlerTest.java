package org.eclipse.mylyn.jira.tests;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.context.tests.support.TestUtil;
import org.eclipse.mylyn.context.tests.support.TestUtil.Credentials;
import org.eclipse.mylyn.context.tests.support.TestUtil.PrivilegeLevel;
import org.eclipse.mylyn.internal.jira.core.model.Issue;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.ui.JiraClientFacade;
import org.eclipse.mylyn.internal.jira.ui.JiraUiPlugin;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.RepositoryTaskData;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUiPlugin;

public class JiraTaskDataHandlerTest extends TestCase {

	private TaskRepository repository;

	private AbstractTaskDataHandler dataHandler;

	private JiraClient server;

	protected void init(String url) throws Exception {
		String kind = JiraUiPlugin.REPOSITORY_KIND;

		Credentials credentials = TestUtil.readCredentials(PrivilegeLevel.USER);

		repository = new TaskRepository(kind, url);
		repository.setAuthenticationCredentials(credentials.username, credentials.password);

		AbstractRepositoryConnector connector = TasksUiPlugin.getRepositoryManager().getRepositoryConnector(kind);
		assertEquals(connector.getConnectorKind(), kind);

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
