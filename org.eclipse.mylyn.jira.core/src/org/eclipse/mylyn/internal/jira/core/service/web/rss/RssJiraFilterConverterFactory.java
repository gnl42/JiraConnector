/*******************************************************************************
 * Copyright (c) 2007 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.service.web.rss;

import org.eclipse.mylyn.internal.jira.core.model.JiraVersion;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;

/**
 * @author Brock Janiczak
 */
public class RssJiraFilterConverterFactory {

	private static final RssFilterConverter filterConverter = new RssFilterConverter();

	private static final RssFilterConverter jira33FilterConverter = new Jira33RssFilterConverter();

	private static final JiraVersion JIRA_3_3 = new JiraVersion("3.3");

	public static RssFilterConverter getConverter(JiraClient server) throws JiraException {
		String version = server.getServerInfo().getVersion();
		if (new JiraVersion(version).compareTo(JIRA_3_3) >= 0) {
			return jira33FilterConverter;
		} else {
			return filterConverter;
		}
	}
}
