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

package org.eclipse.mylyn.internal.jira.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.mylyn.internal.jira.ui.messages"; //$NON-NLS-1$

	static {
		// load message values from bundle file
		reloadMessages();
	}

	public static void reloadMessages() {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String JiraConnectorUi_Bug;

	public static String JiraConnectorUi_Feature;

	public static String JiraConnectorUi_Improvement;

	public static String JiraConnectorUi_In_reply_to_X;

	public static String JiraConnectorUi_In_reply_to_comment_X;

	public static String JiraConnectorUi_In_reply_to_X_comment_X;

	public static String JiraConnectorUi_Issue;

	public static String JiraConnectorUi_Subtask;

	public static String JiraConnectorUi_Task;
}
