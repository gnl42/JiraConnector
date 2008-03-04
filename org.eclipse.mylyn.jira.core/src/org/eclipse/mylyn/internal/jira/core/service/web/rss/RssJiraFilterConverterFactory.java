/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.service.web.rss;

import org.eclipse.mylyn.internal.jira.core.model.JiraVersion;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;

/**
 * @author Brock Janiczak
 */
public class RssJiraFilterConverterFactory {

	private static final RssFilterConverter filterConverter = new RssFilterConverter();

	private static final RssFilterConverter jira33FilterConverter = new Jira33RssFilterConverter();

	public static RssFilterConverter getConverter(JiraClient server) throws JiraException {
		String version = server.getCache().getServerInfo().getVersion();
		if (new JiraVersion(version).compareTo(JiraVersion.JIRA_3_3) >= 0) {
			return jira33FilterConverter;
		} else {
			return filterConverter;
		}
	}
}
