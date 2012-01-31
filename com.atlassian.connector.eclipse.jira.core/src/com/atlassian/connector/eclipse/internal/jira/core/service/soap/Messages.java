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

package com.atlassian.connector.eclipse.internal.jira.core.service.soap;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.atlassian.connector.eclipse.internal.jira.core.service.soap.messages"; //$NON-NLS-1$

	static {
		// load message values from bundle file
		reloadMessages();
	}

	public static void reloadMessages() {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String JiraSoapClient_Due_Date;

	public static String JiraSoapClient_Fix_Version_s;

	public static String JiraSoapClient_Internal_Server_Error;

	public static String JiraSoapClient_The_location_of_the_Jira_server_has_moved;

	public static String JiraSoapClient_No_JIRA_repository_found_at_location_or_proxy;

	public static String JiraSoapClient_Server_error_;

	public static String JiraSoapClient_Unable_to_connect_to_server;

	public static String JiraSoapClient_Unknown_host;
}
