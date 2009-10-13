/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.service;

import org.eclipse.osgi.util.NLS;

public class Messages {
	private static final String BUNDLE_NAME = "org.eclipse.mylyn.internal.jira.core.service.messages"; //$NON-NLS-1$

	static {
		// load message values from bundle file
		reloadMessages();
	}

	public static void reloadMessages() {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String JiraClientCache_Getting_server_information;

	public static String JiraClientCache_Updating_repository_configuration;

	public static String JiraClientCache_getting_issue_types;

	public static String JiraClientCache_getting_priorities;

	public static String JiraClientCache_getting_project_details;

	public static String JiraClientCache_getting_resolutions;

	public static String JiraClientCache_getting_statuses;

	public static String JiraClientCache_project_details_for;

	public static String JiraClientCache_getting_project_roles;
}
