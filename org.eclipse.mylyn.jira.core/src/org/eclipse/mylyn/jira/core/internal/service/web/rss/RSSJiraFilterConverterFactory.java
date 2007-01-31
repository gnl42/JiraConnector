package org.eclipse.mylar.jira.core.internal.service.web.rss;

import org.eclipse.mylar.jira.core.internal.service.JiraServer;

/**
 * @author Brock Janiczak
 */
public class RSSJiraFilterConverterFactory {
	private static final RSSFilterConverter filterConverter = new RSSFilterConverter();

	private static final RSSFilterConverter jira33FilterConverter = new Jira33RSSFilterConverter();

	public static RSSFilterConverter getConverter(JiraServer server) {
		if (server.getServerInfo().getVersion().compareTo("3.3") >= 0) { //$NON-NLS-1$
			return jira33FilterConverter;
		} else {
			return filterConverter;
		}
	}
}
