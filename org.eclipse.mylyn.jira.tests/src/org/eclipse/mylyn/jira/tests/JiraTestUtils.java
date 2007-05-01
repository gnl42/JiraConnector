package org.eclipse.mylar.jira.tests;

import org.eclipse.mylar.internal.jira.core.model.Issue;
import org.eclipse.mylar.internal.jira.core.model.Resolution;
import org.eclipse.mylar.internal.jira.core.service.JiraServer;

public class JiraTestUtils {

	public static Resolution getFixedResolution(JiraServer server) {
		Resolution[] resolutions = server.getResolutions();
		for (Resolution resolution : resolutions) {
			if (Resolution.FIXED_ID.equals(resolution.getId())) {
				return resolution;
			}
		}
		return resolutions[0];
	}

	public static Issue createIssue(JiraServer server, String summary) {
		Issue issue = new Issue();
		issue.setProject(server.getProjects()[0]);
		issue.setType(server.getIssueTypes()[0]);
		issue.setSummary(summary);
		return issue;
	}
	
}
