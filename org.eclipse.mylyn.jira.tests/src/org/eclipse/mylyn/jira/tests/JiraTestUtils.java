package org.eclipse.mylar.jira.tests;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylar.internal.jira.core.model.Issue;
import org.eclipse.mylar.internal.jira.core.model.Resolution;
import org.eclipse.mylar.internal.jira.core.service.JiraException;
import org.eclipse.mylar.internal.jira.core.service.JiraServer;

public class JiraTestUtils {

	public static Resolution getFixedResolution(JiraServer server) throws JiraException {
		refreshDetails(server);
		
		Resolution[] resolutions = server.getResolutions();
		for (Resolution resolution : resolutions) {
			if (Resolution.FIXED_ID.equals(resolution.getId())) {
				return resolution;
			}
		}
		return resolutions[0];
	}

	public static Issue createIssue(JiraServer server, String summary) throws JiraException {
		refreshDetails(server);
		
		Issue issue = new Issue();
		issue.setProject(server.getProjects()[0]);
		issue.setType(server.getIssueTypes()[0]);
		issue.setSummary(summary);
		issue.setAssignee(server.getUserName());
		
		return server.createIssue(issue);
	}

	public static void refreshDetails(JiraServer server) throws JiraException {
		if (server.getProjects().length == 0) {
			server.refreshDetails(new NullProgressMonitor());
		}
	}
	
}
