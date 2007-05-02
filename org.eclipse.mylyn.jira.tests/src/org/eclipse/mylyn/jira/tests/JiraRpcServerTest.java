package org.eclipse.mylar.jira.tests;

import java.net.Proxy;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylar.context.tests.support.MylarTestUtils;
import org.eclipse.mylar.context.tests.support.MylarTestUtils.Credentials;
import org.eclipse.mylar.context.tests.support.MylarTestUtils.PrivilegeLevel;
import org.eclipse.mylar.internal.jira.core.model.Issue;
import org.eclipse.mylar.internal.jira.core.service.AbstractJiraServer;
import org.eclipse.mylar.internal.jira.core.service.soap.JiraRpcServer;

public class JiraRpcServerTest extends TestCase {

	private AbstractJiraServer server;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		Credentials credentials = MylarTestUtils.readCredentials(PrivilegeLevel.USER);
		server = new JiraRpcServer("server", JiraTestConstants.JIRA_381_URL, false,
				credentials.username, credentials.password, Proxy.NO_PROXY, null, null);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	@SuppressWarnings("deprecation")
	public void testLogin() throws Exception {
		server.login();
		server.logout();
		// should automatically login
		server.refreshDetails(new NullProgressMonitor());
	}

	public void testStartIssue() throws Exception {
		Issue issue = JiraTestUtils.createIssue(server, "testStartIssue");
		server.startIssue(issue);
		// no way to tell if issue was actually started		
	}

//	public void testStopIssue() {
//		fail("Not yet implemented");
//	}
//
//	public void testResolveIssue() {
//		fail("Not yet implemented");
//	}
//
//	public void testCloseIssue() {
//		fail("Not yet implemented");
//	}
//
//	public void testReopenIssue() {
//		fail("Not yet implemented");
//	}

}
